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

package org.apache.axis2.rmi.databind;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter;
import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.OMElementCreationException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.exception.XmlSerializingException;
import org.apache.axis2.rmi.metadata.Parameter;
import org.apache.axis2.rmi.util.NamespacePrefix;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.Map;

/**
 * this class is used to create omElements from the
 * differet types of java beans
 */
public class OMElementCreator {

    public OMElement getOMElement(final Object value,
                                  final Parameter parameter,
                                  final Configurator configurator) throws OMElementCreationException {

        OMElement returnOMElement = null;
        Map processedTypeMap = new HashMap();
        Map processedSchemaMap = new HashMap();

        try {
            parameter.populateMetaData(configurator, processedTypeMap);
            parameter.generateSchema(configurator, processedSchemaMap);

            final JavaObjectSerializer javaObjectSerializer =
                    new JavaObjectSerializer(processedSchemaMap, configurator, processedSchemaMap);

            OMDataSource omDataSource = new RMIDataSource() {

                public void serialize(MTOMAwareXMLStreamWriter xmlWriter) throws XMLStreamException {
                    try {
                        javaObjectSerializer.serializeParameter(value, parameter, xmlWriter, new NamespacePrefix());
                    } catch (XmlSerializingException e) {
                        throw new XMLStreamException("Problem in parsing the xml stream", e);
                    }
                }
            };

            QName qname = new QName(parameter.getNamespace(), parameter.getName());
            returnOMElement = new OMSourcedElementImpl(qname, OMAbstractFactory.getOMFactory(), omDataSource);

        } catch (MetaDataPopulateException e) {
            throw new OMElementCreationException("Problem in meta data population", e);
        } catch (SchemaGenerationException e) {
            throw new OMElementCreationException("Problem in Schema generation", e);
        }

        return returnOMElement;
    }

}
