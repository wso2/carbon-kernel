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

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.impl.CacheInvalidator;
import org.wso2.carbon.caching.impl.CacheInvalidatorKey;
import org.wso2.carbon.caching.invalidator.internal.CacheInvalidationDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.utils.CarbonUtils;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AMQPGlobalCacheInvalidationImpl implements CacheInvalidator, Runnable {
    private static final Log log = LogFactory.getLog(AMQPGlobalCacheInvalidationImpl.class);
    private List<String> sentMsgBuffer = new ArrayList<String>();
    private String topicName = null;
    private String providerUrl = null;
    private QueueingConsumer consumer = null;

    public AMQPGlobalCacheInvalidationImpl(){
        if(init()) {
            initMSBroker(); // initialize and subscribe to the server at the bundle activation
        }
        log.debug("Global cache invalidation: initializing the subscription");
    }

    private boolean init(){
        boolean propertyExists = false;
        providerUrl = null;

        String configFilePath = CarbonUtils.getCarbonHome() + File.separator + "repository"
                + File.separator + "conf" + File.separator + "cache.xml";
        try{
            StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(new FileInputStream(configFilePath));
            OMElement documentElement = stAXOMBuilder.getDocumentElement();
            Iterator iterator;

            iterator = documentElement.getChildrenWithName(new QName("providerUrl"));

            if(iterator.hasNext()){
                OMElement cache = (OMElement) iterator.next();
                providerUrl = cache.getText();
            }

            iterator = documentElement.getChildrenWithName(new QName("cacheInvalidateTopic"));

            if(iterator.hasNext()){
                OMElement cache = (OMElement) iterator.next();
                topicName = cache.getText();
            }

            propertyExists = providerUrl != null && !providerUrl.equals("");
            propertyExists &= topicName != null && !topicName.equals("");

            if(!propertyExists){
                log.info("Global cache invalidation is offline according to cache.xml configurations");
            }

        }catch (Exception e){
            log.info("Global cache invalidation is offline according to cache.xml configurations");
        }
        return propertyExists;
    }

    private void initMSBroker() {
        Thread reciever = new Thread(this);
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(providerUrl);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(topicName, "topic");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, topicName, "#");
            consumer = new QueueingConsumer(channel);
            channel.basicConsume(queueName, true, consumer);
            reciever.start();
            log.info("Global cache invalidation is online");
        } catch (Exception e) {
            log.error("Global cache invalidation: Error message broker initialization", e);
        }
    }

    @Override
    public void invalidateCache(int tenantId, String cacheManagerName, String cacheName, CacheInvalidatorKey cacheKey)
    {
        if(providerUrl == null){
            init();
            log.debug("Global cache invalidation: initializing the connection");
        }
        boolean isCoordinator = CacheInvalidationDataHolder.getConfigContext().getAxisConfiguration().getClusteringAgent().isCoordinator();
        if(isCoordinator) {
            //Converting data to json string
            GlobalCacheInvalidationEvent event = new GlobalCacheInvalidationEvent();
            event.setTenantId(tenantId);
            event.setCacheManagerName(cacheManagerName);
            event.setCacheName(cacheName);
            event.setCacheKey(cacheKey);
            String uuid = UUIDGenerator.generateUUID();
            event.setUuid(uuid);
            Gson gson = new Gson();
            String json = gson.toJson(event);
            // Setup the pub/sub connection, session
            // Send the msg (JSON)
            Connection connection = null;
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(providerUrl);
                connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.exchangeDeclare(topicName, "topic");
                channel.basicPublish(topicName, "invlidate.cache", null, json.getBytes());
                sentMsgBuffer.add(uuid.trim());
                log.debug("Global cache invalidation message sent: " + json);
            } catch (Exception e) {
                log.error("Global cache invalidation: Error publishing the message", e);
            }finally {
                if (connection != null) {
                    try {
                        connection.close();
                    }
                    catch (Exception ignore) {}
                }
            }
        }
    }

    public void onMessage(String msg) {
        log.debug("Cache invalidation message received: " + msg);
        boolean isCoordinator = CacheInvalidationDataHolder.getConfigContext().getAxisConfiguration().getClusteringAgent().isCoordinator();
        if(isCoordinator) {
            try {
                Gson gson = new Gson();
                GlobalCacheInvalidationEvent event = gson.fromJson(msg, GlobalCacheInvalidationEvent.class);
                if (!sentMsgBuffer.contains(event.getUuid().trim())) { // Ignore own messages
                    PrivilegedCarbonContext.startTenantFlow();
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
                    sentMsgBuffer.remove(event.getUuid().trim()); // To resolve future performance issues
                    log.debug("Global cache invalidation: own message ignored");
                }
            } catch (Exception e) {
                log.error("Global cache invalidation: error local cache update", e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    @Override
    public void run() {
        while(true) {
            try {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                String message = new String(delivery.getBody());
                onMessage(message);
            } catch (Exception e) {
                log.error("Global cache invalidation: error message recieve", e);
            }
        }
    }
}
