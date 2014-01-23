package org.apache.axis2.description;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.wsdl.WSDLConstants;

/**
 * This is a data-driven test class that verifies that
 * <code>AxisBindingOperation</code>s, <code>AxisOpertion</code>s and
 * <code>AxisMessage</code>s objects are consistently built when the
 * <code>WSDLToAxisServiceBuilder.isServerSide</code> attribute is set both to
 * <code>true</code> and <code>false</code> and the <code>AxisService</code> is
 * populated. <p/> There are assertions that verify that operations' MEPs,
 * message labels, message directions AND schema elements tight to the specific
 * <code>AxisMessage</code>s are correctly populated.
 */
public class MessageExchangePatternsTest extends AbstractTestCase 
        implements WSDL2Constants {

    protected static final String WSDL_PATH = "wsdl/meps-wsdl11.wsdl";
    
    // Convenient access to reversed mappings
    private static final Map REVERSED_MEP;
    private static final Map REVERSED_DIRECTION;
    private static final Map REVERSED_MESSAGE_LABEL;
    private static final Map MESSAGE_LABEL_TO_DIRECTION;
    
    static {
        REVERSED_MEP = new HashMap();
        REVERSED_MEP.put(MEP_URI_OUT_IN, MEP_URI_IN_OUT);
        REVERSED_MEP.put(MEP_URI_OUT_ONLY, MEP_URI_IN_ONLY);
        REVERSED_MEP.put(MEP_URI_IN_OUT, MEP_URI_OUT_IN);
        // Axis2 maps the "robust-out-only" operation type as "out-only"
        REVERSED_MEP.put(MEP_URI_ROBUST_IN_ONLY, MEP_URI_OUT_ONLY);
        REVERSED_MEP.put(MEP_URI_IN_ONLY, MEP_URI_OUT_ONLY);

        REVERSED_DIRECTION = new HashMap();
        REVERSED_DIRECTION.put(WSDLConstants.WSDL_MESSAGE_DIRECTION_IN,
            WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
        REVERSED_DIRECTION.put(WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT,
            WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);

        REVERSED_MESSAGE_LABEL = new HashMap();
        REVERSED_MESSAGE_LABEL.put(MESSAGE_LABEL_IN, MESSAGE_LABEL_OUT);
        REVERSED_MESSAGE_LABEL.put(MESSAGE_LABEL_OUT, MESSAGE_LABEL_IN);

        MESSAGE_LABEL_TO_DIRECTION = new HashMap();
        MESSAGE_LABEL_TO_DIRECTION.put(MESSAGE_LABEL_IN,
            WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);
        MESSAGE_LABEL_TO_DIRECTION.put(MESSAGE_LABEL_OUT,
            WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
    }
    
    protected static String getMEP(String serverSideMEP, boolean isServerSide) {
        return isServerSide ? serverSideMEP : (String)REVERSED_MEP.get(serverSideMEP);
    }
    
    protected static String getMessageDirection(String serverSideMessageDirection, boolean isServerSide) {
        return isServerSide ? serverSideMessageDirection : 
            (String)REVERSED_DIRECTION.get(serverSideMessageDirection);
    }
    
    protected static String getMessageLabel(String serverSideMessageLabel, boolean isServerSide) {
        return isServerSide ? serverSideMessageLabel : 
            (String)REVERSED_MESSAGE_LABEL.get(serverSideMessageLabel);
    }
    
    protected static String getDirectionFromMessageLabel(String serverSideMessageLabel, boolean isServerSide) {
        String serverSideMessageDirection = (String)MESSAGE_LABEL_TO_DIRECTION.get(serverSideMessageLabel);
        return getMessageDirection(serverSideMessageDirection, isServerSide);
    }

    private AxisBindingOperation bindingOperation;
    private TestConfig testConfig;

    public MessageExchangePatternsTest(String testName_) {
        super(testName_);
    }

    public static Test suite() {

        TestConfig[] testConfigs = buildTestConfigs(WSDL_PATH);

        String className = MessageExchangePatternsTest.class.getName();
        TestSuite suite = new TestSuite(className);
        for (int i = 0, n = testConfigs.length; i < n; i++) {
            /* Have JUnit create a TestCase instance for every test method of
               this class. */
            TestSuite testCases = new TestSuite(
                MessageExchangePatternsTest.class, testConfigs[i].toString());

            /* Loop through the TestCase instances and inject them with the test
               data. */
            Enumeration tests = testCases.tests();
            while (tests.hasMoreElements()) {
                MessageExchangePatternsTest test = 
                    (MessageExchangePatternsTest)tests.nextElement();
                test.setTestConfig(testConfigs[i]);
            }

            suite.addTest(testCases);
        }
        return suite;
    }

    protected static TestConfig[] buildTestConfigs(String wsdlPath) {
        // Populate WSDL information
        final String targetNamespace = "http://www.example.org";
        final QName serviceName = new QName(targetNamespace, "message-exchange-patterns");
        final Map operationNamesToServerSideMEP = new HashMap();
        operationNamesToServerSideMEP.put("out-in", MEP_URI_OUT_IN);
        operationNamesToServerSideMEP.put("out-only", MEP_URI_OUT_ONLY);
        operationNamesToServerSideMEP.put("in-out", MEP_URI_IN_OUT);
        operationNamesToServerSideMEP.put("robust-in-only", MEP_URI_ROBUST_IN_ONLY);
        // Axis2 maps the "robust-out-only" operation type as "out-only"
        operationNamesToServerSideMEP.put("robust-out-only", MEP_URI_OUT_ONLY);
        operationNamesToServerSideMEP.put("in-only", MEP_URI_IN_ONLY);
        final String[] portNames = {"soap11", "soap12"};
        final boolean isServerSide[] = {true, false};
        final QName inputSchemaElementName = new QName(targetNamespace, "generic-element-in");
        final QName outputSchemaElementName = new QName(targetNamespace, "generic-element-out");

        /* Create TestConfig objects using combinations between port types (2),
           operations (5) and the isServerSide flag */
        List testConfigs = new ArrayList();
        Iterator mepsIt = operationNamesToServerSideMEP.entrySet().iterator();
        while (mepsIt.hasNext()) {
            Map.Entry operationNameToMEP = (Map.Entry)mepsIt.next();
            QName operationName = new QName(targetNamespace, (String)operationNameToMEP.getKey());
            String serverSideMEP = (String)operationNameToMEP.getValue();
            for (int i = 0; i < portNames.length; i++) {
                OperationReference operationReference = new OperationReference(
                    wsdlPath, serviceName, portNames[i], operationName);
                for (int j = 0; j < isServerSide.length; j++) {
                    String expectedMEP = getMEP(serverSideMEP, isServerSide[j]);
                    testConfigs.add(new TestConfig(
                        operationReference, isServerSide[j], expectedMEP,
                        inputSchemaElementName, outputSchemaElementName
                    ));
                }
            }
        }
        return (TestConfig[])testConfigs.toArray(new TestConfig[testConfigs.size()]);
    }
    
    public String getName() {
        TestConfig testConfig = getTestConfig();
        return super.getName() + ". Expected MEP: " +
            testConfig.getExpectedMep() + " ; isServerSide: " +
            testConfig.isServerSide();
    }

    public void setUp() throws Exception {
        TestConfig testConfig = getTestConfig();
        OperationReference operationReference = testConfig.getOperationReference();
        InputStream contentTypeWsdlIn = getTestResource(operationReference.getWsdlPath());
        // Populate AxisService (WSDL port)
        WSDL11ToAxisServiceBuilder wsdl11Builder = new WSDL11ToAxisServiceBuilder(
            contentTypeWsdlIn, operationReference.getServiceName(),
            operationReference.getPortName());
        wsdl11Builder.setServerSide(testConfig.isServerSide());
        AxisService service = wsdl11Builder.populateService();

        // Get Binding Operation
        AxisEndpoint endpoint = service.getEndpoint(operationReference.getPortName());
        AxisBinding binding = endpoint.getBinding();
        bindingOperation = (AxisBindingOperation)binding.getChild(
            operationReference.getOperationName());
    }
    
    public void testMessageExchangePattern() {
        TestConfig testConfig = getTestConfig();
        AxisOperation axisOperation =  bindingOperation.getAxisOperation();
        Assert.assertEquals("The Message Exchange Pattern is not the expected", 
            testConfig.getExpectedMep(), axisOperation.getMessageExchangePattern());
    }
    
    public void testInputMessageDirection() {
        doTestMessageDirection(MESSAGE_LABEL_IN, 
            getTestConfig().getExpectedInputSchemaElementName());
    }
    
    public void testOutputMessageDirection() {
        doTestMessageDirection(MESSAGE_LABEL_OUT,
            getTestConfig().getExpectedOutputSchemaElementName());
    }
    
    protected void doTestMessageDirection(String serverSideMessageLabel,
                                          QName expectedSchemaElementName) {
        TestConfig testConfig = getTestConfig();
        AxisOperation axisOperation =  bindingOperation.getAxisOperation();
        
        String messageLabel = getMessageLabel(serverSideMessageLabel, 
            testConfig.isServerSide());
        String expectedDirection = getDirectionFromMessageLabel(serverSideMessageLabel, 
            testConfig.isServerSide());
        
        try {
            AxisMessage axisMessage = (AxisMessage)axisOperation.getMessage(messageLabel);
            
            Assert.assertEquals("The Message Direction is not the expected", 
                expectedDirection, axisMessage.getDirection());
            
            Assert.assertEquals("The Schema Element Name is not the expected", 
                expectedSchemaElementName, 
                axisMessage.getElementQName());
            
        } catch (UnsupportedOperationException e) {
            // OK, AxisOperation have no message within messageLabel
        }
    }

    protected TestConfig getTestConfig() {
        return testConfig;
    }

    protected void setTestConfig(TestConfig testConfig_) {
        testConfig = testConfig_;
    }
    
    protected static class TestConfig {

        private final OperationReference operationReference;
        private final boolean isServerSide;
        private final String expectedMep;
        private final QName expectedInputSchemaElementName;
        private final QName expectedOutputSchemaElementName;

        public TestConfig(OperationReference operationReference, 
                          boolean isServerSide,
                          String expectedMep,
                          QName expectedInputSchemaElementName,
                          QName expectedOutputSchemaElementName) {
            this.operationReference = operationReference;
            this.isServerSide = isServerSide;
            this.expectedMep = expectedMep;
            this.expectedInputSchemaElementName = expectedInputSchemaElementName;
            this.expectedOutputSchemaElementName = expectedOutputSchemaElementName;
        }
        
        public OperationReference getOperationReference() {
            return operationReference;
        }

        public boolean isServerSide() {
            return isServerSide;
        }

        public String getExpectedMep() {
            return expectedMep;
        }

        public QName getExpectedInputSchemaElementName() {
            return expectedInputSchemaElementName;
        }

        public QName getExpectedOutputSchemaElementName() {
            return expectedOutputSchemaElementName;
        }

        public String toString() {
            OperationReference opRef = getOperationReference();
            return "port:" + opRef.getPortName() + "; operation:" + 
                opRef.getOperationName() + "; isServerSide:" + isServerSide() + 
                "; expectedMEP:" + getExpectedMep();
        }
    }

}
