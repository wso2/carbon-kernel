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

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Content type rule that attempts to extract the content type from a message property.
 */
public class PropertyRule implements ContentTypeRule {
    private final String propertyName;

    public PropertyRule(String propertyName) {
        this.propertyName = propertyName;
    }

    public ContentTypeInfo getContentType(Message message) throws JMSException {
        String value = message.getStringProperty(propertyName);
        return value == null ? null : new ContentTypeInfo(propertyName, value);
    }

    public String getExpectedContentTypeProperty() {
        return propertyName;
    }
}
