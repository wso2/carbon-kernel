package org.wso2.carbon.identity.profile;

import java.util.Collections;
import java.util.List;

import org.wso2.carbon.identity.claim.Claim;
import org.wso2.carbon.identity.claim.DialectIdentifier;

public class Profile {

	private ProfileIdentifier profileIdentifier;
	private List<Claim> claims;
	private DialectIdentifier dialectIdentifiers;

	/**
	 * 
	 * @param profileIdentifier
	 * @param dialectIdentifier
	 * @param claims
	 */
	public Profile(ProfileIdentifier profileIdentifier, DialectIdentifier dialectIdentifier,
			List<Claim> claims) {
		this.profileIdentifier = profileIdentifier;
		this.dialectIdentifiers = dialectIdentifier;
		this.claims = claims;
	}

	/**
	 * 
	 * @return
	 */
	public ProfileIdentifier getProfileIdentifier() {
		return profileIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public List<Claim> getClaims() {
		return Collections.unmodifiableList(claims);
	}

	/**
	 * 
	 * @return
	 */
	public DialectIdentifier getDialectIdentifiers() {
		return dialectIdentifiers;
	}

}
