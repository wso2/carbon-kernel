/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.kernel.configresolver;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
class Transport {

    private String name;
    private int port;
    private String secure;
    private String desc;
    private String password;

    @XmlAttribute
    public void setSecure(String secure) {
        this.secure = secure;
    }

    public String isSecure() {
        return secure;
    }

    @XmlElement
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @XmlElement
    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    @XmlElement
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    @XmlElement
    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}

@XmlRootElement
class Transports {

    private List<Transport> transport;

    @XmlElement
    public void setTransport(List<Transport> transport) {
        this.transport = transport;
    }

    public List<Transport> getTransport() {
        return transport;
    }

}

/**
 * @since 5.2.0.
 */
@XmlRootElement
public class Configurations {

    private String tenant;
    private Transports transports;

    @XmlElement
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getTenant() {
        return tenant;
    }

    @XmlElement
    public void setTransports(Transports transports) {
        this.transports = transports;
    }

    public Transports getTransports() {
        return transports;
    }
}
