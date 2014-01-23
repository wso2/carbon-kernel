package org.apache.ws.security.message;

import java.io.IOException;
import javax.security.auth.callback.*;

public class CredentialsCallbackHandler implements CallbackHandler {

	private String username = null;
	private String password;

	public CredentialsCallbackHandler(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public CredentialsCallbackHandler(String password) {
		super();
		this.password = password;
	}

	public void handle(Callback callbacks[]) throws IOException, UnsupportedCallbackException {
		for (int i = 0; i < callbacks.length; i++) {
			if (callbacks[i] instanceof NameCallback) {
				NameCallback nc = (NameCallback) callbacks[i];
				nc.setName(username);
				continue;
			}
			if (callbacks[i] instanceof PasswordCallback) {
				PasswordCallback pc = (PasswordCallback) callbacks[i];
				pc.setPassword(password.toCharArray());
			}
		}

	}

}
