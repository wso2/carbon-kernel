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

package org.wso2.carbon.registry.core.jdbc.handlers.filters;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This is a built-in Filter implementation to match against the URL (path) of the resources. This
 * can match different URLs for different handler operations. URLs to match for necessary handler
 * operations can be given as regular expressions. If a URL for particular operation is not
 * specified, it will always evaluate to false.
 * <p/>
 * Handler authors can use this Filter in their configurations if the filtering requirement is only
 * to match against URLs of the resources.
 */
public class URLMatcher extends Filter {

    /**
     * URLs to match against resource path for handler operations. They should be in the form of
     * regular expressions.
     */
    private Pattern getPattern;
    private Pattern executeQueryPattern;
    private Pattern putPattern;
    private Pattern importPattern;
    private Pattern deletePattern;
    private Pattern putChildPattern;
    private Pattern importChildPattern;
    private Pattern invokeAspectPattern;
    private Pattern movePattern;
    private Pattern copyPattern;
    private Pattern renamePattern;
    private Pattern createLinkPattern;
    private Pattern removeLinkPattern;
    private Pattern resourceExistsPattern;
    private Pattern getRegistryContextPattern;
    private Pattern addAssociationPattern;
    private Pattern removeAssociationPattern;
    private Pattern getAllAssociationsPattern;
    private Pattern getAssociationsPattern;
    private Pattern applyTagPattern;
    private Pattern getTagsPattern;
    private Pattern removeTagPattern;
    private Pattern addCommentPattern;
    private Pattern editCommentPattern;
    private Pattern removeCommentPattern;
    private Pattern getCommentsPattern;
    private Pattern rateResourcePattern;
    private Pattern getAverageRatingPattern;
    private Pattern getRatingPattern;
    private Pattern createVersionPattern;
    private Pattern getVersionsPattern;
    private Pattern restoreVersionPattern;
    private Pattern dumpPattern;
    private Pattern restorePattern;


    private String getPatternStr;
    private String executeQueryPatternStr;
    private String putPatternStr;
    private String importPatternStr;
    private String deletePatternStr;
    private String putChildPatternStr;
    private String importChildPatternStr;
    private String invokeAspectPatternStr;
    private String movePatternStr;
    private String copyPatternStr;
    private String renamePatternStr;
    private String createLinkPatternStr;
    private String removeLinkPatternStr;
    private String resourceExistsPatternStr;
    private String getRegistryContextPatternStr;
    private String addAssociationPatternStr;
    private String removeAssociationPatternStr;
    private String getAllAssociationsPatternStr;
    private String getAssociationsPatternStr;
    private String applyTagPatternStr;
    private String getTagsPatternStr;
    private String removeTagPatternStr;
    private String addCommentPatternStr;
    private String editCommentPatternStr;
    private String removeCommentPatternStr;
    private String getCommentsPatternStr;
    private String rateResourcePatternStr;
    private String getAverageRatingPatternStr;
    private String getRatingPatternStr;
    private String createVersionPatternStr;
    private String getVersionsPatternStr;
    private String restoreVersionPatternStr;
    private String dumpPatternStr;
    private String restorePatternStr;

    private volatile String equalsString = null;

    public int hashCode() {
        return getEqualsComparator().hashCode();
    }

