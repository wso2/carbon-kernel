package org.apache.axis2.transport.msmq.ctype;



import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.transport.msmq.util.Message;
import org.apache.axis2.transport.msmq.util.MessageQueueException;

/**
 * A set of content type rules.
 */
public class ContentTypeRuleSet {
    private final List<ContentTypeRule> rules = new ArrayList<ContentTypeRule>();
    private String defaultContentTypeProperty;

    /**
     * Add a content type rule to this set.
     *
     * @param rule the rule to add
     */
    public void addRule(ContentTypeRule rule) {
        rules.add(rule);
        if (defaultContentTypeProperty == null) {
            defaultContentTypeProperty = rule.getExpectedContentTypeProperty();
        }
    }

    /**
     * Determine the content type of the given message.
     * This method will try the registered rules in turn until the first rule matches.
     *
     * @param message the message
     * @return the content type information for the message or null if none of the rules matches
     * @throws MessageQueueException
     */
    public ContentTypeInfo getContentTypeInfo(Message message) throws MessageQueueException {
        for (ContentTypeRule rule : rules) {
            ContentTypeInfo contentTypeInfo = rule.getContentType(message);
            if (contentTypeInfo != null) {
                return contentTypeInfo;
            }
        }
        return null;
    }

    public String getDefaultContentTypeProperty() {
        return defaultContentTypeProperty;
    }
}

