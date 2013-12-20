package org.wso2.carbon.user;

import java.util.Date;

import org.wso2.carbon.user.spi.AuthorizationStore;

public class PrivilegedDelegatedRole extends PrivilegedRole {

	private Date effectiveFrom;
	private Date effectiveUpTo;

	/**
	 * 
	 * @param authzStore
	 * @param roleIdentifier
	 * @param effectiveFrom
	 * @param effectiveUpTo
	 */
	public PrivilegedDelegatedRole(AuthorizationStore authzStore, RoleIdentifier roleIdentifier,
			Date effectiveFrom, Date effectiveUpTo) {
		super(authzStore, roleIdentifier);
		this.effectiveFrom = effectiveFrom;
		this.effectiveUpTo = effectiveUpTo;
	}

	/**
	 * 
	 * @return
	 */
	public Date getEffectiveUpTo() {
		return effectiveUpTo;
	}

	/**
	 * 
	 * @return
	 */
	public Date getEffectiveFrom() {
		return effectiveFrom;
	}

}
