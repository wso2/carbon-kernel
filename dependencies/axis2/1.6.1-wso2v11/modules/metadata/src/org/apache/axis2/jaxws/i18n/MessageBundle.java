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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Accept parameters for ProjectResourceBundle, but defer object instantiation (and therefore
 * resource bundle loading) until required.
 */
public class MessageBundle {
    private boolean loaded = false;

    private ProjectResourceBundle _resourceBundle = null;

    private final String projectName;
    private final String packageName;
    private final String resourceName;
    private final Locale locale;
    private final ClassLoader classLoader;
    private final ResourceBundle parent;


    public final ProjectResourceBundle getResourceBundle() {
        if (!loaded) {
            _resourceBundle = ProjectResourceBundle.getBundle(projectName,
                                                              packageName,
                                                              resourceName,
                                                              locale,
                                                              classLoader,
                                                              parent);
            loaded = true;
        }
        return _resourceBundle;
    }

    /** Construct a new ExtendMessages */
    public MessageBundle(String projectName,
                         String packageName,
                         String resourceName,
                         Locale locale,
                         ClassLoader classLoader,
                         ResourceBundle parent)
            throws MissingResourceException {
        this.projectName = projectName;
        this.packageName = packageName;
        this.resourceName = resourceName;
        this.locale = locale;
        this.classLoader = classLoader;
        this.parent = parent;
    }

    /**
     * Gets a string message from the resource bundle for the given key
     *
     * @param key The resource key
     * @return The message
     */
    public String getMessage(String key) throws MissingResourceException {
        return getMessage(key, (String[])null);
    }

    /**
     * <p>Gets a string message from the resource bundle for the given key. The message may contain
     * variables that will be substituted with the given arguments. Variables have the format:</p>
     * <dir> This message has two variables: {0} and {1} </dir>
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @return The message
     */
    public String getMessage(String key, String arg0) throws MissingResourceException {
        return getMessage(key, new String[] { arg0 });
    }

    /**
     * <p>Gets a string message from the resource bundle for the given key. The message may contain
     * variables that will be substituted with the given arguments. Variables have the format:</p>
     * <dir> This message has two variables: {0} and {1} </dir>
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @param arg1 The argument to place in variable {1}
     * @return The message
     */
    public String getMessage(String key, String arg0, String arg1) throws MissingResourceException {
        return getMessage(key, new String[] { arg0, arg1 });
    }

    /**
     * <p>Gets a string message from the resource bundle for the given key. The message may contain
     * variables that will be substituted with the given arguments. Variables have the format:</p>
     * <dir> This message has two variables: {0} and {1} </dir>
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @param arg1 The argument to place in variable {1}
     * @param arg2 The argument to place in variable {2}
     * @return The message
     */
    public String getMessage(String key, String arg0, String arg1, String arg2)
            throws MissingResourceException {
        return getMessage(key, new String[] { arg0, arg1, arg2 });
    }

    /**
     * <p>Gets a string message from the resource bundle for the given key. The message may contain
     * variables that will be substituted with the given arguments. Variables have the format:</p>
     * <dir> This message has two variables: {0} and {1} </dir>
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @param arg1 The argument to place in variable {1}
     * @param arg2 The argument to place in variable {2}
     * @param arg3 The argument to place in variable {3}
     * @return The message
     */
    public String getMessage(String key, String arg0, String arg1, String arg2, String arg3)
            throws MissingResourceException {
        return getMessage(key, new String[] { arg0, arg1, arg2, arg3 });
    }

    /**
     * <p>Gets a string message from the resource bundle for the given key. The message may contain
     * variables that will be substituted with the given arguments. Variables have the format:</p>
     * <dir> This message has two variables: {0} and {1} </dir>
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @param arg1 The argument to place in variable {1}
     * @param arg2 The argument to place in variable {2}
     * @param arg3 The argument to place in variable {3}
     * @param arg4 The argument to place in variable {4}
     * @return The message
     */
    public String getMessage(String key, String arg0, String arg1, String arg2, String arg3,
                             String arg4) throws MissingResourceException {
        return getMessage(key, new String[] { arg0, arg1, arg2, arg3, arg4 });
    }

    /**
     * <p>Gets a string message from the resource bundle for the given key. The message may contain
     * variables that will be substituted with the given arguments. Variables have the format:</p>
     * <dir> This message has two variables: {0} and {1} </dir>
     *
     * @param key   The resource key
     * @param array An array of objects to place in corresponding variables
     * @return The message
     */
    public String getMessage(String key, String[] array) throws MissingResourceException {
        String msg = null;
        if (getResourceBundle() != null) {
            msg = getResourceBundle().getString(key);
        }

        if (msg == null) {
            throw new MissingResourceException("Cannot find resource key \"" + key +
                    "\" in base name " +
                    getResourceBundle().getResourceName(),
                                               getResourceBundle().getResourceName(), key);
        }

        return MessageFormat.format(msg, (Object[])array);
    }
}
