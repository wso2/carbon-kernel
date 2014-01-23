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

import org.apache.axiom.om.OMDataSource;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamWriter;

import javax.xml.stream.XMLStreamWriter;
import java.io.Writer;

/**
 * This JSONBadgerfishMessageFormatter is the formatter for "Badgerfish" formatted JSON in Axis2.
 * Eg:  &lt;out&gt;&lt;in&gt;mapped JSON&lt;/in&gt;&lt;/out&gt; is converted to...
 * {"out":{"in":{"$":"mapped JSON"}}} This can be used when you want to send messages with
 * namespaces with JSON
 */

public class JSONBadgerfishMessageFormatter extends AbstractJSONMessageFormatter {

    //returns the writer for the badgerfish format
    @Override
    protected XMLStreamWriter getJSONWriter(Writer writer) {
        return new BadgerFishXMLStreamWriter(writer);
    }

    /**
     * If the data source is a "Badgerfish" formatted data source, gives the JSON string by directly
     * taking from the data source.
     *
     * @param dataSource data source to be checked
     * @return the JSON string to write
     */
    @Override
    protected String getStringToWrite(OMDataSource dataSource) {
        if (dataSource instanceof JSONBadgerfishDataSource) {
            return ((JSONBadgerfishDataSource)dataSource).getCompleteJOSNString();
        } else {
            return null;
        }
    }
}
