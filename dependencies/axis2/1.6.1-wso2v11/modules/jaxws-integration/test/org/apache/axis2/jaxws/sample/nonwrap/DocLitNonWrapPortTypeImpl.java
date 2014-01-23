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
package org.apache.axis2.jaxws.sample.nonwrap;

import org.apache.axis2.jaxws.sample.nonwrap.sei.DocLitNonWrapPortType;
import org.test.sample.nonwrap.ObjectFactory;
import org.test.sample.nonwrap.OneWay;
import org.test.sample.nonwrap.OneWayVoid;
import org.test.sample.nonwrap.ReturnType;
import org.test.sample.nonwrap.TwoWay;
import org.test.sample.nonwrap.TwoWayHolder;

import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.Response;
import java.util.concurrent.Future;

@WebService(serviceName="DocLitNonWrapService",
			endpointInterface="org.apache.axis2.jaxws.sample.nonwrap.sei.DocLitNonWrapPortType")
public class DocLitNonWrapPortTypeImpl implements DocLitNonWrapPortType {

	/**
	 * 
	 */
	public DocLitNonWrapPortTypeImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.nonwrap.sei.DocLitNonWrapPortType#oneWayVoid(org.test.sample.nonwrap.OneWayVoid)
	 */
	public void oneWayVoid(OneWayVoid allByMyself) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.nonwrap.sei.DocLitNonWrapPortType#oneWay(org.test.sample.nonwrap.OneWay)
	 */
	public void oneWay(OneWay allByMyself) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.nonwrap.sei.DocLitNonWrapPortType#twoWayHolderAsync(org.test.sample.nonwrap.TwoWayHolder)
	 */
	public Response<TwoWayHolder> twoWayHolderAsync(TwoWayHolder allByMyself) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.nonwrap.sei.DocLitNonWrapPortType#twoWayHolderAsync(org.test.sample.nonwrap.TwoWayHolder, javax.xml.ws.AsyncHandler)
	 */
	public Future<?> twoWayHolderAsync(TwoWayHolder allByMyself,
			AsyncHandler<TwoWayHolder> asyncHandler) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.nonwrap.sei.DocLitNonWrapPortType#twoWayHolder(javax.xml.ws.Holder)
	 */
	public void twoWayHolder(Holder<TwoWayHolder> allByMyself) {
		//TODO Auto-generated method stub
		TwoWayHolder twh = allByMyself.value;
		twh.setTwoWayHolderInt(10);
		twh.setTwoWayHolderStr("Response String");

	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.nonwrap.sei.DocLitNonWrapPortType#twoWayAsync(org.test.sample.nonwrap.TwoWay)
	 */
	public Response<ReturnType> twoWayAsync(TwoWay allByMyself) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.nonwrap.sei.DocLitNonWrapPortType#twoWayAsync(org.test.sample.nonwrap.TwoWay, javax.xml.ws.AsyncHandler)
	 */
	public Future<?> twoWayAsync(TwoWay allByMyself,
			AsyncHandler<ReturnType> asyncHandler) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.nonwrap.sei.DocLitNonWrapPortType#twoWay(org.test.sample.nonwrap.TwoWay)
	 */
	public ReturnType twoWay(TwoWay twoWay) {
		
		String requestString = twoWay.getTwowayStr();
		ObjectFactory of = new ObjectFactory();
		ReturnType rt = of.createReturnType();
		rt.setReturnStr("Acknowledgement : Request String received = "+ requestString);
		
		return rt;
	}

}
