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

package org.apache.axis2.engine;

import java.lang.reflect.Modifier;

import org.apache.axis2.AxisFault;

public class DefaultObjectSupplier implements ObjectSupplier {

	/* (non-Javadoc)
	 * @see org.apache.axis2.engine.ObjectSupplier#getObject(java.lang.Class)
	 */
	public Object getObject(Class clazz) throws AxisFault {
		try {
			Class parent = clazz.getDeclaringClass();
			Object instance = null;

			if (parent != null && !Modifier.isStatic(clazz.getModifiers())) {
				// if this is an inner class then that can be a non static inner class. 
				// those classes have to be instantiated in a different way than a normal initialization.
				instance = clazz.getConstructor(new Class[] { parent })
						.newInstance(new Object[] { getObject(parent) });
			} else {
				instance = clazz.newInstance();
			}

			return instance;
		} catch (Exception e) {
			throw AxisFault.makeFault(e);
		}
	}
}
