/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.caching.invalidator.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.clustering.api.CoordinatedActivity;
import org.wso2.carbon.caching.invalidator.internal.CacheInvalidationDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.cache.CacheManager;
import javax.cache.Caching;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class CacheInvalidationSubscriber implements CoordinatedActivity {
    private static final Log log = LogFactory.getLog(CacheInvalidationSubscriber.class);
    private QueueingConsumer consumer = null;

    public CacheInvalidationSubscriber(){
        if(ConfigurationManager.init()) {
            boolean isCoordinator = CacheInvalidationDataHolder.getConfigContext() != null && CacheInvalidationDataHolder.getConfigContext().getAxisConfiguration().getClusteringAgent().isCoordinator();
            if (isCoordinator && !ConfigurationManager.isSubscribed()) {
                subscribe();
                ConfigurationManager.setSubscribed(true);
            }
        }
    }

    @Override
    public void execute() {
        if(ConfigurationManager.init()) {
            boolean isCoordinator = CacheInvalidationDataHolder.getConfigContext() != null && CacheInvalidationDataHolder.getConfigContext().getAxisConfiguration().getClusteringAgent().isCoordinator();
            if (isCoordinator && !ConfigurationManager.isSubscribed()) {
                subscribe();
                ConfigurationManager.setSubscribed(true);
            }
            if (!isCoordinator && ConfigurationManager.isSubscribed()) {
                ConfigurationManager.setSubscribed(false);
            }
        }
    }

    private void subscribe() {
        log.debug("Global cache invalidation: initializing the subscription");
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(ConfigurationManager.getProviderUrl());
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(ConfigurationManager.getTopicName(), "topic");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, ConfigurationManager.getTopicName(), "#");
            consumer = new QueueingConsumer(channel);
            channel.basicConsume(queueName, true, consumer);
            Thread reciever = new Thread(messageReciever);
            reciever.start();
            log.info("Global cache invalidation is online");
        } catch (Exception e) {
            log.error("Global cache invalidation: Error message broker initialization", e);
        }
    }

    public void onMessage(byte[] data) {
        log.debug("Cache invalidation message received: " + new String(data));
        boolean isCoordinator = CacheInvalidationDataHolder.getConfigContext() != null && CacheInvalidationDataHolder.getConfigContext().getAxisConfiguration().getClusteringAgent().isCoordinator();
        if(isCoordinator) {
            PrivilegedCarbonContext.startTenantFlow();
            try {
                log.debug("Global cache invalidation: deserializing data to object");
                GlobalCacheInvalidationEvent event = (GlobalCacheInvalidationEvent) deserialize(data);
                log.debug("Global cache invalidation: deserializing complete");
                if (!ConfigurationManager.getSentMsgBuffer().contains(event.getUuid().trim())) { // Ignore own messages
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(event.getTenantId(), true);
                    CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(event.getCacheManagerName());
                    if (cacheManager != null) {
                        if (cacheManager.getCache(event.getCacheName()) != null) {
                            cacheManager.getCache(event.getCacheName()).remove(event.getCacheKey());
                            log.debug("Global cache invalidated: " + event.getCacheKey());
                        } else {
                            log.error("Global cache invalidation: error cache is null");
                        }
                    } else {
                        log.error("Global cache invalidation: error cache manager is null");
                    }
                } else {
                    ConfigurationManager.getSentMsgBuffer().remove(event.getUuid().trim()); // To resolve future performance issues
                    log.debug("Global cache invalidation: own message ignored");
                }
            } catch (Exception e) {
                log.error("Global cache invalidation: error local cache update", e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    private Runnable messageReciever = new Runnable() {
        @Override
        public void run() {
            while(consumer != null) {
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                    onMessage(delivery.getBody());
                } catch (Exception e) {
                    log.error("Global cache invalidation: error message recieve.", e);
                }
            }
        }
    };

    private Object deserialize(byte[] bytes) throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return objectInputStream.readObject();
    }
}
