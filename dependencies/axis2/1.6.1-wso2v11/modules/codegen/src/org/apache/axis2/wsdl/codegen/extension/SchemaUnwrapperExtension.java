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

package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;
import org.apache.axis2.wsdl.util.Constants;
import org.apache.axis2.wsdl.util.MessagePartInformationHolder;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This extension invokes the schema unwrapper depending on the users setting. it is desirable to
 * put this extension before other extensions since extnsions such as the databinding extension may
 * well depend on the schema being unwrapped previously. For a complete unwrap the following format
 * of the schema is expected &lt; element &gt; &lt; complexType &gt; &lt; sequence &gt; &lt; element
 * /&gt; &lt; /sequence &gt; &lt; /complexType &gt; &lt; /element &gt;
 * <p/>
 * When an unwrapped WSDL is encountered Axis2 generates a wrapper schema and that wrapper schema
 * has the above mentioned format. This unwrapping algorithm will work on a pure doc/lit WSDL if it
 * has the above mentioned format only
 */
public class SchemaUnwrapperExtension extends AbstractCodeGenerationExtension {

    private CodeGenConfiguration codeGenConfiguration;

    /**
     * @param configuration
     * @throws CodeGenerationException
     */
    public void engage(CodeGenConfiguration configuration) throws CodeGenerationException {
        this.codeGenConfiguration = configuration;

        if (!configuration.isParametersWrapped() && !codeGenConfiguration.getOutputLanguage().equals("jax-ws")) {

            // A check to avoid nasty surprises - Since unwrapping is not
            // supported by all frameworks, we check the framework name to be
            // compatible
            if (!ConfigPropertyFileLoader.getUnwrapSupportedFrameworkNames().
                    contains(configuration.getDatabindingType())) {
                throw new CodeGenerationException(
                        CodegenMessages.getMessage("extension.unsupportedforunwrapping"));
            } else if (!ConfigPropertyFileLoader.getUnwrapDirectFrameworkNames().
                    contains(configuration.getDatabindingType())) {

                //walk the schema and find the top level elements
                List services = configuration.getAxisServices();
                AxisService axisService;

                for (Iterator servicesIter = services.iterator(); servicesIter.hasNext();) {
                    axisService = (AxisService) servicesIter.next();
                    for (Iterator operations = axisService.getOperations();
                         operations.hasNext();) {
                        AxisOperation op = (AxisOperation) operations.next();

                        if (WSDLUtil.isInputPresentForMEP(op.getMessageExchangePattern())) {
                            walkSchema(op.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE),
                                    WSDLConstants.INPUT_PART_QNAME_SUFFIX);
                        }
                        // get the out put parameter details as well to unwrap the responses
                        //TODO: support xmlbeans
                        if (configuration.getDatabindingType().equals("adb")) {
                            if (WSDLUtil.isOutputPresentForMEP(op.getMessageExchangePattern())) {
                                walkSchema(op.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE),
                                        WSDLConstants.OUTPUT_PART_QNAME_SUFFIX);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * walk the given schema element For a successful unwrapping the element should have the
     * following structure &lt; element &gt; &lt; complexType &gt; &lt; sequence &gt; &lt; element
     * /&gt; &lt; /sequence &gt; &lt; /complexType &gt; &lt; /element &gt;
     */

    public void walkSchema(AxisMessage message, String qnameSuffix)
            throws CodeGenerationException {
        //nothing to unwrap
        if (message.getSchemaElement() == null) {
            return;
        }


        List partNameList = new LinkedList();

        XmlSchemaElement schemaElement = message.getSchemaElement();
        XmlSchemaType schemaType = schemaElement.getSchemaType();
        QName schemaTypeQname = schemaElement.getSchemaTypeName();
        if (schemaType == null) {
            if (schemaTypeQname != null) {
                // find the schema type from all the schemas
                // now we need to get the schema of the extension type from the parent schema. For that let's first retrieve
                // the parent schema
                AxisService axisService = message.getAxisOperation().getAxisService();
                ArrayList schemasList = axisService.getSchema();

                XmlSchema schema = null;
                for (Iterator iter = schemasList.iterator(); iter.hasNext();) {
                    schema = (XmlSchema) iter.next();
                    schemaType = getSchemaType(schema, schemaTypeQname);
                    if (schemaType != null) {
                        break;
                    }
                }
            }
        }

        if (schemaType instanceof XmlSchemaComplexType) {
            handleAllCasesOfComplexTypes(schemaType,
                    message,
                    partNameList,
                    qnameSuffix);
        } else if ((schemaType instanceof XmlSchemaSimpleType) ||
               ((schemaTypeQname != null) && (schemaTypeQname.equals(new QName("http://www.w3.org/2001/XMLSchema", "anyType")))) ) {
            QName opName = message.getAxisOperation().getName();
            partNameList.add(WSDLUtil.getPartQName(opName.getLocalPart(),
                    qnameSuffix,
                    schemaElement.getQName().getLocalPart()));
        } else if (schemaType == null) {
            throw new CodeGenerationException("Can not determine the schema type for the "
                    + schemaElement.getName());
        } else {
            //we've no idea how to unwrap a non complexType!!!!!!
            throw new CodeGenerationException(
                    CodegenMessages.getMessage("extension.unsupportedSchemaFormat",
                            schemaType.getName(), "complexType"));
        }


        try {
            //set in the axis message that the unwrapping was success
            message.addParameter(getParameter(
                    Constants.UNWRAPPED_KEY,
                    Boolean.TRUE));

            // attach the opName and the parts name list into the
            // axis message by using the holder
            MessagePartInformationHolder infoHolder = new MessagePartInformationHolder();
            infoHolder.setOperationName(message.getAxisOperation().getName());
            infoHolder.setPartsList(partNameList);

            //attach it to the parameters
            message.addParameter(
                    getParameter(Constants.UNWRAPPED_DETAILS,
                            infoHolder));

        } catch (AxisFault axisFault) {
            throw new CodeGenerationException(axisFault);
        }

    }

    private void handleAllCasesOfComplexTypes(XmlSchemaType schemaType,
                                              AxisMessage message,
                                              List partNameList,
                                              String qnameSuffix) throws CodeGenerationException {

        // if a complex type name exits for a element then
        // we keep that complex type to support unwrapping
        if (schemaType instanceof XmlSchemaComplexType) {
            XmlSchemaComplexType cmplxType = (XmlSchemaComplexType) schemaType;
            if (cmplxType.getContentModel() == null) {
                if (cmplxType.getParticle() != null) {
                    processXMLSchemaSequence(cmplxType.getParticle(), message, partNameList,
                            qnameSuffix);
                }
            } else {
                // now lets handle case with extensions
                processComplexContentModel(cmplxType, message, partNameList, qnameSuffix);
            }
            // handle attributes here
            processAttributes(cmplxType, message, partNameList, qnameSuffix);

        }
    }

    private void processAttributes(XmlSchemaComplexType complexType,
                                   AxisMessage message,
                                   List partNameList,
                                   String qnameSuffix) {
        QName opName = message.getAxisOperation().getName();
        XmlSchemaObjectCollection xmlObjectCollection = complexType.getAttributes();
        XmlSchemaObject item;
        XmlSchemaAttribute xmlSchemaAttribute;
        for (Iterator iter = xmlObjectCollection.getIterator(); iter.hasNext();) {
            item = (XmlSchemaObject) iter.next();
            if (item instanceof XmlSchemaAttribute) {
                xmlSchemaAttribute = (XmlSchemaAttribute) item;
                String partName = xmlSchemaAttribute.getName();
                partNameList.add(
                        WSDLUtil.getPartQName(opName.getLocalPart(),
                                qnameSuffix,
                                partName));
            }
        }

    }

    private void processComplexContentModel(XmlSchemaComplexType cmplxType,
                                            AxisMessage message,
                                            List partNameList,
                                            String qnameSuffix) throws CodeGenerationException {
        XmlSchemaContentModel contentModel = cmplxType.getContentModel();
        if (contentModel instanceof XmlSchemaComplexContent) {
            XmlSchemaComplexContent xmlSchemaComplexContent = (XmlSchemaComplexContent) contentModel;
            XmlSchemaContent content = xmlSchemaComplexContent.getContent();
            if (content instanceof XmlSchemaComplexContentExtension) {
                XmlSchemaComplexContentExtension schemaExtension = (XmlSchemaComplexContentExtension) content;

                // process particles inside this extension, if any
                if (schemaExtension.getParticle() != null) {
                    processXMLSchemaSequence(schemaExtension.getParticle(), message, partNameList,
                            qnameSuffix);
                }

                // now we need to get the schema of the extension type from the parent schema. For that let's first retrieve
                // the parent schema
                AxisService axisService = message.getAxisOperation().getAxisService();
                ArrayList schemasList = axisService.getSchema();

                XmlSchema parentSchema = null;

                XmlSchema schema = null;
                XmlSchemaType extensionSchemaType = null;
                for (Iterator iter = schemasList.iterator(); iter.hasNext();) {
                    schema = (XmlSchema) iter.next();
                    extensionSchemaType = getSchemaType(schema, schemaExtension.getBaseTypeName());
                    if (extensionSchemaType != null) {
                        break;
                    }
                }

                // ok now we got the parent schema. Now let's get the extension's schema type

                handleAllCasesOfComplexTypes(extensionSchemaType, message, partNameList,
                        qnameSuffix);
            }
        }
    }

    private XmlSchemaType getSchemaType(XmlSchema schema, QName typeName) {
        XmlSchemaType xmlSchemaType = null;
        if (schema != null) {
            xmlSchemaType = schema.getTypeByName(typeName);
            if (xmlSchemaType == null) {
                // try to find in an import or an include
                XmlSchemaObjectCollection includes = schema.getIncludes();
                if (includes != null) {
                    Iterator includesIter = includes.getIterator();
                    Object object = null;
                    while (includesIter.hasNext()) {
                        object = includesIter.next();
                        if (object instanceof XmlSchemaImport) {
                            XmlSchema schema1 = ((XmlSchemaImport) object).getSchema();
                            xmlSchemaType = getSchemaType(schema1, typeName);
                        }
                        if (object instanceof XmlSchemaInclude) {
                            XmlSchema schema1 = ((XmlSchemaInclude) object).getSchema();
                            xmlSchemaType = getSchemaType(schema1, typeName);
                        }
                        if (xmlSchemaType != null) {
                            break;
                        }
                    }
                }
            }
        }
        return xmlSchemaType;
    }

    private void processXMLSchemaSequence(XmlSchemaParticle schemaParticle,
                                          AxisMessage message,
                                          List partNameList,
                                          String qnameSuffix) throws CodeGenerationException {
        if (schemaParticle instanceof XmlSchemaSequence) {
            // get the name of the operation name and namespace,
            // part name and hang them somewhere ? The ideal place
            // would be the property bag in the codegen config!
            QName opName = message.getAxisOperation().getName();

            XmlSchemaSequence sequence = (XmlSchemaSequence) schemaParticle;
            XmlSchemaObjectCollection items = sequence.getItems();

            // if this is an empty sequence, return
            if (items.getCount() == 0) {
                return;
            }
            for (Iterator i = items.getIterator(); i.hasNext();) {
                Object item = i.next();
                // get each and every element in the sequence and
                // traverse through them
                if (item instanceof XmlSchemaElement) {
                    //add the element name to the part name list
                    XmlSchemaElement xmlSchemaElement = (XmlSchemaElement) item;
                    XmlSchemaType schemaType = xmlSchemaElement.getSchemaType();
                    String partName = null;
                    if (xmlSchemaElement.getRefName() != null) {
                        partName = xmlSchemaElement.getRefName().getLocalPart();
                    } else {
                        partName = xmlSchemaElement.getName();
                    }

                    //  part names are not unique across messages. Hence
                    //  we need some way of making the part name a unique
                    //  one (due to the fact that the type mapper
                    //  is a global list of types).
                    //  The seemingly best way to do that is to
                    //  specify a namespace for the part QName reference which
                    //  is stored in the  list. This part qname is
                    //  temporary and should not be used with it's
                    //  namespace URI (which happened to be the operation name)
                    //  with _input attached to it


                    partNameList.add(
                            WSDLUtil.getPartQName(opName.getLocalPart(),
                                    qnameSuffix,
                                    partName));

                    // if the particle contains anything other than
                    // a XMLSchemaElement then we are not in a position
                    // to unwrap it
                } else if (item instanceof XmlSchemaAny) {

                    // if this is an instance of xs:any, then there is no part name for it. Using ANY_ELEMENT_FIELD_NAME
                    // for it for now

                    //we have to handle both maxoccurs 1 and maxoccurs > 1 situation
                    XmlSchemaAny xmlSchemaAny = (XmlSchemaAny) item;

                    partNameList.add(
                            WSDLUtil.getPartQName(opName.getLocalPart(),
                                    qnameSuffix,
                                    Constants.ANY_ELEMENT_FIELD_NAME));
                } else {
                    throw new CodeGenerationException(
                            CodegenMessages.getMessage("extension.unsupportedSchemaFormat",
                                    "unknown type", "Element"));
                }
            }

            //we do not know how to deal with other particles
            //such as xs:all or xs:choice. Usually occurs when
            //passed with the user built WSDL where the style
            //is document.
        } else if (schemaParticle instanceof XmlSchemaChoice) {
            throw new CodeGenerationException(
                    CodegenMessages.getMessage("extension.unsupportedSchemaFormat",
                            "choice", "sequence"));

        } else if (schemaParticle instanceof XmlSchemaAll) {
            throw new CodeGenerationException(
                    CodegenMessages.getMessage("extension.unsupportedSchemaFormat",
                            "all", "sequence"));
        } else {
            throw new CodeGenerationException(
                    CodegenMessages.getMessage("extension.unsupportedSchemaFormat",
                            "unknown", "sequence"));
        }
    }

    /**
     * Generate a parametes object
     *
     * @param key
     * @param value
     */
    private Parameter getParameter(String key, Object value) {
        Parameter myParameter = new Parameter();
        myParameter.setName(key);
        myParameter.setValue(value);
        return myParameter;
    }


}
