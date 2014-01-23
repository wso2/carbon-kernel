/**
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.ndatasource.core.utils;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.ndatasource.common.DataSourceConstants;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.DataSourceMetaInfo;
import org.wso2.carbon.ndatasource.core.internal.DataSourceServiceComponent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Data Sources utility class.
 */
public class DataSourceUtils {
	
	private static Log log = LogFactory.getLog(DataSourceUtils.class);
	
	private static SecretResolver secretResolver;
	
	private static ThreadLocal<String> dataSourceId = new ThreadLocal<String>() {
        protected synchronized String initialValue() {
            return null;
        }
    };
    
    public static void setCurrentDataSourceId(String dsId) {
    	dataSourceId.set(dsId);
    }
    
    public static String getCurrentDataSourceId() {
    	return dataSourceId.get();
    }
	
	public static Registry getConfRegistryForTenant(int tenantId) throws DataSourceException {
		try {
			/* be super tenant to retrieve the registry of a given tenant id */
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
					MultitenantConstants.SUPER_TENANT_ID);
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
					MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
			return DataSourceServiceComponent.getRegistryService().getConfigSystemRegistry(
					tenantId);
		} catch (RegistryException e) {
			throw new DataSourceException("Error in retrieving conf registry instance: " + 
		            e.getMessage(), e);
		} finally {
			/* go out of being super tenant */
			PrivilegedCarbonContext.endTenantFlow();
		}
	}
	
	public static Registry getGovRegistryForTenant(int tenantId) throws DataSourceException {
		try {
			/* be super tenant to retrieve the registry of a given tenant id */
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
					MultitenantConstants.SUPER_TENANT_ID);
			return DataSourceServiceComponent.getRegistryService().getGovernanceSystemRegistry(
					tenantId);
		} catch (RegistryException e) {
			throw new DataSourceException("Error in retrieving gov registry instance: " + 
		            e.getMessage(), e);
		} finally {
			/* go out of being super tenant */
			PrivilegedCarbonContext.endTenantFlow();
		}
	}
	
	public static boolean nullAllowEquals(Object lhs, Object rhs) {
		if (lhs == null && rhs == null) {
			return true;
		}
		if ((lhs == null && rhs != null) || (lhs != null && rhs == null)) {
			return false;
		}
		return lhs.equals(rhs);
	}
	
	public static String elementToString(Element element) {
		try {
			if (element == null) {
                                /* return an empty string because, the other way around works the same,
                                where if we give a empty string as the XML, we get a null element
                                from "stringToElement" */
				return "";
			}
		    Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    StringWriter buff = new StringWriter();
		    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		    transformer.transform(new DOMSource(element), new StreamResult(buff));
		    return buff.toString();
		} catch (Exception e) {
			log.error("Error while convering element to string: " + e.getMessage(), e);
			return null;
		}
	}
	
	public static Element stringToElement(String xml) {
		if (xml == null || xml.trim().length() == 0) {
			return null;
		}
		try {
		    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		    return db.parse(new ByteArrayInputStream(xml.getBytes())).getDocumentElement();
		} catch (Exception e) {
			log.error("Error while converting string to element: " + e.getMessage(), e);
			return null;
		}
	}
	
	private static synchronized String loadFromSecureVault(String alias) {
		if (secretResolver == null) {
		    secretResolver = SecretResolverFactory.create((OMElement) null, false);
		    secretResolver.init(DataSourceServiceComponent.
		    		getSecretCallbackHandlerService().getSecretCallbackHandler());
		}
		return secretResolver.resolve(alias);
	}

    private static void secureLoadElement(Element element, boolean checkSecureVault) 
			throws CryptoException {
		if (checkSecureVault) {
			Attr secureAttr = element.getAttributeNodeNS(DataSourceConstants.SECURE_VAULT_NS,
					DataSourceConstants.SECRET_ALIAS_ATTR_NAME);
			if (secureAttr != null) {
				element.setTextContent(loadFromSecureVault(secureAttr.getValue()));
                element.removeAttributeNode(secureAttr);
			} 
		} else {
		    String encryptedStr = element.getAttribute(DataSourceConstants.ENCRYPTED_ATTR_NAME);
		    if (encryptedStr != null) {
			    boolean encrypted = Boolean.parseBoolean(encryptedStr);
			    if (encrypted) {
				    element.setTextContent(new String(CryptoUtil.getDefaultCryptoUtil(
				    		DataSourceServiceComponent.getServerConfigurationService(),
				    		DataSourceServiceComponent.getRegistryService()).
				    		base64DecodeAndDecrypt(element.getTextContent())));
			    }
		    }
		}
		NodeList childNodes = element.getChildNodes();
		int count = childNodes.getLength();
		Node tmpNode;
		for (int i = 0; i < count; i++) {
			tmpNode = childNodes.item(i);
			if (tmpNode instanceof Element) {
				secureLoadElement((Element) tmpNode, checkSecureVault);
			}
		}
	}
	
	public static void secureSaveElement(Element element) throws CryptoException {
		String encryptedStr = element.getAttribute(DataSourceConstants.ENCRYPTED_ATTR_NAME);
		if (encryptedStr != null) {
		    boolean encrypted = Boolean.parseBoolean(encryptedStr);
		    if (encrypted) {
			    element.setTextContent(CryptoUtil.getDefaultCryptoUtil(
			    		DataSourceServiceComponent.getServerConfigurationService(),
			    		DataSourceServiceComponent.getRegistryService()).
			    		encryptAndBase64Encode(element.getTextContent().getBytes()));
		    }
		}
		NodeList childNodes = element.getChildNodes();
		int count = childNodes.getLength();
		Node tmpNode;
		for (int i = 0; i < count; i++) {
			tmpNode = childNodes.item(i);
			if (tmpNode instanceof Element) {
				secureSaveElement((Element) tmpNode);
			}
		}
	}
	
	public static void secureResolveDocument(Document doc, boolean checkSecureVault)
            throws DataSourceException {
        Element element = doc.getDocumentElement();
		if (element != null) {
			try {
				secureLoadElement(element, checkSecureVault);
			} catch (CryptoException e) {
				throw new DataSourceException("Error in secure load of data source meta info: " +
			            e.getMessage(), e);
			}
		}
    }

    public static Document convertToDocument(File file) throws DataSourceException {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setNamespaceAware(true);
        try {
            return fac.newDocumentBuilder().parse(file);
        } catch (Exception e) {
            throw new DataSourceException("Error in creating an XML document from file: " +
                    e.getMessage(), e);
        }
    }

    public static Document convertToDocument(InputStream in) throws DataSourceException {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setNamespaceAware(true);
        try {
            return fac.newDocumentBuilder().parse(in);
        } catch (Exception e) {
            throw new DataSourceException("Error in creating an XML document from stream: " +
                    e.getMessage(), e);
        }
    }
    
    public static InputStream elementToInputStream(Element element) {
		try {
			if (element == null) {
				return null;
			}
		    Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		    transformer.transform(new DOMSource(element), new StreamResult(out));
		    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		    return in;
		} catch (Exception e) {
			log.error("Error while convering element to InputStream: " + e.getMessage(), e);
			return null;
		}
	}
    
    public static Element convertDataSourceMetaInfoToElement(DataSourceMetaInfo dsmInfo, 
    		Marshaller dsmMarshaller) throws DataSourceException{
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	Element element;
		try {
			Document document = factory.newDocumentBuilder().newDocument();
			dsmMarshaller.marshal(dsmInfo, document);
			element = document.getDocumentElement();
		} catch (Exception e) {
			throw new DataSourceException("Error in creating an XML document from stream: " +
                    e.getMessage(), e);
		} 
		return element;
    }

}
