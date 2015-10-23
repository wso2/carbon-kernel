package org.wso2.carbon.kernel.internal.startupcoordinator;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

public class MultiCounterTest {

    private MultiCounter<String> multiCounter;
    private int randomInt;
    private Set<String> keySet;

    @BeforeClass
    public void init() {
        multiCounter = new MultiCounter<>();
        Double randomNumber = Math.random() % 100;
        randomInt = randomNumber.intValue() + 100;
        keySet = new HashSet<String>();
        String key;

        for (int i = 0; i < randomInt; i++) {
            key = "key-" + i;
            multiCounter.incrementAndGet(key);
            keySet.add(key);
        }
    }

    @Test
    public void testIncrementAndGet() throws Exception {
        keySet.add("test-key");
        for (int i = 0; i < randomInt; i++) {
            Assert.assertEquals(multiCounter.incrementAndGet("test-key"), i + 1);
        }

    }

    @Test(dependsOnMethods = "testIncrementAndGet")
    public void testDecrementAndGet() throws Exception {
        Assert.assertEquals(multiCounter.decrementAndGet("test-key"), randomInt - 1);
    }

    @Test(dependsOnMethods = "testDecrementAndGet")
    public void testGet() throws Exception {
        Assert.assertEquals(multiCounter.get("test-key"), randomInt - 1);
    }

    @Test(dependsOnMethods = "testIncrementAndGet")
    public void testGetAllKeys() throws Exception {
        keySet.equals(multiCounter.getAllKeys());
    }
}
