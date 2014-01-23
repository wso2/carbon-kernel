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

package org.apache.axiom.om.impl.llom.factory;

import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

import javax.xml.stream.XMLStreamReader;

/**
 * Class OMXMLBuilderFactory
 * 
 * @deprecated This class is deprecated because it is located in the wrong package and JAR
 *             (it is implementation independent but belongs to LLOM). Please use
 *             {@link org.apache.axiom.om.OMXMLBuilderFactory} instead.
 */
public class OMXMLBuilderFactory {
    /** Field PARSER_XPP */
    public static final String PARSER_XPP = "XPP";

    /** Field PARSER_STAX */
    public static final String PARSER_STAX = "StAX";

    /** Field MODEL_SOAP_SPECIFIC */
    public static final String MODEL_SOAP_SPECIFIC = "SOAP_SPECIFIC";

    /** Field MODEL_OM */
    public static final String MODEL_OM = "OM_ONLY";

    /**
     * Method createStAXSOAPModelBuilder.
     *
     * @param soapFactory
     * @param parser
     * @return Returns StAXSOAPModelBuilder.
     */
    public static StAXSOAPModelBuilder createStAXSOAPModelBuilder(
            SOAPFactory soapFactory, XMLStreamReader parser) {
        return new StAXSOAPModelBuilder(parser, soapFactory, null);
    }

    /**
     * Method createStAXOMBuilder.
     *
     * @param ombuilderFactory
     * @param parser
     * @return Returns StAXOMBuilder.
     */
    public static StAXOMBuilder createStAXOMBuilder(OMFactory ombuilderFactory,
                                                    XMLStreamReader parser) {
        return new StAXOMBuilder(ombuilderFactory, parser);
    }
}
