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

package org.apache.axis2.dataretrieval;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.ws.commons.schema.XmlSchema;

/**
 * Return a XMLSchema as an OMElement.  This is used by any AxisService that wishes
 * to override the standard AxisService2WSDL (see the org.apache.axis2.description
 * package) method of getting XSD.  If one of these is present in the AxisService
 * Parameters under the name "SchemaSupplier", it will be queried.
 */
public interface SchemaSupplier {
    XmlSchema getSchema(AxisService service, String xsd) throws AxisFault;
}
