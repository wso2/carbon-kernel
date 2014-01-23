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
package org.apache.axiom.osgi;

import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Internal component required for OSGi support. This component is
 * responsible for injecting the {@link OMMetaFactory} instance loaded
 * from the implementation bundle into {@link OMAbstractFactory} (using
 * {@link OMAbstractFactory#setMetaFactory(OMMetaFactory)}). This class
 * is for internal use only and MUST NOT be used for other purposes.
 * 
 * @scr.component name="factoryinjection.component" immediate="true"
 * @scr.reference name="metafactory" interface="org.apache.axiom.om.OMMetaFactory" cardinality="0..n" policy="dynamic" bind="setMetaFactory" unbind="unsetMetaFactory"
 */
public class FactoryInjectionComponent {

	private static final Log log = LogFactory
			.getLog(FactoryInjectionComponent.class);

	public FactoryInjectionComponent() {
		if (log.isDebugEnabled()) {
			log.debug("FactoryInjectionComponent created");
		}
	}

	private static List metaFactories = null;
	
	protected void setMetaFactory(OMMetaFactory metafactory) {
		synchronized (FactoryInjectionComponent.class) {
			if (metaFactories == null) {
			    metaFactories = new ArrayList();
			}
			// Special case llom - it's the default
			if (metafactory.getClass().toString().indexOf("llom") != -1) {
				metaFactories.add(0, metafactory);
			} else {
				metaFactories.add(metafactory);
			}
			OMAbstractFactory.setMetaFactory((OMMetaFactory) metaFactories.get(0));
		}
	}

	protected void unsetMetaFactory(OMMetaFactory metafactory) {
		synchronized (FactoryInjectionComponent.class) {
			if (metaFactories != null) {
			    metaFactories.remove(metafactory);
			}
			if (metaFactories.size() == 0) {
			    metaFactories = null;
			} else {
			    OMAbstractFactory.setMetaFactory((OMMetaFactory) metaFactories.get(0));
			}
		}
	}
}
