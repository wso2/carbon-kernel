package org.wso2.carbon.server.admin.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.wso2.carbon.server.admin.privilegedaction.PrivilegedAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @scr.component name="org.wso2.carbon.server.admin.privilegedaction" immediate="true"
 */
public class PrivilegedActionServiceComponent {

    private static final Log log = LogFactory.getLog(PrivilegedActionServiceComponent.class);
    public static List<PrivilegedAction> privilegedActions = new ArrayList<PrivilegedAction>();

    protected void activate (ComponentContext ctxt) {

        ServiceTracker serviceTracker = new ServiceTracker(ctxt.getBundleContext(),
            PrivilegedAction.class.getName(),new ServiceTrackerCustomizer<PrivilegedAction,Object>() {
            @Override
            public Object addingService(ServiceReference<PrivilegedAction> privilegedActionServiceReference) {
                privilegedActions.add(privilegedActionServiceReference.getBundle().getBundleContext().getService(privilegedActionServiceReference));
                Collections.sort(privilegedActions, new PrivilegedActionComparator());
                return privilegedActionServiceReference;
            }

            @Override
            public void modifiedService(ServiceReference<PrivilegedAction> privilegedActionServiceReference, Object service) {

            }

            @Override
            public void removedService(ServiceReference<PrivilegedAction> privilegedActionServiceReference, Object service) {
                privilegedActions.remove(service);
                privilegedActionServiceReference.getBundle().getBundleContext().ungetService(privilegedActionServiceReference);
            }
        });
        serviceTracker.open();
        log.debug("PrivilegedAction bundle is activated");
    }

    /**
         * <code>Comparator</code> that orders <code>PrivilegedAction</code> objects in descending order of their priority
         */
    private static class PrivilegedActionComparator implements
                                                    Comparator<PrivilegedAction> {
        @Override
        public int compare(PrivilegedAction ex1, PrivilegedAction ex2) {
            return ex2.getPriority()-ex1.getPriority();
        }
    }

}
