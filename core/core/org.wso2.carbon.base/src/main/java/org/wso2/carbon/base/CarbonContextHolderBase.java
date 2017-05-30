/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class contains the singleton static references that hold the base of the
 * Carbon Context Holder. This class is supposed to maintain a single static
 * instance spanning across multiple class-loaders. The OSGi servlet bridge will
 * expose this class into the OSGi world.
 */
@SuppressWarnings("unused")
public final class CarbonContextHolderBase {

	private int tenantId = MultitenantConstants.INVALID_TENANT_ID;
    private static final int CARBON_AUTHENTICATION_UTIL_INDEX = 5;
    private static final String CARBON_AUTHENTICATION_UTIL_CLASS = "org.wso2.carbon.core.services.util.CarbonAuthenticationUtil";
    private static final int CARBON_AUTHENTICATION_HANDLER_INDEX = 5;
    private static final String CARBON_AUTHENTICATION_HANDLER_CLASS = "org.wso2.carbon.server.admin.module.handler.AuthenticationHandler";
    
    private String username;
	private String tenantDomain;
	private Map<String, Object> properties;

	private static List<UnloadTenantTask> unloadTenantTasks = null;

	private static final Log log = LogFactory
			.getLog(CarbonContextHolderBase.class);

	/**
	 * The reference to the ws discovery service provider of the current tenant,
	 * as visible to a user.
	 */
	private static final AtomicReference<DiscoveryService> discoveryServiceProvider = new AtomicReference<DiscoveryService>();

	// stores the current CarbonContext local to the running thread.
	private static ThreadLocal<CarbonContextHolderBase> currentContextHolderBase = new ThreadLocal<CarbonContextHolderBase>() {
		protected CarbonContextHolderBase initialValue() {
			return new CarbonContextHolderBase();
		}
	};

	// stores references to the existing CarbonContexts when starting tenant
	// flows. These
	// references will be popped back, when a tenant flow is ended.
	private static ThreadLocal<Stack<CarbonContextHolderBase>> parentContextHolderBaseStack = new ThreadLocal<Stack<CarbonContextHolderBase>>() {
		protected Stack<CarbonContextHolderBase> initialValue() {
			return new Stack<CarbonContextHolderBase>();
		}
	};

	static {
		unloadTenantTasks = new LinkedList<UnloadTenantTask>();
		registerUnloadTenantTask(new CarbonContextCleanupTask());
	}

	/**
	 * Method to obtain an instance to the Discovery Service.
	 * 
	 * @return instance of the Discovery Service
	 */
	public static DiscoveryService getDiscoveryServiceProvider() {
		return discoveryServiceProvider.get();
	}

	/**
	 * Method to define the instance of the Discovery Service.
	 * 
	 * @param discoveryServiceProvider
	 *            the Discovery Service instance.
	 */
	public static void setDiscoveryServiceProvider(
			DiscoveryService discoveryServiceProvider) {
		CarbonContextHolderBase.discoveryServiceProvider
				.set(discoveryServiceProvider);
	}

	/**
	 * Method to obtain the current carbon context holder's base.
	 * 
	 * @return the current carbon context holder's base.
	 */
	public static CarbonContextHolderBase getCurrentCarbonContextHolderBase() {
		return currentContextHolderBase.get();
	}

	/**
	 * Method to register a task that will be executed when a tenant is
	 * unloaded.
	 * 
	 * @param unloadTenantTask
	 *            the task to run.
	 * 
	 * @see UnloadTenantTask
	 */
	public static synchronized void registerUnloadTenantTask(
			UnloadTenantTask unloadTenantTask) {
		if (log.isDebugEnabled()) {
			log.debug("Unload Tenant Task: "
					+ unloadTenantTask.getClass().getName() + " was "
					+ "registered.");
		}
		unloadTenantTasks.add(unloadTenantTask);
	}

