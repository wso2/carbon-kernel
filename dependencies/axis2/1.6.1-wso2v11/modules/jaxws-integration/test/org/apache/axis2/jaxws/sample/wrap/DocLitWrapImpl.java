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

/**
 * 
 */
package org.apache.axis2.jaxws.sample.wrap;

import org.apache.axis2.datasource.jaxb.JAXBCustomBuilderMonitor;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap;
import org.test.sample.wrap.FinancialOperation;
import org.test.sample.wrap.Header;
import org.test.sample.wrap.HeaderPart0;
import org.test.sample.wrap.HeaderPart1;
import org.test.sample.wrap.HeaderResponse;

import javax.jws.WebService;
import javax.xml.ws.Holder;

@WebService(serviceName="DocLitWrapService",
			endpointInterface="org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap")
public class DocLitWrapImpl implements DocLitWrap {

	public FinancialOperation finOp(FinancialOperation op) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 */
	public DocLitWrapImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap#oneWayVoid()
	 */
	public void oneWayVoid() {
        TestLogger.logger.debug("OneWayVoid with no parameters called");

	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap#oneWay(java.lang.String)
	 */
	public void oneWay(String onewayStr) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap#twoWayHolder(javax.xml.ws.Holder, javax.xml.ws.Holder)
	 */
	public void twoWayHolder(Holder<String> twoWayHolderStr,
			Holder<Integer> twoWayHolderInt) {

		twoWayHolderInt.value = 10;
		twoWayHolderStr.value = "Response String";

	}

        /* (non-Javadoc)
         * @see org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap#twoWay(java.lang.String)
         */
        public String twoWay(String twowayStr) {
            if (twowayStr.equals("JAXBCustomBuilderMonitorStart")) {
                // Clear the monitor and start monitoring
                JAXBCustomBuilderMonitor.clear();
                JAXBCustomBuilderMonitor.setMonitoring(true);
                return "JAXBCustomBuilderMonitorStart";
            } else if (twowayStr.equals("JAXBCustomBuilderMonitorEnd")) {
                // End monitoring.
                JAXBCustomBuilderMonitor.setMonitoring(false);
                return "JAXBCustomBuilderMonitorEnd";
            } else if (twowayStr.equals("JAXBCustomBuilderClient")) {
                // Clear monitor so that changes on the client can be detected
                JAXBCustomBuilderMonitor.clear();
                String retStr = twowayStr;
                return retStr;
            } else if (twowayStr.equals("JAXBCustomBuilderServer1")) {
                // Return the number of builders from the monitor
                String retStr = ""  + JAXBCustomBuilderMonitor.getTotalBuilders();
                return retStr;
            } else if (twowayStr.equals("JAXBCustomBuilderServer2")) {
                // Return the number of creates from the monitor
                String retStr = ""  + JAXBCustomBuilderMonitor.getTotalCreates();
                return retStr;
            } else if (twowayStr.equals("JAXBCustomBuilderFault")) {
                // Clear the monitor so that chagnes on the client can be detected
                JAXBCustomBuilderMonitor.clear();
                // An exception is expected for this input
                throw new RuntimeException("System Fault Occurred");
            } else {
                String retStr = new String("Acknowledgement : Request String received = "+ twowayStr);
                return retStr;
            }
        }

        /* (non-Javadoc)
         * @see org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap#invoke(java.lang.String)
         */
        public String invoke(String invokeStr) {
            return invokeStr;
        }

        public HeaderResponse header(Header payload, Holder<HeaderPart0> header0, HeaderPart1 header1){

            HeaderPart0 hpo= (HeaderPart0)header0.value;
            hpo = new HeaderPart0();
            hpo.setHeaderType("Header Type from Endpoint implementation");
            header0.value = hpo;
            //hpo.setHeaderType("");
            HeaderResponse response = new HeaderResponse();
            response.setOut(1000);
            return response;
        }

        public String echoStringWSGEN1(String headerValue) {
            return headerValue;
        }

        public String echoStringWSGEN2(String data) {
            return data;
        }

}
