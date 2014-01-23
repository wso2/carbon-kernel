/*
 *  Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.base;

import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CarbonApplicationContextHolderBase {

	private String applicationName;

	private static final Log log = LogFactory.getLog(CarbonApplicationContextHolderBase.class);

	private CarbonApplicationContextHolderBase() {
		this.applicationName = "";
	}

	// stores the current AppContext local to the running thread.
	private static ThreadLocal<CarbonApplicationContextHolderBase> currentAppContextHolderBase = new ThreadLocal<CarbonApplicationContextHolderBase>() {
		protected CarbonApplicationContextHolderBase initialValue() {
			return new CarbonApplicationContextHolderBase();
		}
	};

	// stores references to the existing CarbonAppContexts when starting app
	// flows. These
	// references will be popped back, when a app flow is ended.
	private static ThreadLocal<Stack<CarbonApplicationContextHolderBase>> parentAppContextHolderBaseStack = new ThreadLocal<Stack<CarbonApplicationContextHolderBase>>() {
		protected Stack<CarbonApplicationContextHolderBase> initialValue() {
			return new Stack<CarbonApplicationContextHolderBase>();
		}
	};

	/**
	 * Method to obtain the current carbon context holder's base.
	 * 
	 * @return the current carbon context holder's base.
	 */
	public static CarbonApplicationContextHolderBase getCurrentCarbonAppContextHolderBase() {
		return currentAppContextHolderBase.get();
	}

	public void startApplicationFlow() {
		log.trace("Starting Application flow.");
		parentAppContextHolderBaseStack.get().push(new CarbonApplicationContextHolderBase(this));
		this.restore(null);
	}

	public void endApplicationFlow() {
		log.trace("Stopping Application flow.");
		this.restore(parentAppContextHolderBaseStack.get().pop());
	}

	public CarbonApplicationContextHolderBase(
			CarbonApplicationContextHolderBase carbonAppContextHolder) {
		this.applicationName = carbonAppContextHolder.applicationName;

	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	/**
	 * This method will destroy the current CarbonAppContext holder.
	 */
	public static void destroyCurrentCarbonaAppContextHolder() {
		currentAppContextHolderBase.remove();
		parentAppContextHolderBaseStack.remove();
	}

	// Utility method to restore a CarbonAppContext.
	private void restore(CarbonApplicationContextHolderBase carbonAppContextHolder) {
		if (carbonAppContextHolder != null) {
			this.applicationName = carbonAppContextHolder.applicationName;
		} else {
			this.applicationName = "";
		}
	}

}
