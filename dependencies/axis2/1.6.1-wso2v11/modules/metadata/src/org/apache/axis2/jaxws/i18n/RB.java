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

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;

/**
 * CURRENTLY NOT USED KEEPING FOR REFERENCE  9/19/2002
 * <p/>
 * <p>Wrapper class for resource bundles. Property files are used to store resource strings, which
 * are the only types of resources available. Property files can inherit properties from other files
 * so that a base property file can be used and a small number of properties can be over-ridden by
 * another property file. For example you may create an english version of a resource file named
 * "resource.properties". You then decide that the British English version of all of the properties
 * except one are the same, so there is no need to redefine all of the properties in
 * "resource_en_GB", just the one that is different.</p> <p>The property file lookup searches for
 * classes with various suffixes on the basis if the desired local and the current default local (as
 * returned by Local.getDefault()). As property files are found the property values are merged so
 * that inheritance is preserved.</p> <p>The order of searching is:</p> <dir> basename + "_" +
 * langage + "_" + country + "_" + variant basename + "_" + langage + "_" + country basename + "_" +
 * langage basename + "_" + defaultLanguage + "_" + defaultCountry + "_" + defaultVariant basename +
 * "_" + defaultLanguage + "_" + defaultCountry basename + "_" + defaultLanguage basename </dir>
 * <p>The basename is the name of the property file without the ".properties" extension.</p>
 * <p>Properties will be cached for performance.<p> <p>Property values stored in the property files
 * can also contain dynamic variables. Any dynamic variable defined in
 * PropertiesUtil.getVariableValue() can be used (such as {date}), as well as arguments in the form
 * {0}, {1}, etc. Argument values are specified in the various overloaded getString() methods.</p>
 */
public class RB {
    // The static cache of properties. The key is the basename + the local +
    // the default local and the element is the Properties object containing
    // the resources
    static Hashtable propertyCache = new Hashtable();

    // The default base name
    public static final String BASE_NAME = "resource";

    // The property file extension
    public static final String PROPERTY_EXT = ".properties";

    // The name of the current base property file (with extension)
    protected String basePropertyFileName;

    // The properties for the current resource bundle
    protected Properties resourceProperties;

    /**
     * Construct a new RB
     *
     * @param name The name of the property file without the ".properties" extension
     */
    public RB(String name) throws MissingResourceException {
        this(null, name, null);
    }

    /**
     * Construct a new RB
     *
     * @param caller The calling object. This is used to get the package name to further construct
     *               the basename as well as to get the proper ClassLoader
     * @param name   The name of the property file without the ".properties" extension
     */
    public RB(Object caller, String name) throws MissingResourceException {
        this(caller, name, null);
    }

    /**
     * Construct a new RB
     *
     * @param caller The calling object. This is used to get the package name to further construct
     *               the basename as well as to get the proper ClassLoader
     * @param name   The name of the property file without the ".properties" extension
     * @param locale The locale
     */
    public RB(Object caller, String name, Locale locale) throws MissingResourceException {
        ClassLoader cl = null;

        if (caller != null) {

            Class c;
            if (caller instanceof Class) {
                c = (Class)caller;
            } else {
                c = caller.getClass();
            }

            // Get the appropriate class loader
            cl = c.getClassLoader();

            if (name.indexOf("/") == -1) {

                // Create the full basename only if not given
                String fullName = c.getName();

                int pos = fullName.lastIndexOf(".");
                if (pos > 0) {
                    name = fullName.substring(0, pos + 1).replace('.', '/') + name;
                }
            }
        } else {
            // Try the shared default properties file...
            if (name.indexOf("/") == -1) {
                name = "org/apache/axis2/default-resource";
            }
        }

        Locale defaultLocale = Locale.getDefault();

        // If the locale given is the same as the default locale, ignore it
        if (locale != null) {
            if (locale.equals(defaultLocale)) {
                locale = null;
            }
        }

        // Load the properties. If no property files exist then a
        // MissingResourceException will be thrown
        loadProperties(name, cl, locale, defaultLocale);
    }

