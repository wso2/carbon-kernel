package org.wso2.carbon.core.persistence.file;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.core.persistence.PersistenceDataNotFoundException;
import org.wso2.carbon.core.persistence.PersistenceException;
import org.wso2.carbon.core.persistence.PersistenceUtils;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Acts as the immediate lower level of ModulePersistenceManager class.
 * This abstracts the persisting the metadata to a file in a synchronized,
 * abstract way.
 * <p/>
 * The methods are synchronized per ResourceFileData object.
 */
public class ModuleFilePersistenceManager extends AbstractFilePersistenceManager {

    private static final Log log = LogFactory.getLog(ModuleFilePersistenceManager.class);

    public ModuleFilePersistenceManager(AxisConfiguration axisConfig) throws AxisFault {
        super(axisConfig);
        try {
            URL repositoryURL = axisConfig.getRepository();
            if (repositoryURL != null) {
                String repoPath = URLDecoder.decode(axisConfig.getRepository().getPath(), "UTF-8");
                //we need to change meta-files location in rare cases (i.e for Jaggery dis), introducing that flexibility via a sys prop @NuwanB
                String moduleMetaFileLocation = System.getProperty("module.metafiles.location");
                if (moduleMetaFileLocation == null) {
                    moduleMetaFileLocation = Resources.MODULES_METAFILES_DIR;
                }
                metafilesDir = new File(repoPath +
                        File.separator + moduleMetaFileLocation);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("metafiles directory URL can not bde decoded.", e);
            throw new AxisFault("metafiles directory URL can not bde decoded.", e);
        }
        if (this.metafilesDir == null) {
            log.error("metafiles directory for services must exist. ");
            throw new AxisFault("metafiles directory for services must exist. " +
                    "May be AxisConfiguration does not have repository url set.");
        }
    }

    /**
     * Reads the relevant service group file from FS and loads the OM to memory.
     * If the file does not exist, create a new OM.
     * This is supposed to work for both serviceGroups and modules.xml metafiles
     * Note: Nested beginTransactions is NOT supported.
     *
     * @param moduleName module name ex. rampart
     * @throws org.wso2.carbon.core.persistence.PersistenceException
     *
     */
    public synchronized void beginTransaction(String moduleName) throws PersistenceException {
        File moduleFile = new File(metafilesDir, getFilePathFromResourceId(moduleName));
        try {
            OMElement moduleElement;
            if (moduleFile.exists()) {
                moduleElement = PersistenceUtils.getResourceDocumentElement(moduleFile);
            } else {                        //the file does not exist.
                moduleElement = omFactory.createOMElement(Resources.ModuleProperties.MODULE_XML_TAG, null);
            }

            ResourceFileData fileData = new ResourceFileData(moduleElement, moduleFile, true);
            resourceMap.put(moduleName, fileData);
        } catch (XMLStreamException e1) {
            log.error("Failed to use XMLStreamReader. Exception in beginning the transaction ", e1);
            throw new PersistenceException("Exception in beginning the transaction " + e1);
        } catch (FileNotFoundException e) {
            log.error("File not found. Exception in beginning the transaction " + moduleFile.getAbsolutePath(), e);
            throw new PersistenceException("Exception in beginning the transaction", e);
        } catch (IOException e) {
            log.error("Exception in closing service group file " + moduleName, e);
            throw new PersistenceException("Exception in closing service group file", e);
        }
    }

    /**
     * Returns the root module element
     * This simply calls #get(String serviceGroupId, String xpathStr) with xpathStr = "/"
     *
     * @param moduleId The module name
     * @return An OMNode. This could be an OMElement, OMAttribute etc. Cast as necessary.
     * @throws org.wso2.carbon.core.persistence.PersistenceDataNotFoundException
     *
     * @see #get(String, String)
     */
    public OMElement get(String moduleId) throws PersistenceDataNotFoundException {
        return (OMElement) get(moduleId, Resources.ModuleProperties.ROOT_XPATH);
    }

    @Override
    public boolean delete(String moduleId, String xpathStr) {
	return super.delete(moduleId, xpathStr);
    }

}
