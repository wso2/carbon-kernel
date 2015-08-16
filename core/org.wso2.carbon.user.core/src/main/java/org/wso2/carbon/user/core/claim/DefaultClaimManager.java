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
package org.wso2.carbon.user.core.claim;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.dto.ClaimConfig;
import org.wso2.carbon.user.core.util.FileBasedClaimBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DefaultClaimManager implements ClaimManager {

    private static Log log = LogFactory.getLog(DefaultClaimManager.class);
    private ClaimManager claimMan = null;
    private DataSource dataSource = null;
    private RealmConfiguration realmConfig = null;
    private int tenantId;

    public ClaimConfig getClaimConfig() {
        return claimConfig;
    }

    public void setClaimConfig(ClaimConfig claimConfig) {
        this.claimConfig = claimConfig;
    }

    protected ClaimConfig claimConfig;
    private UserStoreManager userStoreManager = null;
    private AuthorizationManager authzManager = null;
    private Map<String,ClaimMapping> claims;
    private Map<String, Object> properties = null;
    protected Map<String, ClaimMapping> claimMapping = new HashMap<String, ClaimMapping>();
    protected Map<String, ClaimMapping> localClaimMapping = new HashMap<String, ClaimMapping>();



    public DefaultClaimManager() throws UserStoreException {
        init();
    }


    public void init() throws UserStoreException {

        try {
            claimConfig = FileBasedClaimBuilder.buildClaimMappingsFromConfigFile();
        } catch (IOException e) {
            log.error("Could not find claim configuration file ", e);
        } catch (XMLStreamException e) {
            log.error("Error while parsing claim configuration file ", e);
        }

    }

    private RealmConfiguration loadDefaultRealmConfigs() throws UserStoreException {
        RealmConfigXMLProcessor processor = new RealmConfigXMLProcessor();
        RealmConfiguration config = processor.buildRealmConfigurationFromFile();
        return config;
    }

    /**
     * @param domainName
     * @param claimURI
     * @return
     * @throws UserStoreException
     */
    public String getAttributeName(String domainName, String claimURI) throws UserStoreException {
        claimMapping = claimConfig.getClaims();
        ClaimMapping mapping = claimMapping.get(claimURI);

        if (mapping != null) {
            if (domainName != null) {
                String mappedAttrib = mapping.getMappedAttribute(domainName.toUpperCase());
                if (mappedAttrib != null) {
                    return mappedAttrib;
                }
                return mapping.getMappedAttribute();
            } else {
                return mapping.getMappedAttribute();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */

    public String[] getAllClaimUris() throws UserStoreException {
        return claimMapping.keySet().toArray(new String[claimMapping.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public Claim getClaim(String claimURI) throws UserStoreException {
        //Claim claim=new Claim(claimURI);
        ClaimMapping mapping = claimMapping.get(claimURI);
        if (mapping != null) {
            return mapping.getClaim();
        }
        return null;
    }

    /**
     * @param claimURI
     */

    public ClaimMapping getClaimMapping(String claimURI) throws UserStoreException {
        return claimMapping.get(claimURI);
    }

    /**
     * {@inheritDoc}
     */
    public ClaimMapping[] getAllClaimMappings() throws UserStoreException {
        List<ClaimMapping> claimList = null;
        claimList = new ArrayList<ClaimMapping>();
        Iterator<Map.Entry<String, ClaimMapping>> iterator = claimMapping.entrySet().iterator();

        for (; iterator.hasNext(); ) {
            ClaimMapping claimMapping = iterator.next().getValue();
            claimList.add(claimMapping);
        }
        return claimList.toArray(new ClaimMapping[claimList.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public void addNewClaimMapping(org.wso2.carbon.user.api.ClaimMapping mapping)
            throws UserStoreException{
        claimMapping.put(mapping.getClaim().getClaimUri(), (ClaimMapping) claimMapping);
    }

    /**
     * {@inheritDoc}
     */
    public void updateClaimMapping(org.wso2.carbon.user.api.ClaimMapping mapping)
            throws UserStoreException{
        claimMapping.remove(mapping);
        claimMapping.put(mapping.getClaim().getClaimUri(), (ClaimMapping) claimMapping);
    }
    /**
     * {@inheritDoc}
     */
    public void deleteClaimMapping(org.wso2.carbon.user.api.ClaimMapping mapping) throws UserStoreException {
        claimMapping.remove(mapping);
    }
}
