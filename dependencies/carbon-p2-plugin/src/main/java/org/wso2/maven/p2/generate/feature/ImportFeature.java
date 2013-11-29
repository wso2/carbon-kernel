package org.wso2.maven.p2.generate.feature;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.wso2.maven.p2.generate.utils.P2Utils;

public class ImportFeature{

	/**
     * Feature Id of the feature
     *
     * @parameter
     */

	private String featureId;

	/**
     * Version of the feature
     *
     * @parameter default-value=""
     */
	private String featureVersion;
	
    /**
     * Version Compatibility of the Feature
     *
     * @parameter
     */
	private String compatibility;

	private Artifact artifact;

    private boolean isOptional;

	public void setFeatureId(String featureId) {
		this.featureId = featureId;
	}

	public String getFeatureId() {
		return featureId;
	}

	public void setCompatibility(String compatibility) {
		this.compatibility = compatibility;
	}

	public String getCompatibility() {
		return compatibility;
	}

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    protected static ImportFeature getFeature(String featureDefinition) throws MojoExecutionException{
		String[] split = featureDefinition.split(":");
		ImportFeature feature=new ImportFeature();
		if (split.length>0){
			feature.setFeatureId(split[0]);
			String match="equivalent";
			if (split.length>1){
				if (P2Utils.isMatchString(split[1])){
					match=split[1].toUpperCase();
                    if(match.equalsIgnoreCase("optional"))
                        feature.setOptional(true);
					if (split.length>2)
						feature.setFeatureVersion(split[2]);
				}else{
					feature.setFeatureVersion(split[1]);
					if (split.length>2) {
						if  (P2Utils.isMatchString(split[2])) {
                            match=split[2].toUpperCase();
                            if(match.equalsIgnoreCase("optional"))
                                feature.setOptional(true);
                        }
                    }
				}
			}
			feature.setCompatibility(match);
			return feature;
		}
		throw new MojoExecutionException("Insufficient feature artifact information provided to determine the feature: "+featureDefinition) ; 
	}	

	public void setFeatureVersion(String version) {
        if(featureVersion == null || featureVersion.equals(""))
            featureVersion = Bundle.getOSGIVersion(version);
    }

	public String getFeatureVersion() {
		return featureVersion;
	}
}
