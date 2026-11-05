/*
*  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
*
*  WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.hazelcast;

import com.hazelcast.instance.BuildInfoProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Forces a cluster-compatibility decision on every Hazelcast upgrade: the test fails when the loaded
 * Hazelcast major.minor differs from the last version that was reviewed against the cluster domain
 * suffix, so forgetting to re-check becomes a build failure instead of a crash during a rolling update.
 */
public class ClusterDomainSuffixTest {

    // Last Hazelcast major.minor checked for cluster compatibility against CLUSTER_DOMAIN_SUFFIX.
    private static final String VERIFIED_HAZELCAST_VERSION = "5.7";

    @Test
    public void testClusterDomainSuffixReviewedForHazelcastVersion() {

        String version = BuildInfoProvider.getBuildInfo().getVersion();
        String majorMinor = version.substring(0, version.indexOf('.', version.indexOf('.') + 1));
        Assert.assertEquals(majorMinor, VERIFIED_HAZELCAST_VERSION,
                "Hazelcast was upgraded to " + version + ". If it cannot form a cluster with " +
                VERIFIED_HAZELCAST_VERSION + ", bump CLUSTER_DOMAIN_SUFFIX in HazelcastClusteringAgent. " +
                "Then update VERIFIED_HAZELCAST_VERSION.");
    }
}
