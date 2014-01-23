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

package org.apache.axis2.maven2.aar;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Deploys an AAR to the Axis2 server.
 * 
 * @goal deployaar
 * @phase install
 */
public class DeployAarMojo extends AbstractAarMojo {

    private final static String LOGIN_FAILED_ERROR_MESSAGE = "Invalid auth credentials!";

    /**
     * The URL of the Axis2 administration console.
     *
     * @parameter default-value="http://localhost:8080/axis2/axis2-admin" expression="${axis2.aar.axis2AdminConsoleURL}"
     */
    private URL axis2AdminConsoleURL;

    /**
     * The administrator user name for the Axis2 administration console.
     *
     * @parameter expression="${axis2.aar.axis2AdminUser}"
     */
    private String axis2AdminUser;

    /**
     * The administrator password for the Axis2 administration console.
     *
     * @parameter expression="${axis2.aar.axis2AdminPassword}"
     */
    private String axis2AdminPassword;

    /**
     * Executes the DeployAarMojo on the current project.
     *
     * @throws MojoExecutionException if an error occurred while building the webapp
     */
    public void execute() throws MojoExecutionException {
        getLog().info("Deploying AAR artifact "+project.getArtifact().getFile()+" to Axis2 Web Console "+axis2AdminConsoleURL);
        try {
            deploy(project.getArtifact().getFile());
        } catch(MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("Error deploying aar", e);
        }
    }

    /**
     * Deploys the AAR.
     *
     * @param aarFile the target AAR file
     * @throws MojoExecutionException
     * @throws HttpException
     * @throws IOException
     */
    private void deploy(File aarFile) throws MojoExecutionException, IOException, HttpException {
        if(axis2AdminConsoleURL == null) {
            throw new MojoExecutionException("No Axis2 administrative console URL provided.");
        }

        // TODO get name of web service mount point
        HttpClient client = new HttpClient();
        client.getParams().setParameter(HttpClientParams.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

        // log into Axis2 administration console
        URL axis2AdminConsoleLoginURL = new URL(axis2AdminConsoleURL.toString()+"/login");
        getLog().debug("Logging into Axis2 Admin Web Console "+axis2AdminConsoleLoginURL+" using user ID "+axis2AdminUser);

        PostMethod  post = new PostMethod(axis2AdminConsoleLoginURL.toString());
        NameValuePair[] nvps = new NameValuePair[] {
                new NameValuePair("userName", axis2AdminUser),
                new NameValuePair("password", axis2AdminPassword)
        };
        post.setRequestBody(nvps);

        int status = client.executeMethod(post);
        if(status != 200) {
            throw new MojoExecutionException("Failed to log in");
        }
        if(post.getResponseBodyAsString().indexOf(LOGIN_FAILED_ERROR_MESSAGE)!=-1) {
            throw new MojoExecutionException("Failed to log into Axis2 administration web console using credentials");
        }

        // deploy AAR web service
        URL axis2AdminConsoleUploadURL = new URL(axis2AdminConsoleURL.toString()+"/upload");
        getLog().debug("Uploading AAR to Axis2 Admin Web Console "+axis2AdminConsoleUploadURL);

        post = new PostMethod(axis2AdminConsoleUploadURL.toString());
        Part[] parts = {
                new FilePart(project.getArtifact().getFile().getName(), project.getArtifact().getFile())
        };
        post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

        status = client.executeMethod(post);
        if(status != 200) {
            throw new MojoExecutionException("Failed to log in");
        }

        // log out of web console
        URL axis2AdminConsoleLogoutURL = new URL(axis2AdminConsoleURL.toString()+"/logout");
        getLog().debug("Logging out of Axis2 Admin Web Console "+axis2AdminConsoleLogoutURL);

        GetMethod get = new GetMethod(axis2AdminConsoleLogoutURL.toString());
        status = client.executeMethod(get);
        if(status != 200) {
            throw new MojoExecutionException("Failed to log out");
        }

    }

}
