/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.core.jdbc.handlers;

import org.wso2.carbon.registry.core.Aspect;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.Repository;
import org.wso2.carbon.registry.core.jdbc.VersionRepository;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.SimulationFilter;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.io.Reader;
import java.io.Writer;
import java.util.*;

/**
 * Objects of this class contains the information about the current request to the registry. All
 * attributes of such objects should be either null or should contain a *valid" value. That is, if
 * an attribute contains a non-null value it is safe to assume that attribute contains a valid value
 * as mentioned by the javadoc of that attribute.
 */
@SuppressWarnings("unused")
public class RequestContext {

    /**
     * Specifies whether the processing of the current request is completed or not. If this is true,
     * any further processing of the request is not done.
     */
    private boolean processingComplete;

    /**
     * Path of the currently processing resource as given by the client of the Registry API
     */
    private ResourcePath resourcePath;

    /**
     * Path of the currently processing resource, which may have been modified by Registry
     * implementations to represent the actual path in which resource resides.
     */
    private String actualPath;

    /**
     * Currently processing resource. This is the resource referred by the actual path.
     */
    private Resource resource;

    /**
     * URL where the current resource content is located.
     */
    private String sourceURL;

    /**
     * Path of the parent collection of the current resource.
     */
    private String parentPath;

    /**
     * Parent collection of the current resource.
     */
    private Collection parentCollection;

    private Map<String, Object> properties = new HashMap<String, Object>();

    private Registry registry;
    private Registry systemRegistry;
    private Repository repository;
    private VersionRepository versionRepository;

    private Aspect aspect;
    private String action;

    private String sourcePath;
    private String targetPath;
    private String targetSubPath;

    private String userName;
    private String tag;
    private String associationType;
    private int rating;
    private Comment comment;
    private String versionPath;
    private Map queryParameters;
    private String keywords;
    private Reader dumpingReader;
    private Writer dumpingWriter;
    private long bytesRead;
    private long bytesWritten;

    /**
     * We log activities by default.
     */
    private boolean loggingActivity = true;

    private RegistryContext registryContext;

    /**
     * The old resource (if any).
     */
    private Resource oldResource;

    private List<Association> oldAssociationsOnSource;

    private List<Association> oldAssociationsOnTarget;

    private List<Tag> oldTags;

    private List<Comment> oldComments;

    private int oldRating;

    private float oldAverageRating;

    private Map<String, List<String[]>> handlerExecutionStatusMap;

    private static final String SUCCESSFUL = "Successful";

    private static final String FAILED = "Failed";

    private static ThreadLocal<Integer> handlerExecutionId =
            new ThreadLocal<Integer>() {
                protected Integer initialValue() {
                    return 0;
                }
            };

    /**
     * Get the handler execution id.
     *
     * @return the handler execution id.
     */
    private static Integer getHandlerExecutionId() {
        return handlerExecutionId.get();
    }

    /**
     * Set the handler execution id.
     *
     * @param input the handler execution id.
     */
    private static void setHandlerExecutionId(Integer input) {
        handlerExecutionId.set(input);
    }

    /**
     * Construct the request context.
     *
     * @param registry          the registry
     * @param repository        the repository.
     * @param versionRepository the version repository.
     */
    public RequestContext(
            Registry registry, Repository repository, VersionRepository versionRepository) {
        this.registry = registry;
        this.repository = repository;
        this.versionRepository = versionRepository;
        this.handlerExecutionStatusMap = new HashMap<String, List<String[]>>();
    }

    /**
     * Method to get the Registry.
     *
     * @return the Registry.
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * Method to get the System Registry.
     *
     * @return the System Registry.
     * @throws RegistryException if the operation failed.
     */
    public Registry getSystemRegistry() throws RegistryException {
        if (systemRegistry == null) {
            systemRegistry = RegistryUtils.getSystemRegistry(registry);
        }
        return systemRegistry;
    }

    /**
     * Method to get the Repository.
     *
     * @return the Repository.
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * Method to get the Version Repository.
     *
     * @return the Version Repository.
     */
    public VersionRepository getVersionRepository() {
        return versionRepository;
    }

    /**
     * Method to determine whether processing is complete or not.
     *
     * @return whether processing is complete or not.
     */
    public boolean isProcessingComplete() {
        return processingComplete;
    }

    /**
     * Method to set Processing Complete.
     *
     * @param processingComplete the Processing Complete.
     */
    public void setProcessingComplete(boolean processingComplete) {
        this.processingComplete = processingComplete;
    }

    /**
     * Method to get the Resource Path.
     *
     * @return the Resource Path.
     */
    public ResourcePath getResourcePath() {
        if (resourcePath == null && resource != null) {
            resourcePath = new ResourcePath(resource.getPath());
        }
        return resourcePath;
    }

