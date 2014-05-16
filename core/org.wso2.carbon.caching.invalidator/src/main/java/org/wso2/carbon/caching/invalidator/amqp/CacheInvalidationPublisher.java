/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.impl.CacheInvalidator;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class CacheInvalidationPublisher implements CacheInvalidator {
    private static final Log log = LogFactory.getLog(CacheInvalidationPublisher.class);

    @Override
    public void invalidateCache(int tenantId, String cacheManagerName, String cacheName, Serializable cacheKey) {
        log.debug("Global cache invalidation: initializing the connection");
        if (ConfigurationManager.getProviderUrl() == null) {
            ConfigurationManager.init();
        }
        //Converting data to json string
        GlobalCacheInvalidationEvent event = new GlobalCacheInvalidationEvent();
        event.setTenantId(tenantId);
        event.setCacheManagerName(cacheManagerName);
        event.setCacheName(cacheName);
        event.setCacheKey(cacheKey);
        String uuid = UUIDGenerator.generateUUID();
        event.setUuid(uuid);
        // Setup the pub/sub connection, session
        // Send the msg (byte stream)
        Connection connection = null;
        try {
            log.debug("Global cache invalidation: converting serializable object to byte stream.");
            byte data[] = serialize(event);
            log.debug("Global cache invalidation: converting data to byte stream complete.");
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(ConfigurationManager.getProviderUrl());
            connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(ConfigurationManager.getTopicName(), "topic");
            channel.basicPublish(ConfigurationManager.getTopicName(), "invalidate.cache", null, data);
            ConfigurationManager.getSentMsgBuffer().add(uuid.trim());
            log.debug("Global cache invalidation message sent: " + new String(data));
        } catch (Exception e) {
            log.error("Global cache invalidation: Error publishing the message", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    log.error("Global cache invalidation: error close publish connection", e);
                }
            }
        }
    }

    private byte[] serialize(Object obj) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(obj);
        return byteArrayOutputStream.toByteArray();
    }
}
