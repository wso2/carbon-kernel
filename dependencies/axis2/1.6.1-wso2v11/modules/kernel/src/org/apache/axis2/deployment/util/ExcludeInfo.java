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

package org.apache.axis2.deployment.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * this class is used to keep the exclude property details of all the classes.
 * A map object is used to map the class name to an beanInfo object. here the class name is
 * acctually a regular expression
 *
 */
public class ExcludeInfo {

    private Map classNameToBeanInfoMap;

    public ExcludeInfo() {
        this.classNameToBeanInfoMap = new HashMap();
    }

    public void putBeanInfo(String classNameRegx,BeanExcludeInfo beanExcludeInfo){
        this.classNameToBeanInfoMap.put(classNameRegx,beanExcludeInfo);
    }

    public BeanExcludeInfo getBeanExcludeInfoForClass(String className){
        // here map keys represents the regualr expressions for class names
        // so we have to iterate through the keys and find the beanInfo object
        String classNameRegx = null;
        BeanExcludeInfo beanExcludeInfo = null;
        for (Iterator iter = this.classNameToBeanInfoMap.keySet().iterator(); iter.hasNext();){
             classNameRegx = (String) iter.next();
             if (className.matches(classNameRegx)){
                 beanExcludeInfo = (BeanExcludeInfo) this.classNameToBeanInfoMap.get(classNameRegx);
                 // this means we have find the required object
                 break;
             }
        }
        return beanExcludeInfo;
    }

}
