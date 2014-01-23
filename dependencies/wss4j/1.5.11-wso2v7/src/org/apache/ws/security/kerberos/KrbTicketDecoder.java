package org.apache.ws.security.kerberos;

import java.util.Iterator;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosKey;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.internal.EncTicketPart;
import sun.security.krb5.internal.Ticket;
import sun.security.krb5.internal.crypto.KeyUsage;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;

/**
 * Kerberos Ticket Decoder provides the ability to decode a Kerberos v5 service
 * ticket, so the session key and client principal name can be accessed.
 * Reference : http://thejavamonkey.blogspot.com/2008/05/how-to-decrypt-kerberos-gss-ap-req.html
 */
public class KrbTicketDecoder {

	private byte[] serviceTicket;
	private Subject subject;

	/**
	 * Construct a Kerberos Ticket Decoder. This takes the service ticket that
	 * is to be decoded and the JAAS subject that contains the secret key for
	 * the target service.
	 * 
	 * @param serviceTicket
	 *            the AP-REQ service ticket that is to be decode
	 * @param subject
	 *            the JAAS subject containing the secret key for the server
	 *            principal
	 */
	public KrbTicketDecoder(byte[] serviceTicket, Subject subject) {
		this.serviceTicket = serviceTicket;
		this.subject = subject;
	}

	/**
	 * Get the session key from the decoded service ticket.
	 * 
	 * @return the session key
	 * @throws Exception
	 */
	public SecretKey getSessionKey() throws Exception {
		EncryptionKey encKey = parseServiceTicket(serviceTicket);
		//TODO : Remove DES dependency
		SecretKeySpec keySpec = new SecretKeySpec(encKey.getBytes(), "DES");
		return (SecretKey) keySpec;
	}

	// Parses the service ticket (GSS AP-REQ token)
	private EncryptionKey parseServiceTicket(byte[] ticket) throws Exception {
		DerInputStream ticketStream = new DerInputStream(ticket);
		DerValue[] values = ticketStream.getSet(ticket.length, true);

		// Look for the AP_REQ.
		// AP-REQ ::= [APPLICATION 14] SEQUENCE
		for (int i = 0; i < values.length; i++) {
			DerValue value = values[i];
			if (value.isConstructed((byte) 14)) {
				value.resetTag(DerValue.tag_Set);
				return parseApReq(value.toDerInputStream(), value.length());
			}
		}
		throw new Exception("Could not find AP-REQ in service ticket.");
	}

	// Parse the GSS AP-REQ token.
	private EncryptionKey parseApReq(DerInputStream reqStream, int len)
			throws Exception {
		DerValue ticket = null;

		DerValue[] values = reqStream.getSet(len, true);

		for (int i = 0; i < values.length; i++) {
			DerValue value = values[i];
			if (value.isContextSpecific((byte) 3)) {
				ticket = value.getData().getDerValue();
			}
		}

		if (ticket == null) {
			throw new Exception("No Ticket found in AP-REQ PDU");
		}
		return decryptTicket(new Ticket(ticket), subject);
	}

	private EncryptionKey decryptTicket(Ticket ticket, Subject svrSub)
			throws Exception {

		// Get the private key that matches the encryption type of the ticket.
		EncryptionKey key = getPrivateKey(svrSub, ticket.encPart.getEType());
		// Decrypt the service ticket and get the cleartext bytes.
		byte[] ticketBytes = ticket.encPart.decrypt(key, KeyUsage.KU_TICKET);
		if (ticketBytes.length <= 0) {
			throw new Exception("Key is empty.");
		}
		// EncTicketPart provides access to the decrypted attributes of the
		// service ticket.
		byte[] temp = ticket.encPart.reset(ticketBytes, true);
		EncTicketPart encPart = new EncTicketPart(temp);
		return encPart.key;
	}

	// Get the private server key.
	private EncryptionKey getPrivateKey(Subject sub, int keyType)
			throws Exception {
		KerberosKey key = getKrbKey(sub, keyType);
		return new EncryptionKey(key.getEncoded(), key.getKeyType(),
				new Integer(keyType));
	}

	// Get the Kerberos Key from the subject that matches the given key type.
	private KerberosKey getKrbKey(Subject sub, int keyType) {
		Set<Object> creds = sub.getPrivateCredentials(Object.class);
		for (Iterator<Object> i = creds.iterator(); i.hasNext();) {
			Object cred = i.next();
			if (cred instanceof KerberosKey) {
				KerberosKey key = (KerberosKey) cred;
				if (key.getKeyType() == keyType) {
					return (KerberosKey) cred;
				}
			}
		}
		return null;
	}

}