	/**
	 * Method that will be called when a tenant is unloaded. This will run all
	 * the corresponding tasks that have been registered to be called when a
	 * tenant is unloaded.
	 * 
	 * @param tenantId
	 *            the tenant's identifier.
	 */
	public static void unloadTenant(int tenantId) {
		log.debug("Started unloading tenant");
		for (UnloadTenantTask unloadTenantTask : unloadTenantTasks) {
			unloadTenantTask.cleanup(tenantId);
		}
		log.info("Completed unloading tenant");
	}

	/**
	 * Starts a tenant flow. This will stack the current CarbonContext and begin
	 * a new nested flow which can have an entirely different context. This is
	 * ideal for scenarios where multiple super-tenant and sub-tenant phases are
	 * required within as a single block of execution.
	 */
	public void startTenantFlow() {
		log.trace("Starting tenant flow.");
		parentContextHolderBaseStack.get().push(
				new CarbonContextHolderBase(this));
		this.restore(null);
	}

	/**
	 * This will end the tenant flow and restore the previous CarbonContext.
	 */
	public void endTenantFlow() {
		log.trace("Stopping tenant flow.");
		this.restore(parentContextHolderBaseStack.get().pop());
	}

	/**
	 * Default constructor to disallow creation of the CarbonContext.
	 */
	private CarbonContextHolderBase() {
		this.tenantId = MultitenantConstants.INVALID_TENANT_ID;
		this.username = null;
		this.tenantDomain = null;
		this.properties = new HashMap<String, Object>();
	}

	/**
	 * Constructor that can be used to create clones.
	 * 
	 * @param carbonContextHolder
	 *            the CarbonContext holder instance of which the clone will be
	 *            created from.
	 */
	public CarbonContextHolderBase(CarbonContextHolderBase carbonContextHolder) {
		this.tenantId = carbonContextHolder.tenantId;
		this.username = carbonContextHolder.username;
		this.tenantDomain = carbonContextHolder.tenantDomain;
		this.properties = new HashMap<String, Object>(
				carbonContextHolder.properties);
	}

	/**
	 * Method to obtain the tenant id on this CarbonContext instance.
	 * 
	 * @return the tenant id.
	 */
	public int getTenantId() {
		return tenantId;
	}

	/**
	 * Method to set the tenant id on this CarbonContext instance.
	 * 
	 * @param tenantId
	 *            the tenant id.
	 */
	public void setTenantId(int tenantId) {
	    try {
            if (this.tenantId == MultitenantConstants.INVALID_TENANT_ID  ||
                    this.tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                this.tenantId = tenantId;
            } else if (this.tenantId != tenantId) {
                StackTraceElement[] traces = Thread.currentThread().getStackTrace();
                if (!isAllowedToChangeTenantDomain(traces)) {
                    throw new IllegalStateException("Trying to set the domain from " + this.tenantId + " to " + tenantId);
                }
            }
        } catch (IllegalStateException e) {
            log.error(e.getMessage(), e);
        }
	}

	/**
	 * Method to obtain the username on this CarbonContext instance.
	 * 
	 * @return the username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Method to set the username on this CarbonContext instance.
	 * 
	 * @param username
	 *            the username.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Method to obtain the tenant domain on this CarbonContext instance.
	 * 
	 * @return the tenant domain.
	 */
	public String getTenantDomain() {
		return tenantDomain;
	}

	/**
	 * Method to set the tenant domain on this CarbonContext instance.
	 * 
	 * @param tenantDomain
	 *            the tenant domain.
	 */
	public void setTenantDomain(String domain) {
		try {
		    if (this.tenantDomain == null ||
                    this.tenantDomain == MultitenantConstants.SUPER_TENANT_DOMAIN_NAME ) {
		        this.tenantDomain = domain;
            } else if (!tenantDomain.equals(domain)) {
		        StackTraceElement[] traces = Thread.currentThread().getStackTrace();
                if (!isAllowedToChangeTenantDomain(traces)) {
		            throw new IllegalStateException("Trying to set the domain from " + this.tenantDomain + " to " + domain);
		        }
		    }
        } catch (IllegalStateException e) {
            log.error(e.getMessage(), e);
        }
	}

