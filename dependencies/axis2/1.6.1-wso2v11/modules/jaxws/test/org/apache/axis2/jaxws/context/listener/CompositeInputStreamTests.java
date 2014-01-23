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
package org.apache.axis2.jaxws.context.listener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

public class CompositeInputStreamTests extends TestCase {
	private InputStream is1 = new ByteArrayInputStream("John".getBytes());
	private InputStream is2 = new ByteArrayInputStream("Conner".getBytes());
	
	public void testCreateCompositeStream(){
		//Testing default constructor
		CompositeInputStream cis1 = new CompositeInputStream();
		cis1.append(is1);
		cis1.append(is2);
		try{
			String streamContent = invokeRead(cis1);
			assertTrue(streamContent.equals("JohnConner"));
		}catch(Exception e){
			fail(e.getMessage());
		}
		//Testing constructor with inputstream Object
		resetGlobalInputStream();
		CompositeInputStream cis2 = new CompositeInputStream(is1);
		cis2.append(is2);
		try{
			String streamContent = invokeRead(cis2);
			assertTrue(streamContent.equals("JohnConner"));
		}catch(Exception e){
			fail(e.getMessage());
		}		
		//Null test on onstructor with inputstream Object
		resetGlobalInputStream();
		InputStream nullValue = null;
		cis2 = new CompositeInputStream(nullValue);
		cis2.append(is2);
		try{
			String streamContent = invokeRead(cis2);
			assertTrue(streamContent.equals("Conner"));
		}catch(Exception e){
			fail(e.getMessage());
		}
		
		//Testing Construcot with inputStream Array Object;
		resetGlobalInputStream();
		InputStream[] isArray = new InputStream[]{is1, is2};
		CompositeInputStream cis3 = new CompositeInputStream(isArray);
		try{
			String streamContent = invokeRead(cis3);
			assertTrue(streamContent.equals("JohnConner"));
		}catch(Exception e){
			fail(e.getMessage());
		}
		//Null test on onstructor with inputstream Object
		isArray = new InputStream[]{null, null};
		cis3 = new CompositeInputStream(isArray);
		try{
			String streamContent = invokeRead(cis3);
			assertTrue(streamContent.equals(""));
		}catch(Exception e){
			fail(e.getMessage());
		}
		
	}
	
	public void testRead() throws Exception {		
	    
	    // Read fully
	    resetGlobalInputStream();
	    InputStream[] isArray = new InputStream[]{is1, is2};
	    CompositeInputStream cis3 = new CompositeInputStream(isArray);

	    int avail = cis3.available();
	    assertTrue("Unexpected avail=" + avail, avail == "JohnConner".length());
	    String streamContent = invokeReadArray(cis3, avail);
	    assertTrue(streamContent.equals("JohnConner"));
		
		
		// Read partial...with cross buffer...
        resetGlobalInputStream();
        isArray = new InputStream[]{is1, is2};
        cis3 = new CompositeInputStream(isArray);
        
        streamContent = invokeReadArray(cis3, 1);
        assertTrue(streamContent.equals("J"));
        streamContent = invokeReadArray(cis3, 5);
        assertTrue(streamContent.equals("ohnCo"));
        streamContent = invokeReadArray(cis3, 10);
        assertTrue(streamContent.equals("nner"));
        
		
		//Negative test case
		isArray = new InputStream[]{null, null};
		cis3 = new CompositeInputStream(isArray);

		streamContent = invokeReadArray(cis3, 100);
		assertTrue(streamContent.equals(""));
	
	}
	
	private String invokeRead(CompositeInputStream cis)throws IOException{
		int b;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		while((b = cis.read())!=-1){
			os.write(b);
		}
		byte[] byteArray = os.toByteArray();
		return new String(byteArray);
	}
	
	private String invokeReadArray(CompositeInputStream cis, int len)throws IOException{
		byte[] byteArray = new byte[1024];
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int numBytes = cis.read(byteArray, 0, len);
		if (numBytes < 0) {
		    return "";
		} else {
		    return new String(byteArray, 0, numBytes);
		}
	}
	
	private void resetGlobalInputStream(){
		is1 = new ByteArrayInputStream("John".getBytes());
		is2 = new ByteArrayInputStream("Conner".getBytes());
	}
}
