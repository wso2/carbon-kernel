# JSR 107 (JCache) Implementation for WSO2 Carbon

This Carbon component implements JCache 1.0 for Carbon.

For details about JCache, see the [specification](https://jcp.org/aboutJava/communityprocess/final/jsr107/index.html)

## Usage

Example usage:

### Creating a Cache

```java
CachingProvider provider = cachingService.getCachingProvider();
CacheManager cacheManager = provider.getCacheManager();
        
//configure the cache
MutableConfiguration<String, String> config = new MutableConfiguration<>();
config.setStoreByValue(true)
        .setTypes(String.class, String.class)
        .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(cacheExpiry))
        .setStatisticsEnabled(false);

//create the cache
Cache<String, String> = cacheManager.createCache("myCache", config);
```      
  
Note that cachingService in the above code segment is the org.wso2.carbon.caching.CarbonCachingService OSGi service   
  
### Getting the cache and performing operations on it
  
```java
String cacheName = "myCache";
String key = "k";
String value = "v";
Cache<String, String> cache = cacheManager.getCache(cacheName, String.class, String.class);

// Add to cache
cache.put(key, value);

// Get from cache
String val = cache.get(key);

// Remove from cache
cache.remove(key);
```  

For full source code, see [caching sample](samples/caching-sample).

For more details about the JCache APIs, please refer to the JCache 
[Java docs](http://www.javadoc.io/doc/javax.cache/cache-api/1.0.0)
