package org.wso2.carbon.http.client.cache;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.http.client.ClientUtils;

public class HttpClientCacheTest {

    private static final int ACCESS_TIMEOUT = 1000;

    @Test(description = " Test case for cache put and get")
    public void cachePutAndGetTest() {
        HttpClientCache cache = new HttpClientCache(5, ACCESS_TIMEOUT);

        cache.put("key", ClientUtils.createClient());
        Assert.assertNotNull(cache.get("key"));
    }

    @Test(description = " Test case for cache put and get with loader")
    public void cachePutAndGetWithLoaderTest() {
        HttpClientCache cache = new HttpClientCache(5, ACCESS_TIMEOUT);

        Assert.assertNotNull(cache.get("key", ClientUtils::createClient));
    }

    @Test(description = " Test case for cache eviction")
    public void cacheEvictionTest() throws InterruptedException {
        HttpClientCache cache = new HttpClientCache(5, ACCESS_TIMEOUT);

        cache.put("key", ClientUtils.createClient());
        Thread.sleep(ACCESS_TIMEOUT / 2);
        Assert.assertNotNull(cache.get("key"));
        Thread.sleep(ACCESS_TIMEOUT * 5);
//        cache.cleanUp();
        Assert.assertNull(cache.get("key")); //return null
    }

    @Test(description = " Test case for cache eviction with loader")
    public void cacheEvictionLoaderTest() throws InterruptedException {
        HttpClientCache cache = new HttpClientCache(5, ACCESS_TIMEOUT);

        cache.put("key", ClientUtils.createClient());
        Thread.sleep(ACCESS_TIMEOUT / 2);
        Assert.assertNotNull(cache.get("key"));
        Thread.sleep(ACCESS_TIMEOUT * 5);
        Assert.assertNotNull(cache.get("key", ClientUtils::createClient)); //return null
    }

}
