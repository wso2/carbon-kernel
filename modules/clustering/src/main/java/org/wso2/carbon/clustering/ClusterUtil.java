package org.wso2.carbon.clustering;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.clustering.hazelcast.wka.WKAConstants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ClusterUtil {

    private static Logger logger = LoggerFactory.getLogger(ClusterUtil.class);


    public static List<ClusterMember> loadWellKnownMembers(
            ClusterConfiguration clusterConfiguration) {
        List<ClusterMember> members = new ArrayList<ClusterMember>();
        List<Object> membersList = clusterConfiguration.getElement("wka.members.member");

        for (Object member : membersList) {
            if (member instanceof Node) {
                NodeList nodeList = ((Node) member).getChildNodes();
                String hostName = null;
                String port = null;
                for (int count = 0; count < nodeList.getLength(); count++) {
                    Node node = nodeList.item(count);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        if (WKAConstants.HOST_NAME.equals(node.getNodeName())) {
                            logger.debug("WKA Member host name {}", node.getTextContent());
                            hostName = node.getTextContent();
                        } else if (WKAConstants.PORT.equals(node.getNodeName())) {
                            logger.debug("WKA Member port {}", node.getTextContent());
                            port = node.getTextContent();
                        }
                    }
                }
                if (hostName != null && port != null) {
                    members.add(new ClusterMember(replaceVariables(hostName),
                                                  Integer.parseInt(replaceVariables(port))));
                }
            }
        }
        return members;
    }

    private static String replaceVariables(String text) {
        int indexOfStartingChars;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        if ((indexOfStartingChars = text.indexOf("${")) != -1 &&
            (indexOfClosingBrace = text.indexOf("}")) != -1) { // Is a property used?
            String var = text.substring(indexOfStartingChars + 2,
                                        indexOfClosingBrace);

            String propValue = System.getProperty(var);
            if (propValue == null) {
                propValue = System.getenv(var);
            }
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue +
                       text.substring(indexOfClosingBrace + 1);
            }
        }
        return text;
    }


    public static boolean shouldInitialize(String agentName) {
        boolean initialize = false;
        try {
            String carbonHome = System.getProperty("carbon.home");
            String clusterXmlLocation = carbonHome + File.separator + "repository" +
                                        File.separator + "conf" + File.separator + "cluster.xml";
            File xmlFile = new File(clusterXmlLocation);
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            String clusterAgent = doc.getDocumentElement().getAttribute("agent");
            boolean isEnabled = Boolean.parseBoolean(doc.getDocumentElement().
                    getAttribute("enable"));

            if (clusterAgent != null && agentName.equals(clusterAgent) && isEnabled) {
                initialize = true;
            }
        } catch (Exception e) {
            logger.error("Error while loading cluster configuration file", e);
        }
        return initialize;
    }

    public static String getIpAddress() throws SocketException {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        String address = "127.0.0.1";

        while (e.hasMoreElements()) {
            NetworkInterface netface = (NetworkInterface) e.nextElement();
            Enumeration addresses = netface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress ip = (InetAddress) addresses.nextElement();
                if (!ip.isLoopbackAddress() && isIP(ip.getHostAddress())) {
                    return ip.getHostAddress();
                }
            }
        }

        return address;
    }

    private static boolean isIP(String hostAddress) {
        return hostAddress.split("[.]").length == 4;
    }

}
