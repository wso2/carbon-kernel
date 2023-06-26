package org.wso2.carbon.security.keystore.model;

import java.util.Date;

public class KeyStoreModel {

    // TODO: check whether we need the uuid.
//    private String id;
    private String fileName;
    private String type;
    private String provider;
    private String password;
    private String privateKeyAlias;
    private String privateKeyPass;
    private Date lastUpdated;

    // TODO: check whether we can eliminate retrieving content when not needed.
    private byte[] content;

    public KeyStoreModel() {

        this(null, null, null, null, null);
    }

    public KeyStoreModel(String fileName, String type, String provider, String password, byte[] content) {

        this(fileName, type, provider, password, null, null, content);
    }

    public KeyStoreModel(String fileName, String type, String provider, String password,
                         String privateKeyAlias,
                         String privateKeyPass, byte[] content) {

        this.fileName = fileName;
        this.type = type;
        this.provider = provider;
        this.password = password;
        this.content = content;
        this.privateKeyAlias = privateKeyAlias;
        this.privateKeyPass = privateKeyPass;
    }

    public String getFileName() {

        return fileName;
    }

    public void setFileName(String fileName) {

        this.fileName = fileName;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getProvider() {

        return provider;
    }

    public void setProvider(String provider) {

        this.provider = provider;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getPrivateKeyAlias() {

        return privateKeyAlias;
    }

    public void setPrivateKeyAlias(String privateKeyAlias) {

        this.privateKeyAlias = privateKeyAlias;
    }

    public String getPrivateKeyPass() {

        return privateKeyPass;
    }

    public void setPrivateKeyPass(String privateKeyPass) {

        this.privateKeyPass = privateKeyPass;
    }

    public byte[] getContent() {

        return content;
    }

    public void setContent(byte[] content) {

        this.content = content;
    }


    public Date getLastUpdated() {

        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {

        this.lastUpdated = lastUpdated;
    }
}
