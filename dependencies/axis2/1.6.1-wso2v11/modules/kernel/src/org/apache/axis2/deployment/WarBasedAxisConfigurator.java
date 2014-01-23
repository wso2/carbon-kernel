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

package org.apache.axis2.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.Loader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Processes the init parameters for the AxisServlet.
 * This allows the location of the axis2.xml and the module repository to be different from the default locations.
 * The init parameters support alternate file, or URL values for both of these.
 */
public class WarBasedAxisConfigurator extends DeploymentEngine implements AxisConfigurator {

    private static final Log log = LogFactory.getLog(WarBasedAxisConfigurator.class);
    private ServletConfig config;

    /**
     * The name of the init parameter (axis2.xml.path) that can be used to override the default location for the axis2.xml file. When both this init parameter, and the axis2.xml.url init parameters are not specified in the axis servlet init-parameter, the default location of ${app}/WEB-INF/conf/axis2.xml is used.
     * The value of this path is interpreted as a file system absolute path.
     * This parameter takes precedence over the axis2.xml.url init parameter.
     */
    public static final String PARAM_AXIS2_XML_PATH = "axis2.xml.path";


    /**
     * The name of the init parameter (axis2.xml.url) that when specified indicates the axis2.xml should be loaded using the URL specified as the value of this init parameter. If the axis2.xml.path init parameter is present, this init parameter has no effect.
     */
    public static final String PARAM_AXIS2_XML_URL = "axis2.xml.url";


    /**
     * The name of the init parameter (axis2.repository.path) that when specified indicates the path to the
     */
    public static final String PARAM_AXIS2_REPOSITORY_PATH = "axis2.repository.path";


    /**
     * The name of the init parameter (axis2.repository.url) that when specified indicates the url to be used
     */
    public static final String PARAM_AXIS2_REPOSITORY_URL = "axis2.repository.url";


