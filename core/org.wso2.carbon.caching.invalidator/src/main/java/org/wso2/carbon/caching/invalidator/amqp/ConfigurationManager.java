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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConfigurationManager {
    private static final Log log = LogFactory.getLog(ConfigurationManager.class);

    private static String topicName = null;
    private static String providerUrl = null;
    private static int coordinatorCheckInterval;
    private static boolean subscribed = false;

    private static List<String> sentMsgBuffer = new ArrayList<String>();

    public static boolean init(){
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

            iterator = documentElement.getChildrenWithName(new QName("coordinatorCheckIntervalSeconds"));

            if(iterator.hasNext()){
                OMElement cache = (OMElement) iterator.next();
                coordinatorCheckInterval = Integer.parseInt(cache.getText());
            }

            propertyExists = providerUrl != null && !providerUrl.equals("");
            propertyExists &= topicName != null && !topicName.equals("");
            propertyExists &= coordinatorCheckInterval >= 0;

            if(!propertyExists){
                log.info("Global cache invalidation is offline according to cache.xml configurations");
            }

        }catch (Exception e){
            log.info("Global cache invalidation is offline according to cache.xml configurations");
        }
        return propertyExists;
    }

    public static String getTopicName() {
        return topicName;
    }

    public static String getProviderUrl() {
        return providerUrl;
    }

    public static int getCoordinatorCheckInterval() {
        return coordinatorCheckInterval;
    }

    public static List<String> getSentMsgBuffer() {
        return sentMsgBuffer;
    }

    public static boolean isSubscribed() {
        return subscribed;
    }

    public static void setSubscribed(boolean subscribed) {
        ConfigurationManager.subscribed = subscribed;
    }
}
