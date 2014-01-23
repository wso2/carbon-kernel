/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.server.extensions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.server.CarbonLaunchExtension;
import org.wso2.carbon.server.LauncherConstants;
import org.wso2.carbon.server.util.Utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Converts JAR files containing Axis2 Service Deployers into OSGi bundles
 */
public class AxisServiceDeployerBundleCreator implements CarbonLaunchExtension {

    private static final String DEPLOYERS_DIR =
            "repository" + File.separator + "components" + File.separator + "axis2deployers";

    public void perform() {
        File dropinsFolder = new File(Utils.getCarbonComponentRepo(), "dropins");
        File dir = Utils.getBundleDirectory(DEPLOYERS_DIR);
        File[] files = dir.listFiles(new Utils.JarFileFilter());
        if (files != null) {

            for (File file : files) {
                ZipInputStream zin = null;
                try {
                    ZipEntry entry;
                    String entryName;
                    InputStream inputStream = new FileInputStream(file);
                    zin = new ZipInputStream(inputStream);
                    boolean validComponentXmlFound = false;
                    while ((entry = zin.getNextEntry()) != null) {
                        entryName = entry.getName();
                        if (entryName.equals("META-INF/component.xml")) {
                            URL url = new URL("jar:file:" + file.getAbsolutePath() + "!/" + entryName);
                            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                            DocumentBuilder db = dbf.newDocumentBuilder();
                            Document doc = db.parse(url.openStream());
                            doc.getDocumentElement().normalize();
                            Element rootEle = doc.getDocumentElement();
                            NodeList childNodes = rootEle.getElementsByTagName("deployer");
                            if (childNodes.getLength() > 0) {
                                Element deployerEle = (Element) childNodes.item(0);
                                if (deployerEle.getElementsByTagName("directory").getLength() == 1 &&
                                    deployerEle.getElementsByTagName("extension").getLength() == 1 &&
                                    deployerEle.getElementsByTagName("class").getLength() == 1) {
                                    validComponentXmlFound = true;
                                }
                            }
                        }
                    }

                    if (!validComponentXmlFound) {
                        System.out.println("A valid component.xml was not found in AxisDeployer jar file " +
                                           file.getAbsolutePath() + ". A component.xml file with the " +
                                           "following entries should be placed in the META-INF directory.\n" +
                                           "<deployer>\n" +
                                           "\t<directory>[dir]</directory>\n" +
                                           "\t<extension>[extension]</extension>\n" +
                                           "\t<class>[some.Class]</class>\n" +
                                           "\t</deployer>\n" +
                                           "</deployers>");
                        continue;
                    }

                    Manifest mf = new Manifest();
                    Attributes attribs = mf.getMainAttributes();
                    attribs.putValue(LauncherConstants.DYNAMIC_IMPORT_PACKAGE, "*");
                    attribs.putValue("Axis2Deployer", file.getName());
                    Utils.createBundle(file, dropinsFolder, mf, "");

                } catch (Throwable e) {
                    System.out.println("Cannot create Axis2Deployer bundle from jar file " +
                                       file.getAbsolutePath());
                    e.printStackTrace();
                } finally {
                    try {
                        //close the Stream
                        zin.close();
                    } catch (IOException e) {
                        System.out.println("Unable to close the InputStream " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
