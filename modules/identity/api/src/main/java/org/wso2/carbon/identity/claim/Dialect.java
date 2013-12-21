package org.wso2.carbon.identity.claim;

import java.util.List;

public class Dialect {

	private DialectIdentifier dialectUri;
	private List<MetaClaim> claimMappings;

	public Dialect(DialectIdentifier dialectUri, List<MetaClaim> claimMappings) {
		this.dialectUri = dialectUri;
		this.claimMappings = claimMappings;
	}

	public DialectIdentifier getDialectUri() {
		return dialectUri;
	}

	public List<MetaClaim> getClaimMapping() {
		return claimMappings;
	}

}
