/**
 *  Copyright (c) 2011 Terracotta, Inc.
 *  Copyright (c) 2011 Oracle and/or its affiliates.
 *
 *  All rights reserved. Use is subject to license terms.
 */

package javax.cache;

import org.wso2.carbon.caching.impl.DataHolder;

import javax.cache.spi.AnnotationProvider;
import javax.cache.spi.CachingProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A factory for creating CacheManagers using the SPI conventions in the JDK's {@link java.util.ServiceLoader}
 * <p/>
 * For a provider to be discovered, its jar must contain a resource called:
 * <pre>
 *   META-INF/services/javax.cache.spi.CachingProvider
 * </pre>
 * containing the class name implementing {@link javax.cache.spi.CachingProvider}
 * <p/>
 * For example, in the reference implementation the contents are:
 * <p/>
 * "javax.cache.implementation.RIServiceFactory"
 * <p/>
 * If more than one CachingProvider is found, getCacheManagerFactory will throw an exception
 * <p/>
 * Also keeps track of all CacheManagers created by the factory. Subsequent calls
 * to {@link #getCacheManager()} return the same CacheManager.
 *
 * @author Yannis Cosmadopoulos
 * @see java.util.ServiceLoader
 * @see javax.cache.spi.CachingProvider
 * @since 1.0
 */
public final class Caching {
    /**
     * The name of the default cache manager.
     * This is the name of the CacheManager returned when {@link #getCacheManager()} is invoked.
     * The default CacheManager is always created.
     */
    public static final String DEFAULT_CACHE_MANAGER_NAME = "__default__";

    /**
     * No public constructor as all methods are static.
     */
    private Caching() {
    }

    /**
     * Get the singleton CacheManagerFactory
     *
     * @return the cache manager factory
     * @throws IllegalStateException if no CachingProvider is found or if more than one CachingProvider is found
     */
    public static CacheManagerFactory getCacheManagerFactory() {
        return ServiceFactoryHolder.INSTANCE.getCachingProvider().getCacheManagerFactory();
    }

    /**
     * Get the default cache manager with the default classloader.
     * The default cache manager is named {@link #DEFAULT_CACHE_MANAGER_NAME}
     *
     * @return the default cache manager
     * @throws IllegalStateException if no CachingProvider is found or if more than one CachingProvider is found
     */
    public static CacheManager getCacheManager() {
        return getCacheManager(DEFAULT_CACHE_MANAGER_NAME);
    }

    /**
     * Get the default cache manager.
     * The default cache manager is named {@link #DEFAULT_CACHE_MANAGER_NAME}
     *
     * @param classLoader the ClassLoader that should be used in converting values into Java Objects. May be null.
     * @return the default cache manager
     * @throws IllegalStateException if no CachingProvider is found or if more than one CachingProvider is found
     */
    public static CacheManager getCacheManager(ClassLoader classLoader) {
        return getCacheManager(classLoader, DEFAULT_CACHE_MANAGER_NAME);
    }

    /**
     * Get a named cache manager using the default cache loader as specified by
     * the implementation.
     *
     * @param name the name of the cache manager
     * @return the named cache manager
     * @throws NullPointerException  if name is null
     * @throws IllegalStateException if no CachingProvider is found or if more than one CachingProvider is found
     */
    public static CacheManager getCacheManager(String name) {
        return getCacheManagerFactory().getCacheManager(name);
    }

    /**
     * Get a named cache manager.
     * <p/>
     * The first time a name is used, a new CacheManager is created.
     * Subsequent calls will return the same cache manager.
     * <p/>
     * During creation, the name of the CacheManager is passed through to {@link javax.cache.spi.CachingProvider}
     * so that an implementation it to concrete implementations may use it to point to a specific configuration
     * used to configure the CacheManager. This allows CacheManagers to have different configurations. For example,
     * one CacheManager might be configured for standalone operation and another might be configured to participate
     * in a cluster.
     * <p/>
     * Generally, It makes sense that a CacheManager is associated with a ClassLoader. I.e. all caches emanating
     * from the CacheManager, all code including key and value classes must be present in that ClassLoader.
     * <p/>
     * Secondly, the Caching may be in a different ClassLoader than the
     * CacheManager (i.e. the Caching may be shared in an application server setting).
     * <p/>
     * For this purpose a ClassLoader may be specified. If specified it will be used for all conversion between
     * values and Java Objects. While Java's in-built serialization may be used other schemes may also be used.
     * Either way the specified ClassLoader will be used.
     * <p/>
     * The name parameter may be used to associate a configuration with this CacheManager instance.
     *
     * @param classLoader the ClassLoader that should be used in converting values into Java Objects.
     * @param name        the name of this cache manager
     * @return the new cache manager
     * @throws NullPointerException  if classLoader or name is null
     * @throws IllegalStateException if no CachingProvider is found or if more than one CachingProvider is found
     */
    public static CacheManager getCacheManager(ClassLoader classLoader, String name) {
        return getCacheManagerFactory().getCacheManager(classLoader, name);
    }

    /**
     * Reclaims all resources obtained from this factory.
     * <p/>
     * All cache managers obtained from the factory are shutdown.
     * <p/>
     * Subsequent requests from this factory will return different cache managers than would have been obtained before
     * shutdown. So for example
     * <pre>
     *  CacheManager cacheManager = CacheFactory.getCacheManager();
     *  assertSame(cacheManager, CacheFactory.getCacheManager());
     *  CacheFactory.close();
     *  assertNotSame(cacheManager, CacheFactory.getCacheManager());
     * </pre>
     *
     * @throws javax.cache.CachingShutdownException
     *                               if any of the individual shutdowns failed
     * @throws IllegalStateException if no CachingProvider is found or if more than one CachingProvider is found
     */
    public static void close() throws CachingShutdownException {
        getCacheManagerFactory().close();
    }

    /**
     * Reclaims all resources for a ClassLoader from this factory.
     * <p/>
     * All cache managers linked to the specified CacheLoader obtained from the factory are shutdown.
     *
     * @param classLoader the class loader for which managers will be shut down
     * @return true if found, false otherwise
     * @throws javax.cache.CachingShutdownException
     *                               if any of the individual shutdowns failed
     * @throws IllegalStateException if no CachingProvider is found or if more than one CachingProvider is found
     */
    public static boolean close(ClassLoader classLoader) throws CachingShutdownException {
        return getCacheManagerFactory().close(classLoader);
    }

    /**
     * Reclaims all resources for a ClassLoader from this factory.
     * <p/>
     * the named cache manager obtained from the factory is closed.
     *
     * @param classLoader the class loader for which managers will be shut down
     * @param name        the name of the cache manager
     * @return true if found, false otherwise
     * @throws javax.cache.CachingShutdownException
     *                               if any of the individual shutdowns failed
     * @throws IllegalStateException if no CachingProvider is found or if more than one CachingProvider is found
     */
    public static boolean close(ClassLoader classLoader, String name) throws CachingShutdownException {
        return getCacheManagerFactory().close(classLoader, name);
    }

    /**
     * Indicates whether a optional feature is supported by this implementation
     *
     * @param optionalFeature the feature to check for
     * @return true if the feature is supported
     * @throws IllegalStateException if no CachingProvider is found or if more than one CachingProvider is found
     */
    public static boolean isSupported(OptionalFeature optionalFeature) {
        return ServiceFactoryHolder.INSTANCE.getCachingProvider().isSupported(optionalFeature);
    }

    /**
     * Indicates whether annotations are supported
     *
     * @return true if annotations are supported
     */
    public static boolean isAnnotationsSupported() {
        final AnnotationProvider annotationProvider = ServiceFactoryHolder.INSTANCE.getAnnotationProvider();
        return annotationProvider != null && annotationProvider.isSupported();
    }

    /**
     * Holds the ServiceFactory
     */
    private enum ServiceFactoryHolder {
        /**
         * The singleton.
         */
        INSTANCE;

        private List<CachingProvider> cachingProviders;
        private List<AnnotationProvider> annotationProviders;

        private ServiceFactoryHolder() {
            init();
        }

        private void init() {
            cachingProviders = AccessController.doPrivileged(new PrivilegedAction<List<CachingProvider>>() {

                @Override
                public List<CachingProvider> run() {
                    List<CachingProvider> result = new ArrayList<CachingProvider>();
                    result.add(DataHolder.getInstance().getCachingProvider());
                    return result;
                }
            });

            annotationProviders = AccessController.doPrivileged(new PrivilegedAction<List<AnnotationProvider>>() {

                @Override
                public List<AnnotationProvider> run() {
                    List<AnnotationProvider> result = new ArrayList<AnnotationProvider>();
                    result.add(DataHolder.getInstance().getAnnotationProvider());
                    return result;
                }
            });
        }

        //todo support multiple providers
        public CachingProvider getCachingProvider() {
            switch (cachingProviders.size()) {
                case 0:
                    init();
                    if (cachingProviders.size() == 0) {
                        throw new IllegalStateException("No CachingProviders found in classpath.");
                    } else {
                        return cachingProviders.get(0);
                    }
                case 1:
                    return cachingProviders.get(0);
                default:
                    throw new IllegalStateException("Multiple CachingProviders found in classpath." +
                                                    " There should only be one. CachingProviders found were: "
                                                    + createListOfClassNames(cachingProviders));
            }
        }

        //todo support multiple providers
        public AnnotationProvider getAnnotationProvider() {
            switch (annotationProviders.size()) {
                case 0:
                    return null;
                case 1:
                    return annotationProviders.get(0);
                default:
                    throw new IllegalStateException("Multiple AnnotationProviders found in classpath." +
                                                    " There should only be one. CachingProviders found were: "
                                                    + createListOfClassNames(annotationProviders));
            }
        }

        private static String createListOfClassNames(Collection<?> names) {
            if (names.isEmpty()) {
                return "<none>";
            } else {
                StringBuilder sb = new StringBuilder();
                for (Iterator<?> it = names.iterator(); it.hasNext(); ) {
                    Object o = it.next();
                    sb.append(o.getClass().getName());
                    if (it.hasNext()) {
                        sb.append(", ");
                    }
                }
                return sb.toString();
            }
        }
    }
}
