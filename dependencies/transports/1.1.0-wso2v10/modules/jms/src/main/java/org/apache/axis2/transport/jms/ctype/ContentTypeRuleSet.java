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
package org.apache.axis2.transport.jms.ctype;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;

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
     * @throws JMSException
     */
    public ContentTypeInfo getContentTypeInfo(Message message) throws JMSException {
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
