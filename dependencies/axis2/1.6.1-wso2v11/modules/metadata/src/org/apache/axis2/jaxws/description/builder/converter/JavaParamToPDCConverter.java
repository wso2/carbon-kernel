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

import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebParamAnnot;

import javax.jws.WebParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JavaParamToPDCConverter {

    private Type[] paramTypes;

    private Annotation[][] paramAnnotations;

    public JavaParamToPDCConverter(Type[] paramTypes, Annotation[][] paramAnnotations) {
        this.paramTypes = paramTypes;
        this.paramAnnotations = paramAnnotations;
    }

    public List<ParameterDescriptionComposite> convertParams() {
        List<ParameterDescriptionComposite> pdcList = new
                ArrayList<ParameterDescriptionComposite>();
        for (int i = 0; i < paramTypes.length; i++) {
            ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
            Type paramType = paramTypes[i];
            String fullType = ConverterUtils.getType(paramType, "");
            pdc.setParameterType(fullType);
            pdc.setListOrder(i);
            attachWebParamAnnotation(pdc, i);
            pdc.setIsListType(ConverterUtils.hasXmlListAnnotation(paramAnnotations[i]));
            pdcList.add(pdc);            
        }
        return pdcList;
    }


    /**
     * This method will attach @WebParam annotation data to the <code> ParameterDescriptionComposite</code>
     * if the annotation was found on the parameter represented by this index in the parameter list.
     *
     * @param pdc   - <code>ParameterDescriptionComposite</code>
     * @param order - the current index in the parameter list
     */
    private void attachWebParamAnnotation(ParameterDescriptionComposite pdc, int order) {
        Annotation[] orderAnnots = paramAnnotations[order];
        for (Annotation annot : orderAnnots) {
            if (annot instanceof WebParam) {
                WebParam webParam = (WebParam)annot;
                WebParamAnnot wpAnnot = WebParamAnnot.createWebParamAnnotImpl();
                wpAnnot.setHeader(webParam.header());
                wpAnnot.setMode(webParam.mode());
                wpAnnot.setName(webParam.name());
                wpAnnot.setPartName(webParam.partName());
                wpAnnot.setTargetNamespace(webParam.targetNamespace());
                pdc.setWebParamAnnot(wpAnnot);
            }
        }
    }
}
