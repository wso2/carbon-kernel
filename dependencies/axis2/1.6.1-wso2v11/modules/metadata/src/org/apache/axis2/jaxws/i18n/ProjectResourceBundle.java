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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <p>Wrapper class for resource bundles. Property files are used to store resource strings, which
 * are the only types of resources available. Property files can inherit properties from other files
 * so that a base property file can be used and a small number of properties can be over-ridden by
 * another property file. For example you may create an english version of a resource file named
 * "resource.properties". You then decide that the British English version of all of the properties
 * except one are the same, so there is no need to redefine all of the properties in
 * "resource_en_GB", just the one that is different.</p> <p>The basename is the name of the property
 * file without the ".properties" extension.</p> <p>Properties will be cached for performance.<p>
 * <p>Property values stored in the property files can also contain dynamic variables. Any dynamic
 * variable defined in PropertiesUtil.getVariableValue() can be used (such as {date}), as well as
 * arguments in the form {0}, {1}, etc. Argument values are specified in the various overloaded
 * getString() methods.</p>
 */
public class ProjectResourceBundle extends ResourceBundle {
    private static final Log log = LogFactory.getLog(ProjectResourceBundle.class);


    // The static cache of ResourceBundles.
    // The key is the 'basename + locale + default locale'
    // The element is a ResourceBundle object
    private static final Hashtable bundleCache = new Hashtable();

    private static final Locale defaultLocale = Locale.getDefault();

    private final ResourceBundle resourceBundle;
    private final String resourceName;


    protected Object handleGetObject(String key)
            throws MissingResourceException {
        if (log.isDebugEnabled()) {
            log.debug(this.toString() + "::handleGetObject(" + key + ")");
        }
        Object obj;
        try {
            obj = resourceBundle.getObject(key);
        } catch (MissingResourceException e) {
            /* catch missing resource, ignore, & return null
             * if this method doesn't return null, then parents
             * are not searched
             */
            obj = null;
        }
        return obj;
    }

    public Enumeration getKeys() {
        Enumeration myKeys = resourceBundle.getKeys();
        if (parent == null) {
            return myKeys;
        } else {
            final HashSet set = new HashSet();
            while (myKeys.hasMoreElements()) {
                set.add(myKeys.nextElement());
            }

            Enumeration pKeys = parent.getKeys();
            while (pKeys.hasMoreElements()) {
                set.add(pKeys.nextElement());
            }

            return new Enumeration() {
                private Iterator it = set.iterator();

                public boolean hasMoreElements() {
                    return it.hasNext();
                }

                public Object nextElement() {
                    return it.next();
                }
            };
        }
    }


    /**
     * Construct a new ProjectResourceBundle
     *
     * @param projectName  The name of the project to which the class belongs. It must be a proper
     *                     prefix of the caller's package.
     * @param packageName  The package name to further construct the basename.
     * @param resourceName The name of the resource without the ".properties" extension
     * @throws MissingResourceException if projectName is not a prefix of the caller's package name,
     *                                  or if the resource could not be found/loaded.
     */
    public static ProjectResourceBundle getBundle(String projectName,
                                                  String packageName,
                                                  String resourceName)
            throws MissingResourceException {
        return getBundle(projectName, packageName, resourceName, null, null, null);
    }

    /**
     * Construct a new ProjectResourceBundle
     *
     * @param projectName  The name of the project to which the class belongs. It must be a proper
     *                     prefix of the caller's package.
     * @param caller       The calling class.
     * @param resourceName The name of the resource without the ".properties" extension
     * @throws MissingResourceException if projectName is not a prefix of the caller's package name,
     *                                  or if the resource could not be found/loaded.
     */
    public static ProjectResourceBundle getBundle(String projectName,
                                                  Class caller,
                                                  String resourceName,
                                                  Locale locale)
            throws MissingResourceException {
        return getBundle(projectName,
                         caller,
                         resourceName,
                         locale,
                         null);
    }

