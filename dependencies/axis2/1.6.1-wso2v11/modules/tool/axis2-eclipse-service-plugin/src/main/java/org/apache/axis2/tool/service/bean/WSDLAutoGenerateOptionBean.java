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

package org.apache.axis2.tool.service.bean;

import java.util.Vector;


public class WSDLAutoGenerateOptionBean {
    //rest of the parameters are taken from other
    //beans
    private String classFileName;
    private String style;
    private String outputFileName;
    private Vector listOfMethods;
    

    /**
     * @return Returns the outputFileName.
     */
    public String getOutputFileName() {
        return outputFileName;
    }
    /**
     * @param outputFileName The outputFileName to set.
     */
    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }
    /**
     * @return Returns the classFileName.
     */
    public String getClassFileName() {
        return classFileName;
    }
    /**
     * @param classFileName The classFileName to set.
     */
    public void setClassFileName(String classFileName) {
        this.classFileName = classFileName;
    }
    /**
     * @return Returns the style.
     */
    public String getStyle() {
        return style;
    }
    /**
     * @param style The style to set.
     */
    public void setStyle(String style) {
        this.style = style;
    }
    
    /**
     * @return Returns the listOfMethods.
     */
    public Vector getListOfMethods() {
        return listOfMethods;
    }
    /**
     * @param listOfMethods The listOfMethods to set.
     */
    public void setListOfMethods(Vector listOfMethods) {
        this.listOfMethods = listOfMethods;
    }
}