	/**
	 * Method to obtain a property on this CarbonContext instance.
	 * 
	 * @param name
	 *            the property name.
	 * 
	 * @return the value of the property by the given name.
	 */
	public Object getProperty(String name) {
		return properties.get(name);
	}

	/**
	 * Method to set a property on this CarbonContext instance.
	 * 
	 * @param name
	 *            the property name.
	 * @param value
	 *            the value to be set to the property by the given name.
	 */
	public void setProperty(String name, Object value) {
		log.trace("Setting Property: " + name);
		properties.put(name, value);
	}

	// Method to cleanup all properties.
	private void cleanupProperties() {
		// This method would be called to reclaim memory. Therefore, this might
		// be called on an
		// object which has been partially garbage collected. Even unlikely, it
		// might be possible
		// that the object exists without any field-references, until all
		// WeakReferences are
		// cleaned-up.
		if (properties != null) {
			log.trace("Cleaning up properties.");
			properties.clear();
		}
	}
	
	private boolean isAllowedToChangeTenantDomain(StackTraceElement[] traces) {
	    boolean allowChange = false;
	    if ((traces.length > CARBON_AUTHENTICATION_UTIL_INDEX) &&
                (traces[CARBON_AUTHENTICATION_UTIL_INDEX].getClassName().equals(CARBON_AUTHENTICATION_UTIL_CLASS))) {
	        allowChange = true;
	    } else if ((traces.length > CARBON_AUTHENTICATION_HANDLER_INDEX) &&
                traces[CARBON_AUTHENTICATION_HANDLER_INDEX].getClassName().equals(CARBON_AUTHENTICATION_HANDLER_CLASS)) {
	        allowChange = true;
	    }
	    return allowChange;
	}

	/**
	 * This method will destroy the current CarbonContext holder.
	 */
	public static void destroyCurrentCarbonContextHolder() {
		currentContextHolderBase.remove();
		parentContextHolderBaseStack.remove();
	}

	// Utility method to restore a CarbonContext.
	private void restore(CarbonContextHolderBase carbonContextHolder) {
		if (carbonContextHolder != null) {
			this.tenantId = carbonContextHolder.tenantId;
			this.username = carbonContextHolder.username;
			this.tenantDomain = carbonContextHolder.tenantDomain;
			this.properties = new HashMap<String, Object>(
					carbonContextHolder.properties);
		} else {
			this.tenantId = MultitenantConstants.INVALID_TENANT_ID;
			this.username = null;
			this.tenantDomain = null;
			this.properties = new HashMap<String, Object>();
		}
	}

	private static class CarbonContextCleanupTask implements
			UnloadTenantTask<CarbonContextHolderBase> {

		private Map<Integer, ArrayList<WeakReference<CarbonContextHolderBase>>> contextHolderList = new ConcurrentHashMap<Integer, ArrayList<WeakReference<CarbonContextHolderBase>>>();

		public void register(int tenantId,
				CarbonContextHolderBase contextHolderBase) {
			ArrayList<WeakReference<CarbonContextHolderBase>> list = contextHolderList
					.get(tenantId);
			if (list == null) {
				list = new ArrayList<WeakReference<CarbonContextHolderBase>>();
				list.add(new WeakReference<CarbonContextHolderBase>(
						contextHolderBase));
				contextHolderList.put(tenantId, list);
			} else {
				list.add(new WeakReference<CarbonContextHolderBase>(
						contextHolderBase));
			}
		}

		public void cleanup(int tenantId) {
			ArrayList<WeakReference<CarbonContextHolderBase>> list = contextHolderList
					.remove(tenantId);
			if (list != null) {
				for (WeakReference<CarbonContextHolderBase> carbonContextHolderBaseRef : list) {
					CarbonContextHolderBase carbonContextHolderBase = carbonContextHolderBaseRef
							.get();
					if (carbonContextHolderBase != null) {
						carbonContextHolderBase.cleanupProperties();
					}
				}
				list.clear();
			}
		}
	}

}
