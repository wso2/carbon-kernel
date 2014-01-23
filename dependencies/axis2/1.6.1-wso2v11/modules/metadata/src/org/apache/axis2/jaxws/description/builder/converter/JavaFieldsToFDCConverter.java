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

package org.apache.axis2.jaxws.description.builder.converter;

import org.apache.axis2.jaxws.description.builder.FieldDescriptionComposite;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/** This class will be used to convert Java Fields into FieldDescriptionComposite objects. */
public class JavaFieldsToFDCConverter {

    private Field[] fields;

    public JavaFieldsToFDCConverter(Field[] fields) {
        this.fields = fields;
    }

    /**
     * This method will be called to create <code>FieldDescriptionComposite</code> objects for public
     * fields in a given Class.
     *
     * @return - <code>List</code>
     */
    public List<FieldDescriptionComposite> convertFields() {
        List<FieldDescriptionComposite> fdcList = new
                ArrayList<FieldDescriptionComposite>();
        for (Field field : fields) {
            FieldDescriptionComposite fdc = new FieldDescriptionComposite();
            fdc.setFieldName(field.getName());
            fdc.setModifierType(Modifier.toString(field.getModifiers()));
            attachHandlerChainAnnotation(fdc, field);
            attachWebServiceRefAnnotation(fdc, field);
        }
        return fdcList;
    }

    /**
     * This method will drive the call to attach @HandlerChain annotation data if it is found on the
     * Field.
     *
     * @param fdc   - <code>FieldDescriptionComposite</code>
     * @param field - <code>Field</code>
     */
    private void attachHandlerChainAnnotation(FieldDescriptionComposite fdc,
                                              Field field) {
        ConverterUtils.attachHandlerChainAnnotation(fdc, field);
    }

    /**
     * This method will drive the call to attach @WebServiceRef annotation data
     * if it is found on the Field.
     * @param fdc - <code>FieldDescriptionComposite</code>
     * @param field - <code>Field</code>
     */
    private void attachWebServiceRefAnnotation(FieldDescriptionComposite fdc,
                                               Field field) {
        ConverterUtils.attachWebServiceRefAnnotation(fdc, field);
    }
}
