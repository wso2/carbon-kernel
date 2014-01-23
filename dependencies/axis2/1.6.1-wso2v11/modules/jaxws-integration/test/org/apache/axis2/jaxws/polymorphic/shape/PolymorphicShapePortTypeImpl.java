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

package org.apache.axis2.jaxws.polymorphic.shape;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.polymorphic.shape.sei.PolymorphicShapePortType;
import org.test.shape.Circle;
import org.test.shape.Shape;
import org.test.shape.Square;
import org.test.shape.threed.ThreeDSquare;

import javax.jws.WebService;

@WebService(serviceName="PolymorphicShapeService",
        portName="PolymorphicShapePort",
        targetNamespace="http://sei.shape.polymorphic.jaxws.axis2.apache.org",
        endpointInterface="org.apache.axis2.jaxws.polymorphic.shape.sei.PolymorphicShapePortType", 
		wsdlLocation="test/org/apache/axis2/jaxws/polymorphic/shape/META-INF/shapes.wsdl")
public class PolymorphicShapePortTypeImpl implements PolymorphicShapePortType {

	public Shape draw(Shape request) {
		if(request instanceof Circle){
			Circle circle =(Circle) request;
            TestLogger.logger.debug("Drawing Circle on x =" + request.getXAxis() + " y=" +
                    request.getYAxis() + " With Radius =" + circle.getRadius());
			return request;
		}
		if(request instanceof Square){
			Square square =(Square) request;
            TestLogger.logger.debug("Drawing Square on x =" + request.getXAxis() + " y=" +
                    request.getYAxis() + " With Sides =" + square.getLength());
			return request;
		}
		return null;
	}

	public Shape draw3D(Shape request) {
		if(request instanceof ThreeDSquare){
			ThreeDSquare threeDsquare =(ThreeDSquare) request;
            TestLogger.logger.debug("Drawing 3DSquare on x =" + request.getXAxis() + " y=" +
                    request.getYAxis() + " With Bredth =" + threeDsquare.getBredth());
			return request;
		}
		return null;
	}

}
