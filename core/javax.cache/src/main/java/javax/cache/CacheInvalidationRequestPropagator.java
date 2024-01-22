package javax.cache;

import org.wso2.carbon.caching.impl.clustering.ClusterCacheInvalidationRequest;

public interface CacheInvalidationRequestPropagator {

    void propagate(ClusterCacheInvalidationRequest invalidationRequest);
}