    /**
     * Default constructor for configurator.
     * This determines the axis2.xml file to be used from the init parameters for the AxisServlet in the web.xml.
     * The order of initialization is according the the following precedence:
     * <ul>
     * <li>If the parameter axis2.xml.path is present, the value is webapp relative path to be used as the location to the axis2.xml file.
     * <li>Otherwise, if the parameter axis2.xml.url is present, the URL is used as the location to the axis2.xml file.
     * <li>Otherwise, when both of the above init parameters are not present, file is attempted to be loaded from &lt;repo&gt;/WEB-INF/axis2.xml.
     * <li> When none of the above could be found, the axis2.xml is loaded from the classpath resource, the value of DeploymenConstants.AXIS2_CONFIGURATION_RESOURCE.
     * </ul>
     *
     * @param servletConfig the ServletConfig object from the AxisServlet. This method is called from the init() of the AxisServlet.
     */
    public WarBasedAxisConfigurator(ServletConfig servletConfig) throws DeploymentException {
        try {
            this.config = servletConfig;
            InputStream axis2Stream = null;
            try {
                // when the module is an unpacked war file,
                // we can set the web location path in the deployment engine.
                // This will let us
                String webpath = config.getServletContext().getRealPath("");
                if (webpath == null || webpath.length() == 0) {
                    webpath = config.getServletContext().getRealPath("/");
                }
                if (webpath != null && !"".equals(webpath)) {
                    log.debug("setting web location string: " + webpath);
                    File weblocation = new File(webpath);
                    setWebLocationString(weblocation.getAbsolutePath());
                } // if webpath not null

                String axis2xmlpath = config.getInitParameter(PARAM_AXIS2_XML_PATH);
                if (axis2xmlpath != null) {
                    // when init parameter was present.
                    axis2Stream = new FileInputStream(axis2xmlpath);
                    log.debug("using axis2.xml from path: " + axis2xmlpath);
                }

                if (axis2Stream == null) {
                    String axisurl = config.getInitParameter(PARAM_AXIS2_XML_URL);
                    if (axisurl != null) {
                        axis2Stream = new URL(axisurl).openStream();
                        log.debug("loading axis2.xml from URL: " + axisurl);
                    }
                }

                if (axis2Stream == null) {
                    // both the axis2.xml.path and axis2.xml.url init parameters were not present
                    // try to find the default /WEB-INF/conf/axis2.xml
                    axis2Stream = config.getServletContext()
                            .getResourceAsStream("/WEB-INF/conf/axis2.xml");
                    log.debug("trying to load axis2.xml from module: /WEB-INF/conf/axis2.xml");
                }

                if (axis2Stream == null) {
                    // Simple deployment, no need for conf directory either
                    axis2Stream = config.getServletContext()
                            .getResourceAsStream("/WEB-INF/axis2.xml");
                    log.debug("trying to load axis2.xml from module: /WEB-INF/axis2.xml");
                }
            } // try
            catch (Exception e) {
                log.error(e, e);
                log.warn("Using default configuration: " + DeploymentConstants
                        .AXIS2_CONFIGURATION_RESOURCE);
                // not there, use default configuration from class path resource.
            } // catch

            if (axis2Stream == null) {
                log.info("Could not find axis2.xml, loading default "
                        + DeploymentConstants.AXIS2_CONFIGURATION_RESOURCE + " from classpath");
                axis2Stream =
                        Loader.getResourceAsStream(DeploymentConstants.AXIS2_CONFIGURATION_RESOURCE);
            }
            axisConfig = populateAxisConfiguration(axis2Stream);

            if(axis2Stream != null){
                axis2Stream.close();
            }
            Parameter param = new Parameter();
            param.setName(Constants.Configuration.ARTIFACTS_TEMP_DIR);
            File f = new File((File) config.getServletContext().getAttribute("javax.servlet.context.tempdir"), "_axis2");
            if (f.exists() || f.mkdirs()) {
                param.setValue(f);
            } else {
                f = new File(System.getProperty("java.io.tmpdir"), "_axis2");
                if (f.exists() || f.mkdirs()) {
                    param.setValue(f);
                } else {
                    throw new DeploymentException("Unable to create a temporary working directory");
                }
            }
            try {
                axisConfig.addParameter(param);
            } catch (AxisFault axisFault) {
                log.error(axisFault.getMessage(), axisFault);
            }
        } catch (DeploymentException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }


    /**
     * Gets the axis configuration object by loading the repository.
     * The order of initialization is according the the following precedence:
     * <ul>
     * <li>If the parameter axis2.repository.path is present, this folder is used as the location to the repository.
     * <li>Otherwise, if the parameter axis2.repository.url is present, the URL is used as the location to the repository.
     * <li>Otherwise, when both of the above init parameters are not present, the web applications WEB-INF folder is used as the folder for the repository.
     * </ul>
     *
     * @return the instance of the AxisConfiguration object that reflects the repository according to the rules above.
     * @throws AxisFault when an error occurred in the initialization of the AxisConfiguration.
     */
    public AxisConfiguration getAxisConfiguration() throws AxisFault {
        try {
            String repository = config.getInitParameter(PARAM_AXIS2_REPOSITORY_PATH);
            if (repository != null) {
                loadRepository(repository);
                log.debug("loaded repository from path: " + repository);
            }

            if (repository == null) {
                repository = config.getInitParameter(PARAM_AXIS2_REPOSITORY_URL);
                if (repository != null) {
                    loadRepositoryFromURL(new URL(repository));
                    log.debug("loaded repository from url: " + repository);
                }
            }

            if (repository == null) {
                if (config.getServletContext().getRealPath("") != null) {
                    // this is an unpacked war file
                    repository = config.getServletContext().getRealPath("/WEB-INF");
                }
                if (repository == null) {
                    if (config.getServletContext().getRealPath("/") != null) {
                        // this is an unpacked war file
                        repository = config.getServletContext().getRealPath("/WEB-INF");
                    }
                }
                if (repository != null) {
                    loadRepository(repository);
                    log.debug("loaded repository from /WEB-INF folder (unpacked war)");
                }
            }

            if (repository == null) {
                URL url = config.getServletContext().getResource("/WEB-INF/");
                if (url != null) {
                    repository = url.toString();
                    loadRepositoryFromURL(url);
                    log.debug("loaded repository from /WEB-INF/ folder (URL)");
                }
            }

            if (repository == null) {
                loadFromClassPath();
                log.debug("loaded repository from classpath");
            }

        } catch (Exception ex) {
            log.error(ex + ": loading repository from classpath", ex);
            loadFromClassPath();
        }
        axisConfig.setConfigurator(this);
        return axisConfig;
    }

    //to load services

    /**
     * Loads the services within the repository.
     * When the axis2.repository.path init parameter was present, we just call loadServices() in the deployment engine.<br/>
     * When the axis2.repository.url init parameter was present we load services from the respective URL value of the init parameter.<br/>
     * Otherwise, try to load the services from the /WEB-INF folder within the web application.
     */
    public void loadServices() {
        try {
            String repository;

            repository = config.getInitParameter(PARAM_AXIS2_REPOSITORY_PATH);
            if (repository != null) {
                super.loadServices();
                log.debug("loaded services from path: " + repository);
                return;
            }

            repository = config.getInitParameter(PARAM_AXIS2_REPOSITORY_URL);
            if (repository != null) {
                loadServicesFromUrl(new URL(repository));
                log.debug("loaded services from URL: " + repository);
                return;
            }
            loadServicesFromWebInf();
            if (config.getServletContext().getRealPath("") != null || 
                    config.getServletContext().getRealPath("/") != null) {
                super.loadServices();
                log.debug("loaded services from webapp");
                return;
            }

            URL url = config.getServletContext().getResource("/WEB-INF/");
            if (url != null) {
                loadServicesFromUrl(url);
                log.debug("loaded services from /WEB-INF/ folder (URL)");
            }
        } catch (MalformedURLException e) {
            log.info(e.getMessage());
        }
    }

    //To engage globally listed modules
    public void engageGlobalModules() throws AxisFault {
        engageModules();
    }

    /**
     * This method will look inside the web-inf directory to find services.xml
     * inside that , if it is there will load that and creat service group out
     * of that and add into axisConfig. User can drop corresponding class files
     * into class directory.
     */
    private void loadServicesFromWebInf() {
        try {
            InputStream servicexml = config.getServletContext().
                    getResourceAsStream("/WEB-INF/services.xml");
            if (servicexml != null) {
                HashMap wsdlServices = new HashMap();
                ArchiveReader archiveReader = new ArchiveReader();
                String path = config.getServletContext().getRealPath("/WEB-INF");
                if (path != null) {
                    archiveReader.processFilesInFolder(new File(path), wsdlServices);
                }
                AxisServiceGroup serviceGroup = DeploymentEngine.buildServiceGroup(servicexml,
                                                                                   Thread.currentThread().getContextClassLoader(),
                                                                                   "annonServiceGroup",
                                                                                   configContext,
                                                                                   archiveReader,
                                                                                   wsdlServices);
                axisConfig.addServiceGroup(serviceGroup);
            }
        } catch (AxisFault axisFault) {
            log.info(axisFault);
        } catch (FileNotFoundException e) {
            log.info(e);
        } catch (XMLStreamException e) {
            log.info(e);
        }
    }

    public void setConfigContext(ConfigurationContext configContext) {
        // setting ServletContext into configctx
        configContext.setProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT,
                                  config.getServletContext());
        Parameter servletConfigParam = new Parameter();
        servletConfigParam.setName(HTTPConstants.HTTP_SERVLETCONFIG);
        servletConfigParam.setValue(config);
        try {
            configContext.getAxisConfiguration().addParameter(servletConfigParam);
        } catch (AxisFault axisFault) {
            log.error(axisFault.getMessage(), axisFault);
        }
        super.setConfigContext(configContext);
    }
}
