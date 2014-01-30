package org.wso2.carbon.clustering;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.clustering.spi.ClusteringAgent;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;

public class ClusterUtil {

    private static Logger logger = LoggerFactory.getLogger(ClusterUtil.class);


    /**
     * Build the cluster configuration
     *
     * @param clusteringAgent ClusterAgent
     */
    public static void buildCluster(ClusteringAgent clusteringAgent/*, CarbonConfiguration carbonConfig*/) {

        Document clusterDocument = null;
        logger.info("Clustering has been enabled");

        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//            clusterDocument = dBuilder.parse(xmlInputStream);


//            clusteringAgent.setConfigurationContext(/*configCtx*/);

            //loading the parameters.
//            processParameters(clusterDocument.getgetChildrenWithName(new QName(TAG_PARAMETER)),
//                              clusteringAgent,
//                              null);

            // loading the members
            loadWellKnownMembers(clusteringAgent, clusterDocument);


//            carbonConfig.setClusteringAgent(clusteringAgent);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

//    private boolean isEnabled(OMElement element) {
//        boolean enabled = true;
//        OMAttribute enableAttr = element.getAttribute(new QName("enable"));
//        if (enableAttr != null) {
//            enabled = Boolean.parseBoolean(enableAttr.getAttributeValue().trim());
//        }
//        return enabled;
//    }


    private static void loadWellKnownMembers(ClusteringAgent clusteringAgent,
                                             Document clusterDocument) {
//        clusteringAgent.setStaticMembers(new ArrayList<ClusterMember>());
//        Parameter membershipSchemeParam = clusteringAgent.getParameter("membershipScheme");
//        if (membershipSchemeParam != null) {
//            String membershipScheme = ((String) membershipSchemeParam.getValue()).trim();
//            if (membershipScheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED)) {
//                List<Member> members = new ArrayList<Member>();
//                OMElement membersEle =
//                        clusterElement.getFirstChildWithName(new QName("members"));
//                if (membersEle != null) {
//                    for (Iterator iter = membersEle.getChildrenWithLocalName("member"); iter.hasNext();) {
//                        OMElement memberEle = (OMElement) iter.next();
//                        String hostName =
//                                memberEle.getFirstChildWithName(new QName("hostName")).getText().trim();
//                        String port =
//                                memberEle.getFirstChildWithName(new QName("port")).getText().trim();
//                        members.add(new Member(replaceVariables(hostName),
//                                               Integer.parseInt(replaceVariables(port))));
//                    }
//                }
//                clusteringAgent.setStaticMembers(members);
//            }
//        }
    }

    private String replaceVariables(String text) {
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

//    protected void processParameters(Iterator parameters,
//                                     ParameterInclude parameterInclude,
//                                     ParameterInclude parent) {
//        while (parameters.hasNext()) {
//            // this is to check whether some one has locked the parmeter at the
//            // top level
//            OMElement parameterElement = (OMElement) parameters.next();
//            Parameter parameter = new Parameter();
//            // setting parameterElement
//            parameter.setParameterElement(parameterElement);
//            // setting parameter Name
//            OMAttribute paramName = parameterElement.getAttribute(new QName(ATTRIBUTE_NAME));
//            if (paramName == null) {
//                throw new DeploymentException(Messages.getMessage(
//                        DeploymentErrorMsgs.BAD_PARAMETER_ARGUMENT,
//                        parameterElement.toString()));
//            }
//            parameter.setName(paramName.getAttributeValue());
//            // setting parameter Value (the child element of the parameter)
//            OMElement paramValue = parameterElement.getFirstElement();
//            if (paramValue != null) {
//                parameter.setValue(parameterElement);
//                parameter.setParameterType(Parameter.OM_PARAMETER);
//            } else {
//                String paratextValue = parameterElement.getText();
//
//                parameter.setValue(paratextValue);
//                parameter.setParameterType(Parameter.TEXT_PARAMETER);
//            }
//            // setting locking attribute
//            OMAttribute paramLocked = parameterElement.getAttribute(new QName(
//                    ATTRIBUTE_LOCKED));
//            Parameter parentParam = null;
//            if (parent != null) {
//                parentParam = parent.getParameter(parameter.getName());
//            }
//            if (paramLocked != null) {
//                String lockedValue = paramLocked.getAttributeValue();
//                if (BOOLEAN_TRUE.equals(lockedValue)) {
//                    // if the parameter is locked at some level parameter value
//                    // replace by that
//                    if ((parent != null)
//                        && parent.isParameterLocked(parameter.getName())) {
//                        throw new DeploymentException(Messages.getMessage(
//                                DeploymentErrorMsgs.PARAMETER_LOCKED, parameter.getName()));
//                    } else {
//                        parameter.setLocked(true);
//                    }
//                } else {
//                    parameter.setLocked(false);
//                }
//            }
//            try {
//                if (parent != null) {
//                    if ((parentParam == null)
//                        || !parent.isParameterLocked(parameter.getName())) {
//                        parameterInclude.addParameter(parameter);
//                    }
//                } else {
//                    parameterInclude.addParameter(parameter);
//                }
//            } catch (AxisFault axisFault) {
//                throw new DeploymentException(axisFault);
//            }
//        }
//    }

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

}
