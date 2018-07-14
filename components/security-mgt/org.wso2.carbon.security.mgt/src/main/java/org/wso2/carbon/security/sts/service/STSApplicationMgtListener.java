/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.security.sts.service;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rahas.impl.SAMLTokenIssuerConfig;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementValidationException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.listener.AbstractApplicationMgtListener;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.security.SecurityConfigException;
import org.wso2.carbon.security.config.SecurityServiceAdmin;
import org.wso2.carbon.security.internal.SecurityMgtServiceComponent;
import org.wso2.carbon.security.sts.service.util.TrustedServiceData;
import org.wso2.carbon.utils.ServerConstants;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class STSApplicationMgtListener extends AbstractApplicationMgtListener {

    private static Log log = LogFactory.getLog(STSApplicationMgtListener.class);

    @Override
    public int getDefaultOrderId() {

        // Force this listener to be executed last as we are deleting the ws trust configurations in th pre-delete
        // method.
        return 998;
    }

    @Override
    public boolean doPreDeleteApplication(String applicationName, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        ServiceProvider serviceProvider = appDAO.getApplication(applicationName, tenantDomain);
        if (serviceProvider != null &&
                serviceProvider.getInboundAuthenticationConfig() != null &&
                serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs() != null) {
            InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig()
                    .getInboundAuthenticationRequestConfigs();
            for (InboundAuthenticationRequestConfig config : configs) {
                if (IdentityApplicationConstants.Authenticator.WSTrust.NAME.equalsIgnoreCase(
                        config.getInboundAuthType()) && config.getInboundAuthKey() != null) {
                    try {
                        AxisService stsService = getAxisConfig().getService(ServerConstants.STS_NAME);
                        Parameter origParam =
                                stsService.getParameter(SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG.getLocalPart());

                        if (origParam != null) {
                            OMElement samlConfigElem = origParam.getParameterElement()
                                    .getFirstChildWithName(SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG);

                            SAMLTokenIssuerConfig samlConfig = new SAMLTokenIssuerConfig(samlConfigElem);
                            samlConfig.getTrustedServices().remove(config.getInboundAuthKey());
                            setSTSParameter(samlConfig);
                            STSAdminServiceImpl stsAdminService = new STSAdminServiceImpl();
                            stsAdminService.removeTrustedService(config.getInboundAuthKey());
                        } else {
                            throw new IdentityApplicationManagementException(
                                    "missing parameter : " + SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG.getLocalPart());
                        }
                    } catch (Exception e) {
                        String error = "Error while removing a trusted service: " + config.getInboundAuthKey();
                        throw new IdentityApplicationManagementException(error, e);
                    }
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public void onPreCreateInbound(ServiceProvider serviceProvider, boolean isUpdate)
            throws IdentityApplicationManagementException {

        List<String> validationMsg = new ArrayList<>();

        InboundAuthenticationConfig inboundAuthenticationConfig = serviceProvider.getInboundAuthenticationConfig();
        if (inboundAuthenticationConfig != null &&
                inboundAuthenticationConfig.getInboundAuthenticationRequestConfigs() != null) {
            for (InboundAuthenticationRequestConfig authConfig
                    : inboundAuthenticationConfig.getInboundAuthenticationRequestConfigs()) {

                if (IdentityApplicationConstants.Authenticator.WSTrust.NAME.equals(authConfig.getInboundAuthType())) {
                    if (authConfig.getInboundConfiguration() != null) {
                        TrustedServiceData trustedServiceData = unmarshalTrustedServiceData(
                                authConfig.getInboundConfiguration(), serviceProvider.getApplicationName(),
                                serviceProvider.getOwner().getTenantDomain());
                        if (!authConfig.getInboundAuthKey().equals(trustedServiceData.getServiceAddress())) {
                            validationMsg.add(String.format("The Inbound Auth Key of the  application name %s " +
                                            "is not match with ServiceAddress %s.", authConfig.getInboundAuthKey(),
                                    trustedServiceData.getServiceAddress()));
                        }
                        if (trustedServiceData.getCertAlias() == null || "".equals(trustedServiceData.getCertAlias())) {
                            validationMsg.add(String.format("WS CertAlias is not provided  with ServiceAddress %s.",
                                    authConfig.getInboundAuthKey()));
                        }
                    }
                    break;
                }
            }
        }
        if (!validationMsg.isEmpty()) {
            throw new IdentityApplicationManagementValidationException(validationMsg.toArray(new String[0]));
        }
    }

    @Override
    public void doImportServiceProvider(ServiceProvider serviceProvider) throws IdentityApplicationManagementException {

        InboundAuthenticationConfig inboundAuthenticationConfig = serviceProvider.getInboundAuthenticationConfig();
        if (inboundAuthenticationConfig != null &&
                inboundAuthenticationConfig.getInboundAuthenticationRequestConfigs() != null) {

            for (InboundAuthenticationRequestConfig authConfig
                    : inboundAuthenticationConfig.getInboundAuthenticationRequestConfigs()) {
                if (IdentityApplicationConstants.Authenticator.WSTrust.NAME.equals(authConfig.getInboundAuthType())) {
                    TrustedServiceData trustedServiceData = unmarshalTrustedServiceData(
                            authConfig.getInboundConfiguration(), serviceProvider.getApplicationName(),
                            serviceProvider.getOwner().getTenantDomain());
                    String inboundConfiguration = authConfig.getInboundConfiguration();
                    if (StringUtils.isBlank(inboundConfiguration)) {
                        String errorMsg = String.format("No inbound configurations found for wstrust in the imported " +
                                "%s", serviceProvider.getApplicationName());
                        throw new IdentityApplicationManagementException(errorMsg);
                    }
                    try {
                        STSAdminServiceImpl stsAdminService = new STSAdminServiceImpl();
                        stsAdminService.addTrustedService(trustedServiceData.getServiceAddress(),
                                trustedServiceData.getCertAlias());
                    } catch (SecurityConfigException e) {
                        throw new IdentityApplicationManagementException(String.format("Error in adding trusted " +
                                "service data for %s", serviceProvider.getApplicationName()), e);
                    }
                }
            }
        }
    }

    @Override
    public void doExportServiceProvider(ServiceProvider serviceProvider, Boolean exportSecrets)
            throws IdentityApplicationManagementException {

        InboundAuthenticationConfig inboundAuthenticationConfig = serviceProvider.getInboundAuthenticationConfig();
        if (inboundAuthenticationConfig != null &&
                inboundAuthenticationConfig.getInboundAuthenticationRequestConfigs() != null) {

            for (InboundAuthenticationRequestConfig authConfig
                    : inboundAuthenticationConfig.getInboundAuthenticationRequestConfigs()) {
                if (IdentityApplicationConstants.Authenticator.WSTrust.NAME.equals(authConfig.getInboundAuthType())) {
                    String inboundAuthKey = authConfig.getInboundAuthKey();

                    try {
                        STSAdminServiceImpl stsAdminService = new STSAdminServiceImpl();
                        TrustedServiceData[] trustedServices = stsAdminService.getTrustedServices();
                        if (trustedServices != null) {
                            for (TrustedServiceData trustedServiceData : trustedServices) {
                                if (trustedServiceData.getServiceAddress().equals(inboundAuthKey)) {
                                    authConfig.setInboundConfiguration(marshalTrustedServiceData(trustedServiceData));
                                }
                            }
                        }
                    } catch (SecurityConfigException e) {
                        throw new IdentityApplicationManagementException(String.format("Error in getting trusted " +
                                "service data for %s", serviceProvider.getApplicationName()), e);
                    }
                }
            }
        }
    }

    /**
     * Set STS parameters
     *
     * @param samlConfig SAML config
     * @throws org.apache.axis2.AxisFault
     * @throws org.wso2.carbon.registry.api.RegistryException
     */
    private void setSTSParameter(SAMLTokenIssuerConfig samlConfig) throws AxisFault, RegistryException {

        new SecurityServiceAdmin(getAxisConfig(), getConfigSystemRegistry()).
                setServiceParameterElement(ServerConstants.STS_NAME, samlConfig.getParameter());
    }

    /**
     * Get axis config
     *
     * @return axis configuration
     */
    private AxisConfiguration getAxisConfig() {

        return SecurityMgtServiceComponent.getServerConfigurationContext().getAxisConfiguration();
    }

    /**
     * Get config system registry
     *
     * @return config system registry
     * @throws org.wso2.carbon.registry.api.RegistryException
     */
    private Registry getConfigSystemRegistry() throws RegistryException {

        return SecurityMgtServiceComponent.getRegistryService().getConfigSystemRegistry();
    }

    /**
     * Unmarshal Trusted Service Data.
     *
     * @param inboundConfiguration inbound Configuration
     * @param serviceProviderName  service Provider Name
     * @param tenantDomain         tenant Domain
     * @return Trusted Service Data
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    private TrustedServiceData unmarshalTrustedServiceData(String inboundConfiguration, String serviceProviderName,
                                                           String tenantDomain) throws
            IdentityApplicationManagementException {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TrustedServiceData.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (TrustedServiceData) unmarshaller.unmarshal(new ByteArrayInputStream(
                    inboundConfiguration.getBytes(StandardCharsets.UTF_8)));
        } catch (JAXBException e) {
            throw new IdentityApplicationManagementException(String.format("Error in unmarshelling Trusted Service Data"
                    + " %s@%s", serviceProviderName, tenantDomain), e);
        }

    }

    /**
     * Marshal Trusted Service Data.
     *
     * @param trustedServiceData trusted Service Data
     * @return marshaled trusted Service Data
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    private String marshalTrustedServiceData(TrustedServiceData trustedServiceData)
            throws IdentityApplicationManagementException {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TrustedServiceData.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(trustedServiceData, sw);
            return sw.toString();
        } catch (JAXBException e) {
            throw new IdentityApplicationManagementException(String.format("Error in exporting Trusted Service Data " +
                    "%s", trustedServiceData.getServiceAddress()), e);
        }

    }
}
