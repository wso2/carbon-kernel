package org.wso2.carbon.core.persistence.file;

import org.apache.axiom.om.OMElement;

import java.io.File;

public class ResourceFileData {
    /**
     * The object model of the servicegroup
     */
    private OMElement OMElement;
    /**
     * A File reference to where the metafile for a service group supposed to exist.
     */
    private File file;
    private boolean isTransactionStarted = false;
    private long fileLastModifiedDate;

    public ResourceFileData(OMElement OMElement, File file, boolean isTransactionStarted) {
        this.OMElement = OMElement;
        this.file = file;
        this.isTransactionStarted = isTransactionStarted;
        this.fileLastModifiedDate = file.lastModified();
    }

    public OMElement getOMElement() {
        return OMElement;
    }

    public void setOMElement(OMElement OMElement) {
        this.OMElement = OMElement;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        this.fileLastModifiedDate = file.lastModified();
    }

    public boolean isTransactionStarted() {
        return isTransactionStarted;
    }

    public void setTransactionStarted(boolean transactionStarted) {
        isTransactionStarted = transactionStarted;
    }

    public String toString() {
        if (this.getOMElement() != null) {
            return this.getOMElement().toString();
        } else {
            return super.toString();
        }
    }

    public long getFileLastModifiedDate() {
        return fileLastModifiedDate;
    }
}
