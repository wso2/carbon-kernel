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

package org.apache.axis2.addressing.i18n;


import org.apache.axis2.i18n.MessageBundle;
import org.apache.axis2.i18n.MessagesConstants;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @see org.apache.axis2.i18n.Messages The implementation is the same but just thisClass static
 *      reference has AddressingMessages.class
 */
public class AddressingMessages {

    //Only this changes from the original class
    private static Class<AddressingMessages> thisClass = AddressingMessages.class;

    private static final String projectName = MessagesConstants.projectName;

    private static final String resourceName = MessagesConstants.resourceName;
    private static final Locale locale = MessagesConstants.locale;

    private static final String packageName = getPackage(thisClass.getName());
    private static final ClassLoader classLoader = thisClass.getClassLoader();

    private static final ResourceBundle parent =
            (MessagesConstants.rootPackageName.equals(packageName))
                    ? null
                    : MessagesConstants.rootBundle;


    /** * NO NEED TO CHANGE ANYTHING BELOW **** */

    private static final MessageBundle messageBundle =
            new MessageBundle(projectName, packageName, resourceName,
                              locale, classLoader, parent);

    /**
     * Gets a message from resource.properties from the package of the given object.
     *
     * @param key The resource key
     * @return Returns the formatted message.
     */
    public static String getMessage(String key)
            throws MissingResourceException {
        return messageBundle.getMessage(key);
    }

    /**
     * Gets a message from resource.properties from the package of the given object.
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @return Returns the formatted message.
     */
    public static String getMessage(String key, String arg0)
            throws MissingResourceException {
        return messageBundle.getMessage(key, arg0);
    }

    /**
     * Gets a message from resource.properties from the package of the given object.
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @param arg1 The argument to place in variable {1}
     * @return Returns the formatted message.
     */
    public static String getMessage(String key, String arg0, String arg1)
            throws MissingResourceException {
        return messageBundle.getMessage(key, arg0, arg1);
    }

    /**
     * Gets a message from resource.properties from the package of the given object.
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @param arg1 The argument to place in variable {1}
     * @param arg2 The argument to place in variable {2}
     * @return Returns the formatted message.
     */
    public static String getMessage(String key, String arg0, String arg1, String arg2)
            throws MissingResourceException {
        return messageBundle.getMessage(key, arg0, arg1, arg2);
    }

    /**
     * Gets a message from resource.properties from the package of the given object.
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @param arg1 The argument to place in variable {1}
     * @param arg2 The argument to place in variable {2}
     * @param arg3 The argument to place in variable {3}
     * @return Returns the formatted message.
     */
    public static String getMessage(String key, String arg0, String arg1, String arg2, String arg3)
            throws MissingResourceException {
        return messageBundle.getMessage(key, arg0, arg1, arg2, arg3);
    }

    /**
     * Gets a message from resource.properties from the package of the given object.
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @param arg1 The argument to place in variable {1}
     * @param arg2 The argument to place in variable {2}
     * @param arg3 The argument to place in variable {3}
     * @param arg4 The argument to place in variable {4}
     * @return Returns the formatted message.
     */
    public static String getMessage(String key, String arg0, String arg1, String arg2, String arg3,
                                    String arg4)
            throws MissingResourceException {
        return messageBundle.getMessage(key, arg0, arg1, arg2, arg3, arg4);
    }

    /**
     * Gets a message from resource.properties from the package of the given object.
     *
     * @param key  The resource key
     * @param args An array of objects to place in corresponding variables
     * @return Returns the formatted message.
     */
    public static String getMessage(String key, String[] args)
            throws MissingResourceException {
        return messageBundle.getMessage(key, args);
    }

    public static ResourceBundle getResourceBundle() {
        return messageBundle.getResourceBundle();
    }

    public static MessageBundle getMessageBundle() {
        return messageBundle;
    }

    private static String getPackage(String name) {
        return name.substring(0, name.lastIndexOf('.')).intern();
    }

}
