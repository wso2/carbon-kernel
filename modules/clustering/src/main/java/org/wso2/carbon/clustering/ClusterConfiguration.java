package org.wso2.carbon.clustering;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.base.Utils;
import org.wso2.carbon.base.exception.ConfigurationInitializationException;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ClusterConfiguration {


    private static final Logger logger = LoggerFactory.getLogger(ClusterConfiguration.class);

    private Map<String, List<Object>> configuration = new HashMap<String, List<Object>>();
    private boolean isInitialized;


    /**
     * This initializes the server configuration. This method should only be
     * called once, for successive calls, it will be checked.
     *
     * @throws ClusterConfigurationException if the operation failed.
     */
    public synchronized void build() throws ClusterConfigurationException {
        if (isInitialized) {
            return;
        }
        String configurationXMLLocation = System.getProperty("carbon.home") + File.separator +
                                          "repository" + File.separator + "conf" +
                                          File.separator + "cluster.xml";

        InputStream xmlInputStream = null;
        try {
            xmlInputStream = Utils.parseXmlConfiguration(configurationXMLLocation);
            Utils.buildXmlConfiguration(xmlInputStream, configuration);
            isInitialized = true;
        } catch (ConfigurationInitializationException e) {
            throw new ClusterConfigurationException("Error while building cluster configuration",
                                                    e);
        } finally {
            if (xmlInputStream != null) {
                try {
                    xmlInputStream.close();
                } catch (IOException e) {
                    logger.warn("Cannot close input stream", e);
                }
            }
        }
    }

    /**
     * There can be multiple objects with the same key. This will return the
     * first String from them
     *
     * @param key the search key
     * @return value corresponding to the given key
     */
    public String getFirstProperty(String key) {
        List<Object> valueList = configuration.get(key);
        if (valueList == null || !(valueList.get(0) instanceof String)) {
            return null;
        }
        return (String) valueList.get(0);
    }

    public List<Object> getElement(String key) {
        List<Object> value = configuration.get(key);
        if (value == null) {
            return null;
        }
        return value;
    }
}