    /**
     * Method to set Resource Path.
     *
     * @param resourcePath the Resource Path.
     */
    public void setResourcePath(ResourcePath resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * Method to get the Source URL.
     *
     * @return the Source URL.
     */
    public String getSourceURL() {
        return sourceURL;
    }

    /**
     * Method to set Source URL.
     *
     * @param sourceURL the Source URL.
     */
    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }

    /**
     * Method to get the Actual Path.
     *
     * @return the Actual Path.
     */
    public String getActualPath() {
        return actualPath;
    }

    /**
     * Method to set Actual Path.
     *
     * @param actualPath the Actual Path.
     */
    public void setActualPath(String actualPath) {
        this.actualPath = actualPath;
    }

    /**
     * Method to get the Resource.
     *
     * @return the Resource.
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Method to set Resource.
     *
     * @param resource the Resource.
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * Method to get the Parent Path.
     *
     * @return the Parent Path.
     */
    public String getParentPath() {
        return parentPath;
    }

    /**
     * Method to set Parent Path.
     *
     * @param parentPath the Parent Path.
     */
    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    /**
     * Method to get the Aspect.
     *
     * @return the Aspect.
     */
    public Aspect getAspect() {
        return aspect;
    }

    /**
     * Method to set Aspect.
     *
     * @param aspect the Aspect.
     */
    public void setAspect(Aspect aspect) {
        this.aspect = aspect;
    }

    /**
     * Method to get the Action.
     *
     * @return the Action.
     */
    public String getAction() {
        return action;
    }

    /**
     * Method to set Action.
     *
     * @param action the Action.
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Method to get the Parent Collection.
     *
     * @return the Parent Collection.
     */
    public Collection getParentCollection() {
        return parentCollection;
    }

    /**
     * Method to set Parent Collection.
     *
     * @param parentCollection the Parent Collection.
     */
    public void setParentCollection(Collection parentCollection) {
        this.parentCollection = parentCollection;
    }

    /**
     * Method to set Property.
     *
     * @param name  the name of the property.
     * @param value the value of the property.
     */
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    /**
     * Method to get the Property by name.
     *
     * @param name the name of the property.
     *
     * @return the Property.
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Method to get the Source Path.
     *
     * @return the Source Path.
     */
    public String getSourcePath() {
        return sourcePath;
    }

    /**
     * Method to set Source Path.
     *
     * @param sourcePath the Source Path.
     */
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    /**
     * Method to get the Target Path.
     *
     * @return the Target Path.
     */
    public String getTargetPath() {
        return targetPath;
    }

    /**
     * Method to set Target Path.
     *
     * @param targetPath the Target Path.
     */
    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    /**
     * Method to get the Target Sub Path.
     *
     * @return the Target Sub Path.
     */
    public String getTargetSubPath() {
        return targetSubPath;
    }

    /**
     * Method to set Target Sub Path.
     *
     * @param targetSubPath the Target Sub Path.
     */
    public void setTargetSubPath(String targetSubPath) {
        this.targetSubPath = targetSubPath;
    }

    /**
     * Method to get the User Name.
     *
     * @return the User Name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Method to set User Name.
     *
     * @param userName the User Name.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Method to get the Tag.
     *
     * @return the Tag.
     */
    public String getTag() {
        return tag;
    }

    /**
     * Method to set Tag.
     *
     * @param tag the Tag.
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Method to get the Association Type.
     *
     * @return the Association Type.
     */
    public String getAssociationType() {
        return associationType;
    }

    /**
     * Method to set Association Type.
     *
     * @param associationType the Association Type.
     */
    public void setAssociationType(String associationType) {
        this.associationType = associationType;
    }

    /**
     * Method to get the Rating.
     *
     * @return the Rating.
     */
    public int getRating() {
        return rating;
    }

    /**
     * Method to set Rating.
     *
     * @param rating the Rating.
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * Method to get the Comment.
     *
     * @return the Comment.
     */
    public Comment getComment() {
        return comment;
    }

    /**
     * Method to set Comment.
     *
     * @param comment the Comment.
     */
    public void setComment(Comment comment) {
        this.comment = comment;
    }

    /**
     * Method to get the Version Path.
     *
     * @return the Version Path.
     */
    public String getVersionPath() {
        return versionPath;
    }

    /**
     * Method to set Version Path.
     *
     * @param versionPath the Version Path.
     */
    public void setVersionPath(String versionPath) {
        this.versionPath = versionPath;
    }

    /**
     * Method to get the Query Parameters.
     *
     * @return the Query Parameters.
     */
    public Map getQueryParameters() {
        return queryParameters;
    }

