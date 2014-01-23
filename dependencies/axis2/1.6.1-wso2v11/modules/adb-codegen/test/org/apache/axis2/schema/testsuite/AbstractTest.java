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

package org.apache.axis2.schema.testsuite;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.schema.AbstractTestCase;

public class AbstractTest  extends AbstractTestCase {

     protected OMElement getOMElement(){
        OMFactory fac = OMAbstractFactory.getSOAP12Factory();
        OMNamespace omNamespace = fac.createOMNamespace("http://test.ws02.com","ns1");
        OMElement omElement = fac.createOMElement("TestValue", omNamespace);
        omElement.addChild(fac.createOMText("testString"));
        return omElement;
    }

     protected boolean isObjectsEqual(Object object1,Object object2){
        boolean isEqual = false;
        if ((object1 == null) || (object2 == null)){
            isEqual = (object1 == object2);
        } else {
            isEqual = object1.equals(object2);
        }
        return isEqual;
    }

    public void testMethod(){
	    // dummy test method
    }

    protected boolean assertArrayEqual(Object[] objectArray1, Object[] objectArray2) {
        boolean isEqual = false;
        if ((objectArray1 == null) || (objectArray2 == null)) {
            isEqual = (objectArray1 == objectArray2);
        } else {
            // i.e both are not null
            if (objectArray1.length == objectArray2.length) {
                isEqual = true;
                for (int i = 0; i < objectArray1.length; i++) {
                    if (!isObjectContains(objectArray2, objectArray1[i])) {
                        isEqual = false;
                        break;
                    }
                }
            }
        }
        return isEqual;
    }

    protected boolean isObjectContains(Object[] objectArray, Object object) {
        boolean isContain = false;
        for (int i = 0; i < objectArray.length; i++) {
            if ((objectArray[i] == null) || (object == null)) {
                isContain =  (objectArray[i] == object);
            } else {
                if (object instanceof BookInformation){
                    isContain = isBookInformationObjectsEquals((BookInformation)objectArray[i],(BookInformation)object);
                } else if (object instanceof OMElement){
                    isContain = isOMElementsEqual((OMElement)objectArray[i],(OMElement)object);
                } else {
                    isContain = objectArray[i].equals(object);
                }
            }
            if (isContain) {
                break;
            }
        }
        return isContain;
    }

     protected boolean isBookInformationObjectsEquals(BookInformation bookInformation1,
                                                      BookInformation bookInformation2){
         boolean isEqual;
         if ((bookInformation1 == null) || (bookInformation2 == null)){
            isEqual = (bookInformation1 == bookInformation2);
         }  else {
             isEqual = bookInformation1.getType().equals(bookInformation2.getType()) &&
                     bookInformation1.getTitle().equals(bookInformation2.getTitle()) &&
                     bookInformation1.getIsbn().equals(bookInformation2.getIsbn());
         }
         return isEqual;
     }

     protected BookInformation getBookInformation() {
        BookInformation bookInformation = new BookInformation();
        bookInformation.setType("test");
        bookInformation.setTitle("test");
        bookInformation.setIsbn("test");
        return bookInformation;
    }


}