    // Method to generate a unique string that can be used to compare two objects of the same type
    // for equality.
    private String getEqualsComparator() {
        if (equalsString != null) {
            return equalsString;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append("|");
        sb.append(getPatternStr);
        sb.append("|");
        sb.append(executeQueryPatternStr);
        sb.append("|");
        sb.append(putPatternStr);
        sb.append("|");
        sb.append(importPatternStr);
        sb.append("|");
        sb.append(deletePatternStr);
        sb.append("|");
        sb.append(putChildPatternStr);
        sb.append("|");
        sb.append(importChildPatternStr);
        sb.append("|");
        sb.append(invokeAspectPatternStr);
        sb.append("|");
        sb.append(movePatternStr);
        sb.append("|");
        sb.append(copyPatternStr);
        sb.append("|");
        sb.append(renamePatternStr);
        sb.append("|");
        sb.append(createLinkPatternStr);
        sb.append("|");
        sb.append(removeLinkPatternStr);
        sb.append("|");
        sb.append(resourceExistsPatternStr);
        sb.append("|");
        sb.append(getRegistryContextPatternStr);
        sb.append("|");
        sb.append(addAssociationPatternStr);
        sb.append("|");
        sb.append(removeAssociationPatternStr);
        sb.append("|");
        sb.append(getAllAssociationsPatternStr);
        sb.append("|");
        sb.append(getAssociationsPatternStr);
        sb.append("|");
        sb.append(applyTagPatternStr);
        sb.append("|");
        sb.append(getTagsPatternStr);
        sb.append("|");
        sb.append(removeTagPatternStr);
        sb.append("|");
        sb.append(addCommentPatternStr);
        sb.append("|");
        sb.append(editCommentPatternStr);
        sb.append("|");
        sb.append(removeCommentPatternStr);
        sb.append("|");
        sb.append(getCommentsPatternStr);
        sb.append("|");
        sb.append(rateResourcePatternStr);
        sb.append("|");
        sb.append(getAverageRatingPatternStr);
        sb.append("|");
        sb.append(getRatingPatternStr);
        sb.append("|");
        sb.append(createVersionPatternStr);
        sb.append("|");
        sb.append(getVersionsPatternStr);
        sb.append("|");
        sb.append(restoreVersionPatternStr);
        sb.append("|");
        sb.append(dumpPatternStr);
        sb.append("|");
        sb.append(restorePatternStr);
        sb.append("|");
        sb.append(invert);
        equalsString = sb.toString();
        return equalsString;
    }

    /**
     * Compares this MediaTypeMatcher to the specified object.  The result is {@code true} if and
     * only if the argument is not {@code null} and is a {@code MediaTypeMatcher} object that
     * contains the same values for the fields as this object.
     *
     * @param other The object to compare the {@code MediaTypeMatcher} against
     *
     * @return {@code true} if the given object represents a {@code MediaTypeMatcher} equivalent to
     *         this instance, {@code false} otherwise.
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other == null) {
            return false;
        }
        if (other instanceof URLMatcher) {
            URLMatcher otherURLMatcher = (URLMatcher) other;
            return (getEqualsComparator().equals(otherURLMatcher.getEqualsComparator()));
        }
        return false;
    }

    public boolean handleGet(RequestContext requestContext) throws RegistryException {
        return getPattern != null && (invert !=
                getPattern.matcher(requestContext.getResourcePath().getCompletePath()).matches());
    }

    public boolean handleExecuteQuery(RequestContext requestContext) throws RegistryException {
        return executeQueryPattern != null && requestContext.getResourcePath() != null && (invert !=
                executeQueryPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handlePut(RequestContext requestContext) throws RegistryException {
        return putPattern != null && (invert !=
                putPattern.matcher(requestContext.getResourcePath().getCompletePath()).matches());
    }

    public boolean handleImportResource(RequestContext requestContext) throws RegistryException {
        return importPattern != null && (invert !=
                importPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleDelete(RequestContext requestContext) throws RegistryException {
        return deletePattern != null && (invert !=
                deletePattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handlePutChild(RequestContext requestContext) throws RegistryException {
        return putChildPattern != null && (invert !=
                putChildPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleImportChild(RequestContext requestContext) throws RegistryException {
        return importChildPattern != null && (invert !=
                importChildPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleInvokeAspect(RequestContext requestContext) throws RegistryException {
        return invokeAspectPattern != null && (invert !=
                invokeAspectPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleCopy(RequestContext requestContext) throws RegistryException {
        return copyPattern != null && ((invert !=
                copyPattern.matcher(requestContext.getSourcePath()).matches()) || (invert !=
                copyPattern.matcher(requestContext.getTargetPath()).matches()));
    }

    public boolean handleMove(RequestContext requestContext) throws RegistryException {
        return movePattern != null && ((invert !=
                movePattern.matcher(requestContext.getSourcePath()).matches()) || (invert !=
                movePattern.matcher(requestContext.getTargetPath()).matches()));
    }

    public boolean handleRename(RequestContext requestContext) throws RegistryException {
        return renamePattern != null && (invert !=
                renamePattern.matcher(requestContext.getSourcePath()).matches());
    }

    public boolean handleCreateLink(RequestContext requestContext) throws RegistryException {
        return createLinkPattern != null && (invert !=
                createLinkPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleRemoveLink(RequestContext requestContext) throws RegistryException {
        return removeLinkPattern != null && (invert !=
                removeLinkPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleResourceExists(RequestContext requestContext) throws RegistryException {
        return resourceExistsPattern != null && (invert !=
                resourceExistsPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleGetRegistryContext(RequestContext requestContext) {
        return getRegistryContextPattern != null && (invert !=
                getRegistryContextPattern.matcher(requestContext.getResourcePath()
                        .getCompletePath()).matches());
    }

    public boolean handleAddAssociation(RequestContext requestContext) throws RegistryException {
        return addAssociationPattern != null && (invert !=
                addAssociationPattern.matcher(requestContext.getSourcePath()).matches());
    }

    public boolean handleRemoveAssociation(RequestContext requestContext) throws RegistryException {
        return removeAssociationPattern != null && (invert !=
                removeAssociationPattern.matcher(requestContext.getSourcePath()).matches());
    }

    public boolean handleGetAllAssociations(RequestContext requestContext)
            throws RegistryException {
        return getAllAssociationsPattern != null && (invert !=
                getAllAssociationsPattern.matcher(requestContext.getResourcePath()
                        .getCompletePath()).matches());
    }

    public boolean handleGetAssociations(RequestContext requestContext) throws RegistryException {
        return getAssociationsPattern != null && (invert !=
                getAssociationsPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleApplyTag(RequestContext requestContext) throws RegistryException {
        return applyTagPattern != null && (invert !=
                applyTagPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleGetTags(RequestContext requestContext) throws RegistryException {
        return getTagsPattern != null && (invert !=
                getTagsPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleRemoveTag(RequestContext requestContext) throws RegistryException {
        return removeTagPattern != null && (invert !=
                removeTagPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleAddComment(RequestContext requestContext) throws RegistryException {
        return addCommentPattern != null && (invert !=
                addCommentPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleEditComment(RequestContext requestContext) throws RegistryException {
        return editCommentPattern != null && (invert !=
                editCommentPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleRemoveComment(RequestContext requestContext) throws RegistryException {
        return removeCommentPattern != null && (invert !=
                removeCommentPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleGetComments(RequestContext requestContext) throws RegistryException {
        return getCommentsPattern != null && (invert !=
                getCommentsPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleRateResource(RequestContext requestContext) throws RegistryException {
        return rateResourcePattern != null && (invert !=
                rateResourcePattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleGetAverageRating(RequestContext requestContext) throws RegistryException {
        return getAverageRatingPattern != null && (invert !=
                getAverageRatingPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleGetRating(RequestContext requestContext) throws RegistryException {
        return getRatingPattern != null && (invert !=
                getRatingPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleCreateVersion(RequestContext requestContext) throws RegistryException {
        return createVersionPattern != null && (invert !=
                createVersionPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleGetVersions(RequestContext requestContext) throws RegistryException {
        return getVersionsPattern != null && (invert !=
                getVersionsPattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    public boolean handleRestoreVersion(RequestContext requestContext) throws RegistryException {
        return restoreVersionPattern != null && (invert !=
                restoreVersionPattern.matcher(requestContext.getVersionPath()).matches());
    }

    public boolean handleDump(RequestContext requestContext) throws RegistryException {
        return dumpPattern != null && (invert !=
                dumpPattern.matcher(requestContext.getResourcePath().getCompletePath()).matches());
    }

    public boolean handleRestore(RequestContext requestContext) throws RegistryException {
        return restorePattern != null && (invert !=
                restorePattern.matcher(requestContext.getResourcePath().getCompletePath())
                        .matches());
    }

    private Pattern createPattern(String pattern) {
        return Pattern.compile(pattern);
    }

    /**
     * Method to set get Pattern
     *
     * @param getPattern the get Pattern
     */
    public void setGetPattern(String getPattern) {
        this.getPatternStr = getPattern;
        this.getPattern = createPattern(getPattern);
    }

    /**
     * Method to set get Pattern
     *
     * @param executeQuery the get Pattern
     */
    public void setExecuteQueryPattern(String executeQuery) {
        this.executeQueryPatternStr = executeQuery;
        this.executeQueryPattern = createPattern(executeQuery);
    }

    /**
     * Method to set put Pattern
     *
     * @param putPattern the put Pattern
     */
    public void setPutPattern(String putPattern) {
        this.putPatternStr = putPattern;
        this.putPattern = createPattern(putPattern);
    }

    /**
     * Method to set import Pattern
     *
     * @param importPattern the import Pattern
     */
    public void setImportPattern(String importPattern) {
        this.importPatternStr = importPattern;
        this.importPattern = createPattern(importPattern);
    }

    /**
     * Method to set delete Pattern
     *
     * @param deletePattern the delete Pattern
     */
    public void setDeletePattern(String deletePattern) {
        this.deletePatternStr = deletePattern;
        this.deletePattern = createPattern(deletePattern);
    }

    /**
     * Method to set putChild Pattern
     *
     * @param putChildPattern the putChild Pattern
     */
    public void setPutChildPattern(String putChildPattern) {
        this.putChildPatternStr = putChildPattern;
        this.putChildPattern = createPattern(putChildPattern);
    }

    /**
     * Method to set importChild Pattern
     *
     * @param importChildPattern the importChild Pattern
     */
    public void setImportChildPattern(String importChildPattern) {
        this.importChildPatternStr = importChildPattern;
        this.importChildPattern = createPattern(importChildPattern);
    }

    /**
     * Method to set invokeAspect Pattern
     *
     * @param invokeAspectPattern the invokeAspect Pattern
     */
    public void setInvokeAspectPattern(String invokeAspectPattern) {
        this.invokeAspectPatternStr = invokeAspectPattern;
        this.invokeAspectPattern = createPattern(invokeAspectPattern);
    }

    /**
     * Method to set move Pattern
     *
     * @param movePattern the move Pattern
     */
    public void setMovePattern(String movePattern) {
        this.movePatternStr = movePattern;
        this.movePattern = createPattern(movePattern);
    }

    /**
     * Method to set copy Pattern
     *
     * @param copyPattern the copy Pattern
     */
    public void setCopyPattern(String copyPattern) {
        this.copyPatternStr = copyPattern;
        this.copyPattern = createPattern(copyPattern);
    }

    /**
     * Method to set rename Pattern
     *
     * @param renamePattern the rename Pattern
     */
    public void setRenamePattern(String renamePattern) {
        this.renamePatternStr = renamePattern;
        this.renamePattern = createPattern(renamePattern);
    }

    /**
     * Method to set createLink Pattern
     *
     * @param createLinkPattern the createLink Pattern
     */
    public void setCreateLinkPattern(String createLinkPattern) {
        this.createLinkPatternStr = createLinkPattern;
        this.createLinkPattern = createPattern(createLinkPattern);
    }

    /**
     * Method to set removeLink Pattern
     *
     * @param removeLinkPattern the removeLink Pattern
     */
    public void setRemoveLinkPattern(String removeLinkPattern) {
        this.removeLinkPatternStr = removeLinkPattern;
        this.removeLinkPattern = createPattern(removeLinkPattern);
    }

    /**
     * Method to set resourceExists Pattern
     *
     * @param resourceExistsPattern the resourceExists Pattern
     */
    public void setResourceExistsPattern(String resourceExistsPattern) {
        this.resourceExistsPatternStr = resourceExistsPattern;
        this.resourceExistsPattern = createPattern(resourceExistsPattern);
    }

    /**
     * Method to set getRegistryContext Pattern
     *
     * @param getRegistryContextPattern the getRegistryContext Pattern
     */
    public void setGetRegistryContextPattern(String getRegistryContextPattern) {
        this.getRegistryContextPatternStr = getRegistryContextPattern;
        this.getRegistryContextPattern = createPattern(getRegistryContextPattern);
    }

    /**
     * Method to set addAssociation Pattern
     *
     * @param addAssociationPattern the addAssociation Pattern
     */
    public void setAddAssociationPattern(String addAssociationPattern) {
        this.addAssociationPatternStr = addAssociationPattern;
        this.addAssociationPattern = createPattern(addAssociationPattern);
    }

    /**
     * Method to set removeAssociation Pattern
     *
     * @param removeAssociationPattern the removeAssociation Pattern
     */
    public void setRemoveAssociationPattern(String removeAssociationPattern) {
        this.removeAssociationPatternStr = removeAssociationPattern;
        this.removeAssociationPattern = createPattern(removeAssociationPattern);
    }

    /**
     * Method to set getAllAssociations Pattern
     *
     * @param getAllAssociationsPattern the getAllAssociations Pattern
     */
    public void setGetAllAssociationsPattern(String getAllAssociationsPattern) {
        this.getAllAssociationsPatternStr = getAllAssociationsPattern;
        this.getAllAssociationsPattern = createPattern(getAllAssociationsPattern);
    }

    /**
     * Method to set getAssociations Pattern
     *
     * @param getAssociationsPattern the getAssociations Pattern
     */
    public void setGetAssociationsPattern(String getAssociationsPattern) {
        this.getAssociationsPatternStr = getAssociationsPattern;
        this.getAssociationsPattern = createPattern(getAssociationsPattern);
    }

    /**
     * Method to set applyTag Pattern
     *
     * @param applyTagPattern the applyTag Pattern
     */
    public void setApplyTagPattern(String applyTagPattern) {
        this.applyTagPatternStr = applyTagPattern;
        this.applyTagPattern = createPattern(applyTagPattern);
    }

    /**
     * Method to set getTags Pattern
     *
     * @param getTagsPattern the getTags Pattern
     */
    public void setGetTagsPattern(String getTagsPattern) {
        this.getTagsPatternStr = getTagsPattern;
        this.getTagsPattern = createPattern(getTagsPattern);
    }

    /**
     * Method to set removeTag Pattern
     *
     * @param removeTagPattern the removeTag Pattern
     */
    public void setRemoveTagPattern(String removeTagPattern) {
        this.removeTagPatternStr = removeTagPattern;
        this.removeTagPattern = createPattern(removeTagPattern);
    }

    /**
     * Method to set addComment Pattern
     *
     * @param addCommentPattern the addComment Pattern
     */
    public void setAddCommentPattern(String addCommentPattern) {
        this.addCommentPatternStr = addCommentPattern;
        this.addCommentPattern = createPattern(addCommentPattern);
    }

    /**
     * Method to set editComment Pattern
     *
     * @param editCommentPattern the editComment Pattern
     */
    public void setEditCommentPattern(String editCommentPattern) {
        this.editCommentPatternStr = editCommentPattern;
        this.editCommentPattern = createPattern(editCommentPattern);
    }

    /**
     * Method to set removeComment Pattern
     *
     * @param removeCommentPattern the removeComment Pattern
     */
    public void setRemoveCommentPattern(String removeCommentPattern) {
        this.removeCommentPatternStr = removeCommentPattern;
        this.removeCommentPattern = createPattern(removeCommentPattern);
    }

    /**
     * Method to set getComments Pattern
     *
     * @param getCommentsPattern the getComments Pattern
     */
    public void setGetCommentsPattern(String getCommentsPattern) {
        this.getCommentsPatternStr = getCommentsPattern;
        this.getCommentsPattern = createPattern(getCommentsPattern);
    }

    /**
     * Method to set rateResource Pattern
     *
     * @param rateResourcePattern the rateResource Pattern
     */
    public void setRateResourcePattern(String rateResourcePattern) {
        this.rateResourcePatternStr = rateResourcePattern;
        this.rateResourcePattern = createPattern(rateResourcePattern);
    }

    /**
     * Method to set getAverageRating Pattern
     *
     * @param getAverageRatingPattern the getAverageRating Pattern
     */
    public void setGetAverageRatingPattern(String getAverageRatingPattern) {
        this.getAverageRatingPatternStr = getAverageRatingPattern;
        this.getAverageRatingPattern = createPattern(getAverageRatingPattern);
    }

    /**
     * Method to set getRating Pattern
     *
     * @param getRatingPattern the getRating Pattern
     */
    public void setGetRatingPattern(String getRatingPattern) {
        this.getRatingPatternStr = getRatingPattern;
        this.getRatingPattern = createPattern(getRatingPattern);
    }

    /**
     * Method to set createVersion Pattern
     *
     * @param createVersionPattern the createVersion Pattern
     */
    public void setCreateVersionPattern(String createVersionPattern) {
        this.createVersionPatternStr = createVersionPattern;
        this.createVersionPattern = createPattern(createVersionPattern);
    }

    /**
     * Method to set getVersions Pattern
     *
     * @param getVersionsPattern the getVersions Pattern
     */
    public void setGetVersionsPattern(String getVersionsPattern) {
        this.getVersionsPatternStr = getVersionsPattern;
        this.getVersionsPattern = createPattern(getVersionsPattern);
    }

    /**
     * Method to set restoreVersion Pattern
     *
     * @param restoreVersionPattern the restoreVersion Pattern
     */
    public void setRestoreVersionPattern(String restoreVersionPattern) {
        this.restoreVersionPatternStr = restoreVersionPattern;
        this.restoreVersionPattern = createPattern(restoreVersionPattern);
    }

    /**
     * Method to set dump Pattern
     *
     * @param dumpPattern the dump Pattern
     */
    public void setDumpPattern(String dumpPattern) {
        this.dumpPatternStr = dumpPattern;
        this.dumpPattern = createPattern(dumpPattern);
    }

    /**
     * Method to set restore Pattern
     *
     * @param restorePattern the restore Pattern
     */
    public void setRestorePattern(String restorePattern) {
        this.restorePatternStr = restorePattern;
        this.restorePattern = createPattern(restorePattern);
    }

    /**
     * Method to set the given pattern for all registry operations.
     *
     * @param pattern the pattern to set.
     */
    public void setPattern(String pattern) {
        setResourceExistsPattern(pattern);
        setGetRegistryContextPattern(pattern);
        setGetPattern(pattern);
        setExecuteQueryPattern(pattern);
        setPutPattern(pattern);
        setDeletePattern(pattern);
        setRenamePattern(pattern);
        setMovePattern(pattern);
        setCopyPattern(pattern);
        setGetAverageRatingPattern(pattern);
        setGetRatingPattern(pattern);
        setRateResourcePattern(pattern);
        setGetCommentsPattern(pattern);
        setEditCommentPattern(pattern);
        setAddCommentPattern(pattern);
        setRemoveCommentPattern(pattern);
        setGetTagsPattern(pattern);
        setRemoveTagPattern(pattern);
        setApplyTagPattern(pattern);
        setGetAllAssociationsPattern(pattern);
        setGetAssociationsPattern(pattern);
        setAddAssociationPattern(pattern);
        setDumpPattern(pattern);
        setRestorePattern(pattern);
        setCreateVersionPattern(pattern);
        setGetVersionsPattern(pattern);
        setRestoreVersionPattern(pattern);
        setRemoveAssociationPattern(pattern);
        setImportPattern(pattern);
        setCreateLinkPattern(pattern);
        setRemoveLinkPattern(pattern);
        setInvokeAspectPattern(pattern);
        setImportChildPattern(pattern);
        setPutChildPattern(pattern);
    }
}
