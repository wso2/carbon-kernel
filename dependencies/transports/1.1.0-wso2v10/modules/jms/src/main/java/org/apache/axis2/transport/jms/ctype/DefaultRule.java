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

import javax.jms.Message;

/**
 * Content type rule that always matches and that returns a fixed (default) content type.
 */
public class DefaultRule implements ContentTypeRule {
    private final String contentType;

    public DefaultRule(String contentType) {
        this.contentType = contentType;
    }

    public ContentTypeInfo getContentType(Message message) {
        return new ContentTypeInfo(null, contentType);
    }

    public String getExpectedContentTypeProperty() {
        return null;
    }
}
