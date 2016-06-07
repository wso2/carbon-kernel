package org.wso2.carbon.container.options;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;

public class CarbonDistributionExternalBundleOption implements Option {

    private MavenArtifactUrlReference mavenArtifactUrlReference;

    public CarbonDistributionExternalBundleOption(MavenArtifactUrlReference mavenArtifactUrlReference) {
        this.mavenArtifactUrlReference = mavenArtifactUrlReference;
    }

    public MavenArtifactUrlReference getMavenArtifactUrlReference() {
        return mavenArtifactUrlReference;
    }
}
