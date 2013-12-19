package org.wso2.carbon.user;

import java.util.List;
import java.util.Properties;

public interface ClaimManager {

	/**
	 * 
	 * @param properties
	 */
	public void init(Properties properties);

	/**
	 * 
	 * @param dialectUri
	 * @return
	 */
	public Dialect getDialect(DialectIdentifier dialectUri);

	/**
	 * 
	 * @return
	 */
	public List<DialectIdentifier> getAllDialectUris();

	/**
	 * 
	 * @return
	 */
	public List<Dialect> getAllDialects();

	/**
	 * 
	 * @param dialectUri
	 * @return
	 */
	public List<ClaimIdentifier> getAllClaimUris(DialectIdentifier dialectUri);

	/**
	 * 
	 * @param dialectUri
	 * @return
	 */
	public List<MetaClaim> getAllClaims(DialectIdentifier dialectUri);

	/**
	 * 
	 * @param dialect
	 */
	public void addDialect(Dialect dialect);

	/**
	 * 
	 * @param dialectUri
	 */
	public void dropDialect(DialectIdentifier dialectUri);

	/**
	 * 
	 * @param dialect
	 */
	public void updateDialectIdentifier(DialectIdentifier oldDialect, DialectIdentifier newDialect);

	/**
	 * 
	 * @param dialect
	 * @param claims
	 */
	public void addClaimsToDialect(DialectIdentifier dialectUri, List<MetaClaim> claims);

	/**
	 * 
	 * @param dialect
	 * @param claims
	 */
	public void removeClaimsFromDialect(DialectIdentifier dialectUri, ClaimIdentifier[] claimUris);

	/**
	 * 
	 * @param dialectUri
	 * @param claimUri
	 * @param storeIdentifier
	 * @return
	 */
	public AttributeIdentifier getAttributeIdentifier(DialectIdentifier dialectUri,
			ClaimIdentifier claimUri, StoreIdentifier storeIdentifier);

	/**
	 * 
	 * @param dialectUri
	 * @param storeIdentifier
	 * @return
	 */
	public List<AttributeIdentifier> getAllAttributeIdentifiers(DialectIdentifier dialectUri,
			StoreIdentifier storeIdentifier);

}
