/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.java.security.driver;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.java.security.action.Action;
import org.apache.axis2.java.security.less.LessPermission;
import org.apache.axis2.java.security.less.LessPermissionAccessControlContext;
import org.apache.axis2.java.security.less.LessPermissionPrivilegedExceptionAction;
import org.apache.axis2.java.security.more.MorePermission;
import org.apache.axis2.java.security.more.MorePermissionAccessControlContext;
import org.apache.axis2.java.security.more.MorePermissionPrivilegedExceptionAction;

import java.security.AccessControlException;
import java.security.Permission;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Java2SecTest demostrates the usages of AccessController class and Policy file(s) while Security Manager is enabled:
 * 1. testNoPrivilegePassed shows the usage of no AccessController but it still work fine
 * because it has all the permissions.
 * 2. testNoPrivilegeFailure shows the usage of AccessController with LessPermission.java,
 * which is not right approach.
 * 3. testDoPrivilegePassed shows the correct practice of java 2 security by granting the appropriate
 * permission in the policy file(s0 and wrapping the AccessController calls with MorePermission.java.
 * 4. testDoPrivilegeFailure shows the reverse call order of MorePermission and LessPermission
 * from testDoPrivilegedPassed.
 * 5. testAccessControlContextFailure shows the AccessContext which contains a no-permission class
 * on the stack can cause a failure. In our case, the no-permission class is
 * LessPermissionAccessControlContext.
 */

public class Java2SecTest extends TestCase {
    // Static variable to keep the test result 
    public static String testResult = "";

    // Default constructor
    public Java2SecTest() {
        super();
        System.out.println("\nJava2SecTest ctor 1");
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getDefault());
        System.out.println("Current time => " + sdf.format(cal.getTime()) + "\n");
    }

    // Constructor
    public Java2SecTest(String arg) {
        super(arg);
        System.out.println("\nJava2SecTest ctor 2");
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getDefault());
        System.out.println("Current time => " + sdf.format(cal.getTime()) + "\n");
    }

    // This method is added for running this test as a pure junit test
    public static void main(String[] args) {
        TestRunner.run(suite());

    }

    // This method is added for running this test as a pure junit test
    public static Test suite() {
        TestSuite suite = new TestSuite(Java2SecTest.class);

        return suite;

    }


    /**
     * testNoPrivilegedSuccessed
     */

    public void testNoPrivilegeSuccessed() throws Exception {
        Java2SecTest.testResult = "testNoPrivilegeSuccessed failed.";
        SecurityManager oldSM = null;
        String expectedString = "This line is from public.txt.";

        System.out.println("\ntestNoPrivilegedSuccessed() begins");
        // Check whether the security manager is enabled or not.
        // If not, turn it on
        oldSM = System.getSecurityManager();
        if (oldSM != null) {
            System.out.println("\nSecurity Manager is enabled.");
        } else {
            System.out.println("\nSecurity Manager is disabled.");
            System.out.println("Enabling the default Java Security Manager");
            System.setSecurityManager(new SecurityManager());
        }

        // Run test WITHOUT AccessController.doPrivileged wrapper
        Action dp = new Action("public/public.txt");
        MorePermission mp = new MorePermission(dp, false);
        LessPermission lp = new LessPermission(mp, false);
        lp.takeAction();

        // Disable security manager if it is enabled by this testcsae
        if (System.getSecurityManager() != null && oldSM == null) {
            System.setSecurityManager(null);
            if (System.getSecurityManager() == null) {
                System.out.println("Security Manager is successfully disabled.");
            } else {
                System.out.println("Security Manager is still enabled");
            }
        }
        // Remove extra characters within the result string
        testResult = testResult.replaceAll("\\r", "");
        testResult = testResult.replaceAll("\\n", "");
        System.out.println("Resulting string is " + testResult);

        // Verify the test result by comparing the test result with expected string
        assertTrue("The string contents do not match.",
                   expectedString.equalsIgnoreCase(testResult));

        System.out.println("\ntestNoPrivilegedSuccessed() ends\n\n");
    }


    /**
     * testNoPrivilegedFailure
     */

    public void testNoPrivilegeFailure() throws Exception {
        Java2SecTest.testResult = "testNoPrivilegeFailure failed.";
        SecurityManager oldSM = null;

        System.out.println("\ntestNoPrivilegedFailured() begins");
        // Check whether the security is enable or not.
        // if it is not enabled, turn it on
        oldSM = System.getSecurityManager();
        if (oldSM != null) {
            System.out.println("\nSecurity Manager is enabled.");
        } else {
            System.out.println("\nSecurity Manager is disabled.");
            System.out.println("Enabling the default Security Manager");
            System.setSecurityManager(new SecurityManager());
        }
        // Run test with AccessController.doPrivilege wrapper
        Action dp = new Action("private/private.txt");
        MorePermission mp = new MorePermission(dp, false);
        LessPermission lp = new LessPermission(mp, false);
        try {
            lp.takeAction();
        } catch (Exception e) {
            // verify the test result
            assertTrue("It is not the security exception.",
                       (e instanceof java.security.AccessControlException));
        } finally {
            // Disable security manager if it is enabled by this testcsae
            if (System.getSecurityManager() != null && oldSM == null) {
                System.setSecurityManager(null);
                if (System.getSecurityManager() == null) {
                    System.out.println("Security Manager is successfully disabled.");
                } else {
                    System.out.println("Security Manager is still enabled");
                }
            }
            System.out.println("\ntesNoPrivilegedFailure() ends\n\n");
        }
    }


    /**
     * testDoPrivilegedSuccessed
     */

    public void testDoPrivilegeSuccessed() throws Exception {
        Java2SecTest.testResult = "testDoPrivilegeSuccessed failed.";
        SecurityManager oldSM = null;
        String expectedString = "This line is from private.txt.";

        System.out.println("\ntestDoPrivilegedSuccessed() begins");
        // Check whether the security is enable or not.
        // If it is not enabled, turn it on
        oldSM = System.getSecurityManager();
        if (oldSM != null) {
            System.out.println("\nSecurity Manager is enabled.");
        } else {
            System.out.println("\nSecurity Manager is disabled.");
            System.out.println("Enabling the default Java Security Manager");
            System.setSecurityManager(new SecurityManager());
        }

        // Run test with AccessController.doPrivilege
        Action dp = new Action("private/private.txt");
        MorePermission mp = new MorePermission(dp, true);
        LessPermission lp = new LessPermission(mp, false);
        lp.takeAction();

        // Disable security manager if it is enabled by this testcsae
        if (System.getSecurityManager() != null && oldSM == null) {
            System.setSecurityManager(null);
            if (System.getSecurityManager() == null) {
                System.out.println("Security Manager is successfully disabled.");
            } else {
                System.out.println("Security Manager is still enabled");
            }
        }

        // Remove extra characters within the result string
        testResult = testResult.replaceAll("\\r", "");
        testResult = testResult.replaceAll("\\n", "");
        System.out.println("Resulting string is " + testResult);

        // Verify the test result by comparing the test result with expected string               
        assertTrue("The string contents do not match.",
                   expectedString.equalsIgnoreCase(testResult));
        System.out.println("\ntestDoPrivilegedSuccessed() ends\n\n");
    }


    /**
     * testDoPrivilegedFailure
     */

    public void testDoPrivilegeFailure() throws Exception {
        Java2SecTest.testResult = "testDoPrivilegeFailure failed.";
        SecurityManager oldSM = null;
        String expectedString = "This line is from private.txt.";

        System.out.println("\ntestDoPrivilegedFailure() begins");
        // Check whether the security is enable or not.
        // If it is not enabled, turn it on
        oldSM = System.getSecurityManager();
        if (oldSM != null) {
            System.out.println("\nSecurity Manager is enabled.");
        } else {
            System.out.println("\nSecurity Manager is disabled.");
            System.out.println("Enabling the default Java Security Manager");
            System.setSecurityManager(new SecurityManager());
        }

        // Run test with AccessController.doPrivilege
        Action dp = new Action("private/private.txt");
        MorePermission mp = new MorePermission(dp, false);
        LessPermission lp = new LessPermission(mp, true);
        try {
            mp.takeAction();
        } catch (Exception e) {
            // Verify the test result
            assertTrue("It is not the security exception.",
                       (e instanceof java.security.AccessControlException));

        } finally {
            // Disable security manager if it is enabled by this testcsae
            if (System.getSecurityManager() != null && oldSM == null) {
                System.setSecurityManager(null);
                if (System.getSecurityManager() == null) {
                    System.out.println("Security Manager is successfully disabled.");
                } else {
                    System.out.println("Security Manager is still enabled");
                }
            }
            System.out.println("\ntestDoPrivilegedFailure() ends\n\n");
        }
    }


    /**
     * testAccessControlContextFailure
     */

    public void testAccessControlContextFailure() throws Exception {
        Java2SecTest.testResult = "testAccessControlContextFailure failed.";
        SecurityManager oldSM = null;
        String expectedString = "This line is from private.txt.";

        System.out.println("\ntestAccessControlContextFailure() begins");
        // Check whether the security is enable or not.
        // If it is not enabled, turn it on
        oldSM = System.getSecurityManager();
        if (oldSM != null) {
            System.out.println("\nSecurity Manager is enabled.");
        } else {
            System.out.println("\nSecurity Manager is disabled.");
            System.out.println("Enabling the default Java Security Manager");
            System.setSecurityManager(new SecurityManager());
        }

        // Run test with AccessController.doPrivilege
        Action dp = new Action("private/private.txt");
        MorePermissionAccessControlContext mp = new MorePermissionAccessControlContext(dp, false);
        LessPermissionAccessControlContext lp = new LessPermissionAccessControlContext(mp, true);
        try {
            lp.takeAction();
        } catch (Exception e) {
            // Verify the test result
            assertTrue("It is not the security exception.",
                       (e instanceof java.security.AccessControlException));

        } finally {
            // Disable security manager if it is enabled by this testcsae
            if (System.getSecurityManager() != null && oldSM == null) {
                System.setSecurityManager(null);
                if (System.getSecurityManager() == null) {
                    System.out.println("Security Manager is successfully disabled.");
                } else {
                    System.out.println("Security Manager is still enabled");
                }
            }
            System.out.println("\ntestAccessControlContextFailure() ends\n\n");
        }
    }

    // 2 begins

    /**
     * testPrivilegedExceptionActionSuccessed
     */

    public void testPrivilegedExceptionSuccessed() throws Exception {
        Java2SecTest.testResult = "testPrivielgedExceptionSuccessed failed";
        SecurityManager oldSM = null;
        String expectedString = "This line is from private.txt.";

        System.out.println("\ntestPrivilegedExceptionActionSuccessed() begins");
        // Check whether the security is enable or not.
        // If it is not enabled, turn it on
        oldSM = System.getSecurityManager();
        if (oldSM != null) {
            System.out.println("\nSecurity Manager is enabled.");
        } else {
            System.out.println("\nSecurity Manager is disabled.");
            System.out.println("Enabling the default Java Security Manager");
            System.setSecurityManager(new SecurityManager());
        }

        // Run test with AccessController.doPrivilege
        Action dp = new Action("private/private.txt");
        MorePermissionPrivilegedExceptionAction mp =
                new MorePermissionPrivilegedExceptionAction(dp, true);
        LessPermissionPrivilegedExceptionAction lp =
                new LessPermissionPrivilegedExceptionAction(mp, false);
        lp.takeAction();

        // Disable security manager if it is enabled by this testcsae
        if (System.getSecurityManager() != null && oldSM == null) {
            System.setSecurityManager(null);
            if (System.getSecurityManager() == null) {
                System.out.println("Security Manager is successfully disabled.");
            } else {
                System.out.println("Security Manager is still enabled");
            }
        }

        // Remove extra characters within the result string
        testResult = testResult.replaceAll("\\r", "");
        testResult = testResult.replaceAll("\\n", "");
        System.out.println("testDoPrivilege's result string is " + testResult);

        // Verify the test result by comparing the test result with expected string               
        assertTrue("The string contents do not match.",
                   expectedString.equalsIgnoreCase(testResult));
        System.out.println("\ntestDoPrivilegeSuccessed() ends\n\n");
    }


    /**
     * testPrivilegedExceptionActionFailure
     */

    public void testPrivilegedExceptionActionFailure() throws Exception {
        Java2SecTest.testResult = "testPrivilegedExceptionActionFailure failed.";
        SecurityManager oldSM = null;
        String expectedString = "This line is from private.txt.";

        System.out.println("\ntestPrivilegedExceptionActionFailure() begins");
        // Check whether the security is enable or not.
        // If it is not enabled, turn it on
        oldSM = System.getSecurityManager();
        if (oldSM != null) {
            System.out.println("\nSecurity Manager is enabled.");
        } else {
            System.out.println("\nSecurity Manager is disabled.");
            System.out.println("Enabling the default Java Security Manager");
            System.setSecurityManager(new SecurityManager());
        }

        // Run test with AccessController.doPrivilege
        Action dp = new Action("private/private.txt");
        MorePermissionPrivilegedExceptionAction mp =
                new MorePermissionPrivilegedExceptionAction(dp, false);
        LessPermissionPrivilegedExceptionAction lp =
                new LessPermissionPrivilegedExceptionAction(mp, true);
        try {
            mp.takeAction();
        } catch (Exception e) {
            // Verify the test result
            assertTrue("It is not the security exception.",
                       (e instanceof java.security.PrivilegedActionException));
        } finally {
            // Disable security manager if it is enabled by this testcsae
            if (System.getSecurityManager() != null && oldSM == null) {
                System.setSecurityManager(null);
                if (System.getSecurityManager() == null) {
                    System.out.println("Security Manager is successfully disabled.");
                } else {
                    System.out.println("Security Manager is still enabled");
                }
            }
            System.out.println("\ntestPrivilegedExceptionActionFailure() ends\n\n");
        }
    }

    /**
     * testCheckPermissionAllowed
     */

    public void testCheckPermissionAllowed() throws Exception {
        Java2SecTest.testResult = "testCheckPermissionAllowed failed.";
        SecurityManager oldSM = null;

        System.out.println("\ntestCheckPermissionAllowed() begins.\n");
        boolean allowed = false;
        String fileName = "public/public.txt";

        oldSM = System.getSecurityManager();
        if (oldSM != null) {
            System.out.println("\nSecurity Manager is enabled.");
        } else {
            System.out.println("\nSecurity Manager is disabled.");
            System.out.println("Enabling the default Java Security Manager");
            System.setSecurityManager(new SecurityManager());
        }

        try {
            // Print out maven's base,build, and test direcotories
            String baseDir = AbstractTestCase.basedir;
            System.out.println("basedir => " + baseDir);
            // Convert the \ (back slash) to / (forward slash)
            String baseDirM = baseDir.replace('\\', '/');
            System.out.println("baseDirM => " + baseDirM);
            String fs = "/";

            // Build the file URL
            String fileURL = baseDirM + fs + "test-resources" + fs + "java2sec" + fs + fileName;
            Permission perm = new java.io.FilePermission(fileURL, "read");
            AccessController.checkPermission(perm);
            allowed = true;
        } catch (Exception e) {
            if (e instanceof AccessControlException) {
                e.printStackTrace(System.out);
            }
        } finally {
            assertTrue("Accessing to public.txt file is denied; Test failed.", allowed);
            // Disable security manager if it is enabled by this testcsae
            if (System.getSecurityManager() != null && oldSM == null) {
                System.setSecurityManager(null);
                if (System.getSecurityManager() == null) {
                    System.out.println("Security Manager is successfully disabled.");
                } else {
                    System.out.println("Security Manager is still enabled");
                }
            }
            System.out.println("\ntestCheckPermissionAllowed() ends.\n");
        }

    }


    /**
     * testCheckPermissionDenied
     */

    public void testCheckPermissionDenied() throws Exception {
        Java2SecTest.testResult = "testCheckPermissionDenied failed";
        SecurityManager oldSM = null;

        System.out.println("\ntestCheckPermissionDenied() begins.\n");
        boolean denied = true;
        String fileName = "private/private.txt";

        oldSM = System.getSecurityManager();
        if (oldSM != null) {
            System.out.println("\nSecurity Manager is enabled.");
        } else {
            System.out.println("\nSecurity Manager is disabled.");
            System.out.println("Enabling the default Java Security Manager");
            System.setSecurityManager(new SecurityManager());
        }

        try {
            // Print out maven's base,build, and test direcotories
            String baseDir = AbstractTestCase.basedir;
            System.out.println("basedir => " + baseDir);

            // Convert the \ (back slash) to / (forward slash)
            String baseDirM = baseDir.replace('\\', '/');
            System.out.println("baseDirM => " + baseDirM);

            String fs = "/";

            // Build the file URL
            String fileURL = baseDirM + fs + "test-resources" + fs + "java2sec" + fs + fileName;
            Permission perm = new java.io.FilePermission(fileURL, "read");
            AccessController.checkPermission(perm);
            denied = false;
        } catch (Exception e) {
            if (!(e instanceof AccessControlException)) {
                denied = false;
            }
            e.printStackTrace(System.out);
        } finally {
            assertTrue("Accessing to private.txt file is allowed; Test failed.", denied);

            // Disable security manager if it is enabled by this testcsae
            if (System.getSecurityManager() != null && oldSM == null) {
                System.setSecurityManager(null);
                if (System.getSecurityManager() == null) {
                    System.out.println("Security Manager is successfully disabled.");
                } else {
                    System.out.println("Security Manager is still enabled");
                }
            }
            System.out.println("\ntestCheckPermissionDenied() ends.\n");
        }
    }
}
