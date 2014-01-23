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

import java.util.Iterator;

import javax.jms.BytesMessage;
import javax.jms.TextMessage;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;

/**
 * Utility class to create content type rules and rule sets from XML.
 */
public class ContentTypeRuleFactory {
    private ContentTypeRuleFactory() {}
    
    public static ContentTypeRule parse(OMElement element) throws AxisFault {
        String name = element.getLocalName();
        String value = element.getText();
        if (name.equals("jmsProperty")) {
            return new PropertyRule(value);
        } else if (name.equals("textMessage")) {
            return new MessageTypeRule(TextMessage.class, value);
        } else if (name.equals("bytesMessage")) {
            return new MessageTypeRule(BytesMessage.class, value);
        } else if (name.equals("default")) {
            return new DefaultRule(value);
        } else {
            throw new AxisFault("Unknown content rule type '" + name + "'");
        }
    }
    
    public static ContentTypeRuleSet parse(Parameter param) throws AxisFault {
        ContentTypeRuleSet ruleSet = new ContentTypeRuleSet();
        Object value = param.getValue();
        if (value instanceof OMElement) {
            OMElement element = (OMElement)value;
            
            // DescriptionBuilder#processParameters actually sets the parameter element
            // itself as the value. We need to support this case.
            // TODO: seems like a bug in Axis2 and is inconsistent with Synapse's way of parsing parameter in proxy definitions
            if (element == param.getParameterElement()) {
                element = element.getFirstElement();
            }
            
            if (element.getLocalName().equals("rules")) {
                for (Iterator it = element.getChildElements(); it.hasNext(); ) {
                    ruleSet.addRule(parse((OMElement)it.next()));
                }
            } else {
                throw new AxisFault("Expected <rules> element");
            }
        } else {
            ruleSet.addRule(new DefaultRule((String)value));
        }
        return ruleSet;
    }
}
