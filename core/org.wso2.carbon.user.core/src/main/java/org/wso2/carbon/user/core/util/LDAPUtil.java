package org.wso2.carbon.user.core.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import static org.wso2.carbon.user.core.UserStoreConfigConstants.dateAndTimePattern;
import static org.wso2.carbon.user.core.ldap.LDAPConstants.DEFAULT_LDAP_TIME_FORMATS_PATTERN;

public class LDAPUtil {

    // Regex patterns for LDAP timestamp formats.
    private static final Pattern NO_FRACTION_TIMESTAMP_PATTERN = Pattern.compile("^\\d{14}([-+]\\d{4}|Z)$");
    private static final Pattern THREE_DIGIT_FRACTION_TIMESTAMP_PATTERN =
            Pattern.compile("^\\d{14}[,\\.]\\d{3}([-+]\\d{4}|Z)$");
    private static final Pattern TWO_DIGIT_FRACTION_TIMESTAMP_PATTERN =
            Pattern.compile("^\\d{14}[,\\.]\\d{2}([-+]\\d{4}|Z)$");
    private static final Pattern ONE_DIGIT_FRACTION_TIMESTAMP_PATTERN =
            Pattern.compile("^\\d{12}[,\\.]\\d{1}([-+]\\d{4}|Z)$");
    private static final Pattern ONE_DIGIT_FRACTION_WITH_SECONDS_TIMESTAMP_PATTERN =
            Pattern.compile("^\\d{14}[,\\.]\\d{1}([-+]\\d{4}|Z)$");
    private static final Pattern ONE_DIGIT_FRACTION_WITH_MINUTES_TIMESTAMP_PATTERN =
            Pattern.compile("^\\d{12}[,\\.]\\d{1}([-+]\\d{4}|Z)$");

    private static Log log = LogFactory.getLog(LDAPUtil.class);

    /**
     * @param ctx
     * @param ldapSearchBase
     * @param sid
     * @return
     * @throws NamingException
     */
    public static String findGroupBySID(DirContext ctx, String ldapSearchBase, String sid,
                                        String userAttribute) throws NamingException {

        String searchFilter = "(&(objectClass=group)(objectSid=" + sid + "))";

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter,
                searchControls);

