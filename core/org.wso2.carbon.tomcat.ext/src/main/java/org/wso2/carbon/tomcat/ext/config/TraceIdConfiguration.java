package org.wso2.carbon.tomcat.ext.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

public class TraceIdConfiguration {

    private static Log log = LogFactory.getLog(TraceIdConfiguration.class);

    private static String TRACE_ID;
    private static final String TRACE_ID_CONFIG_XML = "traceId-config.xml";
    private static final String SEPARATOR = "-";
    private static final String ELEM_TRACE_ID_CONFIG = "TraceId";
    private static final String ELEM_TRACE_ID_NAME = "TraceIdName";
    private static final String ELEM_TRAIL_CHARACTER = "TrailCharacter";
    private static final String ELEM_ORGANIZATION = "Organization";
    private static final String ATTR_DISABLED = "disabled";

    /**
     * Singleton TraceId Configuration instance
     */
    private static TraceIdConfiguration instance = new TraceIdConfiguration();

    public static class TraceIdConfig {

        private String trailCharacter;
        private String traceIdName;
        private String organization;

        private TraceIdConfig(String trailCharacter, String organization, String traceId) {
            this.trailCharacter = trailCharacter;
            this.traceIdName = traceId;
            this.organization = organization;
        }

        public String getTrailCharacter() {
            return trailCharacter;
        }

        public String getTraceIdName() {
            return traceIdName;
        }

        public String getOrganization() {
            return organization;
        }

    }

    /**
     * Returns the generated traceId based on the configuration.
     *
     * @return TRACE_ID
     */
    public String getTraceId() {
        return TRACE_ID;
    }

    /**
     * Returns the TraceIdConfiguration singleton instance
     *
     * @return TraceIdConfiguration singleton instance
     */
    public static TraceIdConfiguration getInstance() {

        return instance;
    }

    private TraceIdConfiguration() {

        initialize();
    }

    /**
     * Read the TraceIdConfiguration info from the file and populate the in-memory model
     */
    private void initialize() {
        String traceIdConfigFilePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + TRACE_ID_CONFIG_XML;

        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(new File(traceIdConfigFilePath));
            OMElement documentElement = new StAXOMBuilder(fileInputStream).getDocumentElement();
            // For every traceId defined, create traceId instance
            for (Iterator traceIdElements = documentElement.getChildrenWithLocalName(ELEM_TRACE_ID_CONFIG);
                 traceIdElements.hasNext(); ) {
                TraceIdConfig traceIdConfig = processTraceIdConfigElement((OMElement) traceIdElements.next());

                if (traceIdConfig != null) {
                    TRACE_ID = traceIdConfig.getTrailCharacter() + SEPARATOR +
                            traceIdConfig.getOrganization() + SEPARATOR + traceIdConfig.getTraceIdName();
                }
            }

        } catch (FileNotFoundException| NullPointerException e) {
            log.info("traceId-config.xml file is not available. Carbon Server is starting with the" +
                    "default trace id configurations.");

        } catch (XMLStreamException e) {
            log.error("Error reading the traceId-config.xml. Carbon Server is starting with the default" +
                    "trace id configurations.");
        }
        finally{
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                log.warn("Unable to close the file input stream created for traceId-config.xml");
            }
        }
    }

    /**
     * Create  TraceIdConfig elements for each TraceIdConfig entry.
     *
     * @param  traceIdConfigElem OMElement for CorrelationID
     * @return TraceIdConfig object
     */
    private TraceIdConfig processTraceIdConfigElement(OMElement traceIdConfigElem) {

        // check whether the disabled attribute is set
        boolean disabled = false;
        if(traceIdConfigElem.getAttribute(new QName(ATTR_DISABLED)) != null){
            disabled = Boolean.parseBoolean(traceIdConfigElem.getAttribute(
                    new QName(ATTR_DISABLED)).getAttributeValue());
        }

        if (disabled) {
            return null;
        }

        // Read the trail character.
        String trailChar = null;
        for(Iterator organizationElemItr = traceIdConfigElem.getChildrenWithLocalName(ELEM_TRAIL_CHARACTER);
            organizationElemItr.hasNext();) {
            trailChar = ((OMElement)organizationElemItr.next()).getText();
        }

        if(trailChar == null) {
            log.warn("A TraceId configuration should have a 'TrailCharacter' element.");
            return null;
        }

        // Read the organization.
        String organization = null;
        for(Iterator organizationElemItr = traceIdConfigElem.getChildrenWithLocalName(ELEM_ORGANIZATION);
            organizationElemItr.hasNext();) {
            organization = ((OMElement)organizationElemItr.next()).getText();
        }

        if(organization == null) {
            log.warn("A TraceId configuration should have an 'Organization' element.");
            return null;
        }

        // Read the traceId.
        String traceIdName = null;
        for(Iterator traceIdElemItr = traceIdConfigElem.getChildrenWithLocalName(ELEM_TRACE_ID_NAME);
            traceIdElemItr.hasNext();){
            traceIdName = ((OMElement)traceIdElemItr.next()).getText();
        }

        if(traceIdName == null) {
            log.warn("A TraceId configuration should have a `TraceIdName` element.");
            return null;
        }

        TraceIdConfig traceIdConfig =
                new TraceIdConfig(trailChar, organization, traceIdName);

        return traceIdConfig;

    }
}
