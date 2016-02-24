/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.security.sts.service;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rahas.impl.SAMLTokenIssuerConfig;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.security.SecurityConfigException;
import org.wso2.carbon.security.config.SecurityServiceAdmin;
import org.wso2.carbon.security.keystore.KeyStoreAdmin;
import org.wso2.carbon.security.keystore.service.KeyStoreData;
import org.wso2.carbon.security.sts.service.util.TrustedServiceData;
import org.wso2.carbon.utils.ServerConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class STSAdminServiceImpl extends AbstractAdmin implements STSAdminServiceInterface {

    private static Log log = LogFactory.getLog(STSAdminServiceImpl.class);

    @Override
    public void addTrustedService(String serviceAddress, String certAlias)
            throws SecurityConfigException {
        try {
            AxisService stsService = getAxisConfig().getService(ServerConstants.STS_NAME);
            Parameter origParam = stsService.getParameter(SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG
                    .getLocalPart());
            if (origParam != null) {
                OMElement samlConfigElem = origParam.getParameterElement().getFirstChildWithName(
                        SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG);
                SAMLTokenIssuerConfig samlConfig = new SAMLTokenIssuerConfig(samlConfigElem);
                samlConfig.addTrustedServiceEndpointAddress(serviceAddress, certAlias);
                setSTSParameter(samlConfig);
                persistTrustedService(ServerConstants.STS_NAME,
                        ServerConstants.STS_NAME,
                        serviceAddress,
                        certAlias);
            } else {
                throw new AxisFault("missing parameter : "
                        + SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG.getLocalPart());
            }

        } catch (Exception e) {
            log.error("Error while adding a trusted service", e);
            throw new SecurityConfigException(e.getMessage(), e);
        }
    }

    public void removeTrustedService(String serviceAddress) throws SecurityConfigException {
        try {
            AxisService stsService = getAxisConfig().getService(ServerConstants.STS_NAME);
            Parameter origParam = stsService.getParameter(SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG
                    .getLocalPart());
            if (origParam != null) {
                OMElement samlConfigElem = origParam.getParameterElement().getFirstChildWithName(
                        SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG);
                SAMLTokenIssuerConfig samlConfig = new SAMLTokenIssuerConfig(samlConfigElem);
                samlConfig.getTrustedServices().remove(serviceAddress);
                setSTSParameter(samlConfig);
                removeTrustedService(ServerConstants.STS_NAME, ServerConstants.STS_NAME,
                        serviceAddress);
            } else {
                throw new AxisFault("missing parameter : "
                        + SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG.getLocalPart());
            }

        } catch (Exception e) {
            log.error("Error while removing a trusted service", e);
            throw new SecurityConfigException(e.getMessage(), e);
        }
    }

    @Override
    public TrustedServiceData[] getTrustedServices() throws SecurityConfigException {
        try {
            AxisService service = getAxisConfig().getService(ServerConstants.STS_NAME);
            Parameter origParam = service.getParameter(SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG
                    .getLocalPart());
            if (origParam != null) {
                OMElement samlConfigElem = origParam.getParameterElement().getFirstChildWithName(
                        SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG);
                SAMLTokenIssuerConfig samlConfig = new SAMLTokenIssuerConfig(samlConfigElem);
                Map trustedServicesMap = samlConfig.getTrustedServices();
                Set addresses = trustedServicesMap.keySet();

                List serviceBag = new ArrayList();
                for (Iterator iterator = addresses.iterator(); iterator.hasNext(); ) {
                    String address = (String) iterator.next();
                    String alias = (String) trustedServicesMap.get(address);
                    TrustedServiceData data = new TrustedServiceData(address, alias);
                    serviceBag.add(data);
                }
                return (TrustedServiceData[]) serviceBag.toArray(new TrustedServiceData[serviceBag
                        .size()]);
            } else {
                throw new SecurityConfigException("missing parameter : "
                        + SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG.getLocalPart());
            }
        } catch (Exception e) {
            log.error("Error while retrieving trusted services", e);
            throw new SecurityConfigException(e.getMessage(), e);
        }
    }

    @Override
    public String getProofKeyType() throws SecurityConfigException {
        try {
            AxisService service = getAxisConfig().getService(ServerConstants.STS_NAME);
            Parameter origParam = service.getParameter(SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG
                    .getLocalPart());
            if (origParam != null) {
                OMElement samlConfigElem = origParam.getParameterElement().getFirstChildWithName(
                        SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG);
                SAMLTokenIssuerConfig samlConfig = new SAMLTokenIssuerConfig(samlConfigElem);
                return samlConfig.getProofKeyType();
            } else {
                throw new SecurityConfigException("missing parameter : "
                        + SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG.getLocalPart());
            }
        } catch (Exception e) {
            log.error("Error while retrieving proof key type", e);
            throw new SecurityConfigException(e.getMessage(), e);
        }
    }

    @Override
    public void setProofKeyType(String keyType) throws SecurityConfigException {
        try {
            AxisService service = getAxisConfig().getService(ServerConstants.STS_NAME);
            Parameter origParam = service.getParameter(SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG
                    .getLocalPart());
            if (origParam != null) {
                OMElement samlConfigElem = origParam.getParameterElement().getFirstChildWithName(
                        SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG);
                SAMLTokenIssuerConfig samlConfig = new SAMLTokenIssuerConfig(samlConfigElem);
                samlConfig.setProofKeyType(keyType);
                setSTSParameter(samlConfig);
            } else {
                throw new AxisFault("missing parameter : "
                        + SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG.getLocalPart());
            }

        } catch (Exception e) {
            log.error("Error setting proof key type", e);
            throw new SecurityConfigException(e.getMessage(), e);
        }
    }

    @Override
    public String[] getCertAliasOfPrimaryKeyStore() throws SecurityConfigException {

        KeyStoreData[] keyStores = getKeyStores();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        KeyStoreData primaryKeystore = null;
        for (KeyStoreData keyStore : keyStores) {
            if (keyStore != null) {
                if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                    if (KeyStoreUtil.isPrimaryStore(keyStore.getKeyStoreName())) {
                        primaryKeystore = keyStore;
                        break;
                    }
                } else {
                    if (keyStore.getPrivateStore()) {
                        primaryKeystore = keyStore;
                        break;
                    }
                }
            }
        }

        if (primaryKeystore != null) {
            return getStoreEntries(primaryKeystore.getKeyStoreName());
        }

        throw new SecurityConfigException("Primary Keystore cannot be found.");
    }

    private void setSTSParameter(SAMLTokenIssuerConfig samlConfig) throws AxisFault {
        new SecurityServiceAdmin(getAxisConfig(), getConfigSystemRegistry()).
                setServiceParameterElement(ServerConstants.STS_NAME, samlConfig.getParameter());
    }

    private KeyStoreData[] getKeyStores() throws SecurityConfigException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId(),
                getGovernanceSystemRegistry());
        boolean isSuperTenant = CarbonContext.getThreadLocalCarbonContext().getTenantId() ==
                MultitenantConstants.SUPER_TENANT_ID;
        return admin.getKeyStores(isSuperTenant);
    }

    private String[] getStoreEntries(String keyStoreName) throws SecurityConfigException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId(),
                getGovernanceSystemRegistry());
        return admin.getStoreEntries(keyStoreName);
    }

    private void persistTrustedService(String groupName, String serviceName, String trustedService,
                                       String certAlias) throws SecurityConfigException {
        Registry registry;
        String resourcePath;
        Resource resource;
        try {
            resourcePath = RegistryResources.SERVICE_GROUPS + groupName
                    + RegistryResources.SERVICES + serviceName + "/trustedServices";
            registry = getConfigSystemRegistry(); //TODO: Multitenancy
            if (registry != null) {
                if (registry.resourceExists(resourcePath)) {
                    resource = registry.get(resourcePath);
                } else {
                    resource = registry.newResource();
                }
                if (resource.getProperty(trustedService) != null) {
                    resource.removeProperty(trustedService);
                }
                resource.addProperty(trustedService, certAlias);
                registry.put(resourcePath, resource);
            }
        } catch (Exception e) {
            log.error("Error occured while adding trusted service for STS", e);
            throw new SecurityConfigException("Error occured while adding trusted service for STS",
                    e);
        }
    }

    private void removeTrustedService(String groupName, String serviceName, String trustedService)
            throws SecurityConfigException {
        Registry registry;
        String resourcePath;
        Resource resource;
        try {
            resourcePath = RegistryResources.SERVICE_GROUPS + groupName
                    + RegistryResources.SERVICES + serviceName + "/trustedServices";
            registry = getConfigSystemRegistry(); //TODO: Multitenancy
            if (registry != null && registry.resourceExists(resourcePath)) {
                resource = registry.get(resourcePath);
                if (resource.getProperty(trustedService) != null) {
                    resource.removeProperty(trustedService);
                }
                registry.put(resourcePath, resource);
            }
        } catch (Exception e) {
            log.error("Error occured while removing trusted service for STS", e);
            throw new SecurityConfigException("Error occured while adding trusted service for STS",
                    e);
        }
    }

}