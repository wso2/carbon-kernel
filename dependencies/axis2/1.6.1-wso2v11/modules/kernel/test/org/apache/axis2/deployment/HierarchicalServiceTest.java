package org.apache.axis2.deployment;

import junit.framework.TestCase;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.context.ConfigurationContextFactory;


public class HierarchicalServiceTest extends TestCase {
    AxisConfiguration axisConfig;
    String repo = AbstractTestCase.basedir + "/test-resources/deployment/hierarchicalServiceRepo";


    protected void setUp() throws Exception {
        axisConfig = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repo,
                repo + "/axis2.xml").getAxisConfiguration();
    }

    public void testHierarchicalServices() throws AxisFault {
        //Test for foo/bar/1.0.0 hierarchy
        AxisServiceGroup sg100 = axisConfig.getServiceGroup("foo/bar/1.0.0/testService");
        assertNotNull(sg100);
        AxisService hie100service1 = axisConfig.getService("foo/bar/1.0.0/Hie100Service1");
        assertNotNull(hie100service1);
        AxisService hie100service2 = axisConfig.getService("foo/bar/1.0.0/Hie100Service2");
        assertNotNull(hie100service2);

        //Test for foo/bar/1.0.1 hierarchy
        AxisServiceGroup sg101 = axisConfig.getServiceGroup("foo/bar/1.0.1/testService");
        assertNotNull(sg101);
        AxisService hie101service1 = axisConfig.getService("foo/bar/1.0.1/Hie101Service1");
        assertNotNull(hie101service1);
        AxisService hie101service2 = axisConfig.getService("foo/bar/1.0.1/Hie101Service2");
        assertNotNull(hie101service2);
    }

}
