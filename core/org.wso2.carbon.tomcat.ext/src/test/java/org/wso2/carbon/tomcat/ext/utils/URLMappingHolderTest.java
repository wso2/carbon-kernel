/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.tomcat.ext.utils;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.logging.Logger;

/**
 * URLMappingHolderTest includes test scenarios for
 * [1] functions, getInstance (),  and getTenantName (), isUrlMappingExists (),
 * removeUrlMappingMap () and getUrlMappingsPerApplication of URLMappingHolder.
 * [2] properties, defaultHost and urlMappingOfApplication of URLMappingHolder.
 * @since 4.4.19
 */
public class URLMappingHolderTest {

    private static final Logger log = Logger.getLogger("URLMappingHolderTest");

    /**
     * Checks if getInstance returns instance of correct class.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.utils"})
    public void testGetInstance () {
        log.info("Testing if getInstance () returns instance of correct class");
        Assert.assertTrue(URLMappingHolder.getInstance().getClass() == URLMappingHolder.class,
                "Returned instance class does not match to 'URLMappingHolder'");
    }

    @Test(groups = {"org.wso2.carbon.tomcat.ext.utils"},
            description = "Testing getters and setters for defaultHost.")
    public void testDefaultHost () {
        URLMappingHolder urlMappingHolder = URLMappingHolder.getInstance();
        // calling set method
        urlMappingHolder.setDefaultHost("example.com");
        // checking retrieved values
        log.info("Testing getters and setters for tenant");
        Assert.assertEquals(urlMappingHolder.getDefaultHost(), "example.com",
                "Retrieved value did not match with set value for defaultHost");
    }

    @Test(groups = {"org.wso2.carbon.tomcat.ext.utils"},
            description = "Testing getters and setters for urlMappingOfApplication.")
    public void testUrlMappingOfApplication () {
        URLMappingHolder urlMappingHolder = URLMappingHolder.getInstance();
        // calling set method
        urlMappingHolder.putUrlMappingForApplication("http://example.com/apps/foo", "foo");
        // checking retrieved values
        log.info("Testing getters and setters for urlMappingOfApplication");
        Assert.assertEquals("foo", urlMappingHolder.getApplicationFromUrlMapping("http://example.com/apps/foo"),
                "Retrieved value did not match with set value for urlMappingOfApplication");
    }

    /**
     * Checks isUrlMappingExists functionality.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.utils"})
    public void testIsUrlMappingExists () {
        URLMappingHolder urlMappingHolder = URLMappingHolder.getInstance();
        // calling set method
        urlMappingHolder.putUrlMappingForApplication("http://example.com/apps/foo", "foo");
        // checking retrieved values
        log.info("Testing isUrlMappingExists () for already existing value");
        Assert.assertTrue(urlMappingHolder.isUrlMappingExists("http://example.com/apps/foo"),
                "Method isUrlMappingExists () does not correctly validate already existing entries");
        log.info("Testing isUrlMappingExists () for non-existing value");
        Assert.assertTrue(!(urlMappingHolder.isUrlMappingExists("http://example.com/apps/hello")),
                "Method isUrlMappingExists () does not correctly validate non-existing entries");
    }

    /**
     * Checks removeUrlMappingMap functionality.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.utils"})
    public void testRemoveUrlMappingMap () {
        URLMappingHolder urlMappingHolder = URLMappingHolder.getInstance();
        // calling set method
        urlMappingHolder.putUrlMappingForApplication("http://example.com/apps/foo", "foo");
        urlMappingHolder.putUrlMappingForApplication("http://example.com/apps/bar", "bar");
        urlMappingHolder.putUrlMappingForApplication("http://example.com/apps/hello", "hello");
        // calling remove method
        urlMappingHolder.removeUrlMappingMap("http://example.com/apps/bar");
        log.info("Testing removeUrlMappingMap () functionality");
        Assert.assertEquals(urlMappingHolder.getUrlMappingOfApplication().size(), 2,
                "Retrieved size does not match with expected size");
    }

    /**
     * Checks getUrlMappingsPerApplication functionality.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.utils"})
    public void testGetUrlMappingsPerApplication () {
        URLMappingHolder urlMappingHolder = URLMappingHolder.getInstance();
        // calling set method
        urlMappingHolder.putUrlMappingForApplication("http://example.com/apps/foo", "foo");
        urlMappingHolder.putUrlMappingForApplication("http://example.com/apps/bar", "bar");
        urlMappingHolder.putUrlMappingForApplication("http://example.com/apps/hello", "hello");
        urlMappingHolder.putUrlMappingForApplication("http://example.com/apps/abc", "foo");
        // checking map for size
        log.info("Testing getUrlMappingsPerApplication () per given application");
        Assert.assertEquals(urlMappingHolder.getUrlMappingsPerApplication("foo").size(), 2,
                "Retrieved size does not match with expected size");
    }

    @AfterMethod
    public void ClearURLMappingOfApplication () {
        URLMappingHolder urlMappingHolder = URLMappingHolder.getInstance();
        if (urlMappingHolder.getUrlMappingOfApplication().size() > 0) {
            urlMappingHolder.getUrlMappingOfApplication().clear();
        }
    }
}
