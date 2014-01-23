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

package org.apache.axis2.jaxws.jaxb.wrapper;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

public class WrapperToolTest extends TestCase {
    public void testWrapStockQuote(){
        try{
            JAXBWrapperTool wrapper = new JAXBWrapperToolImpl();

            String jaxbClassName = "org.test.stock2.GetPrice";
            Class jaxbClass;
            try {
                jaxbClass = Class.forName(jaxbClassName, false, ClassLoader.getSystemClassLoader());
            } catch (Exception e){
                jaxbClass = Class.forName(jaxbClassName, false, this.getClass().getClassLoader());
            }
            ArrayList<String> childNames = new ArrayList<String>();
            String childName = "symbol";
            childNames.add(childName);
            String symbolObj = new String("IBM");
            Map<String, Object> childObjects= new WeakHashMap<String, Object>();
            childObjects.put(childName, symbolObj);
            Object jaxbObject = wrapper.wrap(jaxbClass, childNames, childObjects);
            org.test.stock2.GetPrice getPrice = (org.test.stock2.GetPrice)jaxbObject;

        }catch(JAXBWrapperException e){
            e.printStackTrace();
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public void testUnwrapStockQuote(){
        try{
            JAXBWrapperTool wrapper = new JAXBWrapperToolImpl();
            org.test.stock2.GetPrice price = new org.test.stock2.GetPrice();
            price.setSymbol("IBM");

            ArrayList<String> childNames = new ArrayList<String>();
            String childName = "symbol";
            childNames.add(childName);

            Object[] jaxbObjects = wrapper.unWrap(price, childNames);

        }catch(JAXBWrapperException e){
            e.printStackTrace();
        }
    }

    public void testWrapMFQuote(){
        try{
            JAXBWrapperTool wrapper = new JAXBWrapperToolImpl();

            String jaxbClassName = "org.test.stock1.GetPrice";
            Class jaxbClass;
            try {
                jaxbClass = Class.forName(jaxbClassName, false, ClassLoader.getSystemClassLoader());
            } catch (Exception e){
                jaxbClass = Class.forName(jaxbClassName, false, this.getClass().getClassLoader());
            }
            ArrayList<String> childNames = new ArrayList<String>();
            String fund ="fund";
            String fundName = new String("PRGFX");
            String holding = "holdings.";
            String topHolding = new String("GE");
            String nav ="nav";
            String navInMillion = new String("700");

            childNames.add(fund);
            childNames.add(holding);
            childNames.add(nav);

            Map<String, Object> childObjects= new WeakHashMap<String, Object>();

            childObjects.put(fund, fundName);
            childObjects.put(holding, topHolding);
            childObjects.put(nav, navInMillion);

            Object jaxbObject = wrapper.wrap(jaxbClass, childNames, childObjects);
            org.test.stock1.GetPrice getPrice = (org.test.stock1.GetPrice)jaxbObject;

        }catch(JAXBWrapperException e){
            e.printStackTrace();
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public void testUnwrapMFQuote(){
        try{
            JAXBWrapperTool wrapper = new JAXBWrapperToolImpl();
            org.test.stock1.GetPrice price = new org.test.stock1.GetPrice();
            price.setFund("PRGFX");
            price.setHoldings("GE");
            price.setNav("700");

            ArrayList<String> childNames = new ArrayList<String>();
            String fund ="fund";
            childNames.add(fund);
            String holding = "holdings.";
            childNames.add(holding);
            String nav ="nav";
            childNames.add(nav);

            Object[] jaxbObjects = wrapper.unWrap(price, childNames);
        }catch(JAXBWrapperException e){
            e.printStackTrace();
        }
    }
}
