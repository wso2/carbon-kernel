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

package org.apache.axis2.jaxws.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static final Class thisClass = Messages.class;

    private static final String projectName = MessagesConstants.projectName;

    private static final String resourceName = MessagesConstants.resourceName;
    private static final Locale locale = MessagesConstants.locale;

    public static final String DEFAULT_MESSAGE_BUNDLE_KEY = "default";
    private static final String NO_MESSAGE_BUNDLE = "Message Bundle is not available";

    private static final String packageName = getPackage(thisClass.getName());
    private static final ClassLoader classLoader = thisClass.getClassLoader();

    private static final ResourceBundle parent =
            (MessagesConstants.rootPackageName.equals(packageName))
                    ? null
                    : MessagesConstants.rootBundle;

    private static HashMap messageBundleMap = new HashMap();

    static {
        MessageBundle defaultMessageBundle =
                new MessageBundle(projectName, packageName, resourceName,
                                  locale, classLoader, parent);
        addMessageBundle(DEFAULT_MESSAGE_BUNDLE_KEY, defaultMessageBundle);
    }

    /**
     * To add a new Message Bundle to the MessageBundle list.
     *
     * @param messageBundleKey The key which will be used to refer to this message bundle later.
     * @param messageBundle    The message bundle.
     */
    public static void addMessageBundle(String messageBundleKey, MessageBundle messageBundle) {
        messageBundleMap.put(messageBundleKey, messageBundle);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param key The resource key
     * @return The formatted message
     */
    public static String getMessage(String key)
            throws MissingResourceException {
        MessageBundle messageBundle = getMessageBundle(DEFAULT_MESSAGE_BUNDLE_KEY);
        return messageBundle.getMessage(key);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @return The formatted message
     */
    public static String getMessage(String key, String arg0)
            throws MissingResourceException {
        MessageBundle messageBundle = getMessageBundle(DEFAULT_MESSAGE_BUNDLE_KEY);
        return messageBundle.getMessage(key, arg0);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @param arg1 The argument to place in variable {1}
     * @return The formatted message
     */
    public static String getMessage(String key, String arg0, String arg1)
            throws MissingResourceException {
        MessageBundle messageBundle = getMessageBundle(DEFAULT_MESSAGE_BUNDLE_KEY);
        return messageBundle.getMessage(key, arg0, arg1);
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
    public static String getMessage(String key, String arg0, String arg1, String arg2)
            throws MissingResourceException {
        MessageBundle messageBundle = getMessageBundle(DEFAULT_MESSAGE_BUNDLE_KEY);
        return messageBundle.getMessage(key, arg0, arg1, arg2);
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
    public static String getMessage(String key, String arg0, String arg1, String arg2, String arg3)
            throws MissingResourceException {
        MessageBundle messageBundle = getMessageBundle(DEFAULT_MESSAGE_BUNDLE_KEY);
        return messageBundle.getMessage(key, arg0, arg1, arg2, arg3);
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
    public static String getMessage(String key, String arg0, String arg1, String arg2, String arg3,
                                    String arg4)
            throws MissingResourceException {
        MessageBundle messageBundle = getMessageBundle(DEFAULT_MESSAGE_BUNDLE_KEY);
        return messageBundle.getMessage(key, arg0, arg1, arg2, arg3, arg4);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param key  The resource key
     * @param args An array of objects to place in corresponding variables
     * @return The formatted message
     */
    public static String getMessage(String key, String[] args)
            throws MissingResourceException {
        MessageBundle messageBundle = getMessageBundle(DEFAULT_MESSAGE_BUNDLE_KEY);
        return messageBundle.getMessage(key, args);
    }

    public static ResourceBundle getResourceBundle() {
        MessageBundle messageBundle = getMessageBundle(DEFAULT_MESSAGE_BUNDLE_KEY);
        return messageBundle.getResourceBundle();
    }

    public static MessageBundle getMessageBundle() {
        MessageBundle messageBundle = getMessageBundle(DEFAULT_MESSAGE_BUNDLE_KEY);
        return messageBundle;
    }

    public static MessageBundle getMessageBundle(String messageBundleKey) {
        MessageBundle messageBundle = (MessageBundle)messageBundleMap.get(messageBundleKey);
        return messageBundle;
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param messageBundleKey The key for getting the correct message bundle.
     * @param key              The resource key
     * @return The formatted message
     */
    public static String getMessageFromBundle(String messageBundleKey, String key)
            throws MissingResourceException, Exception {
        MessageBundle messageBundle = getMessageBundle(messageBundleKey);
        if (messageBundle == null)
            throw new Exception(NO_MESSAGE_BUNDLE);

        return messageBundle.getMessage(key);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param messageBundleKey The key for getting the correct message bundle.
     * @param key              The resource key
     * @param arg0             The argument to place in variable {0}
     * @return The formatted message
     */
    public static String getMessageFromBundle(String messageBundleKey, String key, String arg0)
            throws MissingResourceException, Exception {
        MessageBundle messageBundle = getMessageBundle(messageBundleKey);
        if (messageBundle == null)
            throw new Exception(NO_MESSAGE_BUNDLE);

        return messageBundle.getMessage(key, arg0);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param messageBundleKey The key for getting the correct message bundle.
     * @param key              The resource key
     * @param arg0             The argument to place in variable {0}
     * @param arg1             The argument to place in variable {1}
     * @return The formatted message
     */
    public static String getMessageFromBundle(String messageBundleKey, String key, String arg0,
                                              String arg1)
            throws MissingResourceException, Exception {
        MessageBundle messageBundle = getMessageBundle(messageBundleKey);
        if (messageBundle == null)
            throw new Exception(NO_MESSAGE_BUNDLE);

        return messageBundle.getMessage(key, arg0, arg1);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param messageBundleKey The key for getting the correct message bundle.
     * @param key              The resource key
     * @param arg0             The argument to place in variable {0}
     * @param arg1             The argument to place in variable {1}
     * @param arg2             The argument to place in variable {2}
     * @return The formatted message
     */
    public static String getMessageFromBundle(String messageBundleKey, String key, String arg0,
                                              String arg1, String arg2)
            throws MissingResourceException, Exception {
        MessageBundle messageBundle = getMessageBundle(messageBundleKey);
        if (messageBundle == null)
            throw new Exception(NO_MESSAGE_BUNDLE);

        return messageBundle.getMessage(key, arg0, arg1, arg2);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param messageBundleKey The key for getting the correct message bundle.
     * @param key              The resource key
     * @param arg0             The argument to place in variable {0}
     * @param arg1             The argument to place in variable {1}
     * @param arg2             The argument to place in variable {2}
     * @param arg3             The argument to place in variable {3}
     * @return The formatted message
     */
    public static String getMessageFromBundle(String messageBundleKey, String key, String arg0,
                                              String arg1, String arg2, String arg3)
            throws MissingResourceException, Exception {
        MessageBundle messageBundle = getMessageBundle(messageBundleKey);
        if (messageBundle == null)
            throw new Exception(NO_MESSAGE_BUNDLE);

        return messageBundle.getMessage(key, arg0, arg1, arg2, arg3);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param messageBundleKey The key for getting the correct message bundle.
     * @param key              The resource key
     * @param arg0             The argument to place in variable {0}
     * @param arg1             The argument to place in variable {1}
     * @param arg2             The argument to place in variable {2}
     * @param arg3             The argument to place in variable {3}
     * @param arg4             The argument to place in variable {4}
     * @return The formatted message
     */
    public static String getMessageFromBundle(String messageBundleKey, String key, String arg0,
                                              String arg1, String arg2, String arg3, String arg4)
            throws MissingResourceException, Exception {
        MessageBundle messageBundle = getMessageBundle(messageBundleKey);
        if (messageBundle == null)
            throw new Exception(NO_MESSAGE_BUNDLE);

        return messageBundle.getMessage(key, arg0, arg1, arg2, arg3, arg4);
    }

    private static String getPackage(String name) {
        return name.substring(0, name.lastIndexOf('.')).intern();
    }
}
