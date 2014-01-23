package org.apache.axis2.transport.msmq.ctype;

import org.apache.axis2.transport.msmq.util.Message;
import org.apache.axis2.transport.msmq.util.MessageQueueException;


/**
 * Interface implemented by content type rules.
 */
public interface ContentTypeRule {
    /**
     * Attempt to determine the content type of the given MSMQ message.
     *
     * @param message the message
     * @return If the rule matches, the return value encapsulates the content type of the
     *         message and the message property name from which is was extracted
     *         (if applicable). If the rule doesn't match, the method returns null.
     * @throws MessageQueueException
     */
    ContentTypeInfo getContentType(Message message) throws MessageQueueException;

    /**
     * Get the name of the message property used to extract the content type from,
     * if applicable.
     *
     * @return the property name or null if not applicable
     */
    String getExpectedContentTypeProperty();
}

