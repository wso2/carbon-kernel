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
package org.wso2.carbon.registry.core.jdbc.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.TaggedResourcePath;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.Filter;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * This class is used to manage handlers belonging to a particular lifecycle phase. There are three
 * defined phases, <b>default</b>, <b>reporting</b>, and <b>user</b>.
 */
public class HandlerLifecycleManager extends HandlerManager {

    private Dictionary<String, HandlerManager> handlerManagers;

    /**
     * This phase contains the default system-level handlers.
     */
    public static final String DEFAULT_SYSTEM_HANDLER_PHASE = "default";
    
    /**
     * This phase contains the tenant-specific system-level handlers.
     */
    public static final String TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE = "tenant";

    /**
     * This phase contains the tenant-specific system-level handlers.
     */
    public static final String USER_DEFINED_SYSTEM_HANDLER_PHASE = "system";

    /**
     * This phase contains all the reporting handlers (such as WS-Eventing & Notification
     * handlers).
     */
    public static final String DEFAULT_REPORTING_HANDLER_PHASE = "reporting";

    /**
     * This phase contains user-defined handlers.
     */
    public static final String USER_DEFINED_HANDLER_PHASE = "user";

    /**
     * This phase contains the handlers that will get executed after a successful commit.
     */
    public static final String COMMIT_HANDLER_PHASE = "commit";

    /**
     * This phase contains the handlers that will get executed after the rollback of a failed
     * commit.
     */
    public static final String ROLLBACK_HANDLER_PHASE = "rollback";

    private static final Log log = LogFactory.getLog(HandlerLifecycleManager.class);

    /**
     * Creates a Handler Manager for a given handler lifecycle phase.
     */
    public HandlerLifecycleManager() {
        handlerManagers = new Hashtable<String, HandlerManager>();
        HandlerManager defaultHandlerManager = new HandlerManager();
        defaultHandlerManager.setEvaluateAllHandlers(false);
        handlerManagers.put(DEFAULT_SYSTEM_HANDLER_PHASE, defaultHandlerManager);
        HandlerManager defaultTenantSpecificHandlerManager = new UserDefinedHandlerManager();
        defaultTenantSpecificHandlerManager.setEvaluateAllHandlers(false);
        handlerManagers.put(TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE,
                defaultTenantSpecificHandlerManager);
        HandlerManager userDefinedSystemHandlerManager = new HandlerManager();
        userDefinedSystemHandlerManager.setEvaluateAllHandlers(false);
        handlerManagers.put(USER_DEFINED_SYSTEM_HANDLER_PHASE, userDefinedSystemHandlerManager);
        HandlerManager userDefinedHandlerManager = new UserDefinedHandlerManager();
        userDefinedHandlerManager.setEvaluateAllHandlers(false);
        handlerManagers.put(USER_DEFINED_HANDLER_PHASE, userDefinedHandlerManager);
        HandlerManager reportingHandlerManager = new HandlerManager();
        reportingHandlerManager.setEvaluateAllHandlers(true);
        handlerManagers.put(DEFAULT_REPORTING_HANDLER_PHASE, reportingHandlerManager);

        HandlerManager commitHandlerManager = new UserDefinedHandlerManager();
        commitHandlerManager.setEvaluateAllHandlers(true);
        handlerManagers.put(COMMIT_HANDLER_PHASE, commitHandlerManager);
        HandlerManager rollbackHandlerManager = new UserDefinedHandlerManager();
        rollbackHandlerManager.setEvaluateAllHandlers(true);
        handlerManagers.put(ROLLBACK_HANDLER_PHASE, rollbackHandlerManager);

        init(MultitenantConstants.SUPER_TENANT_ID);
    }

    /**
     * An initialization method to be called in a multi-tenant environment.
     *
     * @param tenantId the identifier of the tenant.
     */
    public synchronized void init(int tenantId) {
        CurrentSession.setCallerTenantId(tenantId);
        try {
            UserDefinedHandlerManager userDefinedHandlerManager =
                    (UserDefinedHandlerManager) handlerManagers.get(
                            TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE);
            userDefinedHandlerManager.getUserHandlerManager();
            userDefinedHandlerManager =
                    (UserDefinedHandlerManager) handlerManagers.get(USER_DEFINED_HANDLER_PHASE);
            userDefinedHandlerManager.getUserHandlerManager();
        } finally {
            CurrentSession.removeCallerTenantId();
        }
    }

