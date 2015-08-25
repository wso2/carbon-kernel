package org.wso2.maven.p2.generate.feature;

import org.apache.maven.plugin.MojoExecutionException;


public class ImportBundle extends Bundle {


    /**
     * Version Compatibility of the Bundle
     *
     * @parameter default-value="false"
     */
    private boolean exclude;

    /**
     * OSGI Symbolic name
     *
     * @parameter
     */
    private String bundleSymbolicName;

    /**
     * OSGI Version
     *
     * @parameter
     */
    private String bundleVersion;

    public static ImportBundle getBundle(String bundleDefinition) throws MojoExecutionException {
        return (ImportBundle) Bundle.getBundle(bundleDefinition, new ImportBundle());
    }

    public boolean isExclude() {
        return exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    public String getBundleSymbolicName() {
        return bundleSymbolicName;
    }

    public void setBundleSymbolicName(String bundleSymbolicName) {
        this.bundleSymbolicName = bundleSymbolicName;
    }

    public String getBundleVersion() {
        return bundleVersion;
    }

    public void setBundleVersion(String bundleVersion) {
        this.bundleVersion = bundleVersion;
    }
}
