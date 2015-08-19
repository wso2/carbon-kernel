package org.wso2.carbon.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.clustering.exceptions.MembershipSchemeException;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastConstants;
import java.util.Map;

/**
 * This service component is responsible for managing membership schemes registered
 * in the carbon runtime.
 *
 * @scr.component name="org.wso2.carbon.core.internal.MembershipSchemeManagerServiceComponent"
 * immediate="true"
 * @scr.reference name="membership.scheme.service" interface="org.apache.axis2.clustering.MembershipScheme"
 * cardinality="0..n" policy="dynamic"  bind="addMembershipScheme" unbind="removeMembershipScheme"
 */
public class MembershipSchemeManagerServiceComponent {

    private static final Log log = LogFactory.getLog(MembershipSchemeManagerServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        log.debug("Membership scheme manager service component activated");
    }

    public void addMembershipScheme(org.apache.axis2.clustering.MembershipScheme membershipScheme, Map<String, ?> parameters)
            throws MembershipSchemeException {

        String name = findMembershipSchemeName(parameters);
        CarbonCoreDataHolder.getInstance().addMembershipScheme(name, membershipScheme);
    }

    public void removeMembershipScheme(org.apache.axis2.clustering.MembershipScheme membershipScheme, Map<String, ?> parameters)
            throws MembershipSchemeException {

        String name = findMembershipSchemeName(parameters);
        CarbonCoreDataHolder.getInstance().removeMembershipScheme(name, membershipScheme);
    }

    private String findMembershipSchemeName(Map<String, ?> parameters) throws MembershipSchemeException {

        if(!parameters.containsKey(HazelcastConstants.MEMBERSHIP_SCHEME_NAME)) {
            throw new MembershipSchemeException("Membership scheme name not found in service parameters");
        }
        return parameters.get(HazelcastConstants.MEMBERSHIP_SCHEME_NAME).toString();
    }
}
