/**
 *  Copyright (c) 2011-2013 Terracotta, Inc.
 *  Copyright (c) 2011-2013 Oracle and/or its affiliates.
 *
 *  All rights reserved. Use is subject to license terms.
 */

package javax.cache.spi;

import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.OptionalFeature;
import java.io.Closeable;
import java.net.URI;
import java.util.Properties;

/**
 * Provides mechanisms to create, request and later manage the life-cycle of
 * configured {@link CacheManager}s, identified by {@link java.net.URI}s and scoped by
 * {@link ClassLoader}s.
 * <p>
 * The meaning and semantics of the {@link java.net.URI} used to identify a
 * {@link CacheManager} is implementation dependent.  For applications to remain
 * implementation independent, they should avoid attempting to create {@link java.net.URI}s
 * and instead use those returned by {@link #getDefaultURI()}.
 *
 * @author Brian Oliver
 * @author Greg Luck
 * @since 1.0
 */
public interface CachingProvider extends Closeable {

  /**
   * Requests a {@link CacheManager} configured according to the implementation
   * specific {@link java.net.URI} be made available that uses the provided
   * {@link ClassLoader} for loading underlying classes.
   * <p>
   * Multiple calls to this method with the same {@link java.net.URI} and
   * {@link ClassLoader} must return the same {@link CacheManager} instance,
   * except if a previously returned {@link CacheManager} has been closed.
   * <p>
   * Properties are used in construction of a {@link CacheManager} and do not form
   * part of the identity of the CacheManager. i.e. if a second call is made to
   * with the same {@link java.net.URI} and {@link ClassLoader} but different properties,
   * the {@link CacheManager} created in the first call is returned.
   *
   * @param uri         an implementation specific URI for the
   *                    {@link CacheManager} (null means use
   *                    {@link #getDefaultURI()})
   * @param classLoader the {@link ClassLoader}  to use for the
   *                    {@link CacheManager} (null means use
   *                    {@link #getDefaultClassLoader()})
   * @param properties  the {@link java.util.Properties} for the {@link CachingProvider}
   *                    to create the {@link CacheManager} (null means no
   *                    implementation specific Properties are required)
   * @throws CacheException    when a {@link CacheManager} for the
   *                           specified arguments could not be produced
   * @throws SecurityException when the operation could not be performed
   *                           due to the current security settings
   */
  CacheManager getCacheManager(URI uri, ClassLoader classLoader,
                               Properties properties);

  /**
   * Obtains the default {@link ClassLoader} that will be used by the
   * {@link CachingProvider}.
   *
   * @return the default {@link ClassLoader} used by the {@link CachingProvider}
   */
  ClassLoader getDefaultClassLoader();

  /**
   * Obtains the default {@link java.net.URI} for the {@link CachingProvider}.
   * <p>
   * Use this method to obtain a suitable {@link java.net.URI} for the
   * {@link CachingProvider}.
   *
   * @return the default {@link java.net.URI} for the {@link CachingProvider}
   */
  URI getDefaultURI();

  /**
   * Obtains the default {@link java.util.Properties} for the {@link CachingProvider}.
   * <p>
   * Use this method to obtain suitable {@link java.util.Properties} for the
   * {@link CachingProvider}.
   *
   * @return the default {@link java.util.Properties} for the {@link CachingProvider}
   */
  Properties getDefaultProperties();

  /**
   * Requests a {@link CacheManager} configured according to the implementation
   * specific {@link java.net.URI} that uses the provided {@link ClassLoader} for loading
   * underlying classes.
   * <p>
   * Multiple calls to this method with the same {@link java.net.URI} and
   * {@link ClassLoader} must return the same {@link CacheManager} instance,
   * except if a previously returned {@link CacheManager} has been closed.
   *
   * @param uri         an implementation specific {@link java.net.URI} for the
   *                    {@link CacheManager} (null means
   *                    use {@link #getDefaultURI()})
   * @param classLoader the {@link ClassLoader}  to use for the
   *                    {@link CacheManager} (null means
   *                    use {@link #getDefaultClassLoader()})
   * @throws CacheException    when a {@link CacheManager} for the
   *                           specified arguments could not be produced
   * @throws SecurityException when the operation could not be performed
   *                           due to the current security settings
   */
  CacheManager getCacheManager(URI uri, ClassLoader classLoader);

  /**
   * Requests a {@link CacheManager} configured according to the
   * {@link #getDefaultURI()} and {@link #getDefaultProperties()} be made
   * available that using the {@link #getDefaultClassLoader()} for loading
   * underlying classes.
   * <p>
   * Multiple calls to this method must return the same {@link CacheManager}
   * instance, except if a previously returned {@link CacheManager} has been
   * closed.
   *
   * @throws SecurityException when the operation could not be performed
   *                           due to the current security settings
   */
  CacheManager getCacheManager();

  /**
   * Closes all of the {@link CacheManager} instances and associated resources
   * created and maintained by the {@link CachingProvider} across all
   * {@link ClassLoader}s.
   * <p>
   * After closing the {@link CachingProvider} will still be operational.  It
   * may still be used for acquiring {@link CacheManager} instances, though
   * those will now be new.
   *
   * @throws SecurityException when the operation could not be performed
   *                           due to the current security settings
   */
  void close();

  /**
   * Closes all {@link CacheManager} instances and associated resources created
   * by the {@link CachingProvider} using the specified {@link ClassLoader}.
   * <p>
   * After closing the {@link CachingProvider} will still be operational.  It
   * may still be used for acquiring {@link CacheManager} instances, though
   * those will now be new for the specified {@link ClassLoader} .
   *
   * @param classLoader the {@link ClassLoader}  to release
   * @throws SecurityException when the operation could not be performed
   *                           due to the current security settings
   */
  void close(ClassLoader classLoader);

  /**
   * Closes all {@link CacheManager} instances and associated resources created
   * by the {@link CachingProvider} for the specified {@link java.net.URI} and
   * {@link ClassLoader}.
   *
   * @param uri         the {@link java.net.URI} to release
   * @param classLoader the {@link ClassLoader}  to release
   * @throws SecurityException when the operation could not be performed
   *                           due to the current security settings
   */
  void close(URI uri, ClassLoader classLoader);

  /**
   * Determines whether an optional feature is supported by the
   * {@link CachingProvider}.
   *
   * @param optionalFeature the feature to check for
   * @return true if the feature is supported
   */
  boolean isSupported(OptionalFeature optionalFeature);
}
