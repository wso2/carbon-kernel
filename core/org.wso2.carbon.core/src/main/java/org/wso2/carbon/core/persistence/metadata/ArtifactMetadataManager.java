/*
* Copyright 2004,2013 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.core.persistence.metadata;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.Set;

@Deprecated
public class ArtifactMetadataManager {

    private static final Log log = LogFactory.getLog(ArtifactMetadataManager.class);
    private String deploymentRepoPath;
    private static final int ENTITY_EXPANSION_LIMIT = 0;

    public ArtifactMetadataManager(AxisConfiguration axisConfig) throws ArtifactMetadataException {

        try {
            URL repositoryURL = axisConfig.getRepository();
            if (repositoryURL != null) {
                deploymentRepoPath = URLDecoder.decode(axisConfig.getRepository().getPath(), "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Metafiles directory URL can not be decoded. " + axisConfig.getRepository(), e);
            throw new ArtifactMetadataException(e.getMessage(), e);
        }

    }

    public boolean isMetafileExists(String artifactName, ArtifactType artifactType)  {
        File metadataFile = calculateMetafilePath(artifactName, artifactType);
        return metadataFile.exists();
    }

    public void deleteMetafile(String artifactName, ArtifactType artifactType)  {
        File metadataFile = calculateMetafilePath(artifactName, artifactType);
        if (metadataFile.exists()) {
            if (!metadataFile.delete()) {
                metadataFile.deleteOnExit();
            }
        }

    }

    public ArtifactMetadata loadParameters(String artifactName, ArtifactType artifactType)
            throws ArtifactMetadataException {
        File metadataFile = calculateMetafilePath(artifactName, artifactType);
        Properties prop = loadParameters(metadataFile.getAbsolutePath());

        ArtifactMetadata metadataObject = new ArtifactMetadata(artifactName, artifactType, metadataFile);
        metadataObject.setProperties(prop);
        return metadataObject;
    }

    public Properties loadParameters(String absolutePath)
            throws ArtifactMetadataException {
        File metadataFile = new File(absolutePath);
//        ArtifactMetadata metadataObject = new ArtifactMetadata("dummy", new ArtifactType("", ""), metadataFile);

        FileInputStream fis = null;
        try{
            if (metadataFile.exists()) {
                fis = FileUtils.openInputStream(metadataFile);
                Properties properties = new Properties();
                properties.load(fis);
                return properties;
            } else {
                return new Properties();
            }
        } catch (IOException e) {
            handleException(e.getMessage(), e);
        } finally {
            try {
                if (fis != null) { fis.close(); }
            } catch (IOException e) {
                //ignore
            }
        }
        return null;
    }
    
    private void saveParameters(ArtifactMetadata metadataObject) throws ArtifactMetadataException {
        FileOutputStream fileOutputStream = null;
        File file = metadataObject.getFile();
        if(!file.getParentFile().exists()) {
            boolean created = file.getParentFile().mkdirs();
            if (!created) {
                throw new ArtifactMetadataException("Unable to create directory structure for persisting metafiles" +
                file.getPath());
            }
        }
        String comment = metadataObject.getArtifactName() + "@@@" + metadataObject.getArtifactType();
        try{
            fileOutputStream = FileUtils.openOutputStream(file);
            metadataObject.getProperties().store(fileOutputStream, "UTF-8");

        } catch (FileNotFoundException e) {
            handleException("File can not be opened for writing. " + e.getMessage(), e);
        } catch (IOException e) {
            handleException(" Error while saving artifact metafiles for " + metadataObject.getArtifactName()+ ". " +
                    e.getMessage(), e);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                //ignore
            }
        }
    }
    
    public String loadParameter(String artifactName, ArtifactType artifactType, String propertyName)
            throws ArtifactMetadataException{
        ArtifactMetadata metadataObject = loadParameters(artifactName, artifactType);
        return metadataObject.getProperties().getProperty(propertyName);
    }

    public Element loadXMLParameter(String artifactName, ArtifactType artifactType, String propertyName)
            throws ArtifactMetadataException{
        try {
            String param = loadParameter(artifactName, artifactType, propertyName);
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(param));
    
            DocumentBuilderFactory dbf = getSecuredDocumentBuilder();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document d = db.parse(is);
            return d.getDocumentElement();
        } catch (Exception e) {
            String message = "Error while loading parameter as XML. artifact - " +
                    artifactName + ", property - " + propertyName;
            handleException(message, e);
        }
        return null;
    }


    private static DocumentBuilderFactory getSecuredDocumentBuilder() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        } catch (ParserConfigurationException e) {
            log.error(
                    "Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                            Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE);
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return dbf;
    }

    /**
     * if 'replace' is set, then save parameter. Else, only save the parameter if it's not already exist.
     *
     * @param artifactName The artifact name - ex. example.war
     * @param artifactType The artifact type - ex. webapp, service etc.
     * @param propertyName Name of the property to be saved
     * @param propertyValue property value
     * @param replace whether the existing property should be replaced or not
     * @throws ArtifactMetadataException if file can not be read
     */
    public void setParameter(String artifactName, ArtifactType artifactType,String propertyName,
                             String propertyValue, boolean replace) throws ArtifactMetadataException {
        ArtifactMetadata metadataObject = loadParameters(artifactName, artifactType);
        Properties prop = metadataObject.getProperties();
        if (replace || prop.get(propertyName) == null) {
            prop.setProperty(propertyName, propertyValue);
            saveParameters(metadataObject);
        }

    }

    public void setParameters(String artifactName, ArtifactType artifactType,
                             Properties properties) throws ArtifactMetadataException {
        ArtifactMetadata metadataObject = loadParameters(artifactName, artifactType);
        Properties prop = metadataObject.getProperties();

        Set<String> stringPropNames = properties.stringPropertyNames();
        for (String name : stringPropNames) {
            prop.setProperty(name, properties.getProperty(name));
        }
        saveParameters(metadataObject);
    }

    public void removeParameter(String artifactName, ArtifactType artifactType,
                                String propertyName) throws ArtifactMetadataException {
        ArtifactMetadata metadataObject = loadParameters(artifactName, artifactType);
        Properties prop = metadataObject.getProperties();

        prop.remove(propertyName);
        saveParameters(metadataObject);

    }

    private File calculateMetafilePath(String artifactName, ArtifactType artifactType) {
        String metaArtifactDirectoryName = artifactType.getMetadataDirName();
        artifactName = getFilePathFromResourceId(artifactName);
        String artifactPath = deploymentRepoPath + File.separator + metaArtifactDirectoryName + File.separator +
                artifactName;

        return new File(artifactPath);
    }

    /**
     *
     * @param resourceId artifact name
     * @return system dependent file path with correct separator
     */
    protected String getFilePathFromResourceId(String resourceId) {
        if (resourceId != null) {
            String[] names = resourceId.split("/");
            StringBuilder sb = new StringBuilder(names[0]);
            char fs = File.separatorChar;
            for (int i = 1; i < names.length; i++) {
                sb.append(fs).append(names[i]);
            }
            return sb.append(".properties").toString();
        } else {
            return null;
        }
    }

    private void handleException(String message, Exception e) throws ArtifactMetadataException {
        log.error(message, e);
        throw new ArtifactMetadataException(message, e);
    }

}