    /**
     * Construct a new ProjectResourceBundle
     *
     * @param projectName  The name of the project to which the class belongs. It must be a proper
     *                     prefix of the caller's package.
     * @param packageName  The package name to construct base name.
     * @param resourceName The name of the resource without the ".properties" extension
     * @param locale       The locale
     * @throws MissingResourceException if projectName is not a prefix of the caller's package name,
     *                                  or if the resource could not be found/loaded.
     */
    public static ProjectResourceBundle getBundle(String projectName,
                                                  String packageName,
                                                  String resourceName,
                                                  Locale locale,
                                                  ClassLoader loader)
            throws MissingResourceException {
        return getBundle(projectName, packageName, resourceName, locale, loader, null);
    }

    /**
     * Construct a new ProjectResourceBundle
     *
     * @param projectName   The name of the project to which the class belongs. It must be a proper
     *                      prefix of the caller's package.
     * @param caller        The calling class. This is used to get the package name to further
     *                      construct the basename as well as to get the proper ClassLoader.
     * @param resourceName  The name of the resource without the ".properties" extension
     * @param locale        The locale
     * @param extendsBundle If non-null, then this ExtendMessages will default to extendsBundle.
     * @throws MissingResourceException if projectName is not a prefix of the caller's package name,
     *                                  or if the resource could not be found/loaded.
     */
    public static ProjectResourceBundle getBundle(String projectName,
                                                  Class caller,
                                                  String resourceName,
                                                  Locale locale,
                                                  ResourceBundle extendsBundle)
            throws MissingResourceException {
        return getBundle(projectName,
                         getPackage(caller.getClass().getName()),
                         resourceName,
                         locale,
                         caller.getClass().getClassLoader(),
                         extendsBundle);
    }

    /**
     * Construct a new ProjectResourceBundle
     *
     * @param projectName   The name of the project to which the class belongs. It must be a proper
     *                      prefix of the caller's package.
     * @param packageName   The package name to further construct the basename.
     * @param resourceName  The name of the resource without the ".properties" extension
     * @param locale        The locale
     * @param extendsBundle If non-null, then this ExtendMessages will default to extendsBundle.
     * @throws MissingResourceException if projectName is not a prefix of the caller's package name,
     *                                  or if the resource could not be found/loaded.
     */
    public static ProjectResourceBundle getBundle(String projectName,
                                                  String packageName,
                                                  String resourceName,
                                                  Locale locale,
                                                  ClassLoader loader,
                                                  ResourceBundle extendsBundle)
            throws MissingResourceException {
        if (log.isDebugEnabled()) {
            log.debug("getBundle(" + projectName + ","
                    + packageName + ","
                    + resourceName + ","
                    + String.valueOf(locale) + ",...)");
        }

        Context context = new Context();
        context.setLocale(locale);
        context.setLoader(loader);
        context.setProjectName(projectName);
        context.setResourceName(resourceName);
        context.setParentBundle(extendsBundle);

        packageName = context.validate(packageName);

        ProjectResourceBundle bundle = null;
        try {
            bundle = getBundle(context, packageName);
        } catch (RuntimeException e) {
            log.debug("Exception: ", e);
            throw e;
        }

        if (bundle == null) {
            throw new MissingResourceException("Cannot find resource '" +
                    packageName + '.' + resourceName + "'",
                                               resourceName, "");
        }

        return bundle;
    }

    /**
     * get bundle... - check cache - try up hierarchy - if at top of hierarchy, use (link to)
     * context.getParentBundle()
     */
    private static synchronized ProjectResourceBundle getBundle(Context context, String packageName)
            throws MissingResourceException {
        String cacheKey = context.getCacheKey(packageName);

        ProjectResourceBundle prb = (ProjectResourceBundle)bundleCache.get(cacheKey);

        if (prb == null) {
            String name = packageName + '.' + context.getResourceName();
            ResourceBundle rb = context.loadBundle(packageName);
            ResourceBundle parent = context.getParentBundle(packageName);

            if (rb != null) {
                prb = new ProjectResourceBundle(name, rb);
                prb.setParent(parent);
                if (log.isDebugEnabled()) {
                    log.debug("Created " + prb + ", linked to parent " + String.valueOf(parent));
                }
            } else {
                if (parent != null) {
                    if (parent instanceof ProjectResourceBundle) {
                        prb = (ProjectResourceBundle)parent;
                    } else {
                        prb = new ProjectResourceBundle(name, parent);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Root package not found, cross link to " + parent);
                    }
                }
            }

            if (prb != null) {
                // Cache the resource
                bundleCache.put(cacheKey, prb);
            }
        }

        return prb;
    }

