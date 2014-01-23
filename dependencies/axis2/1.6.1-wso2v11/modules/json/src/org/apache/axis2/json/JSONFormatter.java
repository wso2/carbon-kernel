/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.json;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class JSONFormatter extends AbstractJSONMessageFormatter {
    @Override
    public byte[] getBytes(MessageContext msgCtxt, OMOutputFormat format) throws AxisFault {
        if (msgCtxt.getProperty("JSON_STRING") != null) {
            String jsonResponse = (String) msgCtxt.getProperty("JSON_STRING");
            return jsonResponse.getBytes();
        }
        throw new AxisFault("Could not find the JSON response.");
    }

    @Override
    public void writeTo(MessageContext msgCtxt, OMOutputFormat format,
                        OutputStream out, boolean preserve) throws AxisFault {
        if (msgCtxt.getProperty("JSON_STRING") != null) {
            String jsonResponse = (String) msgCtxt.getProperty("JSON_STRING");
            try {
                out.write(jsonResponse.getBytes());
            } catch (IOException e) {
                throw AxisFault.makeFault(e);
            }
        }
    }

    protected XMLStreamWriter getJSONWriter(Writer writer) {
        throw new UnsupportedOperationException("Cannot get a JSON writer");
    }

    protected String getStringToWrite(OMDataSource dataSource) {
        throw new UnsupportedOperationException("Cannot get the JSON string");
    }
}