        if (results.hasMoreElements()) {
            SearchResult searchResult = (SearchResult) results.nextElement();

            // make sure there is not another item available, there should be only 1 match
            if (results.hasMoreElements()) {
                log.error("Matched multiple groups for the group with SID: " + sid);
                return null;
            } else {
                return (String) searchResult.getAttributes().get(userAttribute).get();
            }
        }
        return null;
    }

    /**
     * @param srLdapUser
     * @param objectSid
     * @param primaryGroupID
     * @return
     * @throws NamingException
     */
    public static String getPrimaryGroupSID(SearchResult srLdapUser, String objectSid,
                                            String primaryGroupID) throws NamingException {
        byte[] objectSID = (byte[]) srLdapUser.getAttributes().get(objectSid).get();
        String strPrimaryGroupID = (String) srLdapUser.getAttributes().get(primaryGroupID).get();

        String strObjectSid = decodeSID(objectSID);

        return strObjectSid.substring(0, strObjectSid.lastIndexOf('-') + 1) + strPrimaryGroupID;
    }

    /**
     * The binary data is in the form: byte[0] - revision level byte[1] - count of sub-authorities
     * byte[2-7] - 48 bit authority (big-endian) and then count x 32 bit sub authorities
     * (little-endian)
     * <p/>
     * The String value is: S-Revision-Authority-SubAuthority[n]...
     * <p/>
     * Based on code from here -
     * http://forums.oracle.com/forums/thread.jspa?threadID=1155740&tstart=0
     */
    private static String decodeSID(byte[] sid) {

        final StringBuilder strSid = new StringBuilder("S-");

        // get version
        final int revision = sid[0];
        strSid.append(Integer.toString(revision));

        // next byte is the count of sub-authorities
        final int countSubAuths = sid[1] & 0xFF;

        // get the authority
        long authority = 0;
        // String rid = "";
        for (int i = 2; i <= 7; i++) {
            authority |= ((long) sid[i]) << (8 * (5 - (i - 2)));
        }
        strSid.append("-");
        strSid.append(Long.toHexString(authority));

        // iterate all the sub-auths
        int offset = 8;
        int size = 4; // 4 bytes for each sub auth
        for (int j = 0; j < countSubAuths; j++) {
            long subAuthority = 0;
            for (int k = 0; k < size; k++) {
                subAuthority |= (long) (sid[offset + k] & 0xFF) << (8 * k);
            }

            strSid.append("-");
            strSid.append(subAuthority);

            offset += size;
        }

        return strSid.toString();
    }

    /**
     * Covert the LDAP timestamp format (Zulutime) to the generic timestamp supported by the identity server.
     * Refer to the "Generalized Time" section in the spec: https://www.ietf.org/rfc/rfc4517.txt.
     *
     * @param realmConfig   Realm configuration.
     * @param dateTimestamp Ldap timestamp.
     * @return Given timestamp in the standard format.
     * @throws UserStoreException If an error occurred while converting timestamp or if unsupported timestamp
     *                            configured in the userstore.
     */
    public static String convertToStandardTimeFormat(RealmConfiguration realmConfig, String dateTimestamp)
            throws UserStoreException {

        if (StringUtils.isBlank(dateTimestamp)) {
            return dateTimestamp;
        }
        String userstoreTimestampFormat = realmConfig.getUserStoreProperty(dateAndTimePattern);
        if (StringUtils.isNotBlank(userstoreTimestampFormat) &&
                !StringUtils.equals(userstoreTimestampFormat, DEFAULT_LDAP_TIME_FORMATS_PATTERN)) {
            try {
                return OffsetDateTime.parse(dateTimestamp, DateTimeFormatter.ofPattern(userstoreTimestampFormat))
                        .toInstant()
                        .toString();
            } catch (DateTimeParseException | IllegalArgumentException e) {
                throw new UserStoreException("Invalid timestamp format for pattern: " + userstoreTimestampFormat, e);
            }
        }

        String derivedTimeStampPattern = LDAPUtil.deriveTimestampFormat(dateTimestamp);
        if (StringUtils.isNotBlank(derivedTimeStampPattern)) {
            try {
                return OffsetDateTime.parse(dateTimestamp, DateTimeFormatter.ofPattern(derivedTimeStampPattern))
                        .toInstant()
                        .toString();
            } catch (DateTimeParseException | IllegalArgumentException e) {
                throw new UserStoreException("Invalid timestamp format for pattern: " + derivedTimeStampPattern, e);
            }
        }
        throw new UserStoreException("Unsupported LDAP timestamp format: " + dateTimestamp);
    }

    /**
     * Derives the timestamp format based on the provided timestamp string.
     * The patterns checked here correspond to formats defined in DEFAULT_LDAP_TIME_FORMATS_PATTERN
     * ("[uuuuMMddHHmmss[,SSS][.SSS]X][uuuuMMddHHmmss[,SS][.SS]X][uuuuMMddHHmm[,S][.S]X]").
     *
     * @param timestamp The timestamp string to analyze.
     * @return The derived timestamp format, or null if no matching format is found.
     */
    public static String deriveTimestampFormat(String timestamp) {

        if (StringUtils.isBlank(timestamp)) {
            return null;
        }

        // Case 1: 14 digits with no fractional seconds.
        if (NO_FRACTION_TIMESTAMP_PATTERN.matcher(timestamp).matches()) {
            return "uuuuMMddHHmmssX";
        }
        // Case 2: 14 digits with 3-digit fraction.
        else if (THREE_DIGIT_FRACTION_TIMESTAMP_PATTERN.matcher(timestamp).matches()) {
            return timestamp.contains(",") ? "uuuuMMddHHmmss,SSSX" : "uuuuMMddHHmmss.SSSX";
        }
        // Case 3: 14 digits with 2-digit fraction.
        else if (TWO_DIGIT_FRACTION_TIMESTAMP_PATTERN.matcher(timestamp).matches()) {
            return timestamp.contains(",") ? "uuuuMMddHHmmss,SSX" : "uuuuMMddHHmmss.SSX";
        }
        // Case 4: 14 digits with 1-digit fraction (seconds precision).
        else if (ONE_DIGIT_FRACTION_WITH_SECONDS_TIMESTAMP_PATTERN.matcher(timestamp).matches()) {
            return timestamp.contains(",") ? "uuuuMMddHHmmss,SX" : "uuuuMMddHHmmss.SX";
        }
        // Case 5: 12 digits with 1-digit fraction (minutes precision).
        else if (ONE_DIGIT_FRACTION_WITH_MINUTES_TIMESTAMP_PATTERN.matcher(timestamp).matches()) {
            return timestamp.contains(",") ? "uuuuMMddHHmm,SX" : "uuuuMMddHHmm.SX";
        }

        return null;
    }
}