    private static String getPackage(String name) {
        return name.substring(0, name.lastIndexOf('.')).intern();
    }

    /** Construct a new ProjectResourceBundle */
    private ProjectResourceBundle(String name, ResourceBundle bundle)
            throws MissingResourceException {
        this.resourceBundle = bundle;
        this.resourceName = name;
    }

    public String getResourceName() {
        return resourceName;
    }

    /** Clears the internal cache */
//    public static void clearCache() {
//        bundleCache.clear();
//    }
    public String toString() {
        return resourceName;
    }


    private static class Context {
        private Locale _locale;
        private ClassLoader _loader;
        private String _projectName;
        private String _resourceName;
        private ResourceBundle _parent;

        void setLocale(Locale l) {
            /* 1. Docs indicate that if locale is not specified,
             *    then the default local is used in it's place.
             * 2. A null value for locale is invalid.
             * 
             * Therefore, default...
             */
            _locale = (l == null) ? defaultLocale : l;
        }

        void setLoader(ClassLoader l) {
            _loader = (l != null) ? l : this.getClass().getClassLoader();
            // START FIX: http://nagoya.apache.org/bugzilla/show_bug.cgi?id=16868
            if (_loader == null) {
                _loader = ClassLoader.getSystemClassLoader();
            }
            // END FIX: http://nagoya.apache.org/bugzilla/show_bug.cgi?id=16868
        }

        void setProjectName(String name) {
            _projectName = name.intern();
        }

        void setResourceName(String name) {
            _resourceName = name.intern();
        }

        void setParentBundle(ResourceBundle b) {
            _parent = b;
        }

        Locale getLocale() {
            return _locale;
        }

        ClassLoader getLoader() {
            return _loader;
        }

        String getProjectName() {
            return _projectName;
        }

        String getResourceName() {
            return _resourceName;
        }

        ResourceBundle getParentBundle() {
            return _parent;
        }

        String getCacheKey(String packageName) {
            String loaderName = (_loader == null) ? "" : (":" + _loader.hashCode());
            return packageName + "." + _resourceName + ":" + _locale + ":" + defaultLocale +
                    loaderName;
        }

        ResourceBundle loadBundle(String packageName) {
            try {
                return ResourceBundle.getBundle(packageName + '.' + _resourceName,
                                                _locale,
                                                _loader);
            } catch (MissingResourceException e) {
                // Deliberately surpressing print stack.. just the string for info.
                log.debug("loadBundle: Ignoring MissingResourceException: " + e.getMessage());
            }
            return null;
        }

        ResourceBundle getParentBundle(String packageName) {
            ResourceBundle p;
            if (!packageName.equals(_projectName)) {
                p = getBundle(this, getPackage(packageName));
            } else {
                p = _parent;
                _parent = null;
            }
            return p;
        }

        String validate(String packageName)
                throws MissingResourceException {
            if (_projectName == null || _projectName.length() == 0) {
                log.debug("Project name not specified");
                throw new MissingResourceException("Project name not specified",
                                                   "", "");
            }

            if (packageName == null || packageName.length() == 0) {
                log.debug("Package name not specified");
                throw new MissingResourceException("Package not specified",
                                                   packageName, "");
            }
            packageName = packageName.intern();

            /* Ensure that project is a proper prefix of class.
            * Terminate project name with '.' to ensure proper match.
            */
            if (!packageName.equals(_projectName) && !packageName.startsWith(_projectName + '.')) {
                log.debug("Project not a prefix of Package");
                throw new MissingResourceException("Project '" + _projectName
                        + "' must be a prefix of Package '"
                        + packageName + "'",
                                                   packageName + '.' + _resourceName, "");
            }

            return packageName;
        }
    }
}
