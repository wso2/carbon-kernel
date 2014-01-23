package org.apache.ws.security.message.token;

import org.w3c.dom.Document;
import java.security.NoSuchAlgorithmException;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.util.SecurityUtil;
import org.w3c.dom.Element;

public class KerberosSecurity extends BinarySecurity {

	public static final String KERBEROS_TOKEN_PROFILE_1_1 = "http://docs.oasis-open.org/wss/oasis-wss-kerberos-token-profile-1.1";
	public static final String GSS_KERBEROSv5_AP_REQ = KERBEROS_TOKEN_PROFILE_1_1
			+ "#GSS_Kerberosv5_AP_REQ";
	public static final String GSS_KERBEROSv5_AP_REQ_SHA1 = KERBEROS_TOKEN_PROFILE_1_1
			+ "#Kerberosv5APREQSHA1";

	/**
	 * 
	 * @param elem
	 * @throws WSSecurityException
	 */
	public KerberosSecurity(Element elem) throws WSSecurityException {
		super(elem);
		if (!getValueType().equals(GSS_KERBEROSv5_AP_REQ)) {
			throw new WSSecurityException(4, "invalidValueType", new Object[] {
					GSS_KERBEROSv5_AP_REQ, getValueType() });
		}
	}

	/**
	 * 
	 * @param doc
	 */
	public KerberosSecurity(Document doc) {
		super(doc);
		setValueType(GSS_KERBEROSv5_AP_REQ);
	}

	/**
	 * 
	 * @param tokenData
	 */
	public void setKerberosToken(byte tokenData[]) {
		setToken(tokenData);
	}

	/**
	 * 
	 * @return
	 */
	protected String getType() {
		return GSS_KERBEROSv5_AP_REQ;
	}

	public String getSHA1() throws WSSecurityException {
		try {
			return SecurityUtil.getSHA1(getToken());
		} catch (NoSuchAlgorithmException e) {
			throw new WSSecurityException(WSSecurityException.UNSUPPORTED_ALGORITHM, null, null, e);
		}
	}
}