    /**
     * Gets a string message from the resource bundle for the given key
     *
     * @param key The resource key
     * @return The message
     */
    public String getString(String key) throws MissingResourceException {
        return getString(key, (Object[])null);
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
    public String getString(String key, Object arg0) throws MissingResourceException {
        Object[] o = new Object[1];
        o[0] = arg0;
        return getString(key, o);
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
    public String getString(String key, Object arg0, Object arg1) throws MissingResourceException {
        Object[] o = new Object[2];
        o[0] = arg0;
        o[1] = arg1;
        return getString(key, o);
    }

    /**
     * <p>Gets a string message from the resource bundle for the given key. The message may contain
     * variables that will be substituted with the given arguments. Variables have the format:</p>
     * <dir> This message has two variables: {0} and {1} </dir>
     *
     * @param key  The resource key
     * @param arg0 The argument to place in variable {0}
     * @param arg1 The argument to place in variable {1}
     * @param arg2 The argument to place in variable {1}
     * @return The message
     */
    public String getString(String key, Object arg0, Object arg1, Object arg2)
            throws MissingResourceException {
        Object[] o = new Object[3];
        o[0] = arg0;
        o[1] = arg1;
        o[2] = arg2;
        return getString(key, o);
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
    public String getString(String key, Object[] array) throws MissingResourceException {
        String msg = null;
        if (resourceProperties != null) {
            msg = resourceProperties.getProperty(key);
        }

        if (msg == null) {
            throw new MissingResourceException("Cannot find resource key \"" + key +
                    "\" in base name " + basePropertyFileName,
                                               basePropertyFileName, key);
        }

        msg = MessageFormat.format(msg, array);
        return msg;
    }

    protected void loadProperties(String basename, ClassLoader loader, Locale locale,
                                  Locale defaultLocale)
            throws MissingResourceException {
        // Check the cache first
        String loaderName = "";
        if (loader != null) {
            loaderName = ":" + loader.hashCode();
        }
        String cacheKey = basename + ":" + locale + ":" + defaultLocale + loaderName;
        Properties p = (Properties)propertyCache.get(cacheKey);
        basePropertyFileName = basename + PROPERTY_EXT;

        if (p == null) {
            // The properties were not found in the cache. Search the given locale
            // first
            if (locale != null) {
                p = loadProperties(basename, loader, locale, p);
            }

            // Search the default locale
            if (defaultLocale != null) {
                p = loadProperties(basename, loader, defaultLocale, p);
            }

            // Search for the basename
            p = merge(p, loadProperties(basePropertyFileName, loader));

            if (p == null) {
                throw new MissingResourceException("Cannot find resource for base name " +
                        basePropertyFileName, basePropertyFileName, "");
            }

            // Cache the properties
            propertyCache.put(cacheKey, p);

        }

        resourceProperties = p;
    }

    protected Properties loadProperties(String basename, ClassLoader loader, Locale locale,
                                        Properties props) {

        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        if (variant != null) {
            if (variant.trim().length() == 0) {
                variant = null;
            }
        }

        if (language != null) {

            if (country != null) {

                if (variant != null) {
                    props = merge(props, loadProperties(
                            basename + "_" + language + "_" + country + "_" + variant +
                                    PROPERTY_EXT, loader));
                }
                props = merge(props, loadProperties(basename + "_" + language + "_" + country +
                        PROPERTY_EXT, loader));
            }
            props = merge(props, loadProperties(basename + "_" + language + PROPERTY_EXT, loader));
        }
        return props;
    }

    protected Properties loadProperties(String resname, ClassLoader loader) {
        Properties props = null;

        // Attempt to open and load the properties
        InputStream in = null;
        try {
            if (loader != null) {
                in = loader.getResourceAsStream(resname);
            }

            // Either we're using the system class loader or we didn't find the
            // resource using the given class loader
            if (in == null) {
                in = ClassLoader.getSystemResourceAsStream(resname);
            }
            if (in != null) {
                props = new Properties();
                try {
                    props.load(in);
                } catch (IOException ex) {
                    // On error, clear the props
                    props = null;
                }
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ex) {
                    // Ignore error on close
                }
            }
        }
        return props;
    }

    /** Merge two Properties objects */
    protected Properties merge(Properties p1, Properties p2) {
        if ((p1 == null) &&
                (p2 == null)) {
            return null;
        } else if (p1 == null) {
            return p2;
        } else if (p2 == null) {
            return p1;
        }

        // Now merge. p1 takes precedence
        Enumeration enumeration = p2.keys();
        while (enumeration.hasMoreElements()) {
            String key = (String)enumeration.nextElement();
            if (p1.getProperty(key) == null) {
                p1.put(key, p2.getProperty(key));
            }
        }

        return p1;
    }

    /** Get the underlying properties */
    public Properties getProperties() {
        return resourceProperties;
    }

    // STATIC ACCESSORS

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param caller The calling object, used to get the package name and class loader
     * @param key    The resource key
     * @return The formatted message
     */
    public static String getString(Object caller, String key)
            throws MissingResourceException {
        return getMessage(caller, BASE_NAME, null, key, null);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param caller The calling object, used to get the package name and class loader
     * @param key    The resource key
     * @param arg0   The argument to place in variable {0}
     * @return The formatted message
     */
    public static String getString(Object caller, String key, Object arg0)
            throws MissingResourceException {
        Object[] o = new Object[1];
        o[0] = arg0;
        return getMessage(caller, BASE_NAME, null, key, o);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param caller The calling object, used to get the package name and class loader
     * @param key    The resource key
     * @param arg0   The argument to place in variable {0}
     * @param arg1   The argument to place in variable {1}
     * @return The formatted message
     */
    public static String getString(Object caller, String key, Object arg0, Object arg1)
            throws MissingResourceException {
        Object[] o = new Object[2];
        o[0] = arg0;
        o[1] = arg1;
        return getMessage(caller, BASE_NAME, null, key, o);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param caller The calling object, used to get the package name and class loader
     * @param key    The resource key
     * @param arg0   The argument to place in variable {0}
     * @param arg1   The argument to place in variable {1}
     * @param arg2   The argument to place in variable {2}
     * @return The formatted message
     */
    public static String getString(Object caller, String key, Object arg0, Object arg1, Object arg2)
            throws MissingResourceException {
        Object[] o = new Object[3];
        o[0] = arg0;
        o[1] = arg1;
        o[2] = arg2;
        return getMessage(caller, BASE_NAME, null, key, o);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param caller The calling object, used to get the package name and class loader
     * @param key    The resource key
     * @param arg0   The argument to place in variable {0}
     * @param arg1   The argument to place in variable {1}
     * @param arg2   The argument to place in variable {2}
     * @param arg3   The argument to place in variable {3}
     * @return The formatted message
     */
    public static String getString(Object caller, String key, Object arg0, Object arg1, Object arg2,
                                   Object arg3)
            throws MissingResourceException {
        Object[] o = new Object[4];
        o[0] = arg0;
        o[1] = arg1;
        o[2] = arg2;
        o[3] = arg3;
        return getMessage(caller, BASE_NAME, null, key, o);
    }


    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param caller The calling object, used to get the package name and class loader
     * @param key    The resource key
     * @param arg0   The argument to place in variable {0}
     * @param arg1   The argument to place in variable {1}
     * @param arg2   The argument to place in variable {2}
     * @param arg3   The argument to place in variable {3}
     * @param arg4   The argument to place in variable {4}
     * @return Returns the formatted message.
     */
    public static String getString(Object caller, String key, Object arg0, Object arg1, Object arg2,
                                   Object arg3, Object arg4)
            throws MissingResourceException {
        Object[] o = new Object[5];
        o[0] = arg0;
        o[1] = arg1;
        o[2] = arg2;
        o[3] = arg3;
        o[4] = arg4;
        return getMessage(caller, BASE_NAME, null, key, o);
    }


    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param caller The calling object, used to get the package name and class loader
     * @param key    The resource key
     * @param args   An array of objects to place in corresponding variables
     * @return Returns the formatted message.
     */
    public static String getString(Object caller, String key, Object[] args)
            throws MissingResourceException {
        return getMessage(caller, BASE_NAME, null, key, args);
    }


    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param caller The calling object, used to get the package name and class loader
     * @param locale The locale
     * @param key    The resource key
     * @return The formatted message
     */
    public static String getString(Object caller, Locale locale, String key)
            throws MissingResourceException {
        return getMessage(caller, BASE_NAME, locale, key, null);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param caller The calling object, used to get the package name and class loader
     * @param locale The locale
     * @param key    The resource key
     * @param arg0   The argument to place in variable {0}
     * @return The formatted message
     */
    public static String getString(Object caller, Locale locale, String key, Object arg0)
            throws MissingResourceException {
        Object[] o = new Object[1];
        o[0] = arg0;
        return getMessage(caller, BASE_NAME, locale, key, o);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param caller The calling object, used to get the package name and class loader
     * @param locale The locale
     * @param key    The resource key
     * @param arg0   The argument to place in variable {0}
     * @param arg1   The argument to place in variable {1}
     * @return The formatted message
     */
    public static String getString(Object caller, Locale locale, String key, Object arg0,
                                   Object arg1)
            throws MissingResourceException {
        Object[] o = new Object[2];
        o[0] = arg0;
        o[1] = arg1;
        return getMessage(caller, BASE_NAME, locale, key, o);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param caller The calling object, used to get the package name and class loader
     * @param locale The locale
     * @param key    The resource key
     * @param arg0   The argument to place in variable {0}
     * @param arg1   The argument to place in variable {1}
     * @param arg2   The argument to place in variable {2}
     * @return The formatted message
     */
    public static String getString(Object caller, Locale locale, String key, Object arg0,
                                   Object arg1, Object arg2)
            throws MissingResourceException {
        Object[] o = new Object[3];
        o[0] = arg0;
        o[1] = arg1;
        o[2] = arg2;
        return getMessage(caller, BASE_NAME, locale, key, o);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param caller The calling object, used to get the package name and class loader
     * @param locale The locale
     * @param key    The resource key
     * @param arg0   The argument to place in variable {0}
     * @param arg1   The argument to place in variable {1}
     * @param arg2   The argument to place in variable {2}
     * @param arg3   The argument to place in variable {3}
     * @return The formatted message
     */
    public static String getString(Object caller, Locale locale, String key, Object arg0,
                                   Object arg1, Object arg2, Object arg3)
            throws MissingResourceException {
        Object[] o = new Object[4];
        o[0] = arg0;
        o[1] = arg1;
        o[2] = arg2;
        o[3] = arg3;
        return getMessage(caller, BASE_NAME, locale, key, o);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param caller The calling object, used to get the package name and class loader
     * @param locale The locale
     * @param key    The resource key
     * @param arg0   The argument to place in variable {0}
     * @param arg1   The argument to place in variable {1}
     * @param arg2   The argument to place in variable {2}
     * @param arg3   The argument to place in variable {3}
     * @return Returns the formatted message.
     */
    public static String getString(Object caller, Locale locale, String key, Object arg0,
                                   Object arg1, Object arg2, Object arg3, Object arg4)
            throws MissingResourceException {
        Object[] o = new Object[5];
        o[0] = arg0;
        o[1] = arg1;
        o[2] = arg2;
        o[3] = arg3;
        o[4] = arg4;
        return getMessage(caller, BASE_NAME, locale, key, o);
    }

    /**
     * Get a message from resource.properties from the package of the given object.
     *
     * @param caller The calling object, used to get the package name and class loader
     * @param locale The locale
     * @param key    The resource key
     * @param args   An array of objects to place in corresponding variables
     * @return Returns the formatted message.
     */
    public static String getString(Object caller, Locale locale, String key, Object[] args)
            throws MissingResourceException {
        return getMessage(caller, BASE_NAME, locale, key, args);
    }

    // Workhorse that does the resource loading and key lookup
    public static String getMessage(Object caller, String basename, Locale locale, String key,
                                    Object[] args)
            throws MissingResourceException {
        String msg = null;
        MissingResourceException firstEx = null;
        String fullName = null;
        Class curClass = null;
        boolean didNull = false;

        if (caller != null) {
            if (caller instanceof Class)
                curClass = (Class)caller;
            else
                curClass = caller.getClass();
        }

        while (msg == null) {

            // Get the full name of the resource
            if (curClass != null) {

                // Create the full basename
                String pkgName = curClass.getName();

                int pos = pkgName.lastIndexOf(".");
                if (pos > 0) {
                    fullName = pkgName.substring(0, pos + 1).replace('.', '/') + basename;
                } else {
                    fullName = basename;
                }
            } else {
                fullName = basename;
            }

            try {
                RB rb = new RB(caller, fullName, locale);
                msg = rb.getString(key, args);
            } catch (MissingResourceException ex) {
                if (curClass == null) {
                    throw ex;
                }

                // Save the first exception
                if (firstEx == null) {
                    firstEx = ex;
                }

                // Get the superclass
                curClass = curClass.getSuperclass();
                if (curClass == null) {
                    if (didNull)
                        throw firstEx;
                    didNull = true;
                    caller = null;
                } else {
                    String cname = curClass.getName();
                    if (cname.startsWith("java.") ||
                            cname.startsWith("javax.")) {
                        if (didNull)
                            throw firstEx;
                        didNull = true;
                        caller = null;
                        curClass = null;
                    }
                }
            }

        }
        return msg;
    }

    /** Clears the internal cache. */
    public static void clearCache() {
        propertyCache.clear();
    }
}
