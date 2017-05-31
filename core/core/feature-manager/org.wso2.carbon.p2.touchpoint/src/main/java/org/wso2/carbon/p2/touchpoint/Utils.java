package org.wso2.carbon.p2.touchpoint;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class Utils {

    public static IStatus createError(String message) {
        return createError(message, null);
    }

    public static IStatus createError(String message, Exception e) {
        return new Status(IStatus.ERROR, TouchpointActionConstants.PLUGIN_ID, message, e);

    }
}
