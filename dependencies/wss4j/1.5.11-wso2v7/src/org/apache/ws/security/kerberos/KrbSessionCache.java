package org.apache.ws.security.kerberos;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KrbSessionCache {

	private ThreadLocal<Stack<KrbSession>> threadLocal = new ThreadLocal<Stack<KrbSession>>();
	private Map<String, KrbSession> kerbSessionsByThumbprint = new ConcurrentHashMap<String, KrbSession>();


	private static KrbSessionCache instance = new KrbSessionCache();
	private static Log log = LogFactory.getLog(KrbSessionCache.class.getName());

	private KrbSessionCache() {
	}

	/**
	 * 
	 * @return
	 */
	public static KrbSessionCache getInstance() {
		return instance;
	}

	/**
	 * 
	 * @return
	 */
	private Stack<KrbSession> getLocalSession() {
		if (threadLocal.get() == null) {
			threadLocal.set(new Stack<KrbSession>());
		}
		return threadLocal.get();
	}

	/**
	 * 
	 * @param session
	 */
	public void addSession(KrbSession session) {

		Stack<KrbSession> localSession = getLocalSession();
		kerbSessionsByThumbprint.put(session.getThumbPrintEncoded(), session);

		if (!localSession.contains(session)) {
			localSession.push(session);
			if (log.isDebugEnabled()) {
				log.debug("Session added : " + session.getThumbPrintEncoded());
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Session already exists : " + session.getThumbPrintEncoded());
			}
		}

	}
	
	public KrbSession getSession(String thumbPrintEncoded) {
		KrbSession result = (KrbSession) kerbSessionsByThumbprint.get(thumbPrintEncoded);
		if (result != null) {
			kerbSessionsByThumbprint.remove(thumbPrintEncoded);
		}

		return result;
	}

	/**
	 * 
	 * @return
	 */
	public KrbSession getCurrentSession() {
		Stack<KrbSession> localSession = getLocalSession();
		if (localSession != null && !localSession.isEmpty()) {
			return localSession.pop();
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param kerberosSession
	 */
	public void setCurrentSession(KrbSession kerberosSession) {
		Stack<KrbSession> localSession = getLocalSession();
		if (!localSession.contains(kerberosSession)) {
			localSession.push(kerberosSession);
			if (log.isDebugEnabled()) {
				log.debug("Session added : " + kerberosSession.getThumbPrintEncoded());
			}
		}

	}

}
