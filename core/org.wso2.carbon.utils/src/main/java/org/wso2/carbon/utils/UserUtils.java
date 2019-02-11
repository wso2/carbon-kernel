package org.wso2.carbon.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;

public final class UserUtils {

    private static Log log = LogFactory.getLog(UserUtils.class);
    private static final int ENTITY_EXPANSION_LIMIT = 0;

    public static boolean hasMultipleUserStores() throws Exception{
        String pathToUserMgtXML = CarbonUtils.getUserMgtXMLPath();
        if(pathToUserMgtXML == null || "".equals(pathToUserMgtXML)){
            return false;
        }

        File file = new File(pathToUserMgtXML);
        Document doc = null;
        try {
            DocumentBuilderFactory docBuilderFactory = getSecuredDocumentBuilder();
            doc = docBuilderFactory.newDocumentBuilder().parse(file);
        } catch (Exception e) {
            log.error("Failed pasring config file " + pathToUserMgtXML + ". ", e);
            throw e;
        }

        NodeList userMgtConfig = doc.getElementsByTagName("UserStoreManager");
        if(userMgtConfig != null && userMgtConfig.getLength() > 1){
            return true;
        }
        return false;
    }

    private static DocumentBuilderFactory getSecuredDocumentBuilder() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        } catch (ParserConfigurationException e) {
            log.error(
                    "Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                            Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE);
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return dbf;
    }
}
