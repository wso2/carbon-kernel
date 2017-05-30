/* 
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.wso2.carbon.utils.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {
    private static final Class THIS_CLASS = Messages.class;

    private static final String PROJECT_NAME = MessagesConstants.PROJECT_NAME;

    private static final String RESOURCE_NAME = MessagesConstants.RESOURCE_NAME;
    private static final Locale LOCALE = MessagesConstants.LOCALE;

    private static final String PACKAGE_NAME = getPackage(THIS_CLASS.getName());
    private static final ClassLoader CLASS_LOADER = THIS_CLASS.getClassLoader();

    private static final ResourceBundle parent =
            (MessagesConstants.rootPackageName.equals(PACKAGE_NAME))
            ? null
            : MessagesConstants.rootBundle;


    /**
     * ** NO NEED TO CHANGE ANYTHING BELOW ****
     */

    private static final MessageBundle MESSAGE_BUNDLE =
            new MessageBundle(PROJECT_NAME, PACKAGE_NAME, RESOURCE_NAME,
                                                  LOCALE, CLASS_LOADER, parent);

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param key The resource key
     * @return The formatted message
     */
    public static String getMessage(String key) {
        return MESSAGE_BUNDLE.getMessage(key);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @return The formatted message
     */
    public static String getMessage(String key, String arg0) {
        return MESSAGE_BUNDLE.getMessage(key, arg0);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @param arg1 The argument to place in variable {1}
     * @return The formatted message
     */
    public static String getMessage(String key, String arg0, String arg1) {
        return MESSAGE_BUNDLE.getMessage(key, arg0, arg1);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @param arg1 The argument to place in variable {1}
     * @param arg2 The argument to place in variable {2}
     * @return The formatted message
     */
    public static String getMessage(String key, String arg0, String arg1, String arg2) {
        return MESSAGE_BUNDLE.getMessage(key, arg0, arg1, arg2);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @param arg1 The argument to place in variable {1}
     * @param arg2 The argument to place in variable {2}
     * @param arg3 The argument to place in variable {3}
     * @return The formatted message
     */
    public static String getMessage(String key, String arg0, String arg1, String arg2, String arg3) {
        return MESSAGE_BUNDLE.getMessage(key, arg0, arg1, arg2, arg3);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @param arg1 The argument to place in variable {1}
     * @param arg2 The argument to place in variable {2}
     * @param arg3 The argument to place in variable {3}
     * @param arg4 The argument to place in variable {4}
     * @return The formatted message
     */
    public static String getMessage(String key, String arg0, String arg1, String arg2, String arg3, String arg4) {
        return MESSAGE_BUNDLE.getMessage(key, arg0, arg1, arg2, arg3, arg4);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param key  The resource key
     * @param args An array of objects to place in corresponding variables
     * @return The formatted message
     */
    public static String getMessage(String key, String[] args) {
        return MESSAGE_BUNDLE.getMessage(key, args);
    }

    public static ResourceBundle getResourceBundle() {
        return MESSAGE_BUNDLE.getResourceBundle();
    }

    public static MessageBundle getMessageBundle() {
        return MESSAGE_BUNDLE;
    }

    private static String getPackage(String name) {
        return name.substring(0, name.lastIndexOf('.')).intern();
    }
}
