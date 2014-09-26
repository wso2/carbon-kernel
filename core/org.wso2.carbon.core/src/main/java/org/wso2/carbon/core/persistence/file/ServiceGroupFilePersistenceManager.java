package org.wso2.carbon.core.persistence.file;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.FileUtils;
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
import java.util.List;

/**
 * Acts as the immediate lower level of other *PersistenceManager classes.
 * This abstracts the persisting the metadata to a file in a synchronized,
 * abstract way.
 * <p/>
 * The methods are synchronized per ResourceFileData object.
 */
@Deprecated
public class ServiceGroupFilePersistenceManager extends AbstractFilePersistenceManager {

    private static final Log log = LogFactory.getLog(ServiceGroupFilePersistenceManager.class);

    public ServiceGroupFilePersistenceManager(AxisConfiguration axisConfig) throws AxisFault {
        super(axisConfig);
        try {
            URL repositoryURL = axisConfig.getRepository();
            if (repositoryURL != null) {
                String repoPath = URLDecoder.decode(axisConfig.getRepository().getPath(), "UTF-8");
//                metafilesDir = new File(repoPath +
//                        File.separator + Resources.SERVICES_METAFILES_DIR);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("metafiles directory URL can not be decoded.", e);
            throw new AxisFault("metafiles directory URL can not be decoded.", e);
        }
//        if (this.metafilesDir == null) {
//            log.error("metafiles directory for services must exist. ");
//            throw new AxisFault("metafiles directory for services must exist. " +
//                    "May be AxisConfiguration does not have the repository url set.");
//        }
    }

    /**
     * Reads the relevant service group file from FS and loads the OM to memory.
     * If the file does not exist, create a new OM.
     * <p/>
     * This is supposed to work for both serviceGroups and modules.xml metafiles
     *
     * @param serviceGroupId service Group name
     * @throws org.wso2.carbon.core.persistence.PersistenceException
     *
     */
    public synchronized void beginTransaction(String serviceGroupId) throws PersistenceException {

//        File sgFile = new File(metafilesDir, getFilePathFromResourceId(serviceGroupId));
//
//        try {
//            if (resourceMap.get(serviceGroupId) != null &&
//                    resourceMap.get(serviceGroupId).isTransactionStarted()) {
//                throw new PersistenceException("A transaction is already started for this service group. " +
//                        "Nested transactions are no longer supported in this persistence model - " + serviceGroupId);
//            }
//
//            OMElement sgElement = null;
//            if (sgFile.exists()) {
//            	if(sgFile.length() > 0){
//            		sgElement = PersistenceUtils.getResourceDocumentElement(sgFile);
//            	}else{
//            		log.info(" File is empty, so it will be deleted  and regenerated ");
//            		sgFile.delete();
//            		//the file does not exist.
//                    sgElement = omFactory.createOMElement(Resources.ServiceGroupProperties.SERVICE_GROUP_XML_TAG, null);
//            	}
//
//            } else {                        //the file does not exist.
//                sgElement = omFactory.createOMElement(Resources.ServiceGroupProperties.SERVICE_GROUP_XML_TAG, null);
//            }
//
//            ResourceFileData fileData = new ResourceFileData(sgElement, sgFile, true);
//            resourceMap.put(serviceGroupId, fileData);
//        } catch (XMLStreamException e1) {
//            log.error("Failed to use XMLStreamReader. Exception in beginning the transaction ", e1);
//            throw new PersistenceException("Exception in beginning the transaction ", e1);
//        } catch (FileNotFoundException e) {
//            log.error("File not found. Exception in beginning the transaction " + sgFile.getAbsolutePath(), e);
//            throw new PersistenceException("Exception in beginning the transaction", e);
//        } catch (IOException e) {
//            log.error("Exception in closing service group file " + serviceGroupId, e);
//            throw new PersistenceException("Exception in closing service group file", e);
//        }
    }

    /**
     * Returns the root serviceGroup element
     * This simply calls #get(String serviceGroupId, String xpathStr) with xpathStr = "/serviceGroup[1]"
     *
     * @param serviceGroupId service Group name
     * @return An OMNode. This could be an OMElement, OMAttribute etc. Cast as necessary.
     * @see #get(String, String)
     */
    public OMElement get(String serviceGroupId) throws PersistenceDataNotFoundException {
        return (OMElement) get(serviceGroupId, Resources.ServiceGroupProperties.ROOT_XPATH);
    }

    /**
     * Returns the root serviceGroup element
     * This simply calls #get(String serviceGroupId, String xpathStr) with xpathStr = "/serviceGroup[1]"
     * <p/>
     * Note: Don't use this to retrieve a module association. It's different in format.
     *
     * @param serviceGroupId       service Group name
     * @param xpathOfParentElement The xpath expression to the association elements. Generally, no need of using attr predicate because this does a this#getAll
     * @param associationName      the type of association. ex. exposedTransports
     * @return An OMNode. This could be an OMElement, OMAttribute etc. Cast as necessary.
     * @throws PersistenceDataNotFoundException
     *          if an error occured during xpath evaluation
     * @see org.wso2.carbon.core.persistence.PersistenceUtils#createModule(String, String, String)
     * @see #get(String, String)
     */
    public List getAssociations(String serviceGroupId, String xpathOfParentElement, String associationName) throws
            PersistenceDataNotFoundException {
        String associationXPath = xpathOfParentElement + "/" + Resources.Associations.ASSOCIATION_XML_TAG +
                PersistenceUtils.getXPathAttrPredicate(Resources.ModuleProperties.TYPE, associationName);
        return getAll(serviceGroupId, associationXPath);
    }


    @Override
    public boolean delete(String serviceGroupId, String xpathStrOfElement) {
	    return super.delete(serviceGroupId, xpathStrOfElement);
    }

    public void init() {
//        try {
//            if (!metafilesDir.exists()) {
//                FileUtils.forceMkdir(metafilesDir);
//            }
//        } catch (IOException e) {
//            log.error("Error creating the services meta files directory " + metafilesDir.getAbsolutePath(), e);
//        }
    }
}
