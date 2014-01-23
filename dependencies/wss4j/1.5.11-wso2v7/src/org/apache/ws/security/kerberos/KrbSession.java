package org.apache.ws.security.kerberos;

import javax.crypto.SecretKey;

public class KrbSession {
	private final String thumbPrintEncoded;
	private String clientPrincipalName;
	private String serverPrincipalName;
	private final SecretKey sessionKey;
	private String serviceTicket;

	/**
	 * 
	 * @param expiryDate
	 * @param thumbPrintEncoded
	 * @param sessionKey
	 */
	public KrbSession(String thumbPrintEncoded, SecretKey sessionKey) {
		this.thumbPrintEncoded = thumbPrintEncoded;
		this.sessionKey = sessionKey;
	}

	/**
	 * 
	 * @param clientPrincipalName
	 */
	public void setClientPrincipalName(String clientPrincipalName) {
		this.clientPrincipalName = clientPrincipalName;
	}

	/**
	 * 
	 * @param serverPrincipalName
	 */
	public void setServerPrincipalName(String serverPrincipalName) {
		this.serverPrincipalName = serverPrincipalName;
	}

	/**
	 * 
	 * @param serviceTicket
	 */
	public void setServiceTicket(String serviceTicket) {
		this.serviceTicket = serviceTicket;
	}

	/**
	 * 
	 * @return
	 */
	public String getClientPrincipalName() {
		return clientPrincipalName;
	}

	/**
	 * 
	 * @return
	 */
	public String getServerPrincipalName() {
		return serverPrincipalName;
	}

	/**
	 * 
	 * @return
	 */
	public String getServiceTicket() {
		return serviceTicket;
	}

	/**
	 * 
	 * @return
	 */
	public SecretKey getSessionKey() {
		return sessionKey;
	}

	/**
	 * 
	 * @return
	 */
	public String getThumbPrintEncoded() {
		return thumbPrintEncoded;
	}

	/**
	 * 
	 */
	public boolean equals(Object object) {
		if (object != null && (object instanceof KrbSession)) {
			KrbSession session = (KrbSession) object;
			return session.getThumbPrintEncoded().equals(thumbPrintEncoded);
		} else {
			return false;
		}
	}

	/**
	 * 
	 */
	public int hashCode() {
		return thumbPrintEncoded.hashCode();
	}

	/**
	 * 
	 * @return
	 */
	public String getPrincipalKey() {
		return createPrincipalKey(clientPrincipalName, serverPrincipalName);
	}

	/**
	 * 
	 * @param clientPrincipalName
	 * @param serverPrincipalName
	 * @return
	 */
	private String createPrincipalKey(String clientPrincipalName, String serverPrincipalName) {
		return clientPrincipalName + "-" + serverPrincipalName;
	}

}
