package org.apache.ws.security;

import javax.crypto.SecretKey;

public class KerberosTokenPrincipal extends CustomTokenPrincipal {

	private static final long serialVersionUID = -6903767865261191113L;
	private byte[] sessionKey;
	private SecretKey secretKey;
	private String clientPrincipalName;
	private String servicePrincipalName;

	public KerberosTokenPrincipal(String name) {
		super(name);
	}
	
	@Override
	public String getName() {
		String name = getClientPrincipalName();
		if (name.indexOf("@") > -1) {
			return name.substring(0, name.indexOf("@"));
		}
		return name;
	}
	

	public String getClientPrincipalName() {
		return clientPrincipalName;
	}

	public void setClientPrincipalName(String clientPrincipalName) {
		this.clientPrincipalName = clientPrincipalName;
	}

	public String getServicePrincipalName() {
		return servicePrincipalName;
	}

	public void setServicePrincipalName(String servicePrincipalName) {
		this.servicePrincipalName = servicePrincipalName;
	}

	public SecretKey getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(SecretKey secretKey) {
		this.secretKey = secretKey;
	}

	public byte[] getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(byte[] sessionKey) {
		this.sessionKey = sessionKey;
	}

}
