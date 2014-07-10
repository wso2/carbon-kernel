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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.builder.ClaimBuilder;
import org.wso2.carbon.user.core.claim.builder.ClaimBuilderException;
import org.wso2.carbon.user.core.claim.dao.ClaimDAO;
import org.wso2.carbon.user.core.internal.UMListenerServiceComponent;
import org.wso2.carbon.user.core.listener.ClaimManagerListener;

public class DefaultClaimManager implements ClaimManager {

	private ClaimDAO claimDAO = null;

	private ClaimInvalidationCache claimCache;
	private DataSource datasource;
	private ClaimBuilder claimBuilder;
	
	private Map<String, ClaimMapping> claimMapping;

	/**
	 * 
	 * @param claimMapping
	 */
	public DefaultClaimManager(Map<String, ClaimMapping> claimMapping, DataSource dataSource,
			int tenantId) {
		
		this.datasource = dataSource;
		this.claimBuilder = new ClaimBuilder(tenantId);
		this.claimDAO = new ClaimDAO(dataSource, tenantId);
		
		this.claimCache = ClaimInvalidationCache.getInstance();		
		// If there is value for hash, load existing claim mapping in db
		if(this.claimBuilder != null && this.claimCache.isInvalid()) {
			try {
				this.claimMapping = getClaimMapFromDB();
			} catch (UserStoreException e) {
				this.claimMapping = new ConcurrentHashMap<String, ClaimMapping>();
				this.claimMapping.putAll(claimMapping);
				this.claimCache.invalidateCache();
			}
		}
		// else this is the first instance load claims
		else {
			this.claimMapping = new ConcurrentHashMap<String, ClaimMapping>();
			this.claimMapping.putAll(claimMapping);
			this.claimCache.invalidateCache();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAttributeName(String claimURI) throws UserStoreException {
        // call listeners
        for (ClaimManagerListener listener : UMListenerServiceComponent
                .getClaimManagerListeners()) {
            if (!listener.getAttributeName(claimURI)) {
                return null;
            }
        }

		if(claimCache.isInvalid()) {
			this.claimMapping = getClaimMapFromDB();
		}
		ClaimMapping mapping = claimMapping.get(claimURI);
		if (mapping != null) {
			return mapping.getMappedAttribute();
		}
		return null;
	}

	/**
	 * 
	 * @param domainName
	 * @param claimURI
	 * @return
	 * @throws UserStoreException
	 */
	public String getAttributeName(String domainName, String claimURI) throws UserStoreException {
        // call listeners
        for (ClaimManagerListener listener : UMListenerServiceComponent
                .getClaimManagerListeners()) {
            if (!listener.getAttributeName(domainName, claimURI)) {
                return null;
            }
        }

        if(claimCache.isInvalid()) {
            this.claimMapping = getClaimMapFromDB();
        }
        ClaimMapping mapping = claimMapping.get(claimURI);
        if (mapping != null) {
            if (domainName != null) {
                String mappedAttrib = mapping.getMappedAttribute(domainName.toUpperCase());
                if(mappedAttrib != null){
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
	public Claim getClaim(String claimURI) throws UserStoreException {
        // call listeners
        for (ClaimManagerListener listener : UMListenerServiceComponent
                .getClaimManagerListeners()) {
            if (!listener.getClaim(claimURI)) {
                return null;
            }
        }

		if(claimCache.isInvalid()) {
			this.claimMapping = getClaimMapFromDB();
		}
		ClaimMapping mapping = claimMapping.get(claimURI);
		if (mapping != null) {
			return mapping.getClaim();
		}
		return null;
	}

	public ClaimMapping getClaimMapping(String claimURI) throws UserStoreException {
        // call listeners
        for (ClaimManagerListener listener : UMListenerServiceComponent
                .getClaimManagerListeners()) {
            if (!listener.getClaimMapping(claimURI)) {
                return null;
            }
        }
		if(claimCache.isInvalid()) {
			this.claimMapping = getClaimMapFromDB();
		}
		return claimMapping.get(claimURI);
	}

	/**
	 * {@inheritDoc}
	 */
	public org.wso2.carbon.user.api.ClaimMapping[] getAllSupportClaimMappingsByDefault()
			throws UserStoreException {
        // call listeners
        for (ClaimManagerListener listener : UMListenerServiceComponent
                .getClaimManagerListeners()) {
            if (!listener.getAllSupportClaimMappingsByDefault()) {
                return null;
            }
        }
		if(claimCache.isInvalid()) {
			this.claimMapping = getClaimMapFromDB();
		}
		List<ClaimMapping> claimList = new ArrayList<ClaimMapping>();
		Iterator<Entry<String, ClaimMapping>> iterator = claimMapping.entrySet().iterator();

		for (; iterator.hasNext();) {
			ClaimMapping claimMapping = iterator.next().getValue();
			Claim claim = claimMapping.getClaim();
			if (claim.isSupportedByDefault()) {
				claimList.add(claimMapping);
			}
		}

		return claimList.toArray(new ClaimMapping[claimList.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public org.wso2.carbon.user.api.ClaimMapping[] getAllClaimMappings() throws UserStoreException {
        // call listeners
        for (ClaimManagerListener listener : UMListenerServiceComponent
                .getClaimManagerListeners()) {
            if (!listener.getAllClaimMappings()) {
                return null;
            }
        }

		if(claimCache.isInvalid()) {
			this.claimMapping = getClaimMapFromDB();
		}
		List<ClaimMapping> claimList = null;
		claimList = new ArrayList<ClaimMapping>();
		Iterator<Entry<String, ClaimMapping>> iterator = claimMapping.entrySet().iterator();

		for (; iterator.hasNext();) {
			ClaimMapping claimMapping = iterator.next().getValue();
			claimList.add(claimMapping);
		}
		return claimList.toArray(new ClaimMapping[claimList.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public org.wso2.carbon.user.api.ClaimMapping[] getAllClaimMappings(String dialectUri)
			throws UserStoreException {
        // call listeners
        for (ClaimManagerListener listener : UMListenerServiceComponent
                .getClaimManagerListeners()) {
            if (!listener.getAllClaimMappings(dialectUri)) {
                return null;
            }
        }
		if(claimCache.isInvalid()) {
			this.claimMapping = getClaimMapFromDB();
		}
		List<ClaimMapping> claimList = null;
		claimList = new ArrayList<ClaimMapping>();
		Iterator<Entry<String, ClaimMapping>> iterator = claimMapping.entrySet().iterator();

		for (; iterator.hasNext();) {
			ClaimMapping claimMapping = iterator.next().getValue();
			if (claimMapping.getClaim().getDialectURI().equals(dialectUri)) {
				claimList.add(claimMapping);
			}
		}
		return claimList.toArray(new ClaimMapping[claimList.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public org.wso2.carbon.user.api.ClaimMapping[] getAllRequiredClaimMappings()
			throws UserStoreException {
        // call listeners
        for (ClaimManagerListener listener : UMListenerServiceComponent
                .getClaimManagerListeners()) {
            if (!listener.getAllRequiredClaimMappings()) {
                return null;
            }
        }
		if(claimCache.isInvalid()) {
			this.claimMapping = getClaimMapFromDB();
		}
		List<ClaimMapping> claimList = null;
		claimList = new ArrayList<ClaimMapping>();
		Iterator<Entry<String, ClaimMapping>> iterator = claimMapping.entrySet().iterator();

		for (; iterator.hasNext();) {
			ClaimMapping claimMapping = iterator.next().getValue();
			Claim claim = claimMapping.getClaim();
			if (claim.isRequired()) {
				claimList.add(claimMapping);
			}
		}

		return claimList.toArray(new ClaimMapping[claimList.size()]);
	}

	public String[] getAllClaimUris() throws UserStoreException {
        // call listeners
        for (ClaimManagerListener listener : UMListenerServiceComponent
                .getClaimManagerListeners()) {
            if (!listener.getAllClaimUris()) {
                return null;
            }
        }
		if(claimCache.isInvalid()) {
			this.claimMapping = getClaimMapFromDB();
		}
		return claimMapping.keySet().toArray(new String[claimMapping.size()]);
	}

	public void addNewClaimMapping(org.wso2.carbon.user.api.ClaimMapping mapping)
			throws org.wso2.carbon.user.api.UserStoreException {
        // call listeners
        for (ClaimManagerListener listener : UMListenerServiceComponent
                .getClaimManagerListeners()) {
            if (!listener.addNewClaimMapping(mapping)) {
                return;
            }
        }
		if(claimCache.isInvalid()) {
			this.claimMapping = getClaimMapFromDB();
		}
		addNewClaimMapping(getClaimMapping(mapping));
		this.claimCache.invalidateCache();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addNewClaimMapping(ClaimMapping mapping) throws UserStoreException {
        // call listeners
        for (ClaimManagerListener listener : UMListenerServiceComponent
                .getClaimManagerListeners()) {
            if (!listener.addNewClaimMapping(mapping)) {
                return;
            }
        }
		if (mapping != null && mapping.getClaim() != null) {

			if(claimCache.isInvalid()) {
				this.claimMapping = getClaimMapFromDB();
			}
			if (!claimMapping.containsKey(mapping.getClaim().getClaimUri())) {
				claimMapping.put(mapping.getClaim().getClaimUri(), mapping);
				claimDAO.addClaimMapping(mapping);
				this.claimCache.invalidateCache();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteClaimMapping(org.wso2.carbon.user.api.ClaimMapping mapping)
			throws UserStoreException {
        // call listeners
        for (ClaimManagerListener listener : UMListenerServiceComponent
                .getClaimManagerListeners()) {
            if (!listener.deleteClaimMapping(mapping)) {
                return;
            }
        }
		if (mapping != null && mapping.getClaim() != null) {
			
			if(claimCache.isInvalid()) {
				this.claimMapping = getClaimMapFromDB();
			}
			if (claimMapping.containsKey(mapping.getClaim().getClaimUri())) {
				claimMapping.remove(mapping.getClaim().getClaimUri());
				claimDAO.deleteClaimMapping(getClaimMapping(mapping));
				this.claimCache.invalidateCache();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateClaimMapping(org.wso2.carbon.user.api.ClaimMapping mapping)
			throws UserStoreException {
        // call listeners
        for (ClaimManagerListener listener : UMListenerServiceComponent
                .getClaimManagerListeners()) {
            if (!listener.updateClaimMapping(mapping)) {
                return;
            }
        }
		if (mapping != null && mapping.getClaim() != null) {

			if(claimCache.isInvalid()) {
				this.claimMapping = getClaimMapFromDB();
			}
			if (claimMapping.containsKey(mapping.getClaim().getClaimUri())) {
				claimMapping.put(mapping.getClaim().getClaimUri(), getClaimMapping(mapping));
				claimDAO.updateClaim(getClaimMapping(mapping));
				this.claimCache.invalidateCache();
			}
		}
	}

	private ClaimMapping getClaimMapping(org.wso2.carbon.user.api.ClaimMapping claimMapping) {
		ClaimMapping claimMap = new ClaimMapping();
		if (claimMapping != null) {
			claimMap.setClaim(getClaim(claimMapping.getClaim()));
			claimMap.setMappedAttribute(claimMapping.getMappedAttribute());
			claimMap.setMappedAttributes(claimMapping.getMappedAttributes());
		}
		return claimMap;
	}

	private Claim getClaim(org.wso2.carbon.user.api.Claim claim) {

		Claim clm = new Claim();
		if (claim != null) {
			clm.setCheckedAttribute(claim.isCheckedAttribute());
			clm.setClaimUri(claim.getClaimUri());
			clm.setDescription(claim.getDescription());
			clm.setDialectURI(claim.getDialectURI());
			clm.setDisplayOrder(claim.getDisplayOrder());
			clm.setDisplayTag(claim.getDisplayTag());
			clm.setReadOnly(claim.isReadOnly());
			clm.setRegEx(claim.getRegEx());
			clm.setRequired(claim.isRequired());
			clm.setSupportedByDefault(claim.isSupportedByDefault());
			clm.setValue(claim.getValue());
		}
		return clm;
	}
	private Map<String, ClaimMapping> getClaimMapFromDB() throws UserStoreException {
		Map<String, ClaimMapping> claimMap = new ConcurrentHashMap<String, ClaimMapping>();
		try {
			Map<String, ClaimMapping> dbClaimMap = this.claimBuilder.buildClaimMappingsFromDatabase(this.datasource, null);
			claimMap.putAll(dbClaimMap);
		} catch (ClaimBuilderException e) {
			throw new UserStoreException(e.getMessage(), e);
		}
		return claimMap;
	}

}
