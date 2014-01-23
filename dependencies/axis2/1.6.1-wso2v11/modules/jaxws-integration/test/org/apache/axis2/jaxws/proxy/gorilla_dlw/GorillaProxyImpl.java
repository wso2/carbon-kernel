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

package org.apache.axis2.jaxws.proxy.gorilla_dlw;

import org.apache.axis2.jaxws.proxy.gorilla_dlw.data.Fruit;
import org.apache.axis2.jaxws.proxy.gorilla_dlw.sei.AssertFault;
import org.apache.axis2.jaxws.proxy.gorilla_dlw.sei.GorillaInterface;

import javax.jws.WebService;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import java.util.List;

/**
 * Tests more complicated Document/Literal Wrapped scenarios
 *
 */
@WebService(
		targetNamespace = "http://org/apache/axis2/jaxws/proxy/gorilla_dlw",
		serviceName="GorillaService",
        endpointInterface="org.apache.axis2.jaxws.proxy.gorilla_dlw.sei.GorillaInterface")
public class GorillaProxyImpl implements GorillaInterface {

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.proxy.gorilla_dlw.sei.GorillaInterface#echoString(java.lang.String)
     */
    public String echoString(String data) throws AssertFault {
        return data;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.proxy.gorilla_dlw.sei.GorillaInterface#echoString2(java.lang.String, javax.xml.ws.Holder)
     */
    public void echoString2(String data, Holder<String> inout)
            throws AssertFault {
        // Combine the strings together.
        // The strings may be null on input
        String result = null;
        if (data != null && inout.value != null) {
            result = data + inout.value;
        } else  {
            result = (data != null) ? data : inout.value;
        }
        inout.value = result;

    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.proxy.gorilla_dlw.sei.GorillaInterface#echoInt(java.lang.Integer)
     */
    public Integer echoInt(Integer data) throws AssertFault {
       return data;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.proxy.gorilla_dlw.sei.GorillaInterface#echoAnyType(java.lang.Object)
     */
    public Object echoAnyType(Object data) throws AssertFault {
        return data;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.proxy.gorilla_dlw.sei.GorillaInterface#echoStringList(java.util.List)
     */
    public List<String> echoStringList(List<String> data)
            throws AssertFault {
        return data;
    }
    
    public String[] echoStringListAlt(String[] data) throws AssertFault {
       return data;
    }

    public List<List<String>> echoStringListArray(List<List<String>> data) throws AssertFault {
        return data;
    }
    
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.proxy.gorilla_dlw.sei.GorillaInterface#echoStringArray(java.util.List)
     */
    public List<String> echoStringArray(List<String> data) throws AssertFault {
        return data;
    }
    
    public String[] echoStringArrayAlt(String[] data) throws AssertFault {
        return data;
    }

    public List<String> echoIndexedStringArray(List<String> data) throws AssertFault {
        return data;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.proxy.gorilla_dlw.sei.GorillaInterface#echoString2Array(java.util.List, javax.xml.ws.Holder)
     */
    public void echoString2Array(List<String> data, Holder<List<String>> inout)
            throws AssertFault {
        inout.value.addAll(data);
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.proxy.gorilla_dlw.sei.GorillaInterface#echoIntArray(java.util.List)
     */
    public List<Integer> echoIntArray(List<Integer> data) throws AssertFault {
        return data;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.proxy.gorilla_dlw.sei.GorillaInterface#echoAnyTypeArray(java.util.List)
     */
    public List<Object> echoAnyTypeArray(List<Object> data) throws AssertFault {
        return data;
    }

    public Fruit echoEnum(Fruit data) throws AssertFault {
        return data;
    }

    public List<Fruit> echoEnumArray(List<Fruit> data) throws AssertFault {
        return data;
    }

	public XMLGregorianCalendar echoDate(XMLGregorianCalendar requestedTerminationTime, Duration requestedLifetimeDuration) {

		return requestedTerminationTime;
	}


	public void echoPolymorphicDate(XMLGregorianCalendar request) { 
		//Test to make sure polymorpic cases can marshal on client and server.
		if(request==null){
			
		}
	}
	
	/**
     * The following non-doc method is not invoked.  It is only present to test the 
     * generic reflection code. 
     */
	public List<org.test.stock1.GetPrice> sampleMethod() { return null;}
    
}
