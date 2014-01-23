/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.transport.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.rabbitmq.utils.RabbitMQUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


/**
 * Each service will have one ServiceTaskManager instance that will create, manage and also destroy
 * idle tasks created for it, for message receipt. It uses the MessageListenerTask to poll for the
 * RabbitMQ AMQP Listening destination and consume messages. The consumed messages is build and sent to
 * axis2 engine for processing
 */

public class ServiceTaskManager {
    private static final Log log = LogFactory.getLog(ServiceTaskManager.class);

    private static final int STATE_STOPPED = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_PAUSED = 2;
    private static final int STATE_SHUTTING_DOWN = 3;
    private static final int STATE_FAILURE = 4;
    private volatile int activeTaskCount = 0;

    private WorkerPool workerPool = null;
    private String serviceName;
    private Hashtable<String, String> rabbitMQProperties = new Hashtable<String, String>();
    private final ConnectionFactory connectionFactory;
    private final List<MessageListenerTask> pollingTasks =
            Collections.synchronizedList(new ArrayList<MessageListenerTask>());
    private RabbitMQMessageReceiver rabbitMQMessageReceiver;
    private int serviceTaskManagerState = STATE_STOPPED;


    public ServiceTaskManager(
            ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Start  the Task Manager by adding a new MessageListenerTask to he worker pool.
     */
    public synchronized void start() {
        workerPool.execute(new MessageListenerTask());
        serviceTaskManagerState = STATE_STARTED;
    }


    public synchronized void stop() {
        if (serviceTaskManagerState != STATE_FAILURE) {
            serviceTaskManagerState = STATE_SHUTTING_DOWN;
        }

        synchronized (pollingTasks) {
            for (MessageListenerTask lstTask : pollingTasks) {
                lstTask.requestShutdown();
            }
        }

        if (serviceTaskManagerState != STATE_FAILURE) {
            serviceTaskManagerState = STATE_STOPPED;
        }
    }

    public synchronized void pause() {
        //TODO implement me ..
    }

    public synchronized void resume() {
        //TODO implement me ..
    }

    public void setWorkerPool(WorkerPool workerPool) {
        this.workerPool = workerPool;
    }

    public void setRabbitMQMessageReceiver(RabbitMQMessageReceiver rabbitMQMessageReceiver) {
        this.rabbitMQMessageReceiver = rabbitMQMessageReceiver;
    }

    public Hashtable<String, String> getRabbitMQProperties() {
        return rabbitMQProperties;
    }

    public void addRabbitMQProperties(Map<String, String> rabbitMQProperties) {
        this.rabbitMQProperties.putAll(rabbitMQProperties);
    }

    public void removeAMQPProperties(String key) {
        this.rabbitMQProperties.remove(key);
    }


    /**
     * The actual threads/tasks that perform message polling
     */
    private class MessageListenerTask implements Runnable {

        private Connection connection = null;
        private Channel channel = null;
        private boolean autoAck = false;

        private volatile int workerState = STATE_STOPPED;
        private volatile boolean idle = false;
        private volatile boolean connected = false;

        /**
         * As soon as we create a new polling task, add it to the STM for control later
         */
        MessageListenerTask() {
            synchronized (pollingTasks) {
                pollingTasks.add(this);
            }
        }

        public void pause() {
            //TODO implement me
        }

        public void resume() {
            //TODO implement me
        }

        /**
         * Execute the polling worker task
         */
        public void run() {
            workerState = STATE_STARTED;
            activeTaskCount++;
            try {
                connection = getConnection();
                if (channel == null) {
                    channel = connection.createChannel();
                }

                QueueingConsumer queueingConsumer = createQueueConsumer(channel);

                while (isActive()) {

                    try {
                        channel.txSelect();
                    } catch (IOException e) {
                        log.error("Error while starting transaction", e);
                        continue;
                    }

                    boolean successful = false;

                    RabbitMQMessage message = null;
                    try {
                        message = getConsumerDelivery(queueingConsumer);
                    } catch (InterruptedException e) {
                        log.error("Error while consuming message", e);
                        continue;
                    }

                    if (message != null) {
                        idle = false;
                        try {
                            successful = handleMessage(message);
                        } finally {
                            if (successful) {
                                try {
                                    channel.basicAck(message.getDeliveryTag(), false);
                                    channel.txCommit();
                                } catch (IOException e) {
                                    log.error("Error while commiting transaction", e);
                                }
                            } else {
                                try {
                                    channel.txRollback();
                                } catch (IOException e) {
                                    log.error("Error while trying to roll back transaction", e);
                                }
                            }
                        }
                    } else {
                        idle = true;
                    }
                }

            } catch (IOException e) {
                handleException("Error while reciving message from queue", e);
            } finally {
                closeConnection();
                workerState = STATE_STOPPED;
                activeTaskCount--;
                synchronized (pollingTasks) {
                    pollingTasks.remove(this);
                }
            }
        }

        /**
         * Create a queue consumer using the properties form transport listener configuration
         *
         * @return the queue consumer
         * @throws IOException on error
         */
        private QueueingConsumer createQueueConsumer(Channel channel) throws IOException {
            QueueingConsumer consumer = null;
            try {

                String queueName = rabbitMQProperties.get(RabbitMQConstants.QUEUE_NAME);
                String exchangeName = rabbitMQProperties.get(RabbitMQConstants.EXCHANGE_NAME);
                String autoAckStringValue = rabbitMQProperties.get(RabbitMQConstants.QUEUE_AUTO_ACK);
                if (autoAckStringValue != null) {
                    autoAck = Boolean.parseBoolean(autoAckStringValue);
                }
                //If no queue name is specified then service name will be used as queue name
                if (queueName == null || queueName.equals("")) {
                    queueName = serviceName;
                    log.warn("No queue name is specified for " + serviceName + ". " +
                             "Service name will be used as queue name");
                }

                channel.queueDeclare(queueName,
                        RabbitMQUtils.isDurableQueue(rabbitMQProperties),
                        RabbitMQUtils.isExclusiveQueue(rabbitMQProperties),
                        RabbitMQUtils.isAutoDeleteQueue(rabbitMQProperties), null);
                consumer = new QueueingConsumer(channel);

                if (exchangeName != null && !exchangeName.equals("")) {
                    String exchangerType = rabbitMQProperties.get(RabbitMQConstants.EXCHANGE_TYPE);
                    if (exchangerType != null) {
                        String durable = rabbitMQProperties.get(RabbitMQConstants.EXCHANGE_DURABLE);
                        if (durable != null) {
                            channel.exchangeDeclare(exchangeName, exchangerType, Boolean.parseBoolean(durable));
                        } else {
                            channel.exchangeDeclare(exchangeName, exchangerType, true);
                        }
                    } else {
                        channel.exchangeDeclare(exchangeName, "direct", true);
                    }

                    channel.queueBind(queueName, exchangeName, queueName);
                }

                String consumerTagString = rabbitMQProperties.get(RabbitMQConstants.CONSUMER_TAG);
                if (consumerTagString != null) {
                    channel.basicConsume(queueName, autoAck, consumerTagString, consumer);
                } else {
                    channel.basicConsume(queueName, autoAck, consumer);
                }

            } catch (IOException e) {
                handleException("Error while creating consumer", e);
            }


            return consumer;
        }

        /**
         * Returns the delivery from the consumer
         *
         * @param consumer the consumer to get the delivery
         * @return RabbitMQMessage consumed by the consumer
         * @throws InterruptedException on error
         */
        private RabbitMQMessage getConsumerDelivery(QueueingConsumer consumer)
                throws InterruptedException {
            RabbitMQMessage message = new RabbitMQMessage();
            QueueingConsumer.Delivery delivery = null;
            try {
                delivery = consumer.nextDelivery();
            } catch (ShutdownSignalException e) {
                //ignore
                return null;
            }
            if (delivery != null) {
                AMQP.BasicProperties properties = delivery.getProperties();
                Map<String, Object> headers = properties.getHeaders();
                message.setBody(delivery.getBody());
                message.setDeliveryTag(delivery.getEnvelope().getDeliveryTag());
                message.setReplyTo(properties.getReplyTo());
                message.setMessageId(properties.getMessageId());
                message.setContentType(properties.getContentType());
                message.setContentEncoding(properties.getContentEncoding());
                message.setCorrelationId(properties.getCorrelationId());
                if (headers != null) {
                    message.setHeaders(headers);
                    message.setSoapAction(headers.get(RabbitMQConstants.SOAP_ACTION).toString());
                }
            }
            return message;
        }

        /**
         * Invoke message receiver on received messages
         *
         * @param message the AMQP message received
         */
        private boolean handleMessage(RabbitMQMessage message) {
            boolean successful;
            successful = rabbitMQMessageReceiver.onMessage(message);
            return successful;
        }


        protected void requestShutdown() {
            workerState = STATE_SHUTTING_DOWN;
            closeConnection();
        }

        private boolean isActive() {
            return workerState == STATE_STARTED;
        }

        protected boolean isTaskIdle() {
            return idle;
        }

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        private Connection getConnection() {
            if (connection == null) {
                connection = createConnection();
                setConnected(true);
            }
            return connection;
        }

        private void closeConnection() {
            if (connection != null && connection.isOpen()) {
                try {
                    connection.close();
                } catch (IOException e) {
                    log.error("Error while closing connection ", e);
                } finally {
                    connection = null;
                }
            }
        }

        private Connection createConnection() {
            Connection connection = null;
            try {
                connection = connectionFactory.createConnection();
            } catch (Exception e) {
                log.error("Error while creating AMQP Connection...", e);
            }
            return connection;
        }
    }


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new AxisRabbitMQException(msg, e);
    }

}
