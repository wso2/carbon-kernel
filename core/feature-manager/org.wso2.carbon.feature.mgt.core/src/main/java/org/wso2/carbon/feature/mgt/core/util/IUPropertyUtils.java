/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.wso2.carbon.feature.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.internal.p2.metadata.InstallableUnit;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.*;
import org.eclipse.equinox.p2.query.Collector;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.wso2.carbon.feature.mgt.core.ProvisioningException;
import org.wso2.carbon.feature.mgt.core.internal.ServiceHolder;
import org.eclipse.equinox.p2.metadata.ILicense;

import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class IUPropertyUtils {

    private static Log log = LogFactory.getLog(IUPropertyUtils.class);

    // TODO: these constants should come from API, eg. IInstallableUnit or ???
    static final Locale DEFAULT_LOCALE = new Locale("df", "LT"); //$NON-NLS-1$//$NON-NLS-2$
    static final String NAMESPACE_IU_LOCALIZATION = "org.eclipse.equinox.p2.localization"; //$NON-NLS-1$

    // Cache the IU fragments that provide localizations for a given locale.
    //    map: locale => soft reference to a collector
    private static Map<Locale,SoftReference<Collection>> LocaleCollectionCache = new HashMap<Locale,
            SoftReference<Collection>>(2);

    // Get the license in the default locale.
    public static ILicense getLicense(IInstallableUnit iu) throws URISyntaxException {
        //return getLicense(iu, getCurrentLocale());
    	return getLicense(iu, DEFAULT_LOCALE);
    }

    // Get the copyright in the default locale.
    public static ICopyright getCopyright(IInstallableUnit iu) {
        //return getCopyright(iu, getCurrentLocale());
    	return getCopyright(iu, DEFAULT_LOCALE);
    }

    // Get a property in the default locale
    public static String getIUProperty(IInstallableUnit iu, String propertyKey) {
        //return getIUProperty(iu, propertyKey, getCurrentLocale());
    	return getIUProperty(iu, propertyKey,DEFAULT_LOCALE);
    }

    public static ILicense getLicense(IInstallableUnit iu, Locale locale) throws URISyntaxException {
    	ILicense iLicense =  null;
    	
    	Collection<ILicense> licenses = iu.getLicenses();
    	if(licenses.size() != 0){
    		ILicense[] licenseArray = licenses.toArray(new ILicense[licenses.size()]);		
    		ILicense license = licenseArray[0];
    		String body = (license != null ? license.getBody() : null);
            if (body == null || body.length() <= 1 || body.charAt(0) != '%') {
                return null;
            } else {
            	final String actualKey = body.substring(1); // Strip off the %
                //String localizedKey = makeLocalizedKey(actualKey, locale.toString());
                body = getLocalizedIUProperty(iu, actualKey, locale);
                if (body != null) {
                	iLicense = MetadataFactory.createLicense(license.getLocation(), body);
                } else {
                	return null;
                }
                
            }
    	}
    	return iLicense;
    }

    public static ICopyright getCopyright(IInstallableUnit iu, Locale locale) {
        ICopyright copyright = iu.getCopyright();
        String body = (copyright != null ? copyright.getBody() : null);
        if (body == null || body.length() <= 1 || body.charAt(0) != '%') {
            return null;
        }
        final String actualKey = body.substring(1); // Strip off the %
        body = getLocalizedIUProperty(iu, actualKey, locale);
        if (body != null) {
        	 return MetadataFactory.createCopyright(copyright.getLocation(), body);
        } else {
        	return null;
        }
    }

    public static String getIUProperty(IInstallableUnit iu, String propertyKey, Locale locale) {
        String value = iu.getProperty(propertyKey);
        if (value == null || value.length() <= 1 || value.charAt(0) != '%') {
            return value;
        }
        // else have a localizable property
        final String actualKey = value.substring(1); // Strip off the %
        return getLocalizedIUProperty(iu, actualKey, locale);
    }

    private static String getLocalizedIUProperty(IInstallableUnit iu, String actualKey, Locale locale) {

            String localizedKey = makeLocalizedKey(actualKey, locale.toString());
            return iu.getProperty(localizedKey, locale.toString());
           /* String localizedValue = null;

            //first check for a cached localized value
            if (iu instanceof InstallableUnit) {
                localizedValue = ((InstallableUnit) iu).getLocalizedProperty(localizedKey);
            }
            //next check if the localized value is stored in the same IU (common case)
            if (localizedValue == null) {
                localizedValue = iu.getProperty(localizedKey);
            }
            if (localizedValue != null) {
                return localizedValue;
            }

            final List locales = buildLocaleVariants(locale);
            final IInstallableUnit theUnit = iu;

            Collector localizationFragments = getLocalizationFragments(locale, locales);

            Collector hostLocalizationCollector = new Collector() {
                public boolean accept(Object object) {
                    boolean haveHost = false;
                    if (object instanceof IInstallableUnitFragment) {
                        IInstallableUnitFragment fragment = (IInstallableUnitFragment) object;
                        IRequiredCapability[] hosts = fragment.getHost();
                        for (int i = 0; i < hosts.length; i++) {
                            IRequiredCapability nextHost = hosts[i];
                            if (IInstallableUnit.NAMESPACE_IU_ID.equals(nextHost.getNamespace()) && //
                                    theUnit.getId().equals(nextHost.getName()) && //
                                    nextHost.getRange() != null && //
                                    nextHost.getRange().isIncluded(theUnit.getVersion())) {
                                haveHost = true;
                                break;
                            }
                        }
                    }
                    return (haveHost ? super.accept(object) : true);
                }
            };

            IUPropertyQuery iuQuery = new IUPropertyQuery(IInstallableUnit.PROP_TYPE_FRAGMENT, "true"); //$NON-NLS-1$
            Collector collected = iuQuery.perform(localizationFragments.iterator(), hostLocalizationCollector);

            if (!collected.isEmpty()) {
                String translation = null;
                for (Iterator iter = collected.iterator(); iter.hasNext() && translation == null;) {
                    IInstallableUnit localizationIU = (IInstallableUnit) iter.next();
                    for (Iterator jter = locales.iterator(); jter.hasNext();) {
                        String localeKey = makeLocalizedKey(actualKey, (String) jter.next());
                        translation = localizationIU.getProperty(localeKey);
                        if (translation != null) {
                            return cacheResult(iu, localizedKey, translation);
                        }
                    }
                }
            }

            for (Iterator iter = locales.iterator(); iter.hasNext();) {
                String nextLocale = (String) iter.next();
                String localeKey = makeLocalizedKey(actualKey, nextLocale);
                String nextValue = iu.getProperty(localeKey);
                if (nextValue != null) {
                    return cacheResult(iu, localizedKey, nextValue);
                }
            }

            return cacheResult(iu, localizedKey, actualKey);*/
          //return "testValue";
        }

    	
    
    /**
     * Cache the translated property value to optimize future retrieval of the same value.
     * Currently we just cache on the installable unit object in memory. In future
     * we should push support for localized property retrieval into IInstallableUnit
     * so we aren't required to reach around the API here.
     */
    private static String cacheResult(IInstallableUnit iu, String localizedKey, String localizedValue) {
        if (iu instanceof InstallableUnit) {
            ((InstallableUnit) iu).setLocalizedProperty(localizedKey, localizedValue);
        }
        return localizedValue;
    }

    /**
     * Collects the installable unit fragments that contain locale data for the given locales.
     */
    private static synchronized Collection getLocalizationFragments(Locale locale, List localeVariants) {
        SoftReference collectorRef = (SoftReference) LocaleCollectionCache.get(locale);
        if (collectorRef != null) {
            Collection cached = (Collection)collectorRef.get();
            if (cached != null) {
                return cached;
            }
        }

        final List locales = localeVariants;

        Collector localeFragmentCollector = new Collector() {
            public boolean accept(Object object) {
                boolean haveLocale = false;
                if (object instanceof IInstallableUnitFragment) {
                    IInstallableUnitFragment fragment = (IInstallableUnitFragment) object;
                    Collection<IProvidedCapability> providedCapabilities = fragment.getProvidedCapabilities();
                    for (IProvidedCapability providedCapability : providedCapabilities) {
                        IProvidedCapability nextProvide = providedCapability;
                        if (NAMESPACE_IU_LOCALIZATION.equals(nextProvide.getNamespace())) {
                            String providedLocale = nextProvide.getName();
                            if (providedLocale != null) {
                                for (Iterator iter = locales.iterator(); iter.hasNext();) {
                                    if (providedLocale.equals(iter.next())) {
                                        haveLocale = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                return (haveLocale ? super.accept(object) : true);
            }
        };

        //Due to performance problems we restrict locale lookup to the current profile (see bug 233958)
        IProfileRegistry profileRegistry = null;
        try {
            profileRegistry = (IProfileRegistry) ServiceHolder.getProfileRegistry();
        } catch (ProvisioningException e) {
            log.warn("Profile registry unavailable. Default language will be used.");
            return Collections.emptySet();
        }

        IProfile profile = profileRegistry.getProfile(IProfileRegistry.SELF);
        if (profile == null) {
            log.warn("Profile unavailable. Default language will be used.");
            return Collections.emptySet();
        }
        Collection collection = profile.query(QueryUtil.createIUPropertyQuery(
                MetadataFactory.InstallableUnitDescription.PROP_TYPE_FRAGMENT, "true"), null).toUnmodifiableSet();
        LocaleCollectionCache.put(locale, new SoftReference(collection));
        return collection;
    }

    /**
     */
    private static List buildLocaleVariants(Locale locale) {
        String nl = locale.toString();
        ArrayList result = new ArrayList(4);
        int lastSeparator;
        while (true) {
            result.add(nl);
            lastSeparator = nl.lastIndexOf('_');
            if (lastSeparator == -1) {
                break;
            }
            nl = nl.substring(0, lastSeparator);
        }
        // Add the default locale (most general)
        result.add(DEFAULT_LOCALE.toString());
        return result;
    }

    private static String makeLocalizedKey(String actualKey, String localeImage) {
        return localeImage + '.' + actualKey;
    }

    private static Locale getCurrentLocale() {
        return Locale.getDefault();
    }
}

