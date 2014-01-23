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


package org.apache.axis2.deployment.repository.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.DescriptionBuilder;
import org.apache.axis2.deployment.ModuleBuilder;
import org.apache.axis2.deployment.ServiceBuilder;
import org.apache.axis2.deployment.ServiceGroupBuilder;
import org.apache.axis2.deployment.resolver.AARBasedWSDLLocator;
import org.apache.axis2.deployment.resolver.AARFileBasedURIResolver;
import org.apache.axis2.deployment.resolver.WarBasedWSDLLocator;
import org.apache.axis2.deployment.resolver.WarFileBasedURIResolver;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.WSDL11ToAllAxisServicesBuilder;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.WSDL20ToAllAxisServicesBuilder;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.WSDLToAxisServiceBuilder;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ArchiveReader implements DeploymentConstants {
    private static final Log log = LogFactory.getLog(ArchiveReader.class);

    public ArrayList<AxisService> buildServiceGroup(InputStream zin, DeploymentFileData currentFile,
                                       AxisServiceGroup axisServiceGroup, HashMap<String, AxisService> wsdlServices,
                                       ConfigurationContext configCtx)
            throws XMLStreamException, AxisFault {

        DescriptionBuilder builder = new DescriptionBuilder(zin, configCtx);
        OMElement rootElement = builder.buildOM();
        String elementName = rootElement.getLocalName();

        if (TAG_SERVICE.equals(elementName)) {
            AxisService axisService = null;
            String serviceName = DescriptionBuilder.getShortFileName(currentFile.getName());
            if (serviceName != null) {
                axisService = wsdlServices.get(serviceName);
            }
            if (axisService == null) {
                axisService = wsdlServices.get(
                        DescriptionBuilder.getShortFileName(currentFile.getName()));
            }
            if (axisService == null) {
                axisService = new AxisService(serviceName);
            } else {
                axisService.setWsdlFound(true);
                axisService.setCustomWsdl(true);
            }

            axisService.setParent(axisServiceGroup);
            axisService.setClassLoader(currentFile.getClassLoader());

            ServiceBuilder serviceBuilder = new ServiceBuilder(configCtx, axisService);
            serviceBuilder.setWsdlServiceMap(wsdlServices);
            AxisService service = serviceBuilder.populateService(rootElement);

            ArrayList<AxisService> serviceList = new ArrayList<AxisService>();
            serviceList.add(service);
            return serviceList;
        } else if (TAG_SERVICE_GROUP.equals(elementName)) {
            ServiceGroupBuilder groupBuilder = new ServiceGroupBuilder(rootElement, wsdlServices,
                                                                       configCtx);
            return groupBuilder.populateServiceGroup(axisServiceGroup);
        }
        throw new AxisFault("Invalid services.xml found");
    }

    /**
     * Extracts Service XML files and builds the service groups.
     *
     * @param filename
     * @param axisServiceGroup
     * @param extractService
     * @param wsdlServices
     * @param configCtx
     * @return Returns ArrayList.
     * @throws DeploymentException
     */
    public ArrayList<AxisService> processServiceGroup(String filename, DeploymentFileData currentFile,
                                         AxisServiceGroup axisServiceGroup,
                                         boolean extractService,
                                         HashMap<String, AxisService> wsdlServices,
                                         ConfigurationContext configCtx)
            throws AxisFault {
        // get attribute values
        if (!extractService) {
            ZipInputStream zin = null;
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(filename);
                zin = new ZipInputStream(fin);
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    if (entry.getName().equalsIgnoreCase(SERVICES_XML)) {
                        axisServiceGroup.setServiceGroupName(
                                DescriptionBuilder.getShortFileName(currentFile.getName()));
                        return buildServiceGroup(zin, currentFile, axisServiceGroup, wsdlServices,
                                                 configCtx);
                    }
                }
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.SERVICE_XML_NOT_FOUND, filename));
            } catch (Exception e) {
                throw new DeploymentException(e);
            } finally {
                if (zin != null) {
                    try {
                        zin.close();
                    } catch (IOException e) {
                        log.info(Messages.getMessage("errorininputstreamclose"));
                    }
                }
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (IOException e) {
                        log.info(Messages.getMessage("errorininputstreamclose"));
                    }
                }
            }
        } else {
            File file = new File(filename, SERVICES_XML);
            if (!file.exists()) {
                // try for meta-inf
                file = new File(filename, SERVICES_XML.toLowerCase());
            }
            if (file.exists()) {
                InputStream in = null;
                try {
                    in = new FileInputStream(file);
                    axisServiceGroup.setServiceGroupName(currentFile.getName());
                    return buildServiceGroup(in, currentFile, axisServiceGroup, wsdlServices, configCtx);
                } catch (FileNotFoundException e) {
                    throw new DeploymentException(
                            Messages.getMessage(DeploymentErrorMsgs.FILE_NOT_FOUND,
                                                e.getMessage()));
                } catch (XMLStreamException e) {
                    throw new DeploymentException(
                            Messages.getMessage(DeploymentErrorMsgs.XML_STREAM_EXCEPTION,
                                                e.getMessage()));
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            log.info(Messages.getMessage("errorininputstreamclose"));
                        }
                    }
                }
            } else {
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.SERVICE_XML_NOT_FOUND));
            }
        }
    }

    /**
     * Creats AxisService.
     *
     * @param in
     * @return Returns AxisService.
     * @throws DeploymentException
     */
    private List<AxisService> processWSDLFile(WSDLToAxisServiceBuilder axisServiceBuilder,
                                 File serviceArchiveFile,
                                 boolean isArchive, InputStream in, String baseURI)
            throws DeploymentException {
        try {

            if (serviceArchiveFile != null && isArchive) {
                axisServiceBuilder.setCustomResolver(
                        new AARFileBasedURIResolver(serviceArchiveFile));
                if (axisServiceBuilder instanceof WSDL11ToAllAxisServicesBuilder) {

                    ((WSDL11ToAllAxisServicesBuilder) axisServiceBuilder).setCustomWSDLResolver(
                            new AARBasedWSDLLocator(baseURI, serviceArchiveFile, in));

                    ((WSDL11ToAllAxisServicesBuilder) axisServiceBuilder).setDocumentBaseUri(
                            serviceArchiveFile.getCanonicalFile().toURI().toString());

                } else if (axisServiceBuilder instanceof WSDL20ToAllAxisServicesBuilder) {
                    ((WSDL20ToAllAxisServicesBuilder) axisServiceBuilder).setCustomWSDLResolver(
                            new AARBasedWSDLLocator(baseURI, serviceArchiveFile, in));
                    // trying to use the jar scheme as the base URI. I think this can be used to handle
                    // wsdl 1.1 as well without using a custom URI resolver. Need to look at it later.
                    axisServiceBuilder.setBaseUri(
                            "jar:file://" + serviceArchiveFile.toURI() + "!/" + baseURI);
                }
            } else {
                if (serviceArchiveFile != null) {
                    axisServiceBuilder.setBaseUri(
                            serviceArchiveFile.getParentFile().toURI().toString());

                    if (axisServiceBuilder instanceof WSDL11ToAllAxisServicesBuilder) {
                        ((WSDL11ToAllAxisServicesBuilder) axisServiceBuilder).setDocumentBaseUri(
                                serviceArchiveFile.getCanonicalFile().toURI().toString());
                    }
                }
            }
            if (axisServiceBuilder instanceof WSDL11ToAllAxisServicesBuilder) {
                return ((WSDL11ToAllAxisServicesBuilder) axisServiceBuilder).populateAllServices();
            } else if (axisServiceBuilder instanceof WSDL20ToAllAxisServicesBuilder) {
                return ((WSDL20ToAllAxisServicesBuilder) axisServiceBuilder).populateAllServices();
            }
        } catch (AxisFault axisFault) {
            log.info("Trouble processing wsdl file :" + axisFault.getMessage());
            if (log.isDebugEnabled()) {
                log.debug(axisFault);
            }
        } catch (IOException ioex) {
            log.info("Trouble processing wsdl file :" + ioex.getMessage());
            if (log.isDebugEnabled()) {
                log.debug(ioex);
            }
        }
        return null;
    }

    /**
     * Creates service objects from wsdl file inside a service archive file.
     *
     * @param file <code>ArchiveFileData</code>
     * @throws DeploymentException <code>DeploymentException</code>
     */
    public HashMap<String, AxisService> processWSDLs(DeploymentFileData file)
            throws DeploymentException {
        File serviceFile = file.getFile();
        // to store service come from wsdl files
        HashMap<String, AxisService> servicesMap = new HashMap<String, AxisService>();
        boolean isDirectory = serviceFile.isDirectory();
        if (isDirectory) {
            try {
                File metaInfFolder = new File(serviceFile, META_INF);

                if (!metaInfFolder.exists()) {
                    metaInfFolder = new File(serviceFile, META_INF.toLowerCase());
                    if (!metaInfFolder.exists()) {
                        throw new DeploymentException(
                                Messages.getMessage(
                                        DeploymentErrorMsgs.META_INF_MISSING,
                                        serviceFile.getName()));
                    }
                }

                processFilesInFolder(metaInfFolder, servicesMap);

            } catch (FileNotFoundException e) {
                throw new DeploymentException(e);
            } catch (IOException e) {
                throw new DeploymentException(e);
            } catch (XMLStreamException e) {
                throw new DeploymentException(e);
            }
        } else {
            ZipInputStream zin;
            FileInputStream fin;
            try {
                fin = new FileInputStream(serviceFile);
                zin = new ZipInputStream(fin);

                //TODO Check whether this WSDL is empty

                ZipEntry entry;
                byte[] buf = new byte[1024];
                int read;
                ByteArrayOutputStream out;
                while ((entry = zin.getNextEntry()) != null) {
                    String entryName = entry.getName().toLowerCase();
                    if (entryName.startsWith(META_INF.toLowerCase())
                        && entryName.endsWith(SUFFIX_WSDL)) {
                        out = new ByteArrayOutputStream();

                        // we do not want to generate the services for the
                        // imported wsdl of one file.
                        if ((entryName.indexOf("/") != entryName.lastIndexOf("/"))
                            || (entryName.indexOf("wsdl_") != -1)) {
                            //only care abt the toplevel wsdl
                            continue;
                        }

                        while ((read = zin.read(buf)) > 0) {
                            out.write(buf, 0, read);
                        }

                        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

                        // now the question is which version of WSDL file this archive contains.
                        // lets check the namespace of the root element and decide. But since we are
                        // using axiom (dude, you are becoming handy here :)), we will not build the
                        // whole thing.
                        OMNamespace documentElementNS =
                                ((OMElement) XMLUtils.toOM(in)).getNamespace();
                        if (documentElementNS != null) {
                            WSDLToAxisServiceBuilder wsdlToAxisServiceBuilder;
                            if (WSDL2Constants.WSDL_NAMESPACE
                                    .equals(documentElementNS.getNamespaceURI())) {
                                // we have a WSDL 2.0 document here.
                                wsdlToAxisServiceBuilder = new WSDL20ToAllAxisServicesBuilder(
                                        new ByteArrayInputStream(out.toByteArray()));
                                wsdlToAxisServiceBuilder.setBaseUri(entryName);
                            } else if (Constants.NS_URI_WSDL11.
                                    equals(documentElementNS.getNamespaceURI())) {
                                wsdlToAxisServiceBuilder = new WSDL11ToAllAxisServicesBuilder(
                                        new ByteArrayInputStream(out.toByteArray()));
                                ((WSDL11ToAxisServiceBuilder) wsdlToAxisServiceBuilder).setDocumentBaseUri(entryName);
                            } else {
                                throw new DeploymentException(Messages.getMessage("invalidWSDLFound"));
                            }
                            List<AxisService> services = processWSDLFile(wsdlToAxisServiceBuilder,
                                                            serviceFile, true,
                                                            new ByteArrayInputStream(
                                                                    out.toByteArray()),
                                                            entry.getName());
                            if (services != null) {
                                for (AxisService axisService : services) {
                                    if (axisService != null) {
                                        servicesMap.put(axisService.getName(), axisService);
                                    }
                                }
                            }
                        }
                    }
                }
                try {
                    zin.close();
                } catch (IOException e) {
                    log.info(e);
                }
                try {
                    fin.close();
                } catch (IOException e) {
                    log.info(e);
                }
            } catch (FileNotFoundException e) {
                throw new DeploymentException(e);
            } catch (IOException e) {
                throw new DeploymentException(e);
            } catch (XMLStreamException e) {
                throw new DeploymentException(e);
            }
        }
        return servicesMap;
    }

    public List<AxisService> getAxisServiceFromWsdl(InputStream in,
                                       ClassLoader loader, String wsdlUrl) throws Exception {
//         ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        // now the question is which version of WSDL file this archive contains.
        // lets check the namespace of the root element and decide. But since we are
        // using axiom (dude, you are becoming handy here :)), we will not build the
        // whole thing.
        OMElement element = (OMElement) XMLUtils.toOM(in);
        OMNamespace documentElementNS = element.getNamespace();
        if (documentElementNS != null) {
            WSDLToAxisServiceBuilder wsdlToAxisServiceBuilder;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            element.serialize(out);
            if (Constants.NS_URI_WSDL11.
                    equals(documentElementNS.getNamespaceURI())) {
                wsdlToAxisServiceBuilder = new WSDL11ToAllAxisServicesBuilder(
                        new ByteArrayInputStream(out.toByteArray()));
                ((WSDL11ToAllAxisServicesBuilder)wsdlToAxisServiceBuilder).setCustomWSDLResolver(new WarBasedWSDLLocator(wsdlUrl,
                                                                                         loader,
                                                                                         new ByteArrayInputStream(
                                                                                                 out.toByteArray())));
                wsdlToAxisServiceBuilder.setCustomResolver(
                        new WarFileBasedURIResolver(loader));
                return ((WSDL11ToAllAxisServicesBuilder)wsdlToAxisServiceBuilder).populateAllServices();
            } else if (WSDL2Constants.WSDL_NAMESPACE.
                    equals(documentElementNS.getNamespaceURI())){
                wsdlToAxisServiceBuilder = new WSDL20ToAllAxisServicesBuilder(
                        new ByteArrayInputStream(out.toByteArray()));
                ((WSDL20ToAllAxisServicesBuilder)wsdlToAxisServiceBuilder).setCustomWSDLResolver(new WarBasedWSDLLocator(wsdlUrl,
                                                                                         loader,
                                                                                         new ByteArrayInputStream(
                                                                                                 out.toByteArray())));
                wsdlToAxisServiceBuilder.setCustomResolver(
                        new WarFileBasedURIResolver(loader));
                return ((WSDL20ToAllAxisServicesBuilder)wsdlToAxisServiceBuilder).populateAllServices();
            }
            else {
                throw new DeploymentException(Messages.getMessage("invalidWSDLFound"));
            }
        }
        return null;
    }

    public void processFilesInFolder(File folder, HashMap<String, AxisService> servicesMap)
            throws FileNotFoundException, XMLStreamException, DeploymentException {
        File files[] = folder.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file1 = files[i];
            if (file1.getName().toLowerCase().endsWith(SUFFIX_WSDL)) {
                InputStream in = new FileInputStream(file1);
                FileInputStream in2;

                // now the question is which version of WSDL file this archive contains.
                // lets check the namespace of the root element and decide. But since we are
                // using axiom (dude, you are becoming handy here :)), we will not build the
                // whole thing.
                OMNamespace documentElementNS = ((OMElement) XMLUtils.toOM(in)).getNamespace();
                if (documentElementNS != null) {
                    WSDLToAxisServiceBuilder wsdlToAxisServiceBuilder;
                    if (WSDL2Constants.WSDL_NAMESPACE
                            .equals(documentElementNS.getNamespaceURI())) {
                        // we have a WSDL 2.0 document here.
                        in2 = new FileInputStream(file1);
                        wsdlToAxisServiceBuilder = new WSDL20ToAllAxisServicesBuilder(in2);
                    } else if (Constants.NS_URI_WSDL11.
                            equals(documentElementNS.getNamespaceURI())) {
                        in2 = new FileInputStream(file1);
                        wsdlToAxisServiceBuilder = new WSDL11ToAllAxisServicesBuilder(in2);
                        ((WSDL11ToAxisServiceBuilder) wsdlToAxisServiceBuilder).setDocumentBaseUri(file1.toURI()
                                                                                                        .toString());
                    } else {
                        throw new DeploymentException(Messages.getMessage("invalidWSDLFound"));
                    }

                    FileInputStream in3 = new FileInputStream(file1);
                    List<AxisService> services = processWSDLFile(wsdlToAxisServiceBuilder, file1, false,
                                                    in2, file1.toURI().toString());

                    if (services != null) {
                        for (AxisService axisService : services) {
                            if (axisService != null) {
                                servicesMap.put(axisService.getName(), axisService);
                            }
                        }
                    }
                    try {
                        in2.close();
                        in3.close();
                    } catch (IOException e) {
                        log.info(e);
                    }
                }
                try {
                    in.close();
                } catch (IOException e) {
                    log.info(e);
                }
            }
        }
    }

    public void readModuleArchive(DeploymentFileData deploymentFile,
                                  AxisModule module, boolean explodedDir,
                                  AxisConfiguration axisConfig)
            throws DeploymentException {

        // get attribute values
        boolean moduleXMLFound = false;
        String shortFileName = DescriptionBuilder.getShortFileName(deploymentFile.getName());
        if (!explodedDir) {
            ZipInputStream zin;
            FileInputStream fin;
            try {
                fin = new FileInputStream(deploymentFile.getAbsolutePath());
                zin = new ZipInputStream(fin);
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    if (entry.getName().equalsIgnoreCase(MODULE_XML)) {
                        moduleXMLFound = true;
                        ModuleBuilder builder = new ModuleBuilder(zin, module, axisConfig);
                        // setting module name and version
                        module.setArchiveName(shortFileName);
                        builder.populateModule();
                        break;
                    }
                }
                zin.close();
                fin.close();
                if (!moduleXMLFound) {
                    throw new DeploymentException(
                            Messages.getMessage(
                                    DeploymentErrorMsgs.MODULE_XML_MISSING,
                                    deploymentFile.getAbsolutePath()));
                }
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        } else {
            File file = new File(deploymentFile.getAbsolutePath(), MODULE_XML);

            if (file.exists() ||
                (file = new File(deploymentFile.getAbsolutePath(), MODULE_XML.toLowerCase()))
                        .exists()) {
                InputStream in = null;
                try {
                    in = new FileInputStream(file);
                    ModuleBuilder builder = new ModuleBuilder(in, module, axisConfig);
                    // setting module name and version
                    module.setArchiveName(shortFileName);
                    builder.populateModule();
                } catch (FileNotFoundException e) {
                    throw new DeploymentException(
                            Messages.getMessage(DeploymentErrorMsgs.FILE_NOT_FOUND,
                                                e.getMessage()));
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            log.info(Messages.getMessage("errorininputstreamclose"));
                        }
                    }
                }
            } else {
                throw new DeploymentException(
                        Messages.getMessage(
                                DeploymentErrorMsgs.MODULE_XML_MISSING,
                                deploymentFile.getAbsolutePath()));
            }
        }
    }
}
