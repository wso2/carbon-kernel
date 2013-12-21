package org.wso2.carbon.identity.authz;

import java.util.Date;
import java.util.List;

public class DelegatedRole extends Role {

	private Date effectiveFrom;
	private Date effectiveUpTo;

	/**
	 * 
	 * @param roleIdentifier
	 * @param permission
	 * @param effectiveFrom
	 * @param effectiveUpTo
	 */
	public DelegatedRole(RoleIdentifier roleIdentifier, List<Permission> permission,
			Date effectiveFrom, Date effectiveUpTo) {
		super(roleIdentifier, permission);
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