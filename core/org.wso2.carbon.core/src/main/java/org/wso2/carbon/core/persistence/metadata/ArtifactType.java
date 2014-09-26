package org.wso2.carbon.core.persistence.metadata;

@Deprecated
public class ArtifactType {
    
    private String artifactType;
    private String metadataDirName;
    
    public ArtifactType(String artifactType, String metadataDirName) {
        this.setArtifactType(artifactType);
        this.setMetadataDirName(metadataDirName);
    }

    public String getArtifactType() {
        return artifactType;
    }

    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }

    public String getMetadataDirName() {
        return metadataDirName;
    }

    public void setMetadataDirName(String metadataDirName) {
        this.metadataDirName = metadataDirName;
    }
}

