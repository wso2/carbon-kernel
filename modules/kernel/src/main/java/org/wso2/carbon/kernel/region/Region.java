package org.wso2.carbon.kernel.region;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import java.util.Collection;

public interface Region {

    String getId();

    void addBundle(Bundle bundle) throws BundleException;

    void removeBundle(Bundle bundle);

    Collection<Bundle> getBundles();
}
