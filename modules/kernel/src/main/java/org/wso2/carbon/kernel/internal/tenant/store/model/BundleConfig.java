package org.wso2.carbon.kernel.internal.tenant.store.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class BundleConfig {

    @XmlAttribute(name = "id", required = true)
    private long id;

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "version", required = true)
    private String version;

    @XmlValue
    private String location;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBundleLocation() {
        return location;
    }

    public void setBundleLocation(String location) {
        this.location = location;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
