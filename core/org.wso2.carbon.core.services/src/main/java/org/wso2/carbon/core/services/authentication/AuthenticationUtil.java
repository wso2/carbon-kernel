/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.core.services.authentication;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.common.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class to wrap authentication functionality.
 */
public class AuthenticationUtil {

    /**
     * Ref X-Originating-IP Form 1
     */
    public static final String HEADER_X_ORIGINATING_IP_FORM_1 = "X-Originating-IP";

    /**
     * Ref X-Originating-IP Form 1
     */
    public static final String HEADER_X_ORIGINATING_IP_FORM_2 = "X-IP";

    /**
     * Ref X-Forwarded-For Form 1
     */
    public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";


    private static final String IP_ADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    private static final Log log = LogFactory.getLog(AuthenticationUtil.class);


    /**
     * This will first check whether "X-Originating-IP" is present in the HTTP header. If it is not
     * present then it will check whether "X-IP" header is present in the header.
     * @param msgCtx The message context.
     * @return Value for X-Originating-IP or X-IP if X-Originating-IP is not present. If both X-IP and X-Originating-IP
     * are not present this method will return null.
     */
    public static String getOriginatingIPAddress(MessageContext msgCtx) {

        // First look for originating IP
        String originatingIP = AuthenticationUtil.getHeader(HEADER_X_ORIGINATING_IP_FORM_1, msgCtx);

        if (originatingIP == null) {
            originatingIP = AuthenticationUtil.getHeader(HEADER_X_ORIGINATING_IP_FORM_2, msgCtx);
        }

        return originatingIP;
    }

    /**
     * This method will get forwarding addresses.More specifically this is looking for "X-Forwarded-For" header.
     * The header value would look like client1, proxy1, proxy2. The value is a comma+space separated list of IP
     * addresses, the left-most being the farthest downstream client.
     * Reference - http://en.wikipedia.org/wiki/X-Forwarded-For
     * @param msgCtx The message context.
     * @return The comma separated hops as an array.
     */
    public static String[] getForwardingAddresses(MessageContext msgCtx) {

        String forwardingAddresses = AuthenticationUtil.getHeader(HEADER_X_FORWARDED_FOR, msgCtx);

        if (forwardingAddresses != null) {
            return forwardingAddresses.split(",");
        }

        return new String[0];
    }

    private static void printForwardingAddresses(String[] forwardingAddresses) throws AuthenticationException {

        if (log.isDebugEnabled()) {

            StringBuilder stringBuilder = new StringBuilder("The request passed following hops - ");

            for (String address : forwardingAddresses) {

                validateRemoteAddress(address);

                stringBuilder.append(address);
                stringBuilder.append(",");
            }

            log.debug(stringBuilder.toString());
        }
    }


    /**
     * This method will return the remote address which request originated. This will first check for header
     * X-Originating-IP. If it is not found it will check for X-IP. Then again if X-IP header is also not
     * present this will check for "X-Forwarded-For" header and will take the first IP address or DNS value as
     * originating address. If any of above headers are not found this will check for "REMOTE_ADDR in message
     * context
     * This will also validate the address provided. This will check whether address is a valid IP address
     * or valid DNS address,
     *
     * @param msgCtx The request message context.
     * @return The remote address or null if remote address is not found in any mechanism specified above.
     * @throws AuthenticationException If the remote address provided is not a valid IP or DNS address.
     */
    public static String getRemoteAddress(MessageContext msgCtx) throws AuthenticationException {

        if (msgCtx == null) {
            return null;
        }

        // First check for originating IP address
        String remoteAddress = getOriginatingIPAddress(msgCtx);

        if (remoteAddress == null) {
            String[] forwardingAddresses = getForwardingAddresses(msgCtx);

            if (forwardingAddresses.length > 0) {
                remoteAddress = forwardingAddresses[0].trim();
                printForwardingAddresses(forwardingAddresses);
            }
        }

        if (remoteAddress == null) {
            remoteAddress = (String) msgCtx.getProperty(MessageContext.REMOTE_ADDR);
        }

        validateRemoteAddress(remoteAddress);

        return remoteAddress;
    }

    /**
     * This method validates the remote address. Checks whether given address is a valid IP address or a valid
     * DNS entry.
     * @param address The remote address.
     * @throws AuthenticationException If validation failed.
     */
    public static void validateRemoteAddress(String address) throws AuthenticationException {

        if (address == null || address.isEmpty()) {
            return;
        }

        address = address.replaceAll("\\s+", "");
        address = address.trim();

        if (!isValidIPAddress(address)) {
            if (!isValidDNSAddress(address)) {
                throw new AuthenticationException("Authentication Failed : Invalid remote address passed - " + address);
            }
        }
    }

    private static boolean isValidDNSAddress(String address) {
        try {
            InetAddress ipAddress = InetAddress.getByName(address);
            return isValidIPAddress(ipAddress.getHostAddress());
        } catch (UnknownHostException e) {
            log.warn("Could not find IP address for domain name : " + address);
        }

        return false;
    }

    private static boolean isValidIPAddress(String ipAddress) {

      Pattern pattern = Pattern.compile(IP_ADDRESS_PATTERN);
      Matcher matcher = pattern.matcher(ipAddress);
      return matcher.matches();
    }


    /**
     * Gets the given header name from message context. It first checks in the HTTP servlet request.
     * If servlet request is not found, header is looked up in the message context TRANSPORT_HEADERS.
     *
     * @param headerName     Name of the header.
     * @param messageContext The message context.
     * @return Header value.
     */
    public static String getHeader(String headerName, MessageContext messageContext) {

        HttpServletRequest request = (HttpServletRequest) messageContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);

        // First check header in servlet request. But for some transports (nhttp)
        // servlet request is null. In those cases we have to lookup through
        // transport headers in message context.
        if (request != null) {

            String tmp = request.getHeader(headerName);
            if (tmp == null) {
                tmp = request.getHeader(headerName.toLowerCase());
            }

            return tmp;
        } else {

            Map map = (Map) messageContext.getProperty(MessageContext.TRANSPORT_HEADERS);
            if (map != null) {
                String tmp = (String) map.get(headerName);
                if (tmp == null) {
                    tmp = (String) map.get(headerName.toLowerCase());
                }

                return tmp;
            }
        }

        return null;
    }
}
