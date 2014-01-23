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
