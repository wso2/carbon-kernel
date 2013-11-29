package org.wso2.maven.p2;

import java.net.URL;

public class P2Repository {
    /**
     * URL of the Metadata Repository
     *
     * @parameter
     */
    private URL metadataRepository;

    /**
     * URL of the Artifact Repository
     *
     * @parameter
     */
    private URL artifactRepository;
    
    /**
     * URL of the P2 Repository
     *
     * @parameter
     */
    private URL repository;
    
    /**
     * Genrate P2 Repository on the fly
     *
     * @parameter
     */
    private RepositoryGenMojo generateRepo;

	public void setGenerateRepo(RepositoryGenMojo generateRepo) {
		this.generateRepo = generateRepo;
	}

	public RepositoryGenMojo getGenerateRepo() {
		return generateRepo;
	}

	public void setRepository(URL repository) {
		this.repository = repository;
	}

	public URL getRepository() {
		return repository;
	}

	public void setArtifactRepository(URL artifactRepository) {
		this.artifactRepository = artifactRepository;
	}

	public URL getArtifactRepository() {
		return artifactRepository;
	}

	public void setMetadataRepository(URL metadataRepository) {
		this.metadataRepository = metadataRepository;
	}

	public URL getMetadataRepository() {
		return metadataRepository;
	} 
}
