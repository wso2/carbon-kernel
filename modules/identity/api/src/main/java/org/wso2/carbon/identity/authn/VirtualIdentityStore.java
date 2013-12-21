package org.wso2.carbon.identity.authn;

import java.util.List;

import org.wso2.carbon.identity.authn.spi.GroupSearchCriteria;
import org.wso2.carbon.identity.authn.spi.UserSearchCriteria;
import org.wso2.carbon.identity.credential.spi.Credential;

public interface VirtualIdentityStore {

	/**
	 * 
	 * @param credential
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public PrivilegedUser authenticate(Credential credential);

	/**
	 * Creates a user in the underlying user store.
	 * 
	 * @param user
	 * @return
	 */
	public PrivilegedUser createUser(User user);

	/**
	 * 
	 * @param userIdentifier
	 * @return
	 */
	public PrivilegedUser getUser(UserIdentifier userIdentifier);

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedUser> getUsers(UserSearchCriteria searchCriteria);

	/**
	 * Creates a group in the underlying user store.
	 * 
	 * @param group
	 * @return
	 */
	public PrivilegedGroup createGroup(Group group);

	/**
	 * 
	 * @param userIdentifier
	 * @return
	 */
	public PrivilegedGroup getGroup(GroupIdentifier userIdentifier);

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedGroup> getGroups(GroupSearchCriteria searchCriteria);

}
