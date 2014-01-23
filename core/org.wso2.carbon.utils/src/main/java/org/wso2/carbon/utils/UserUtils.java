package org.wso2.carbon.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public final class UserUtils {

    private static Log log = LogFactory.getLog(UserUtils.class);

    public static boolean hasMultipleUserStores() throws Exception{
        String pathToUserMgtXML = CarbonUtils.getUserMgtXMLPath();
        if(pathToUserMgtXML == null || "".equals(pathToUserMgtXML)){
            return false;
        }

        File file = new File(pathToUserMgtXML);
        Document doc = null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
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
}
