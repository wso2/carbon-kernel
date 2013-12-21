package org.wso2.carbon.identity.authn.spi;

import java.util.List;
import java.util.Properties;

import org.wso2.carbon.identity.account.spi.LinkedAccountStore;
import org.wso2.carbon.identity.authn.GroupIdentifier;
import org.wso2.carbon.identity.authn.PrivilegedGroup;
import org.wso2.carbon.identity.authn.PrivilegedUser;
import org.wso2.carbon.identity.authn.StoreDialectCollection;
import org.wso2.carbon.identity.authn.StoreIdentifier;
import org.wso2.carbon.identity.authn.UserIdentifier;
import org.wso2.carbon.identity.authn.VirtualIdentityStore;
import org.wso2.carbon.identity.claim.Claim;
import org.wso2.carbon.identity.claim.ClaimIdentifier;
import org.wso2.carbon.identity.claim.DialectIdentifier;
import org.wso2.carbon.identity.commons.EntityTree;
import org.wso2.carbon.identity.commons.EntryIdentifier;
import org.wso2.carbon.identity.credential.spi.Credential;
import org.wso2.carbon.identity.credential.spi.CredentialStore;
import org.wso2.carbon.identity.profile.Profile;
import org.wso2.carbon.identity.profile.ProfileIdentifier;

public interface IdentityStore extends VirtualIdentityStore {

	/**
	 * 
	 * @param storeDialectCollection
	 * @param accountManager
	 * @param credentialStore
	 * @param properties
	 */
	public void init(StoreDialectCollection storeDialectCollection,
			LinkedAccountStore linkedAccontStore, CredentialStore credentialStore,
			Properties properties);

	/**
	 * 
	 * @param userIdentifier
	 */
	public void dropUser(UserIdentifier userIdentifier);

	/**
	 * 
	 * @param userIdentifier
	 * @param dialectIdentifier
	 * @param claims
	 * @param profileIdentifier
	 */
	public void addUserAttributes(UserIdentifier userIdentifier,
			DialectIdentifier dialectIdentifier, List<Claim> claims,
			ProfileIdentifier profileIdentifier);

	/**
	 * 
	 * @param userIdentifier
	 * @param dialectIdentifier
	 * @param claims
	 * @param profileIdentifier
	 */
	public void updateUserAttributes(UserIdentifier userIdentifier,
			DialectIdentifier dialectIdentifier, List<Claim> claims,
			ProfileIdentifier profileIdentifier);

	/**
	 * 
	 * @param userIdentifier
	 * @param dialectIdentifier
	 * @param claimIdentifiers
	 * @param profileIdentifier
	 */
	public void removeUserAttributes(UserIdentifier userIdentifier,
			DialectIdentifier dialectIdentifier, List<ClaimIdentifier> claimIdentifiers,
			ProfileIdentifier profileIdentifier);

	/**
	 * 
	 * @param userIdentifier
	 * @param dialectIdentifier
	 * @param claimIdentifiers
	 * @param profileIdentifier
	 * @return
	 */
	public List<Claim> getUserAttributes(UserIdentifier userIdentifier,
			DialectIdentifier dialectIdentifier, List<ClaimIdentifier> claimIdentifiers,
			ProfileIdentifier profileIdentifier);

	/**
	 * 
	 * @param userIdentifier
	 * @param dialectIdentifier
	 * @param profileIdentifier
	 * @return
	 */
	public List<Claim> getUserAttributes(UserIdentifier userIdentifier,
			DialectIdentifier dialectIdentifier, ProfileIdentifier profileIdentifier);

	/**
	 * 
	 * @param userIdentifier
	 * @param profileIdentifier
	 */
	public void createUserProfile(UserIdentifier userIdentifier, ProfileIdentifier profileIdentifier);

	/**
	 * 
	 * @param userIdentifier
	 * @param dialectIdentifier
	 * @param profileIdentifier
	 * @return
	 */
	public Profile getUserProfile(UserIdentifier userIdentifier,
			DialectIdentifier dialectIdentifier, ProfileIdentifier profileIdentifier);

	/**
	 * 
	 * @param userIdentifier
	 * @param dialectIdentifier
	 * @param claimUris
	 * @return
	 */
	public List<Profile> getUserProfiles(UserIdentifier userIdentifier,
			DialectIdentifier dialectIdentifier, List<ClaimIdentifier> claimUris);

	/**
	 * 
	 * @param userIdentifier
	 * @param oldIdentifier
	 * @param newIdentifier
	 */
	public void updateUserProfileIdentifier(UserIdentifier userIdentifier,
			ProfileIdentifier oldIdentifier, ProfileIdentifier newIdentifier);

	/**
	 * 
	 * @param groupIdentifier
	 * @param dialectIdentifier
	 * @param claims
	 */
	public void addGroupAttributes(GroupIdentifier groupIdentifier,
			DialectIdentifier dialectIdentifier, List<Claim> claims);

