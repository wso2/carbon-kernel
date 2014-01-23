package org.apache.axis2.transport.msmq.ctype;


/**
 * Class encapsulating the content type information for a given message.
 */
public class ContentTypeInfo {
    private final String propertyName;
    private final String contentType;

    public ContentTypeInfo(String propertyName, String contentType) {
        this.propertyName = propertyName;
        this.contentType = contentType;
    }

    /**
     * Get the name of the message property from which the content type
     * has been extracted.
     *
     * @return the property name or null if the content type was not determined
     *         by extracting it from a message property
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Get the content type of the message.
     *
     * @return The content type of the message. The return value is never null.
     */
    public String getContentType() {
        return contentType;
    }
}

