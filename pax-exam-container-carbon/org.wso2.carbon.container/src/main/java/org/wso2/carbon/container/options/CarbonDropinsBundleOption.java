package org.wso2.carbon.container.options;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;

/**
 * Install any bundle to dropins directory.
 */
public class CarbonDropinsBundleOption implements Option {

    private MavenArtifactUrlReference mavenArtifactUrlReference;

    public CarbonDropinsBundleOption(MavenArtifactUrlReference mavenArtifactUrlReference) {
        this.mavenArtifactUrlReference = mavenArtifactUrlReference;
    }

    public MavenArtifactUrlReference getMavenArtifactUrlReference() {
        return mavenArtifactUrlReference;
    }
}
