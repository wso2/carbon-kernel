package org.wso2.maven.p2.generate.feature;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.wso2.maven.p2.generate.utils.P2Utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Bundle {

    public static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";
    public static final String BUNDLE_VERSION = "Bundle-Version";
    private static final Pattern OSGI_VERSION_PATTERN = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+(\\.[0-9A-Za-z_-]+)?");
    private static final Pattern ONLY_NUMBERS = Pattern.compile("[0-9]+");
    /**
     * Group Id of the Bundle
     *
     * @parameter
     * @required
     */
    private String groupId;
    /**
     * Artifact Id of the Bundle
     *
     * @parameter
     * @required
     */
    private String artifactId;
    /**
     * Version of the Bundle
     *
     * @parameter default-value=""
     */
    private String version;
    private Artifact artifact;
    private String bundleSymbolicName;
    private String bundleVersion;
    private String compatibility;

    protected static Bundle getBundle(String bundleDefinition, Bundle bundle) throws MojoExecutionException {
        String[] split = bundleDefinition.split(":");
        if (split.length > 1) {
            bundle.setGroupId(split[0]);
            bundle.setArtifactId(split[1]);

            String match = "equivalent";
            if (split.length > 2) {
                if (P2Utils.isMatchString(split[2])) {
                    match = split[2].toUpperCase();
                    if (split.length > 3) {
                        bundle.setVersion(split[3]);
                    }
                } else {
                    bundle.setVersion(split[2]);
                    if (split.length > 3) {
                        if (P2Utils.isMatchString(split[3])) {
                            match = split[3].toUpperCase();
                        }
                    }
                }
            }
            bundle.setCompatibility(match);
            return bundle;
        }
        throw new MojoExecutionException("Insufficient artifact information provided to determine the bundle: " +
                bundleDefinition);
    }

    public static Bundle getBundle(String bundleDefinition) throws MojoExecutionException {
        return getBundle(bundleDefinition, new Bundle());
    }

    public static String getOSGIVersion(String version) {
        String osgiVersion;

        // Matcher m = P_VERSION.matcher(version);
        // if (m.matches()) {
        // osgiVersion = m.group(1) + "." + m.group(3);
        // }

        /* TODO need a regexp guru here */

        Matcher m;

        /* if it's already OSGi compliant don't touch it */
        m = OSGI_VERSION_PATTERN.matcher(version);
        if (m.matches()) {
            return version;
        }

        osgiVersion = version;

        /* check for dated snapshot versions with only major or major and minor */
        Pattern DATED_SNAPSHOT = Pattern.compile("([0-9])(\\.([0-9]))?(\\.([0-9]))?\\-([0-9]{8}\\.[0-9]{6}\\-[0-9]*)");
        m = DATED_SNAPSHOT.matcher(osgiVersion);
        if (m.matches()) {
            String major = m.group(1);
            String minor = (m.group(3) != null) ? m.group(3) : "0";
            String service = (m.group(5) != null) ? m.group(5) : "0";
            String qualifier = m.group(6).replaceAll("-", "_").replaceAll("\\.", "_");
            osgiVersion = major + "." + minor + "." + service + "." + qualifier;
        }

        /* else transform first - to . and others to _ */
        osgiVersion = osgiVersion.replaceFirst("-", "\\.");
        osgiVersion = osgiVersion.replaceAll("-", "_");
        m = OSGI_VERSION_PATTERN.matcher(osgiVersion);
        if (m.matches()) {
            return osgiVersion;
        }

        /* remove dots in the middle of the qualifier */
        Pattern DOTS_IN_QUALIFIER = Pattern.compile("([0-9])(\\.[0-9])?\\.([0-9A-Za-z_-]+)\\.([0-9A-Za-z_-]+)");
        m = DOTS_IN_QUALIFIER.matcher(osgiVersion);
        if (m.matches()) {
            String s1 = m.group(1);
            String s2 = m.group(2);
            String s3 = m.group(3);
            String s4 = m.group(4);

            Matcher qualifierMatcher = ONLY_NUMBERS.matcher(s3);
            /*
             * if last portion before dot is only numbers then it's not in the middle of the
             * qualifier
             */
            if (!qualifierMatcher.matches()) {
                osgiVersion = s1 + s2 + "." + s3 + "_" + s4;
            }
        }

        /* convert
         * 1.string   -> 1.0.0.string
         * 1.2.string -> 1.2.0.string
         * 1          -> 1.0.0
         * 1.1        -> 1.1.0
         */
        //Pattern NEED_TO_FILL_ZEROS = Pattern.compile( "([0-9])(\\.([0-9]))?\\.([0-9A-Za-z_-]+)" );
        Pattern NEED_TO_FILL_ZEROS = Pattern.compile("([0-9])(\\.([0-9]))?(\\.([0-9A-Za-z_-]+))?");
        m = NEED_TO_FILL_ZEROS.matcher(osgiVersion);
        if (m.matches()) {
            String major = m.group(1);
            String minor = m.group(3);
            String service = null;
            String qualifier = m.group(5);

            /* if there's no qualifier just fill with 0s */
            if (qualifier == null) {
                osgiVersion = getOSGIVersion(major, minor, service, qualifier);
            } else {
                /* if last portion is only numbers then it's not a qualifier */
                Matcher qualifierMatcher = ONLY_NUMBERS.matcher(qualifier);
                if (qualifierMatcher.matches()) {
                    if (minor == null) {
                        minor = qualifier;
                    } else {
                        service = qualifier;
                    }
                    osgiVersion = getOSGIVersion(major, minor, service, null);
                } else {
                    osgiVersion = getOSGIVersion(major, minor, service, qualifier);
                }
            }
        }

        m = OSGI_VERSION_PATTERN.matcher(osgiVersion);
        /* if still its not OSGi version then add everything as qualifier */
        if (!m.matches()) {
            String major = "0";
            String minor = "0";
            String service = "0";
            String qualifier = osgiVersion.replaceAll("\\.", "_");
            osgiVersion = major + "." + minor + "." + service + "." + qualifier;
        }

        return osgiVersion;
    }

    private static String getOSGIVersion(String major, String minor, String service, String qualifier) {
        StringBuffer sb = new StringBuffer();
        sb.append(major != null ? major : "0");
        sb.append('.');
        sb.append(minor != null ? minor : "0");
        sb.append('.');
        sb.append(service != null ? service : "0");
        if (qualifier != null) {
            sb.append('.');
            sb.append(qualifier);
        }
        return sb.toString();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return (version == null) ? "" : version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCompatibility() {
        return compatibility;
    }

    public void setCompatibility(String compatibility) {
        this.compatibility = compatibility;
    }

    public void resolveVersion(MavenProject project) throws MojoExecutionException {
        if (version == null) {
            List dependencies = project.getDependencies();
            for (Iterator iterator = dependencies.iterator(); iterator.hasNext(); ) {
                Dependency dependancy = (Dependency) iterator.next();
                if (dependancy.getGroupId().equalsIgnoreCase(getGroupId()) &&
                        dependancy.getArtifactId().equalsIgnoreCase(getArtifactId())) {
                    setVersion(dependancy.getVersion());
                }

            }
        }
        if (version == null) {
            if (project.getDependencyManagement() != null) {
                List dependencies = project.getDependencyManagement().getDependencies();
                for (Iterator iterator = dependencies.iterator(); iterator.hasNext(); ) {
                    Dependency dependancy = (Dependency) iterator.next();
                    if (dependancy.getGroupId().equalsIgnoreCase(getGroupId()) &&
                            dependancy.getArtifactId().equalsIgnoreCase(getArtifactId())) {
                        setVersion(dependancy.getVersion());
                    }

                }
            }
        }
        if (version == null) {
            throw new MojoExecutionException("Could not find the version for " + getGroupId() + ":" + getArtifactId());
        }
        Properties properties = project.getProperties();
        for (Object key : properties.keySet()) {
            version = version.replaceAll(Pattern.quote("${" + key + "}"), properties.get(key).toString());
        }
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) throws MojoExecutionException {
        this.artifact = artifact;
        resolveOSGIInfo();
    }

    public String toString() {
        if (getVersion() != null && !getVersion().equalsIgnoreCase("")) {
            return getGroupId() + ":" + getArtifactId() + ":" + getVersion();
        } else {
            return getGroupId() + ":" + getArtifactId();
        }
    }

    public String toOSGIString() {
        return getBundleSymbolicName() + ":" + getBundleVersion();
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

    public void resolveOSGIInfo() throws MojoExecutionException {
        try {
            JarFile jarFile = new JarFile(getArtifact().getFile());
            Manifest manifest = jarFile.getManifest();
            if (getBundleSymbolicName() == null) {
                String value = manifest.getMainAttributes().getValue(BUNDLE_SYMBOLIC_NAME);
                if (value == null) {
                    throw new MojoExecutionException(BUNDLE_SYMBOLIC_NAME +
                            " cannot be found in the bundle: " + getArtifact().getFile());
                }
                String[] split = value.split(";");
                setBundleSymbolicName(split[0]);
            }
            if (getBundleVersion() == null) {
                setBundleVersion(manifest.getMainAttributes().getValue(BUNDLE_VERSION));
            }
            jarFile.close();
            if (getBundleSymbolicName() == null || getBundleVersion() == null) {
                throw new MojoExecutionException("Artifact doesn't contain OSGI info: " + getGroupId() + ":" +
                        getArtifactId() + ":" + getVersion());
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to retreive osgi bundle info: " + getGroupId() + ":" +
                    getArtifactId() + ":" + getVersion(), e);
        }

    }


}