    /**
     * Method to set Query Parameters.
     *
     * @param queryParameters the Query Parameters.
     */
    public void setQueryParameters(Map queryParameters) {
        this.queryParameters = queryParameters;
    }

    /**
     * Method to get the Keywords.
     *
     * @return the Keywords.
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Method to set Keywords.
     *
     * @param keywords the Keywords.
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * Method to get the bytes read when performing a registry restoration operation.
     *
     * @return the number of bytes read.
     */
    public long getBytesRead() {
        return bytesRead;
    }

    /**
     * Method to set the bytes read when performing a registry restoration operation.
     *
     * @param bytesRead the number of bytes read.
     */
    public void setBytesRead(long bytesRead) {
        this.bytesRead = bytesRead;
    }

    /**
     * Method to get the bytes written when performing a registry dump operation.
     *
     * @return the number of bytes written.
     */
    public long getBytesWritten() {
        return bytesWritten;
    }

    /**
     * Method to set the bytes written when performing a registry dump operation.
     *
     * @param bytesWritten the number of bytes written.
     */
    public void setBytesWritten(long bytesWritten) {
        this.bytesWritten = bytesWritten;
    }

    /**
     * Method to get the Dumping Reader.
     *
     * @return the Dumping Reader.
     */
    public Reader getDumpingReader() {
        return dumpingReader;
    }

    /**
     * Method to set Dumping Reader.
     *
     * @param dumpingReader the Dumping Reader.
     */
    public void setDumpingReader(Reader dumpingReader) {
        this.dumpingReader = dumpingReader;
    }

    /**
     * Method to get the Dumping Writer.
     *
     * @return the Dumping Writer.
     */
    public Writer getDumpingWriter() {
        return dumpingWriter;
    }

    /**
     * Method to set Dumping Writer.
     *
     * @param dumpingWriter the Dumping Writer.
     */
    public void setDumpingWriter(Writer dumpingWriter) {
        this.dumpingWriter = dumpingWriter;
    }

    /**
     * Method to get the Registry Context.
     *
     * @return the Registry Context.
     */
    public RegistryContext getRegistryContext() {
        return registryContext;
    }

    /**
     * Method to set Registry Context.
     *
     * @param registryContext the Registry Context.
     */
    public void setRegistryContext(RegistryContext registryContext) {
        this.registryContext = registryContext;
    }

    /**
     * Method to get the Old Resource.
     *
     * @return the Old Resource.
     */
    public Resource getOldResource() {
        return oldResource;
    }

    /**
     * Method to set Old Resource.
     *
     * @param oldResource the Old Resource.
     */
    public void setOldResource(Resource oldResource) {
        this.oldResource = oldResource;
    }

    /**
     * Method to get the Old Associations On Source.
     *
     * @return the Old Associations On Source.
     */
    public Association[] getOldAssociationsOnSource() {
        // We are returning the stored list as an array of items to hide the internal
        // representation.
        if (oldAssociationsOnSource == null) {
            return null;
        }
        return oldAssociationsOnSource.toArray(new Association[oldAssociationsOnSource.size()]);
    }

    /**
     * Method to set Old Associations On Source.
     *
     * @param oldAssociationsOnSource the Old Associations On Source.
     */
    public void setOldAssociationsOnSource(Association[] oldAssociationsOnSource) {
        // We are storing a list of the array items to hide the internal representation.
        if (oldAssociationsOnSource != null) {
            this.oldAssociationsOnSource = Arrays.asList(oldAssociationsOnSource);
        }
    }

    /**
     * Method to get the Old Associations On Target.
     *
     * @return the Old Associations On Target.
     */
    public Association[] getOldAssociationsOnTarget() {
        // We are returning the stored list as an array of items to hide the internal
        // representation.
        if (oldAssociationsOnTarget == null) {
            return null;
        }
        return oldAssociationsOnTarget.toArray(new Association[oldAssociationsOnTarget.size()]);
    }

    /**
     * Method to set Old Associations On Target.
     *
     * @param oldAssociationsOnTarget the Old Associations On Target.
     */
    public void setOldAssociationsOnTarget(Association[] oldAssociationsOnTarget) {
        // We are storing a list of the array items to hide the internal representation.
        if (oldAssociationsOnTarget != null) {
            this.oldAssociationsOnTarget = Arrays.asList(oldAssociationsOnTarget);
        }
    }

    /**
     * Method to get the Old Tags.
     *
     * @return the Old Tags.
     */
    public Tag[] getOldTags() {
        // We are returning the stored list as an array of items to hide the internal
        // representation.
        if (oldTags == null) {
            return null;
        }
        return oldTags.toArray(new Tag[oldTags.size()]);
    }

