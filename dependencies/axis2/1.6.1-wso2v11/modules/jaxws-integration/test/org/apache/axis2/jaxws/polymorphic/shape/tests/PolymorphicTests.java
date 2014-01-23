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

package org.apache.axis2.jaxws.polymorphic.shape.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.polymorphic.shape.sei.PolymorphicShapePortType;
import org.apache.axis2.jaxws.polymorphic.shape.sei.PolymorphicShapeService;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.axis2.jaxws.util.WSDLWrapper;
import org.apache.axis2.jaxws.wsdl.SchemaReaderException;
import org.apache.axis2.jaxws.wsdl.impl.SchemaReaderImpl;
import org.test.shape.Shape;
import org.test.shape.Square;
import org.test.shape.threed.ThreeDSquare;

import javax.wsdl.WSDLException;
import javax.xml.ws.BindingProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Set;

public class PolymorphicTests extends AbstractTestCase {
    String axisEndpoint = "http://localhost:6060/axis2/services/PolymorphicShapeService.PolymorphicShapePort";
	
    public static Test suite() {
        return getTestSetup(new TestSuite(PolymorphicTests.class));
    }

	public void testFormalAndActualTypeInDifferentPackages(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		PolymorphicShapeService service = new PolymorphicShapeService();
		PolymorphicShapePortType port = service.getPolymorphicShapePort();
        BindingProvider p = (BindingProvider) port;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);

		Shape shapeType;

        TestLogger.logger.debug("Sending Request to draw Square");
		Square shape = new Square();
		shape.setXAxis(1);
		shape.setYAxis(1);
		shape.setLength(10);
		shapeType = port.draw(shape);
		assertTrue(shapeType instanceof Square);
        TestLogger.logger.debug("Square was drawn");

        TestLogger.logger.debug("Sending Request to draw 3D Square");
		ThreeDSquare threeDshape = new ThreeDSquare();
		threeDshape.setXAxis(1);
		threeDshape.setYAxis(1);
		threeDshape.setLength(10);
		threeDshape.setWidth(10);
		threeDshape.setBredth(10);
		shapeType = port.draw3D(threeDshape);
		assertTrue(shapeType instanceof ThreeDSquare);
        TestLogger.logger.debug("3D Square was drawn");
        TestLogger.logger.debug("-------------------------------");
	}
	
	public void testInlineUseOfJAXBBinding(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		String schemaBindingPkgName = "org.test.echomessage";
		String standardPkgName= "org.test.complextype.nonanonymous";
		String wsdlLocation="test-resources/wsdl/JAXB_Customization_Sample.wsdl";
		URL url = null;
		try{
			try{
				String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
				wsdlLocation = new File(baseDir +File.separator+ wsdlLocation).getAbsolutePath();
			}catch(Exception e){
				e.printStackTrace();
				fail();
			}
			File file = new File(wsdlLocation);
			url = file.toURL();
			WSDLWrapper wsdlWrapper = new WSDL4JWrapper(url);
			org.apache.axis2.jaxws.wsdl.SchemaReader sr= new SchemaReaderImpl();
			Set<String> set= sr.readPackagesFromSchema(wsdlWrapper.getDefinition());
			assertNotNull(set);
			Iterator<String> iter = set.iterator();
			while(iter.hasNext()){
				String pkg = iter.next();
                TestLogger.logger.debug("Package = " + pkg);
			}
            TestLogger.logger.debug("------------------------------");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail();
		}catch(FileNotFoundException e) {
			e.printStackTrace();
			fail();
		}catch(UnknownHostException e) {
			e.printStackTrace();
			fail();
		}catch(ConnectException e) {
			e.printStackTrace();
			fail();
		}catch(IOException e) {
		    e.printStackTrace();
            fail();
        }catch(WSDLException e){
			e.printStackTrace();
			fail();
		}catch(SchemaReaderException e){
			e.printStackTrace();
			fail();
		}
	}
	
	public void testSchemaReader(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		String wsdlLocation="test-resources/wsdl/shapes.wsdl";
		URL url = null;
		try{
			try{
				String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
				wsdlLocation = new File(baseDir +File.separator+ wsdlLocation).getAbsolutePath();
			}catch(Exception e){
				e.printStackTrace();
				fail();
			}
			File file = new File(wsdlLocation);
			url = file.toURL();
			WSDLWrapper wsdlWrapper = new WSDL4JWrapper(url);
			org.apache.axis2.jaxws.wsdl.SchemaReader sr= new SchemaReaderImpl();
			Set<String> set= sr.readPackagesFromSchema(wsdlWrapper.getDefinition());
			assertNotNull(set);
			Iterator<String> iter = set.iterator();
			while(iter.hasNext()){
                TestLogger.logger.debug("Package =" + iter.next());
			}
            TestLogger.logger.debug("------------------------------");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail();
		}catch(FileNotFoundException e) {
			e.printStackTrace();
			fail();
		}catch(UnknownHostException e) {
			e.printStackTrace();
			fail();
		}catch(ConnectException e) {
			e.printStackTrace();
			fail();
        }catch(IOException e) {
            e.printStackTrace();
            fail();
		}catch(WSDLException e){
			e.printStackTrace();
			fail();
		}catch(SchemaReaderException e){
			e.printStackTrace();
			fail();
		}
		
		
		        
	}
}
