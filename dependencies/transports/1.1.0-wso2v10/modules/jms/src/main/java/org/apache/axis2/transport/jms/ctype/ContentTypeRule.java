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
 * Interface implemented by content type rules.
 */
public interface ContentTypeRule {
    /**
     * Attempt to determine the content type of the given JMS message.
     * 
     * @param message the message
     * @return If the rule matches, the return value encapsulates the content type of the
     *         message and the message property name from which is was extracted
     *         (if applicable). If the rule doesn't match, the method returns null.
     * @throws JMSException
     */
    ContentTypeInfo getContentType(Message message) throws JMSException;
    
    /**
     * Get the name of the message property used to extract the content type from,
     * if applicable.
     * 
     * @return the property name or null if not applicable
     */
    String getExpectedContentTypeProperty();
}