    /**
     * Method to obtain the handler manager for the specified phase.
     *
     * @param lifecyclePhase The name of the lifecycle phase.
     *
     * @return the handler manager corresponding to the specified phase.
     */
    public HandlerManager getHandlerManagerForPhase(String lifecyclePhase) {
        if (lifecyclePhase.equals(COMMIT_HANDLER_PHASE) ||
                lifecyclePhase.equals(ROLLBACK_HANDLER_PHASE)) {
            return handlerManagers.get(lifecyclePhase);
        } else {
            String msg = "Unable to provide handler manager for internally " +
                    "managed or invalid phase" + lifecyclePhase;
            log.error(msg);
            throw new SecurityException(msg);
        }
    }

    @Override
    public void addHandler(String[] methods, Filter filter, Handler handler) {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).addHandler(methods, filter, handler);
    }

    @Override
    public void addHandlerWithPriority(String[] methods, Filter filter, Handler handler) {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).addHandlerWithPriority(
                methods, filter, handler);
    }

    @Override
    public void removeHandler(Handler handler) {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).removeHandler(handler);
    }

    @Override
    public void removeHandler(String[] methods, Filter filter, Handler handler) {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).removeHandler(methods, filter, handler);
    }

    @Override
    public void addHandler(String[] methods, Filter filter, Handler handler,
                           String lifecyclePhase) {
        if (lifecyclePhase == null) {
            addHandler(methods, filter, handler);
            return;
        }
        HandlerManager hm = handlerManagers.get(lifecyclePhase);
        if (hm == null) {
            log.warn("Invalid handler lifecycle phase: " + lifecyclePhase +
                    ". Adding handler to the default phase.");
            addHandler(methods, filter, handler);
        } else {
            hm.addHandler(methods, filter, handler);
        }
    }

    @Override
    public void addHandlerWithPriority(String[] methods, Filter filter, Handler handler,
                                       String lifecyclePhase) {
        if (lifecyclePhase == null) {
            addHandlerWithPriority(methods, filter, handler);
            return;
        }
        HandlerManager hm = handlerManagers.get(lifecyclePhase);
        if (hm == null) {
            log.warn("Invalid handler lifecycle phase: " + lifecyclePhase +
                    ". Adding handler to the default phase.");
            addHandlerWithPriority(methods, filter, handler);
        } else {
            hm.addHandlerWithPriority(methods, filter, handler, lifecyclePhase);
        }
    }

    @Override
    public void removeHandler(Handler handler, String lifecyclePhase) {
        if (lifecyclePhase == null) {
            removeHandler(handler);
            return;
        }
        HandlerManager hm = handlerManagers.get(lifecyclePhase);
        if (hm == null) {
            log.warn("Invalid handler lifecycle phase: " + lifecyclePhase +
                    ". Removing handler from the default phase.");
            removeHandler(handler);
        } else {
            hm.removeHandler(handler, lifecyclePhase);
        }
    }

    @Override
    public void removeHandler(String[] methods, Filter filter, Handler handler,
                              String lifecyclePhase) {
        if (lifecyclePhase == null) {
            removeHandler(methods, filter, handler);
            return;
        }
        HandlerManager hm = handlerManagers.get(lifecyclePhase);
        if (hm == null) {
            log.warn("Invalid handler lifecycle phase: " + lifecyclePhase +
                    ". Removing handler from the default phase.");
            removeHandler(methods, filter, handler);
        } else {
            hm.removeHandler(methods, filter, handler, lifecyclePhase);
        }
    }

    @Override
    public void editComment(RequestContext requestContext) throws RegistryException {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).editComment(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            handlerManagers.get(TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).editComment(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            handlerManagers.get(USER_DEFINED_SYSTEM_HANDLER_PHASE).editComment(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        handlerManagers.get(USER_DEFINED_HANDLER_PHASE).editComment(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).editComment(
						requestContext);
		}
    }

    @Override
    public void removeComment(RequestContext requestContext) throws RegistryException {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).removeComment(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            handlerManagers.get(TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).removeComment(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            handlerManagers.get(USER_DEFINED_SYSTEM_HANDLER_PHASE).removeComment(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        handlerManagers.get(USER_DEFINED_HANDLER_PHASE).removeComment(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).removeComment(
						requestContext);
		}
    }

    @Override
    public void createVersion(RequestContext requestContext) throws RegistryException {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).createVersion(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            handlerManagers.get(TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).createVersion(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            handlerManagers.get(USER_DEFINED_SYSTEM_HANDLER_PHASE).createVersion(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        handlerManagers.get(USER_DEFINED_HANDLER_PHASE).createVersion(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).createVersion(
						requestContext);
		}
    }

    @Override
    public void restoreVersion(RequestContext requestContext) throws RegistryException {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).restoreVersion(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).restoreVersion(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            handlerManagers.get(USER_DEFINED_SYSTEM_HANDLER_PHASE).restoreVersion(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        handlerManagers.get(USER_DEFINED_HANDLER_PHASE).restoreVersion(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE)
						.restoreVersion(requestContext);
		}
    }

    @Override
    public void rateResource(RequestContext requestContext) throws RegistryException {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).rateResource(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            handlerManagers.get(TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).rateResource(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            handlerManagers.get(USER_DEFINED_SYSTEM_HANDLER_PHASE).rateResource(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        handlerManagers.get(USER_DEFINED_HANDLER_PHASE).rateResource(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).rateResource(
						requestContext);
		}
    }

    @Override
    public void removeTag(RequestContext requestContext) throws RegistryException {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).removeTag(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            handlerManagers.get(TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).removeTag(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            handlerManagers.get(USER_DEFINED_SYSTEM_HANDLER_PHASE).removeTag(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        handlerManagers.get(USER_DEFINED_HANDLER_PHASE).removeTag(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).removeTag(
					requestContext);
		}
    }

    @Override
    public void applyTag(RequestContext requestContext) throws RegistryException {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).applyTag(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            handlerManagers.get(TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).applyTag(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            handlerManagers.get(USER_DEFINED_SYSTEM_HANDLER_PHASE).applyTag(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        handlerManagers.get(USER_DEFINED_HANDLER_PHASE).applyTag(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).applyTag(
					requestContext);
		}
    }

    @Override
    public void removeAssociation(RequestContext requestContext) throws RegistryException {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).removeAssociation(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).removeAssociation(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).removeAssociation(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        handlerManagers.get(USER_DEFINED_HANDLER_PHASE).removeAssociation(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE)
					.removeAssociation(requestContext);
		}
    }

    @Override
    public void addAssociation(RequestContext requestContext) throws RegistryException {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).addAssociation(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).addAssociation(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            handlerManagers.get(USER_DEFINED_SYSTEM_HANDLER_PHASE).addAssociation(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        handlerManagers.get(USER_DEFINED_HANDLER_PHASE).addAssociation(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE)
					.addAssociation(requestContext);
		}
    }

    @Override
    public void delete(RequestContext requestContext) throws RegistryException {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).delete(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            handlerManagers.get(TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).delete(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            handlerManagers.get(USER_DEFINED_SYSTEM_HANDLER_PHASE).delete(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        handlerManagers.get(USER_DEFINED_HANDLER_PHASE).delete(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).delete(
					requestContext);
		}
    }

    @Override
    public void putChild(RequestContext requestContext) throws RegistryException {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).putChild(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            handlerManagers.get(TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).putChild(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            handlerManagers.get(USER_DEFINED_SYSTEM_HANDLER_PHASE).putChild(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        handlerManagers.get(USER_DEFINED_HANDLER_PHASE).putChild(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).putChild(
					requestContext);
		}
    }

    @Override
    public void importChild(RequestContext requestContext) throws RegistryException {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).importChild(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            handlerManagers.get(TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).importChild(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            handlerManagers.get(USER_DEFINED_SYSTEM_HANDLER_PHASE).importChild(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        handlerManagers.get(USER_DEFINED_HANDLER_PHASE).importChild(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).importChild(
					requestContext);
		}
    }

    @Override
    public void invokeAspect(RequestContext requestContext) throws RegistryException {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).invokeAspect(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            handlerManagers.get(TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).invokeAspect(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            handlerManagers.get(USER_DEFINED_SYSTEM_HANDLER_PHASE).invokeAspect(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        handlerManagers.get(USER_DEFINED_HANDLER_PHASE).invokeAspect(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).invokeAspect(
					requestContext);
		}
    }

    @Override
    public void createLink(RequestContext requestContext) throws RegistryException {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).createLink(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            handlerManagers.get(TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).createLink(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            handlerManagers.get(USER_DEFINED_SYSTEM_HANDLER_PHASE).createLink(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        handlerManagers.get(USER_DEFINED_HANDLER_PHASE).createLink(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).createLink(
					requestContext);
		}
    }

    @Override
    public void removeLink(RequestContext requestContext) throws RegistryException {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).removeLink(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            handlerManagers.get(TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).removeLink(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            handlerManagers.get(USER_DEFINED_SYSTEM_HANDLER_PHASE).removeLink(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        handlerManagers.get(USER_DEFINED_HANDLER_PHASE).removeLink(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).removeLink(
					requestContext);
		}
    }

    @Override
    public void restore(RequestContext requestContext) throws RegistryException {
        handlerManagers.get(DEFAULT_SYSTEM_HANDLER_PHASE).restore(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            handlerManagers.get(TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).restore(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            handlerManagers.get(USER_DEFINED_SYSTEM_HANDLER_PHASE).restore(requestContext);
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        handlerManagers.get(USER_DEFINED_HANDLER_PHASE).restore(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).restore(
					requestContext);
		}
    }

    @Override
    public Association[] getAllAssociations(RequestContext requestContext)
            throws RegistryException {
        Association[] defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).getAllAssociations(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            Association[] tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).getAllAssociations(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            Association[] systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).getAllAssociations(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        Association[] userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).getAllAssociations(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE)
					.getAllAssociations(requestContext);
		}

        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public Association[] getAssociations(RequestContext requestContext) throws RegistryException {
        Association[] defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).getAssociations(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            Association[] tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).getAssociations(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            Association[] systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).getAssociations(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        Association[] userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).getAssociations(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE)
					.getAssociations(requestContext);
		}
        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public TaggedResourcePath[] getResourcePathsWithTag(RequestContext requestContext)
            throws RegistryException {
        TaggedResourcePath[] defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).getResourcePathsWithTag(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            TaggedResourcePath[] tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).getResourcePathsWithTag(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            TaggedResourcePath[] systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).getResourcePathsWithTag(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        TaggedResourcePath[] userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).getResourcePathsWithTag(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(
					DEFAULT_REPORTING_HANDLER_PHASE).getResourcePathsWithTag(
					requestContext);
		}

        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public Tag[] getTags(RequestContext requestContext)
            throws RegistryException {
        Tag[] defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).getTags(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            Tag[] tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).getTags(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            Tag[] systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).getTags(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        Tag[] userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).getTags(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(
					DEFAULT_REPORTING_HANDLER_PHASE).getTags(requestContext);
		}

        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public Comment[] getComments(RequestContext requestContext) throws RegistryException {
        Comment[] defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).getComments(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            Comment[] tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).getComments(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            Comment[] systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).getComments(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        Comment[] userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).getComments(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE)
							.getComments(requestContext);
		}

        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public String[] getVersions(RequestContext requestContext) throws RegistryException {
        String[] defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).getVersions(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            String[] tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).getVersions(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            String[] systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).getVersions(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        String[] userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).getVersions(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(
					DEFAULT_REPORTING_HANDLER_PHASE)
					.getVersions(requestContext);
		}

        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public Collection executeQuery(RequestContext requestContext) throws RegistryException {
        Collection defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).executeQuery(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            Collection tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).executeQuery(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            Collection systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).executeQuery(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        Collection userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).executeQuery(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(
					DEFAULT_REPORTING_HANDLER_PHASE).executeQuery(
					requestContext);
		}

        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public Collection searchContent(RequestContext requestContext) throws RegistryException {
        Collection defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).searchContent(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            Collection tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).searchContent(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            Collection systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).searchContent(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        Collection userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).searchContent(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(
					DEFAULT_REPORTING_HANDLER_PHASE).searchContent(
					requestContext);
		}

        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public String addComment(RequestContext requestContext) throws RegistryException {
        String defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).addComment(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            String tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).addComment(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            String systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).addComment(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        String userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).addComment(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(
					DEFAULT_REPORTING_HANDLER_PHASE).addComment(requestContext);
		}

        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public Resource get(RequestContext requestContext) throws RegistryException {
        Resource defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).get(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            Resource tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).get(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            Resource systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).get(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        Resource userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).get(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);
        Resource resourceOnContext = requestContext.getResource();
        Resource output = (userDefinedValue != null) ? userDefinedValue : defaultValue;
        requestContext.setResource(output);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(
					DEFAULT_REPORTING_HANDLER_PHASE).get(requestContext);
		}
        requestContext.setResource(resourceOnContext);
        return output;
    }

    @Override
    public String put(RequestContext requestContext) throws RegistryException {
        String defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).put(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            String tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).put(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            String systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).put(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        String userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).put(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(
					DEFAULT_REPORTING_HANDLER_PHASE).put(requestContext);
		}
        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public String importResource(RequestContext requestContext) throws RegistryException {
        String defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).importResource(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            String tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).importResource(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            String systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).importResource(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        String userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).importResource(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		// Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(
					DEFAULT_REPORTING_HANDLER_PHASE).importResource(
					requestContext);
		}

        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public String copy(RequestContext requestContext) throws RegistryException {
        String defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).copy(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            String tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).copy(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            String systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).copy(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        String userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).copy(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		//Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).copy(requestContext);
		}

        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public String move(RequestContext requestContext) throws RegistryException {
        String defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).move(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            String tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).move(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            String systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).move(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        String userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).move(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);
		
		//Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).move(
					requestContext);
		}

        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public String rename(RequestContext requestContext) throws RegistryException {
        String defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).rename(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            String tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).rename(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            String systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).rename(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        String userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).rename(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		//Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).rename(requestContext);
		}

        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public OMElement dump(RequestContext requestContext) throws RegistryException {
        OMElement defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).dump(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            OMElement tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).dump(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            OMElement systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).dump(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        OMElement userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).dump(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		//Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).dump(
					requestContext);
		}
		
        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public float getAverageRating(RequestContext requestContext) throws RegistryException {
        float defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).getAverageRating(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            float tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).getAverageRating(requestContext);
            if (tenantSpecificValue != -1) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            float systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).getAverageRating(requestContext);
            if (systemSpecificValue != -1) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        float userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).getAverageRating(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);
		
		//Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).getAverageRating(
					requestContext);
		}

        if (userDefinedValue != -1) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public int getRating(RequestContext requestContext) throws RegistryException {
        int defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).getRating(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            int tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).getRating(requestContext);
            if (tenantSpecificValue != -1) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            int systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).getRating(requestContext);
            if (systemSpecificValue != -1) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        int userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).getRating(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);
		
		//Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).getRating(requestContext);
		}

        if (userDefinedValue != -1) {
            return userDefinedValue;
        }
        return defaultValue;
    }

    @Override
    public boolean resourceExists(RequestContext requestContext) throws RegistryException {
        boolean defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).resourceExists(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            boolean tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).resourceExists(requestContext);
            defaultValue = defaultValue || tenantSpecificValue;
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            boolean systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).resourceExists(requestContext);
            defaultValue = defaultValue || systemSpecificValue;
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        boolean userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).resourceExists(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		//Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).resourceExists(
					requestContext);
		}
		return userDefinedValue || defaultValue;
    }

    @Override
    public RegistryContext getRegistryContext(RequestContext requestContext) {
        RegistryContext defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).getRegistryContext(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            RegistryContext tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).getRegistryContext(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            RegistryContext systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).getRegistryContext(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        RegistryContext userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).getRegistryContext(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		//Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).getRegistryContext(requestContext);
		}
        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }
    
    @Override
    public OMElement dumpLite(RequestContext requestContext) throws RegistryException {
        OMElement defaultValue = handlerManagers.get(
                DEFAULT_SYSTEM_HANDLER_PHASE).dumpLite(requestContext);
        boolean isProcessingComplete = requestContext.isProcessingComplete();
        if (!isProcessingComplete) {
            OMElement tenantSpecificValue = handlerManagers.get(
                    TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE).dumpLite(requestContext);
            if (tenantSpecificValue != null) {
                defaultValue = tenantSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        if (!isProcessingComplete) {
            OMElement systemSpecificValue = handlerManagers.get(
                    USER_DEFINED_SYSTEM_HANDLER_PHASE).dumpLite(requestContext);
            if (systemSpecificValue != null) {
                defaultValue = systemSpecificValue;
            }
            isProcessingComplete = requestContext.isProcessingComplete();
        }
        requestContext.setProcessingComplete(false);
        OMElement userDefinedValue = handlerManagers.get(
                USER_DEFINED_HANDLER_PHASE).dumpLite(requestContext);
        isProcessingComplete |= requestContext.isProcessingComplete();
        // The reporting handler phase needs to know about the state of processing
        requestContext.setProcessingComplete(isProcessingComplete);

		//Check process completion to avoid receiving two notifications
		if (!isProcessingComplete) {
			handlerManagers.get(DEFAULT_REPORTING_HANDLER_PHASE).dumpLite(requestContext);
		}

        if (userDefinedValue != null) {
            return userDefinedValue;
        }
        return defaultValue;
    }
}
