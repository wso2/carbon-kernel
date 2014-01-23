/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.json;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;

import java.io.Reader;

/** Makes the OMSourcedElementImpl object with the JSONDataSource inside. */

public class JSONOMBuilder extends AbstractJSONOMBuilder {
    @Override
    protected AbstractJSONDataSource getDataSource(Reader
            jsonReader, String prefix, String localName) {
        return new JSONDataSource(jsonReader, "\"" + prefix + localName + "\"");
    }

    @Override
    public String getDefaultJSONString(MessageContext msgCtx, String localName) throws AxisFault {
        return new StringBuffer("{\"").append(localName).append("\":\"\"}").toString();
    }

}