	/**
	 * 
	 * @param groupIdentifier
	 * @param dialectIdentifier
	 * @param claims
	 */
	public void updateGroupAttributes(GroupIdentifier groupIdentifier,
			DialectIdentifier dialectIdentifier, List<Claim> claims);

	/**
	 * 
	 * @param groupIdentifier
	 * @param dialectIdentifier
	 * @param attributes
	 */
	public void removeGroupAttributes(GroupIdentifier groupIdentifier,
			DialectIdentifier dialectIdentifier, List<ClaimIdentifier> attributes);

	/**
	 * 
	 * @param userIdentifier
	 * @return
	 */
	public EntryIdentifier getUserEntryId(UserIdentifier userIdentifier);

	/**
	 * 
	 * @param groupIdentifier
	 * @return
	 */
	public EntryIdentifier getGroupEntryId(GroupIdentifier groupIdentifier);

	/**
	 * 
	 * @param groupIdentifier
	 */
	public void dropGroup(GroupIdentifier groupIdentifier);

	/**
	 * 
	 * @param groupIdentifier
	 * @return
	 */
	public PrivilegedGroup getGroup(GroupIdentifier groupIdentifier);

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedGroup> getGroups(GroupSearchCriteria searchCriteria);

	/**
	 * 
	 * @param groupIdentifier
	 * @param group
	 */
	public void updateGroupIdentifier(GroupIdentifier oldGroupIdentifier,
			GroupIdentifier newGroupIdentifier);

	/**
	 * 
	 * @param groupIdentifier
	 * @param userIdentifiers
	 */
	public void addUsersToGroup(GroupIdentifier groupIdentifier,
			List<UserIdentifier> userIdentifiers);

	/**
	 * 
	 * @param groupIdentifier
	 * @param userIdentifiers
	 */
	public void addUsersToGroups(List<GroupIdentifier> groupIdentifier,
			List<UserIdentifier> userIdentifiers);

	/**
	 * 
	 * @param groupIdentifier
	 * @param userIdentifiers
	 */
	public void addUserToGroup(GroupIdentifier groupIdentifier, UserIdentifier userIdentifiers);

	/**
	 * 
	 * @param groupIdentifier
	 * @param userIdentifiers
	 */
	public void addUserToGroups(List<GroupIdentifier> groupIdentifier,
			UserIdentifier userIdentifiers);

	/**
	 * 
	 * @param userIdentifier
	 * @param groupIdentifier
	 * @return
	 */
	public boolean isUserInGroup(UserIdentifier userIdentifier, GroupIdentifier groupIdentifier);

	/**
	 * 
	 * @param groupIdentifier
	 * @return
	 */
	public List<PrivilegedUser> getUsersInGroup(GroupIdentifier groupIdentifier);

	/**
	 * 
	 * @param groupIdentifier
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedUser> getUsersInGroup(GroupIdentifier groupIdentifier,
			UserSearchCriteria searchCriteria);

	/**
	 * 
	 * @param groupIdentifier
	 * @return
	 */
	public List<EntityTree> getGroupChildren(GroupIdentifier groupIdentifier);

	/**
	 * 
	 * @param parentGroupIdentifier
	 * @param childGroupIdentifier
	 * @return
	 */
	public boolean hasChildGroup(GroupIdentifier parentGroupIdentifier,
			GroupIdentifier childGroupIdentifier);

	/**
	 * 
	 * @param childGroupIdentifier
	 * @param parentGroupIdentifier
	 * @return
	 */
	public boolean hasParentGroup(GroupIdentifier childGroupIdentifier,
			GroupIdentifier parentGroupIdentifier);

	/**
	 * 
	 * @param userIdentifier
	 * @return
	 */
	public List<PrivilegedGroup> getGroups(UserIdentifier userIdentifier);

	/**
	 * 
	 * @param userIdentifier
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedGroup> getGroups(UserIdentifier userIdentifier,
			GroupSearchCriteria searchCriteria);

	/**
	 * 
	 * @return
	 */
	public StoreIdentifier getStoreIdentifier();

	/**
	 * 
	 * @return
	 */
	public boolean isReadOnly();

	/**
	 * 
	 * @return
	 */
	public LinkedAccountStore getLinkedAccountStore();

	/**
	 * 
	 * @param newCredentials
	 */
	@SuppressWarnings("rawtypes")
	public void resetCredentials(Credential newCredentials);

	/**
	 * 
	 * @param credential
	 */
	@SuppressWarnings("rawtypes")
	public void addCredential(Credential credential);

	/**
	 * 
	 * @param credential
	 */
	@SuppressWarnings("rawtypes")
	public void removeCredential(Credential credential);

}
