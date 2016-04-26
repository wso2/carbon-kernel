/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.tomcat.internal;

import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.WebappServiceLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.annotation.HandlesTypes;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The class to process ServletContainerInitializers in form of jars or OSGi bundles.
 * This class overrides the 'processServletContainerInitializers' method in
 * {@link org.apache.catalina.startup.ContextConfig} class.
 */
public class SCIRegistrarContextConfig extends ContextConfig {
    private static Log log = LogFactory.getLog(SCIRegistrarContextConfig.class);

    public SCIRegistrarContextConfig() {
        super();
    }

    @Override
    protected void processServletContainerInitializers() {
        //Scan JARs for ServletContainerInitializer implementations.
        // Code below is from {@link org.apache.catalina.startup.ContextConfig}
        List<ServletContainerInitializer> detectedScis;
        try {
            WebappServiceLoader<ServletContainerInitializer> loader =
                    new WebappServiceLoader<ServletContainerInitializer>(context);
            detectedScis = loader.load(ServletContainerInitializer.class);
        } catch (IOException e) {
            log.error(sm.getString("contextConfig.servletContainerInitializerFail", context.getName()), e);//prints the full stack trace
            ok = false;
            return;
        }

        //code belongs to org.wso2.carbon.tomcat.internal.SCIRegistrarContextConfig
        List<ServletContainerInitializer> mutableDetectedScis = getCustomSciList(detectedScis);

        // Code below is from {@link org.apache.catalina.startup.ContextConfig}
        //made changes in logging.
        for (ServletContainerInitializer sci : mutableDetectedScis) {
            initializerClassMap.put(sci, new HashSet<Class<?>>());
            HandlesTypes ht;
            try {
                ht = sci.getClass().getAnnotation(HandlesTypes.class);
            } catch (Exception e) {//class defined in the @HandlesTypes annotation can be missing.Can throw
                // NullPointerException.
                if (log.isDebugEnabled()) {
                    log.debug(sm.getString("contextConfig.sci.debug", sci.getClass().getName()), e);//changed log.info to log.debug.The debug level prints the full stack trace.
                } else {
                    log.info(sm.getString("contextConfig.sci.info", sci.getClass().getName()));
                }
                continue;
            }
            if (ht == null) {
                continue;
            }
            Class<?>[] types = ht.value();
            if (types == null) {
                continue;
            }
            for (Class<?> type : types) {
                if (type.isAnnotation()) {
                    handlesTypesAnnotations = true;
                } else {
                    handlesTypesNonAnnotations = true;
                }
                Set<ServletContainerInitializer> scis = typeInitializerMap.get(type);
                if (scis == null) {
                    scis = new HashSet<ServletContainerInitializer>();
                    typeInitializerMap.put(type, scis);
                }
                scis.add(sci);
            }
        }
    }

    /**
     * Reads the sci's returned from {@link org.apache.catalina.startup.ContextConfig}, and add it to mutable list & finally
     * other sci's which comes as OSGi bundles and return the full list of sci's.
     *
     * @param detectedScis sci's which are already detected.
     * @return List<ServletContainerInitializer>
     */
    private List<ServletContainerInitializer> getCustomSciList(List<ServletContainerInitializer> detectedScis) {
        //since the WebappServiceLoader, 'loadServices' method returns an 'unmodifiableList'
        List<ServletContainerInitializer> mutableDetectedScis = new ArrayList<ServletContainerInitializer>();
        mutableDetectedScis.addAll(detectedScis);
        List<ServletContainerInitializer> sciList = DataHolder.getInstance().getServiceContainerInitializers();
        mutableDetectedScis.addAll(sciList);
        return mutableDetectedScis;
    }
}
