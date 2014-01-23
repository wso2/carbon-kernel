package org.apache.ws.security.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SecurityUtil {

	/**
	 * 
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String getSHA1(byte input[]) throws NoSuchAlgorithmException {
		MessageDigest sha = null;
		sha = MessageDigest.getInstance("SHA");
		sha.reset();
		sha.update(input);
		byte[] data = sha.digest();
		return Base64.encode(data);
	}

	/**
	 * 
	 * @param input
	 * @param algo
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String getDigest(byte input[], String algo) throws NoSuchAlgorithmException {
		MessageDigest sha = null;
		sha = MessageDigest.getInstance(algo);
		sha.reset();
		sha.update(input);
		byte[] data = sha.digest();
		return Base64.encode(data);
	}

}
