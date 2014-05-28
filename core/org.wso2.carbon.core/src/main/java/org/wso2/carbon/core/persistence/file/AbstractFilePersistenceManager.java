package org.wso2.carbon.core.persistence.file;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.XMLPrettyPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.core.persistence.PersistenceDataNotFoundException;
import org.wso2.carbon.core.persistence.PersistenceException;
import org.wso2.carbon.core.persistence.PersistenceUtils;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractFilePersistenceManager {

    protected AxisConfiguration axisConfig;
    File metafilesDir;

    Map<String, ResourceFileData> resourceMap = new HashMap<String, ResourceFileData>();

    /**
     *   Include meta files that gets modified due to a user action done via the UI.
     */
    private Set<String> userModifiedItems = new HashSet<String>();

    /**
     *    Includes meta files that are being modified in memory.. This is to identify if the meta
     *    file needs to be rewritten to the filesystem.
     */
    private Set<String> modifyingMetaFiles = new HashSet<String>();

    protected OMFactory omFactory = OMAbstractFactory.getOMFactory();

    private static final Log log = LogFactory.getLog(AbstractFilePersistenceManager.class);

    protected AbstractFilePersistenceManager(AxisConfiguration axisConfig) {
        this.axisConfig = axisConfig;
    }

    public abstract void beginTransaction(String resourceId) throws
            IOException, XMLStreamException, PersistenceException;

    public synchronized void commitTransaction(String resourceId) throws PersistenceException {

        //if the meta file is not modified omit overriding the meta file
       if(!isMetaFileModification(resourceId)){
           if (log.isDebugEnabled()){
                log.debug("No metafile modification done for : " + resourceId);
           }
           resourceMap.remove(resourceId);
           return;
        }

        if(CarbonUtils.isWorkerNode()){
            if (log.isDebugEnabled()){
                log.debug("Worker nodes do not need to update metafiles.");
            }
            resourceMap.remove(resourceId);
            return;
        }

        OutputStream outputStream = null;
        try {
            synchronized (resourceMap.get(resourceId)) {
                ResourceFileData fileData = resourceMap.get(resourceId);
                File f;
                if (fileData != null && fileData.getOMElement() == null) {   //the resource has been deleted
                    String childFilePath = getFilePathFromResourceId(resourceId);
                    f = new File(metafilesDir, childFilePath);
                    if (f.exists()) {
                        FileUtils.forceDelete(f);
                    }

                    if(log.isDebugEnabled()){
                        log.debug("Successfully deleted persisted resource contents " + resourceId + " " + f.getName());
                    }
                    resourceMap.remove(resourceId);
                    return;
                } else if (fileData != null) {
                    f = fileData.getFile();
                } else {
                    resourceMap.remove(resourceId);
                    throw new PersistenceException("persistence data not found");
                }
                outputStream = FileUtils.openOutputStream(f);
                fileData.getOMElement().serializeAndConsume(outputStream);
                XMLPrettyPrinter.prettify(f);
                resourceMap.remove(resourceId);
                setUserModification(resourceId);
            }
        } catch (XMLStreamException e1) {
            log.error("Exception in persisting the transaction of " + resourceId, e1);
            handleExceptionWithRollback(resourceId, "Exception in persisting the transaction " + resourceId, e1);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            handleExceptionWithRollback(resourceId, "IOException in persisting the transaction " + resourceId, e);
        } finally {
            try{
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                //ignore
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Added new resource - " + resourceId);
        }
    }

    /**
     * with commons-io 2.2, you may simply replace this with FileUtils#getFile()
     *
     * @param resourceId resource id
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
            return sb.append(Resources.METAFILE_EXTENSION).toString();
        } else {
            return null;
        }
    }

    /**
     * @param resourceId service group name of module name
     */
    public void rollbackTransaction(String resourceId) {
        if(log.isDebugEnabled()){
            log.debug("rollbackTransaction for : " + resourceId);
        }
        isMetaFileModification(resourceId);
        ResourceFileData fileData = resourceMap.get(resourceId);
        if (fileData != null) {
            fileData.setOMElement(null);
            fileData.setTransactionStarted(false);
        }
    }

    /**
     * Returns the root resource's element
     * This simply calls #get(String resourceId, String xpathStr)
     * with xpathStr = Resources.ServiceGroupProperties.ROOT_XPATH or "/module[1]" as relevant
     *
     * @param resourceId module name or service group name as applicable
     * @return An OMNode. This could be an OMElement, OMAttribute etc. Cast as necessary.
     * @throws PersistenceDataNotFoundException
     *          if the file don't have a root element
     * @see #get(String, String)
     */
    public abstract OMElement get(String resourceId) throws PersistenceDataNotFoundException;

    /**
     * Use this to retrieve a matched OMNode.
     * NOTE: OMAttribute does not extend OMNode.
     *
     * @param resourceId module name or service group name as applicable
     * @param xpathStr   The path in the xml file where the said xml content can be found
     * @return An OMNode. This could be an OMElement, OMAttribute etc. Cast as necessary.
     * @throws PersistenceDataNotFoundException
     *          if no data found for the given xpathString or syntax error
     * @see #getAttribute(String, String)
     */
    public OMNode get(String resourceId, String xpathStr) throws PersistenceDataNotFoundException {
        List list = getAll(resourceId, xpathStr);
        if (list.size() > 0) {
            return (OMNode) list.get(0);
        } else {
            return null;
        }
    }

    /**
     * Use this if you are retrieving an attribute using an xpath.
     * Since OMNode is not extended by OMAttribute interface, we cannot use
     * same get method for this.
     *
     * @param resourceId module name or service group name as applicable
     * @param xpathStr   The path in the xml file where the said xml content can be found
     * @return An OMNode. This could be an OMElement, OMAttribute etc. Cast as necessary.
     * @throws PersistenceDataNotFoundException
     *          if no data found for the given xpathString or syntax error
     * @see #get(String, String)
     */
    public OMAttribute getAttribute(String resourceId, String xpathStr) throws PersistenceDataNotFoundException {
        List list = getAll(resourceId, xpathStr);
        if (list.size() > 0) {
            return (OMAttribute) list.get(0);
        } else {
            return null;
        }
    }

    /**
     * @param resourceName  service group name / module name
     * @param content       the OMElement to be added as a child to the location of xpathOfParent
     * @param xpathOfParent The parent path in the xml file where the said xml content should be added.
     * @throws PersistenceDataNotFoundException
     *          if parent is null or xpath syntax error
     */
    public void put(String resourceName, OMElement content, String xpathOfParent) throws
            PersistenceDataNotFoundException {
        try {
            ResourceFileData fileData = resourceMap.get(resourceName);
            if (fileData != null) {
                OMElement sgElement = fileData.getOMElement();
                AXIOMXPath xpathExpr = new AXIOMXPath(xpathOfParent);
                OMElement parent = (OMElement) xpathExpr.selectSingleNode(sgElement);
                if (parent != null) {
                    if (!parent.equals(content.getParent())) {
                        parent.addChild(content);
                        setMetaFileModification(resourceName);
                    } else {
                        /* If the transaction is started for this resource,
                           Resource file data is taken from memory and updated.
                         */
                        setMetaFileModification(resourceName);
                        if (log.isDebugEnabled()) {
                            log.debug("Trying add a child to the same parent. " + resourceName + content.toString());
                        }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("parent can not be found - " + resourceName + fileData.getOMElement());
                    }
                    throw new PersistenceDataNotFoundException("The resource can not be added because Parent could " +
                            "not be found." + resourceName + " The XPath - " + xpathOfParent);
                }
            } else {
                throw new PersistenceDataNotFoundException("ResourceFileData not found. " +
                        "The Transaction May not have been initialized. see #beginTransaction. " + resourceName +
                        "The xpath used was " + xpathOfParent);
            }
        } catch (JaxenException e) {
            log.error("Error selecting parent in " + resourceName, e);
            throw new PersistenceDataNotFoundException("Error selecting parent in " + resourceName, e);
        }
    }

    /**
     * Puts an attribute
     *
     * @param resourceName   The service group ID or module id
     * @param attr           Content to be added
     * @param xpathOfElement The Xpath of the OMElement where this attribute should be added
     * @throws PersistenceDataNotFoundException
     *          if parent is null or xpath syntax error
     */
    public void put(String resourceName, OMAttribute attr, String xpathOfElement) throws
            PersistenceDataNotFoundException {
        try {
            ResourceFileData fileData = resourceMap.get(resourceName);
            if (fileData != null) {
                OMElement sgElement = fileData.getOMElement();
                AXIOMXPath xpathExpr = new AXIOMXPath(xpathOfElement);
                OMElement parent = (OMElement) xpathExpr.selectSingleNode(sgElement);
                parent.addAttribute(attr);
                setMetaFileModification(resourceName);
            } else {
                log.error("put attr = " + attr.getAttributeType() + attr.getAttributeValue());
                throw new PersistenceDataNotFoundException("ResourceFileData not found. " +
                        "The Transaction May not have been initialized. see #beginTransaction. " + resourceName +
                        "The xpath used was " + xpathOfElement);
            }
        } catch (JaxenException e) {
            log.error("XPath syntaxt error " + xpathOfElement, e);
            throw new PersistenceDataNotFoundException("XPath syntaxt error for " + resourceName + xpathOfElement, e);
        }
    }

    public boolean fileExists(String resourceId) {
        ResourceFileData fileData = resourceMap.get(resourceId);
        //if a transaction is started
        if (fileData != null && fileData.isTransactionStarted() && fileData.getFile() != null) {
            return fileData.getFile().exists();
        } else {
            return new File(metafilesDir, getFilePathFromResourceId(resourceId)).
                    exists();
        }
    }

    public boolean elementExists(String resourceId, String elementXpathStr) {
        try {
            ResourceFileData fileData = resourceMap.get(resourceId);
            AXIOMXPath xpathExpr = new AXIOMXPath(elementXpathStr);
            //if a transaction is started
            if (fileData != null && fileData.isTransactionStarted() && fileData.getOMElement() != null) {
                return xpathExpr.selectSingleNode(fileData.getOMElement()) != null;
            } else if ((fileData != null && !fileData.isTransactionStarted()) ||
                    fileData == null) {
                File f = new File(metafilesDir, getFilePathFromResourceId(resourceId));
                if (f.exists() && f.length() > 0) {
                    OMElement element = PersistenceUtils.getResourceDocumentElement(f);
                    return xpathExpr.selectSingleNode(element) != null;
                }
            }
        } catch (JaxenException e) {
            log.error("Xpath error " + elementXpathStr, e);
        } catch (XMLStreamException e) {
            log.error(e.getMessage() + resourceId, e);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage() + resourceId, e);
        } catch (IOException e) {
            log.error(e.getMessage() + resourceId, e);
        }
        return false;
    }

    public List getAll(String resourceId, String xpathStr) throws PersistenceDataNotFoundException {
        try {
            ResourceFileData fileData = resourceMap.get(resourceId);
            //If a transaction is in progress. Just use the in-memory OM.
            if (fileData != null && fileData.isTransactionStarted()) {
                OMElement resourceElement = fileData.getOMElement();
                AXIOMXPath xpathExpr = new AXIOMXPath(xpathStr);
                return xpathExpr.selectNodes(resourceElement);
            } else {
                File resourceFile = new File(metafilesDir, getFilePathFromResourceId(resourceId));
                if (resourceFile.exists() && resourceFile.length() > 0) {
                    OMElement resourceOMElement = PersistenceUtils.getResourceDocumentElement(resourceFile);

                    /**
                     * xpath expression behaves differently when there is a OMDocument holding the OMElement.
                     * So, to have one expression work everywhere, we detach the element if the parent has the
                     * type OMDocument
                     */
                    if (resourceOMElement.getParent() instanceof OMDocument) {
                        resourceOMElement.detach();
                    }
                    AXIOMXPath xpathExpr = new AXIOMXPath(xpathStr);
                    return xpathExpr.selectNodes(resourceOMElement);
                }
            }
        } catch (JaxenException e) {
            log.error("Error parsing xpath string " + resourceId + xpathStr, e);
            throw new PersistenceDataNotFoundException("Error parsing xpath string ", e);
        } catch (IOException e) {
            log.error("metafile for resource " + resourceId + " not found. ", e);
            throw new PersistenceDataNotFoundException("metafile for resource " + resourceId + " not found. ", e);
        } catch (XMLStreamException e) {
            log.error("XMLStreamException " + resourceId + " not found. ", e);
            throw new PersistenceDataNotFoundException("XMLStreamException " + resourceId + " not found. ", e);
        }
        return new ArrayList(0);
    }

    /**
     * @param resourceId module name or service group name as applicable
     * @param xpathStr   xpath to the element which needs to be deleted
     * @return true if deleted successfully
     */
    public boolean delete(String resourceId, String xpathStr) {
        ResourceFileData fileData = resourceMap.get(resourceId);

        try {
            if (fileData != null && fileData.isTransactionStarted()) {
                OMElement sgElement = fileData.getOMElement();
                AXIOMXPath xpathExpr = new AXIOMXPath(xpathStr);
                OMElement el = (OMElement) xpathExpr.selectSingleNode(sgElement);
                if (el == null) {
                    return false;
                }
                if (el.getParent() == null) { //this is the root element
                    fileData.setOMElement(null);
                } else {
                    el.detach();
                }
                setMetaFileModification(resourceId);
                return true;
            } else {
                log.error("The Element specified by path not found or a transaction isn't started yet. " +
                        xpathStr);
                return false;
            }
        } catch (JaxenException e) {
            log.error("Error parsing xpath string " + resourceId + xpathStr, e);
            return false;
        }
    }
    
    /**
     * @param resourceId module name or service group name as applicable
     * @param xpathStr   xpath to the element which needs to be deleted
     * @return
     */
    public boolean deleteAll(String resourceId, String xpathStr) {
        ResourceFileData fileData = resourceMap.get(resourceId);

        try {
            if (fileData != null && fileData.isTransactionStarted()) {
                OMElement sgElement = fileData.getOMElement();
                AXIOMXPath xpathExpr = new AXIOMXPath(xpathStr);
                List nodeList = xpathExpr.selectNodes(sgElement);
                if (nodeList == null || nodeList.isEmpty()) {
                    return false;
                }
                for (Object obj : nodeList) {
                    OMElement el = (OMElement) obj;
                    if (el.getParent() == null) { //this is the root element
                        fileData.setOMElement(null);
                    } else {
                        el.detach();
                    }
                }
                setMetaFileModification(resourceId);
                return true;
            } else {
                log.error("The Element specified by path not found or a transaction isn't started yet. " +
                        xpathStr);
            }
        } catch (JaxenException e) {
            log.error("Error parsing xpath string " + resourceId + xpathStr, e);
        }
        return false;
    }

    /**
     * Handles exception and rollbacks an already started transaction. Don't use this method if
     * you haven't already started a registry transaction
     * <p/>
     * For a serviceGroup or service or operation we need the resourceId of serviceGroup.
     * For modules, we need the module id.
     *
     * @param resourceId The id/name of resource
     * @param msg        - Message to log
     * @param e          - original exception
     * @throws PersistenceException after rollbacking the transaction, this metho throws this exception
     */
    protected void handleExceptionWithRollback(String resourceId, String msg, Throwable e) throws
            PersistenceException {
        log.error(msg, e);
        rollbackTransaction(resourceId);
        throw new PersistenceException(msg, e);
    }

    protected void handleException(String msg, Throwable e) throws PersistenceException {
        log.error(msg, e);
        throw new PersistenceException(msg, e);
    }

    protected void handleException(String msg) throws PersistenceException {
        log.error(msg);
        throw new PersistenceException(msg);
    }

    /**
     * Checks whether a transaction is already started on the given module.
     * Since, this implementation does not support nested transactions, use this method
     * to make sure that a transaction is not yet started for a given module.
     *
     * @param resourceId resourceId
     * @return true if a transactio is already started
     */
    public boolean isTransactionStarted(String resourceId) {

        return resourceMap.get(resourceId) != null &&
                resourceMap.get(resourceId).isTransactionStarted();

    }

    public void init() {
        try {
            if (!metafilesDir.exists()) {
                FileUtils.forceMkdir(metafilesDir);
            }
        } catch (IOException e) {
            log.error("Error creating the resource meta files directory for " + metafilesDir.getAbsolutePath(), e);
        }
    }

    public boolean isUserModification(String name) {
        return userModifiedItems.remove(name);
    }

    public boolean setUserModification(String name) {
        return userModifiedItems.add(name);
    }

    protected boolean isMetaFileModification(String resourceId) {
        return modifyingMetaFiles.remove(resourceId);
    }

    public void setMetaFileModification(String resourceId) {
        modifyingMetaFiles.add(resourceId);
    }
}
