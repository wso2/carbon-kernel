package org.wso2.carbon.security.keystore.model;

public class PubCertModel {

    private String fileNameAppender;

    // TODO: check whether we can eliminate retrieving content when not needed.
    private byte[] content;

    public PubCertModel(String fileNameAppender, byte[] content) {

        this.fileNameAppender = fileNameAppender;
        this.content = content;
    }

    public PubCertModel() {

    }

    public String getFileNameAppender() {

        return fileNameAppender;
    }

    public void setFileNameAppender(String fileNameAppender) {

        this.fileNameAppender = fileNameAppender;
    }

    public byte[] getContent() {

        return content;
    }

    public void setContent(byte[] content) {

        this.content = content;
    }
}
