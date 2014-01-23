/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.core.transports.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.core.transports.CarbonHttpRequest;
import org.wso2.carbon.core.transports.CarbonHttpResponse;
import org.wso2.carbon.core.transports.HttpGetRequestProcessor;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

/**
 *
 */
public class CertProcessor implements HttpGetRequestProcessor {
    private static Log log = LogFactory.getLog(CertProcessor.class);
    private CarbonCoreDataHolder dataHolder = CarbonCoreDataHolder.getInstance();

    public CertProcessor() {
        super();
        //TODO: Method implementation
    }

    public void process(CarbonHttpRequest request,
                        CarbonHttpResponse response,
                        ConfigurationContext configurationContext) throws Exception {
        String requestURI = request.getRequestURI();
        String contextPath = configurationContext.getServiceContextPath();
        String serviceName =
                requestURI.substring(requestURI.indexOf(contextPath) + contextPath.length() + 1);
        
        AxisService axisService =
                configurationContext.getAxisConfiguration().getServiceForActivation(serviceName);
        OutputStream outputStream = response.getOutputStream();
        
        if (!axisService.isActive()) {
            response.addHeader(HTTP.CONTENT_TYPE, "text/html");
            outputStream.write(("<h4>Service " + serviceName +
                                " is inactive. Cannot retrieve certificate.</h4>").getBytes());
            outputStream.flush();
        } else {

            RegistryService registryService = dataHolder.getRegistryService();
            Registry registry = registryService.getConfigSystemRegistry();

            String servicePath = RegistryResources.SERVICE_GROUPS
            + axisService.getAxisServiceGroup().getServiceGroupName()
            + RegistryResources.SERVICES + axisService.getName();

            Resource serviceResource = registry.get(servicePath);
            Association[] assoc = registry.getAssociations(servicePath, RegistryResources.Associations.PRIVATE_KEYSTORE);
            
            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(
                    MultitenantConstants.SUPER_TENANT_ID);
            
            KeyStore keyStore = null;
            if(assoc.length < 1){

                boolean httpsEnabled = false;
                Association[] associations =
                    registry.getAssociations(servicePath, RegistryResources.Associations.EXPOSED_TRANSPORTS);
                for (Association association : associations) {
                    Resource resource = registry.get(association.getDestinationPath());
                    String transportProtocol = resource.getProperty(RegistryResources.Transports.PROTOCOL_NAME);
                    if(transportProtocol.equals("https")){
                        httpsEnabled = true;
                        break;
                    }
                    resource.discard();
                }
                
                if (httpsEnabled ||Boolean.valueOf(serviceResource.getProperty(RegistryResources.ServiceProperties.EXPOSED_ON_ALL_TANSPORTS))) {
                    keyStore = keyStoreManager.getPrimaryKeyStore();
                } 
            } else {
                KeyStore ks = null;
                String kspath = assoc[0].getDestinationPath();
                if(kspath.equals(RegistryResources.SecurityManagement.PRIMARY_KEYSTORE_PHANTOM_RESOURCE)){
                    keyStore = keyStoreManager.getPrimaryKeyStore();
                }else{
                    String keyStoreName = kspath.substring(kspath.lastIndexOf('/')+1);
                    keyStore = keyStoreManager.getKeyStore(keyStoreName);
                }
            }
            serviceResource.discard();

            String alias = null;
            if(keyStore != null){
                alias = KeyStoreUtil.getPrivateKeyAlias(keyStore);
            }
            
            if(alias != null){
                Certificate cert = KeyStoreUtil.getCertificate(alias, keyStore);
                serializeCert(cert, response, outputStream, serviceName);
            }else {
                response.addHeader(HTTP.CONTENT_TYPE, "text/html");
                outputStream.write(("<h4>Service " + serviceName +
                                    " does not have a private key.</h4>").getBytes());
                outputStream.flush();
            }
        }
    }

    /**
     * Pump out the certificate
     *
     * @param certificate  cert
     * @param response     response
     * @param outputStream out stream
     * @param serviceName  service name
     * @throws AxisFault will be thrown
     */
    private void serializeCert(Certificate certificate, CarbonHttpResponse response,
                               OutputStream outputStream, String serviceName) throws AxisFault {
        try {
            response.addHeader(HTTP.CONTENT_TYPE, "application/octet-stream");
            response.addHeader("Content-Disposition", "filename=" + serviceName + ".cert");
            outputStream.write(certificate.getEncoded());
        } catch (CertificateEncodingException e) {
            String msg = "Could not get encoded format of certificate";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (IOException e) {
            String msg = "Faliour when serializing to stream";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } finally {
            try {
                outputStream.flush();
            } catch (IOException e) {
                String msg = "Faliour when serializing to stream";
                log.error(msg, e);
            }
        }
    }
}
