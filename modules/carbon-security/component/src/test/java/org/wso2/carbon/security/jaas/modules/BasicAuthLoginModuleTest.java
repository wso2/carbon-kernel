package org.wso2.carbon.security.jaas.modules;

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.security.jaas.CarbonCallbackHandler;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.util.Base64;
import java.util.HashMap;

public class BasicAuthLoginModuleTest {

    private static final Logger log = LoggerFactory.getLogger(BasicAuthLoginModuleTest.class);


    @Test
    public void testBasicAuthLogin() {

        HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "");
        httpRequest.headers().add("Authorization", "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes()));

        CallbackHandler callbackHandler = new CarbonCallbackHandler(httpRequest);

        LoginContext loginContext;
        try {

            loginContext = new LoginContext("CarbonSecurityConfig", new Subject(), callbackHandler, new Configuration() {
                @Override
                public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                    AppConfigurationEntry[] configurationEntries = new AppConfigurationEntry[1];
                    configurationEntries[0] = new AppConfigurationEntry(BasicAuthLoginModule.class.getName(),
                                                                        AppConfigurationEntry.LoginModuleControlFlag
                                                                                .REQUIRED, new HashMap<>());

                    return configurationEntries;
                }
            });
            loginContext.login();
            Assert.assertTrue(true);

        } catch (LoginException e) {
            log.error("Error while authenticating.", e);
            Assert.assertTrue(false);
        }

    }
}

