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
package org.wso2.carbon.registry.app;

import org.apache.abdera.model.AtomDate;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;
import org.apache.abdera.util.EntityTag;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.util.Date;

/**
 * Utility class containing various Registry APP (Atom Publishing Protocol) implementation related
 * functionality.
 */
public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    private static EmbeddedRegistryService embeddedRegistryService;

    // The list of hex numbers
    private static final char[] HEX_FIGURE_MAP =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    // Map of ASCII characters in Hex format
    private static final String[] ASCII_HEX_CHAR_MAP = {
            "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07", "%08", "%09", "%0A", "%0B",
            "%0C", "%0D", "%0E", "%0F",
            "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17", "%18", "%19", "%1A", "%1B",
            "%1C", "%1D", "%1E", "%1F",
            "%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27", "%28", "%29", "%2A", "%2B",
            "%2C", "%2D", "%2E", "%2F",
            "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37", "%38", "%39", "%3A", "%3B",
            "%3C", "%3D", "%3E", "%3F",
            "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47", "%48", "%49", "%4A", "%4B",
            "%4C", "%4D", "%4E", "%4F",
            "%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57", "%58", "%59", "%5A", "%5B",
            "%5C", "%5D", "%5E", "%5F",
            "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67", "%68", "%69", "%6A", "%6B",
            "%6C", "%6D", "%6E", "%6F",
            "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77", "%78", "%79", "%7A", "%7B",
            "%7C", "%7D", "%7E", "%7F",
            "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87", "%88", "%89", "%8A", "%8B",
            "%8C", "%8D", "%8E", "%8F",
            "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97", "%98", "%99", "%9A", "%9B",
            "%9C", "%9D", "%9E", "%9F",
            "%A0", "%A1", "%A2", "%A3", "%A4", "%A5", "%A6", "%A7", "%A8", "%A9", "%AA", "%AB",
            "%AC", "%AD", "%AE", "%AF",
            "%B0", "%B1", "%B2", "%B3", "%B4", "%B5", "%B6", "%B7", "%B8", "%B9", "%BA", "%BB",
            "%BC", "%BD", "%BE", "%BF",
            "%C0", "%C1", "%C2", "%C3", "%C4", "%C5", "%C6", "%C7", "%C8", "%C9", "%CA", "%CB",
            "%CC", "%CD", "%CE", "%CF",
            "%D0", "%D1", "%D2", "%D3", "%D4", "%D5", "%D6", "%D7", "%D8", "%D9", "%DA", "%DB",
            "%DC", "%DD", "%DE", "%DF",
            "%E0", "%E1", "%E2", "%E3", "%E4", "%E5", "%E6", "%E7", "%E8", "%E9", "%EA", "%EB",
            "%EC", "%ED", "%EE", "%EF",
            "%F0", "%F1", "%F2", "%F3", "%F4", "%F5", "%F6", "%F7", "%F8", "%F9", "%FA", "%FB",
            "%FC", "%FD", "%FE", "%FF"};

    /**
     * Method to obtain the registry from the session. If the registry is not available, it will be
     * created and added to the session. If the tenant was changed, the registry on the session will
     * not be changed, if it exists. The new instance will be created on the fly per each request
     * made, until the session expires or the registry instance becomes unavailable. If the user
     * does not send in the authorization information, an anonymous instance will be created.
     *
     * @param request the HTTP Servlet Request.
     *
     * @return the registry instance on the session.
     * @throws RegistryException if the operation failed.
     */
    public static UserRegistry getRegistry(HttpServletRequest request) throws RegistryException {
        String username = null;
        String password = null;
        UserRegistry registry =
                (UserRegistry) request.getSession().getAttribute(
                        RegistryConstants.ROOT_REGISTRY_INSTANCE);
        String tenantDomain = (String) request.getAttribute(MultitenantConstants.TENANT_DOMAIN);
        int calledTenantId = MultitenantConstants.SUPER_TENANT_ID;
        if (tenantDomain != null &&
        		!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            try {
                if (RegistryContext.getBaseInstance().getRealmService() == null) {
                    String msg = "Error in getting the tenant manager. " +
                            "The realm service is not available.";
                    log.error(msg);
                    throw new RegistryException(msg);
                }
                calledTenantId = RegistryContext.getBaseInstance().getRealmService().
                        getTenantManager().getTenantId(tenantDomain);

                if (!RegistryContext.getBaseInstance().getRealmService().getTenantManager()
                        .isTenantActive(calledTenantId)) {
                    // the tenant is not active.
                    String msg = "The tenant is not active. Domain: " + tenantDomain + ".";
                    log.error(msg);
                    throw new RegistryException(msg);
                }
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                String msg = "Error in converting tenant domain to the id for tenant domain: "
                        + tenantDomain + ".";
                log.error(msg);
                throw new RegistryException(msg);
            }
        }
        boolean registryInvalidated = false;
        if (registry != null && registry.getTenantId() != calledTenantId) {
            // invalidate the registry.
            registry = null;
            registryInvalidated = true;
        }
        if (registry == null) {
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.length() > 0) {
                String[] aParts = authorization.trim().split(" ");
                if (aParts.length == 2) {
                    String credentials = aParts[1];
                    String decodedCredentials = new String(Base64.decode(credentials));
                    String[] cParts = decodedCredentials.trim().split(":");
                    if (cParts.length == 2) {
                        username = cParts[0];
                        password = cParts[1];
                    }
                }
            }
            if (username != null) {
                registry = getEmbeddedRegistryService().getRegistry(username, password,
                        calledTenantId);
                if (!registryInvalidated) {
                    request.getSession().setAttribute(RegistryConstants.ROOT_REGISTRY_INSTANCE,
                            registry);
                }
            } else {
                if (calledTenantId != MultitenantConstants.SUPER_TENANT_ID) {
                    RegistryUtils.initializeTenant(getEmbeddedRegistryService(), calledTenantId);
                }
                registry = getEmbeddedRegistryService().getRegistry(
                        CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, calledTenantId);
            }
        }
        return registry;
    }

    /**
     * Method to set the embedded registry service instance.
     *
     * @param registryService the embedded registry service instance.
     */
    public static synchronized void setEmbeddedRegistry(EmbeddedRegistryService registryService) {
        embeddedRegistryService = registryService;
    }

    /**
     * Method to obtain the embedded registry service instance.
     *
     * @return the embedded registry service instance.
     * @throws RegistryException if the embedded registry service is not available.
     */
    public static EmbeddedRegistryService getEmbeddedRegistryService() throws RegistryException {
        if (embeddedRegistryService == null) {
            String msg = "Embedded Registry service is not available. Make sure that the " +
                    "required version of the Registry core component is properly installed.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        return embeddedRegistryService;
    }

    /**
     * Generates a UUID for the given URI.
     *
     * @param uri the URI.
     *
     * @return the generated UUID.
     */
    public static String getUUID(String uri) {
        char[] tempArray = new char[36];
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(uri.getBytes());
            byte[] uriDigest = md5.digest();
            int count = 0;
            for (byte uriDigestByte : uriDigest) {
                if (count == 8 || count == 13 || count == 18 || count == 23) {
                    tempArray[count++] = '-';
                }
                tempArray[count++] = HEX_FIGURE_MAP[(uriDigestByte & 0x000000F0) >> 4];
                tempArray[count++] = HEX_FIGURE_MAP[(uriDigestByte & 0x0000000F)];

            }
        } catch (Exception e) {
            log.error("An error occurred while calculating the UUID for the given URI", e);
        }
        return (new String(tempArray));
    }

    /**
     * Method to encode a registry path.
     *
     * @param path the registry path.
     *
     * @return the encoded path.
     */
    public static String encodeRegistryPath(String path) {
        StringBuffer buffer = new StringBuffer();
        int len = path.length();
        for (int i = 0; i < len; i++) {
            int ch = path.charAt(i);
            if (('A' <= ch && ch <= 'Z')          // alpha numeric characters
                    || ('a' <= ch && ch <= 'z')
                    || ('0' <= ch && ch <= '9')) {
                buffer.append((char) ch);
            } else if (ch == '-' || ch == '_'        // allowed characters
                    || ch == '.' || ch == '/'
                    || ch == ' ' || ch == ';' || ch == ':') {
                buffer.append((char) ch);
            } else if (ch <= 0x007F) {            // other ASCII
                buffer.append(ASCII_HEX_CHAR_MAP[ch]);
            } else if (ch <= 0x07FF) {            // non-ASCII <= 0x7FF
                buffer.append(ASCII_HEX_CHAR_MAP[0xC0 | (ch >> 6)]);
                buffer.append(ASCII_HEX_CHAR_MAP[0x80 | (ch & 0x3F)]);
            } else {                                // 0x7FF < ch <= 0xFFFF
                buffer.append(ASCII_HEX_CHAR_MAP[0xE0 | (ch >> 12)]);
                buffer.append(ASCII_HEX_CHAR_MAP[0x80 | ((ch >> 6) & 0x3F)]);
                buffer.append(ASCII_HEX_CHAR_MAP[0x80 | (ch & 0x3F)]);
            }
        }
        return buffer.toString();
    }

    /**
     * Calculate the entity tag based on the resource, this is equivalent to the
     * RegistryAdapter::calculateEntityTag function which calculate entity tag based on the feed
     *
     * @param r Resource
     *
     * @return calculated entity tag as a string
     */
    public static String calculateEntityTag(Resource r) {
        String id = "urn:uuid:" + Utils.getUUID(r.getId());
        Date modifiedDate = r.getLastModified();
        String modified = AtomDate.valueOf(modifiedDate).getValue();

        return EntityTag.generate(id, modified).toString();
    }

    public static Link getLinkWithRel(Entry entry, String rel) {
        Link result = entry.getLink(rel);
        if (result == null) {
            for (Link link : entry.getLinks()) {
                if (link.getRel() != null &&
                        link.getRel().equals(rel)) {
                    result = link;
                    break;
                }
            }
        }
        return result;
    }
}
