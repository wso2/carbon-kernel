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
package org.wso2.carbon.caching.invalidator.jms;

import com.google.gson.Gson;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.impl.CacheInvalidator;
import org.wso2.carbon.caching.invalidator.internal.CacheInvalidationDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.utils.CarbonUtils;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.jms.*;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class JMSGlobalCacheInvalidationImpl implements CacheInvalidator, MessageListener {
    private static final Log log = LogFactory.getLog(JMSGlobalCacheInvalidationImpl.class);
    private List<String> sentMsgBuffer = new ArrayList<String>();
    private TopicConnection conn = null;
    private TopicSession session = null;
    private Topic topic = null;

    public JMSGlobalCacheInvalidationImpl(){
        init(); // initialize and subscribe to the server at the bundle activation
        log.debug("Global cache invalidation: initializing the subscription");
    }

    private void init(){
        String namingFactory = null;
        String providerUrl = null;
        String topicName = null;

        String configFilePath = CarbonUtils.getCarbonHome() + File.separator + "repository"
                + File.separator + "conf" + File.separator + "cache.xml";
        try{
            StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(new FileInputStream(configFilePath));
            OMElement documentElement = stAXOMBuilder.getDocumentElement();
            Iterator iterator;
            iterator = documentElement.getChildrenWithName(new QName("namingFactory"));

            if(iterator.hasNext()){
                OMElement cache = (OMElement) iterator.next();
                namingFactory = cache.getText();
            }

            iterator = documentElement.getChildrenWithName(new QName("namingProviderUrl"));

            if(iterator.hasNext()){
                OMElement cache = (OMElement) iterator.next();
                providerUrl = cache.getText();
            }

            iterator = documentElement.getChildrenWithName(new QName("cacheInvalidateTopic"));

            if(iterator.hasNext()){
                OMElement cache = (OMElement) iterator.next();
                topicName = cache.getText();
            }

            boolean propertyExists = namingFactory != null && !namingFactory.equals("");
            propertyExists &= providerUrl != null && !providerUrl.equals("");
            propertyExists &= topicName != null && !topicName.equals("");

            if(propertyExists){
                Properties properties = new Properties();
                properties.setProperty("java.naming.factory.initial", namingFactory);
                properties.setProperty("java.naming.provider.url", providerUrl);
                properties.setProperty("topic." + topicName, topicName);
                initJMSBroker(properties, topicName);
            }else{
                log.info("Global cache invalidation is offline according to cache.xml configurations");
            }

        }catch (Exception e){
            log.info("Global cache invalidation is offline according to cache.xml configurations");
        }
    }

    private void initJMSBroker(Properties properties, String topicName) {
        try {
            InitialContext iniCtx = new InitialContext(properties);
            Object tmp = iniCtx.lookup("ConnectionFactory");
            TopicConnectionFactory tcf = (TopicConnectionFactory) tmp;
            TopicConnection conn = tcf.createTopicConnection();
            topic = (Topic) iniCtx.lookup(topicName);
            session = conn.createTopicSession(false,
                    TopicSession.AUTO_ACKNOWLEDGE);
            TopicSubscriber subscriber = session.createSubscriber(topic);
            subscriber.setMessageListener(this);
            conn.start();
            log.info("Global cache invalidation is online");
        } catch (Exception e) {
            log.error("Global cache invalidation: Error jms broker initialization", e);
        }
    }

    @Override
    public void invalidateCache(int tenantId, String cacheManagerName, String cacheName, Serializable cacheKey)
    {
        if(session == null){
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
            try {
                TopicPublisher publisher = session.createPublisher(topic);
                TextMessage tm = session.createTextMessage(json);
                publisher.publish(tm);
                sentMsgBuffer.add(uuid.trim());
                publisher.close();
            } catch (Exception e) {
                log.error("Global cache invalidation: Error publishing the message", e);
            }
        }
    }

    @Override
    public void onMessage(Message message) {

        try{
            String msg = ((TextMessage) message).getText();
            log.debug("Cache invalidation message received: " + msg);
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
            }
        } catch (Exception e) {
            log.error("Global cache invalidation: error local cache update", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void stop()
    {
        try{
            if(conn != null && session != null) {
                conn.stop();
                session.close();
                conn.close();
            }else{
                log.debug("Global cache invalidation: no connection");
            }
        }catch(Exception e){
            log.error("Global cache invalidation: error stopping connection", e);
        }

    }

}