    /**
     * Method to set Old Tags.
     *
     * @param oldTags the Old Tags.
     */
    public void setOldTags(Tag[] oldTags) {
        // We are storing a list of the array items to hide the internal representation.
        if (oldTags != null) {
            this.oldTags = Arrays.asList(oldTags);
        }
    }

    /**
     * Method to get the Old Comments.
     *
     * @return the Old Comments.
     */
    public Comment[] getOldComments() {
        // We are returning the stored list as an array of items to hide the internal
        // representation.
        if (oldComments == null) {
            return null;
        }
        return oldComments.toArray(new Comment[oldComments.size()]);
    }

    /**
     * Method to set Old Comments.
     *
     * @param oldComments the Old Comments.
     */
    public void setOldComments(Comment[] oldComments) {
        // We are storing a list of the array items to hide the internal representation.
        if (oldComments != null) {
            this.oldComments = Arrays.asList(oldComments);
        }
    }

    /**
     * Method to get the Old Rating.
     *
     * @return the Old Rating.
     */
    public int getOldRating() {
        return oldRating;
    }

    /**
     * Method to set Old Rating.
     *
     * @param oldRating the Old Rating.
     */
    public void setOldRating(int oldRating) {
        this.oldRating = oldRating;
    }

    /**
     * Method to get the Old Average Rating.
     *
     * @return the Old Average Rating.
     */
    public float getOldAverageRating() {
        return oldAverageRating;
    }

    /**
     * Method to set Old Average Rating.
     *
     * @param oldAverageRating the Old Average Rating.
     */
    public void setOldAverageRating(float oldAverageRating) {
        this.oldAverageRating = oldAverageRating;
    }

    /**
     * Method to get the Handler Execution Id
     *
     * @return the Handler Execution Id String.
     */
    public synchronized String getHandlerExecutionIdString() {
        Integer executionId = getHandlerExecutionId() + 1;
        setHandlerExecutionId(executionId);
        return Integer.toString(executionId);
    }

    /**
     * Method to set Execution Status.
     *
     * @param handler the Execution Status.
     * @param status  the status.
     */
    public void setExecutionStatus(Handler handler, boolean status) {
        if (handler == null) {
            return;
        }
        String handlerClass = handler.getClass().getName();
        List<String[]> statusList = handlerExecutionStatusMap.get(handlerClass);
        if (statusList == null) {
            statusList = new LinkedList<String[]>();
        }
        String[] statusArr = new String[2];
        statusArr[0] = status ? SUCCESSFUL : FAILED;
        statusArr[1] = getHandlerExecutionIdString();
        statusList.add(statusArr);
        handlerExecutionStatusMap.put(handlerClass, statusList);
    }

    /**
     * Method to set Execution Status.
     *
     * @param handler   the Execution Status.
     * @param exception the exception to throw at a failure.
     */
    public void setExecutionStatus(Handler handler, Throwable exception) {
        if (handler == null) {
            return;
        }
        String handlerClass = handler.getClass().getName();
        List<String[]> statusList = handlerExecutionStatusMap.get(handlerClass);
        if (statusList == null) {
            statusList = new LinkedList<String[]>();
        }
        String[] statusArr = new String[2];
        statusArr[0] = exception.getMessage();
        statusArr[1] = getHandlerExecutionIdString();
        statusList.add(statusArr);
        handlerExecutionStatusMap.put(handlerClass, statusList);
    }

    /**
     * Check whether the execution status is set.
     *
     * @param handler The handler the execution status checking.
     *
     * @return whether the status is set or not.
     */
    public boolean isExecutionStatusSet(Handler handler) {
        return ((handler != null) &&
                (handlerExecutionStatusMap.get(handler.getClass().getName()) != null));
    }

    /**
     * Method to get the HandlerExecutionStatusMap.
     *
     * @return the HandlerExecutionStatusMap.
     */
    public Map<String, List<String[]>> getHandlerExecutionStatusMap() {
        return handlerExecutionStatusMap;
    }

    /**
     * Check whether this is running in simulation mode
     *
     * @return true if it is running on simulation mode, false otherwise.
     */
    public boolean isSimulation() {
        return SimulationFilter.isSimulation();
    }

    /**
     * Check whether activities must be logged or not. This is true by default.
     *
     * @return true if activities must be logged, false otherwise.
     */
    public boolean isLoggingActivity() {
        return loggingActivity;
    }

    /**
     * Method to set whether activities must be logged for this operation.
     *
     * @param loggingActivity whether activities should be logged or not.
     */
    public void setLoggingActivity(boolean loggingActivity) {
        this.loggingActivity = loggingActivity;
    }
}
