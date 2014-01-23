package org.apache.axis2.util;

import junit.framework.TestCase;

public class OnDemandLoggerTest extends TestCase {

    public void test() {
        OnDemandLogger log = new OnDemandLogger(OnDemandLoggerTest.class);
        
        // log should not be available until needed
        assertTrue(!log.hasLog());
        
        // This triggers a usage and should cause the logger to be built
        log.isDebugEnabled();
        assertTrue(log.hasLog());
        
        // Force logger reset
        log.resetLog();
        assertTrue(!log.hasLog());
    }
}
