/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.utils.transport;

/**
 * If proxyPort parameter present in a listener derived
 * from org.wso2.carbon.utils.transport.AbstractTransportListener, this will get populated. A user can
 * get hold of the proxy related ports and inject into any UI generation framework. 
 *
 */
public class ProxyCache {
    private int httpPort = -1;
    private int httpsPort = -1;
    private static ProxyCache instance;
    private static final Object obj = new Object();

    public static  ProxyCache getInstance() {
        synchronized (obj) {
            if (instance == null) {
                instance = new ProxyCache();
            }
            return instance;
        }
    }

    private ProxyCache() {}

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
    }
}
