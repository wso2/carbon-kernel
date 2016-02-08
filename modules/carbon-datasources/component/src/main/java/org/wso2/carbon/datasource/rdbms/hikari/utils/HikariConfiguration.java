package org.wso2.carbon.datasource.rdbms.hikari.utils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "configuration")
public class HikariConfiguration {

    private String url;

    private String username;

    private Password passwordPersist;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        if (this.getPasswordPersist() == null) {
            this.passwordPersist = new Password();
        }
        this.passwordPersist.setValue(password);
    }

    @XmlTransient
    public String getPassword() {
        if (this.getPasswordPersist() != null) {
            return this.getPasswordPersist().getValue();
        } else {
            return null;
        }
    }

    @XmlElement(name = "password")
    public Password getPasswordPersist() {
        return passwordPersist;
    }

    public void setPasswordPersist(Password passwordPersist) {
        this.passwordPersist = passwordPersist;
    }

    @XmlRootElement (name = "password")
    public static class Password {

        private boolean encrypted = true;

        private String value;

        @XmlAttribute(name = "encrypted")
        public boolean isEncrypted() {
            return encrypted;
        }

        public void setEncrypted(boolean encrypted) {
            this.encrypted = encrypted;
        }

        @XmlValue
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }
}
