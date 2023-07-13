package org.wso2.carbon.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.conn.ssl.AbstractVerifier;

import javax.net.ssl.SSLException;

/**
 * Custom hostname verifier class.
 */
public class CustomHostNameVerifier extends AbstractVerifier {

    private final static String[] LOCALHOSTS = {"::1", "127.0.0.1", "localhost", "localhost.localdomain"};

    @Override
    public void verify(String s, String[] strings, String[] subjectAlts) throws SSLException {

        String[] subjectAltsWithLocalhosts = ArrayUtils.addAll(subjectAlts, LOCALHOSTS);

        if (strings != null && strings.length > 0 && strings[0] != null) {

            String[] subjectAltsWithLocalhostsAndCN = ArrayUtils.add(subjectAltsWithLocalhosts, strings[0]);
            this.verify(s, strings, subjectAltsWithLocalhostsAndCN, false);
        } else {
            this.verify(s, strings, subjectAltsWithLocalhosts, false);
        }
    }
}
