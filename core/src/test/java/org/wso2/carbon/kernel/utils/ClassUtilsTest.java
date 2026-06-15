package org.wso2.carbon.utils;

import org.testng.annotations.Test;

public class ClassUtilsTest {

    // 1. We create a tiny fake object to test with
    private static class TestTarget {
        private String secretMessage = "Hello";
    }

    // Test 1: Test that it works perfectly under normal conditions
    @Test
    public void testSetToPrivateFieldSuccess() {
        TestTarget target = new TestTarget();
        
        // Change "secretMessage" to "Success"
        ClassUtils.setToPrivateField(target, "secretMessage", "Success");
    }

    // Test 2: THE GAP FIX! Test what happens when a field doesn't exist
    @Test(expectedExceptions = RuntimeException.class)
    public void testSetToPrivateFieldThrowsExceptionWhenFieldNotFound() {
        TestTarget target = new TestTarget();

        // This forces ClassUtils to jump into the catch block and throw a RuntimeException.
        ClassUtils.setToPrivateField(target, "thisFieldDoesNotExist", "Boom");
    }
}
