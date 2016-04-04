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

import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.dto.ClaimConfig;
import org.wso2.carbon.user.core.util.FileBasedClaimBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryClaimManager implements ClaimManager {

    private static final Log log = LogFactory.getLog(DefaultClaimManager.class);

    public ClaimConfig getClaimConfig() {
        return claimConfig;
    }

    public void setClaimConfig(ClaimConfig claimConfig) {
        this.claimConfig = claimConfig;
    }

    protected static ClaimConfig claimConfig;
    protected Map<String, ClaimMapping> claimMapping = new HashMap<>();

    static {
        try {
            claimConfig = FileBasedClaimBuilder.buildClaimMappingsFromConfigFile();
        } catch (IOException e) {
            log.error("Could not find claim configuration file", e);
        } catch (XMLStreamException e) {
            log.error("Error while parsing claim configuration file", e);
        } catch (UserStoreException e) {
            log.error("Error while initializing claim manager");
        }
    }


    public InMemoryClaimManager() throws UserStoreException {
        claimMapping = claimConfig.getClaims();
    }

    /**
     * @param domainName
     * @param claimURI
     * @return
     * @throws UserStoreException
     */
    @Override
    public String getAttributeName(String domainName, String claimURI) throws UserStoreException {
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

    @Override
    public String getAttributeName(String claimURI) throws UserStoreException {
        ClaimMapping mapping = claimMapping.get(claimURI);
        if (mapping != null) {
            return mapping.getMappedAttribute();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getAllClaimUris() throws UserStoreException {
        return claimMapping.keySet().toArray(new String[claimMapping.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Claim getClaim(String claimURI) throws UserStoreException {
        ClaimMapping mapping = claimMapping.get(claimURI);
        if (mapping != null) {
            return mapping.getClaim();
        }
        return null;
    }

    /**
     * @param claimURI
     */
    @Override
    public ClaimMapping getClaimMapping(String claimURI) throws UserStoreException {
        return claimMapping.get(claimURI);
    }

    @Override
    public ClaimMapping[] getAllSupportClaimMappingsByDefault() throws org.wso2.carbon.user.api.UserStoreException {
        List<ClaimMapping> claimList = new ArrayList<>();

        for (Map.Entry<String, ClaimMapping> entry : claimMapping.entrySet()) {
            ClaimMapping claimMapping = entry.getValue();
            org.wso2.carbon.user.core.claim.Claim claim = claimMapping.getClaim();
            if (claim.isSupportedByDefault()) {
                claimList.add(claimMapping);
            }
        }
        return claimList.toArray(new ClaimMapping[claimList.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClaimMapping[] getAllClaimMappings() throws UserStoreException {
        List<ClaimMapping> claimList = new ArrayList<>();

        for (Map.Entry<String, ClaimMapping> entry : claimMapping.entrySet()) {
            ClaimMapping claimMapping = entry.getValue();
            claimList.add(claimMapping);
        }
        return claimList.toArray(new ClaimMapping[claimList.size()]);
    }

    @Override
    public ClaimMapping[] getAllClaimMappings(String dialectUri) throws org.wso2.carbon.user.api.UserStoreException {
        List<ClaimMapping> claimList = new ArrayList<>();

        for (Map.Entry<String, ClaimMapping> entry : claimMapping.entrySet()) {
            ClaimMapping claimMapping = entry.getValue();
            if (claimMapping.getClaim().getDialectURI().equals(dialectUri)) {
                claimList.add(claimMapping);
            }
        }
        return claimList.toArray(new ClaimMapping[claimList.size()]);
    }

    @Override
    public ClaimMapping[] getAllRequiredClaimMappings() throws UserStoreException {
        List<ClaimMapping> claimList = new ArrayList<>();

        for (Map.Entry<String, ClaimMapping> entry : claimMapping.entrySet()) {
            ClaimMapping claimMapping = entry.getValue();
            if (claimMapping.getClaim().isRequired()) {
                claimList.add(claimMapping);
            }
        }
        return claimList.toArray(new ClaimMapping[claimList.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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