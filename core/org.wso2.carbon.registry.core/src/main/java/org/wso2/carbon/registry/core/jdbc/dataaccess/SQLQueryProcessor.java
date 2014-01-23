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

package org.wso2.carbon.registry.core.jdbc.dataaccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.StaticConfiguration;
import org.wso2.carbon.registry.core.dao.ResourceDAO;
import org.wso2.carbon.registry.core.dao.TagsDAO;
import org.wso2.carbon.registry.core.dataaccess.DataAccessManager;
import org.wso2.carbon.registry.core.dataaccess.QueryProcessor;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.DatabaseConstants;
import org.wso2.carbon.registry.core.dao.CommentsDAO;
import org.wso2.carbon.registry.core.dao.RatingsDAO;
import org.wso2.carbon.registry.core.jdbc.dao.JDBCCommentsDAO;
import org.wso2.carbon.registry.core.jdbc.dao.JDBCRatingsDAO;
import org.wso2.carbon.registry.core.jdbc.dataobjects.RatingDO;
import org.wso2.carbon.registry.core.jdbc.dataobjects.TaggingDO;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * The query processor to execute sql queries.
 */
public class SQLQueryProcessor implements QueryProcessor {

    private static final Log log = LogFactory.getLog(SQLQueryProcessor.class);
    private ResourceDAO resourceDAO;
    private CommentsDAO commentsDAO;
    private RatingsDAO ratingsDAO;
    private TagsDAO tagsDAO;

    /**
     * DataSource of the registry database. URL handlers can access this to construct resources by
     * combining various tables (e.g. comments).
     */
    protected DataSource dataSource;

    /**
     * Initialize the sql query processor
     *
     * @param dataAccessManager the data access manager to be set.
     */
    public SQLQueryProcessor(DataAccessManager dataAccessManager) {
        if (dataAccessManager instanceof JDBCDataAccessManager) {
            this.dataSource = ((JDBCDataAccessManager)dataAccessManager).getDataSource();
        } else {
            log.error("Invalid data access manager.");
        }
        this.resourceDAO = dataAccessManager.getDAOManager().getResourceDAO();
        this.commentsDAO = dataAccessManager.getDAOManager().getCommentsDAO(
                StaticConfiguration.isVersioningComments());
        this.ratingsDAO = dataAccessManager.getDAOManager().getRatingsDAO(
                StaticConfiguration.isVersioningRatings());
        this.tagsDAO = dataAccessManager.getDAOManager().getTagsDAO(
                StaticConfiguration.isVersioningTags());
    }

    public Collection executeQuery(Registry registry, Resource query, Map parameters)
            throws RegistryException {

        Collection resultCollection = null;

        Connection conn;
        String sqlString;
        ResultSet results = null;
        PreparedStatement s = null;

        try {

            Object obj = query.getContent();
            if (parameters != null) {
                Object querySQL = parameters.get("query");
                if (querySQL != null) {
                    obj = querySQL;
                }
                Object resultType = parameters.get(RegistryConstants.RESULT_TYPE_PROPERTY_NAME);
                if (resultType != null) {
                    if (resultType instanceof String) {
                        query.setProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                                (String) resultType);
                    }
                }
            }
            if (obj instanceof String) {
                sqlString = (String) obj;
            } else if (obj instanceof byte[]) {
                sqlString = RegistryUtils.decodeBytes((byte[]) obj);
            } else {
                throw new RegistryException("Unable to execute query at " + query.getPath()
                        + ".Found resource content of type " +
                        (obj == null ? "null" : obj.getClass().getName())
                        + ".Expected java.lang.String or byte[]");
            }

            conn = JDBCDatabaseTransaction.getConnection();

            // adding the tenant ids for the query
            TenantAwareSQLTransformer transformer = new TenantAwareSQLTransformer(sqlString);
            String transformedQuery = transformer.getTransformedQuery();
            int transformedParameterCount = transformer.getAdditionalParameterCount();
//            int trailingParameterCount = transformer.getTrailingParameterCount();

            s = conn.prepareStatement(transformedQuery);
            /*s = conn.prepareStatement(transformedQuery, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);*/

            int nextParameterIndex = 0;
            /*if (parameters != null) {
                List<String> exclusions = Arrays.asList("content", "query",
                        RegistryConstants.RESULT_TYPE_PROPERTY_NAME);
                for (Object parameterNumberObject : parameters.keySet()) {
                    String parameterNumber = (String) parameterNumberObject;
                    if (exclusions.contains(parameterNumber)) {
                        continue;
                    }
                    Object parameterValue = parameters.get(parameterNumber);
                    s.setObject(Integer.parseInt(parameterNumber), parameterValue);
                    nextParameterIndex++;
                }
                if (trailingParameterCount > 0 && transformedParameterCount > 0) {
                    // Move trailing parameters to the end.
                    for (int i = 0; i < trailingParameterCount; i++) {
                        nextParameterIndex--;
                        s.setObject(nextParameterIndex + transformedParameterCount,
                                parameters.get(Integer.toString(nextParameterIndex)));
                    }
                }
            }
            // adding the additional parameters caused due to adding the tenant id,
            for (int i = 0; i < transformedParameterCount; i++) {
                s.setInt(nextParameterIndex, CurrentSession.getTenantId());
                nextParameterIndex++;
            }*/

            // adding the additional parameters caused due to adding the tenant id,
            for (int i = 0; i < transformedParameterCount; i++) {
                nextParameterIndex++;
                s.setInt(nextParameterIndex, CurrentSession.getTenantId());
            }

            if (parameters != null) {
                List<String> exclusions = Arrays.asList("content", "query", "mediaType",
                        RegistryConstants.RESULT_TYPE_PROPERTY_NAME);
                for (Object parameterNumberObject : parameters.keySet()) {
                    String parameterNumber = (String) parameterNumberObject;
                    if (exclusions.contains(parameterNumber)) {
                        continue;
                    }
                    Object parameterValue = parameters.get(parameterNumber);
                    s.setObject(Integer.parseInt(parameterNumber) + nextParameterIndex,
                            parameterValue);
                }
            }

            results = s.executeQuery();

            String resultType = query.getProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME);

            if (resultType == null) {
                resultType = RegistryConstants.RESOURCES_RESULT_TYPE;
                query.setProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME, resultType);
            }

            if (resultType.equals(RegistryConstants.RESOURCES_RESULT_TYPE)) {

                // Result is a normal resource, which is stored in the Resources table or a
                // collection of normal resources.

                resultCollection = fillResourcesCollection(results);

            } else if (resultType.equals(RegistryConstants.RESOURCE_UUID_RESULT_TYPE)) {
                resultCollection = fillResourceUUIDCollection(results);

            } else if (resultType.equals(RegistryConstants.COMMENTS_RESULT_TYPE)) {

                resultCollection = fillCommentsCollection(results, conn);

            } else if (resultType.equals(RegistryConstants.RATINGS_RESULT_TYPE)) {

                resultCollection = fillRatingsCollection(results);

            } else if (resultType.equals(RegistryConstants.TAGS_RESULT_TYPE)) {

                resultCollection = fillTagsCollection(results);
            } else if (resultType.equals(RegistryConstants.TAG_SUMMARY_RESULT_TYPE)){
                resultCollection = fillTagSummaryCollection(results);

                // Result is in the form of (tag, number of taggings) tuples
                // Fill TagCount objects with results and add them as an array

/*
            } else if (resultType.equals("ResourcePathsWithTagCount")) {

                // Result is in the form of (resource path, number of taggings) tuples
                // Fill ResourceTagCount objects with results and add them as an array
*/

            }

            if (resultCollection == null) {
                String msg = "Unknown result type: " + resultType + " defined for the query: " +
                        sqlString + ((query.getPath() != null) ? " located in path: " +
                        query.getPath() : "");
                log.error(msg);
                throw new RegistryException(msg);
            }

        } catch (SQLException e) {
            throw new RegistryException(e.getMessage());

        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException e) {
                    String msg = "Failed to close the result set. " + e.getMessage();
                    log.error(msg, e);
                }
            }

            if (s != null) {
                try {
                    s.close();
                } catch (SQLException e) {
                    log.error("Failed to close the statement. " + e.getMessage());
                }
            }
        }

        if (resultCollection != null && RegistryConstants.RESOURCES_RESULT_TYPE.equals(
                query.getProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME))) {

            List<String> filteredResults = new ArrayList<String>();
            String[] resultPaths = resultCollection.getChildren();
            for (String resultPath : resultPaths) {
                if (AuthorizationUtils.authorize(resultPath, ActionConstants.GET)) {
                    filteredResults.add(resultPath);
                }
            }

            String[] filteredContent = filteredResults.toArray(new String[filteredResults.size()]);
            resultCollection.setContent(filteredContent);
        }

        return resultCollection;
    }

    /**
     * Fil the resource collection from a query result set with resources
     *
     * @param results The result set object.
     *
     * @return A collection containing results as children.
     * @throws SQLException      throws if the iterating results failed.
     * @throws RegistryException throws if constructing child pas failed.
     */
    private Collection fillResourcesCollection(ResultSet results)
            throws SQLException, RegistryException {

        // Result is a normal resource, which is stored in the Resources table or a collection
        // of normal resources.
	
	//We can't use a HashSet here, because it doesn't keep the order that may lead to failures of queries having ORDER BY
        Set<String>  pathSet = new LinkedHashSet<String>();        
        while (results.next()) {
            int pathId = results.getInt(DatabaseConstants.PATH_ID_FIELD);
            String resourceName = results.getString(DatabaseConstants.NAME_FIELD);
            String path = resourceDAO.getPath(pathId, resourceName, false);
            if(path!=null){
                pathSet.add(path);
            }
        }
        String[] paths = pathSet.toArray(new String[pathSet.size()]);
        return new CollectionImpl(paths);
    }


    /**
     * Fil the resource collection from a query result set with comments
     *
     * @param results The result set object.
     * @param conn    connection object.
     *
     * @return A collection containing results as children.
     * @throws SQLException      throws if the iterating results failed.
     * @throws RegistryException throws if constructing child pas failed.
     */
    private Collection fillCommentsCollection(ResultSet results, Connection conn)
            throws SQLException, RegistryException {

        if (!(commentsDAO instanceof JDBCCommentsDAO)) {
            String msg = "Failed to list of comments. Invalid comments data access object.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        // SQL query should return a list of DatabaseConstants.COMMENT_ID_FIELD.
        // resultArtifact contains a String[] of URLs for comments.

        // URL for a comment <resource_path>?comment<comment_id>
        // e.g. /p1/r1?comment12

        List<Long> commentIDs = new ArrayList<Long>();

        while (results.next()) {
            long commentID = results.getLong(DatabaseConstants.COMMENT_ID_FIELD);
            commentIDs.add(commentID);
        }

        String[] commentPaths = ((JDBCCommentsDAO)commentsDAO).getResourcePathsOfComments(
                commentIDs.toArray(new Long[commentIDs.size()]), conn);

        return new CollectionImpl(commentPaths);
    }

    /**
     * Fil the resource collection from a query result set with ratings
     *
     * @param results The result set object.
     *
     * @return A collection containing results as children.
     * @throws SQLException      throws if the iterating results failed.
     * @throws RegistryException throws if constructing child pas failed.
     */
    private Collection fillRatingsCollection(ResultSet results)
            throws SQLException, RegistryException {

        if (!(ratingsDAO instanceof JDBCRatingsDAO)) {
            String msg = "Failed to list of ratings. Invalid ratings data access object.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        // SQL query should return a list of DatabaseConstants.RATING_ID_FIELD
        // resultArtifact contains a String[] of URLs for ratings.

        // URL for a comment <resource_path>;ratings:<userName>
        // e.g. /p1/r1;ratings:foo

        List<String> ratingPathList = new ArrayList<String>();

        while (results.next()) {
            long ratingID = results.getLong(DatabaseConstants.RATING_ID_FIELD);
            RatingDO ratingDAO = ((JDBCRatingsDAO)ratingsDAO).getRating(ratingID);
            String ratingPath =
                    ratingDAO.getResourcePath() + RegistryConstants.URL_SEPARATOR + "ratings:" +
                            ratingDAO.getRatedUserName();
            ratingPathList.add(ratingPath);
        }

        String[] ratingPaths =
                ratingPathList.toArray(new String[ratingPathList.size()]);

        return new CollectionImpl(ratingPaths);
    }

    /**
     * Fil the resource collection from a query result set with tags
     *
     * @param results The result set object.
     *
     * @return A collection containing results as children.
     * @throws SQLException      throws if the iterating results failed.
     * @throws RegistryException throws if constructing child pas failed.
     */
    private Collection fillTagsCollection(ResultSet results)
            throws SQLException, RegistryException {

        // URL for a tag /p1/r1;tags:tagName:userName

        List<String> tagPathList = new ArrayList<String>();
        while (results.next()) {

            long taggingID = results.getLong(DatabaseConstants.TAGGING_ID_FIELD);

            TaggingDO taggingDO = tagsDAO.getTagging(taggingID);

            String tagPath = taggingDO.getResourcePath() + RegistryConstants.URL_SEPARATOR +
                    "tags:" + taggingDO.getTagName() + ":" + taggingDO.getTaggedUserName();
            tagPathList.add(tagPath);
        }

        String[] tagPaths = tagPathList.toArray(new String[tagPathList.size()]);

        return new CollectionImpl(tagPaths);
    }

    /**
     * A summary count of all tags. Format is "tagname # totalcount"
     * @param results
     * @return
     * @throws SQLException
     * @throws RegistryException
     */
    private Collection fillTagSummaryCollection(ResultSet results)
        throws SQLException, RegistryException{
        List<String> tagPathList = new ArrayList<String>();
        String mockPath;
        String tagName;
        int tagOccurrence;

        while(results.next()){
            mockPath = results.getString(DatabaseConstants.MOCK_PATH);
            tagName = results.getString(DatabaseConstants.TAG_NAME);
            tagOccurrence = results.getInt(DatabaseConstants.USED_COUNT);

            String tagPath = mockPath+";"+tagName +":"+String.valueOf(tagOccurrence);
            tagPathList.add(tagPath);
        }
        String[] tagPaths = tagPathList.toArray(new String[tagPathList.size()]);
        return new CollectionImpl(tagPaths);
    }

    /**
     * Fill resource path collection from a query result set
     *
     * @param results The result set object.
     *
     * @return A collection containing results as children.
     * @throws SQLException      throws if the iterating results failed.
     * @throws RegistryException throws if constructing child pas failed.
     */
    private Collection fillResourceUUIDCollection(ResultSet results)
            throws SQLException, RegistryException {
        List<String> uuidList = new ArrayList<String>();
        String mockPath;
        String resourceUUID;
        String path;
        while (results.next()) {
            mockPath = results.getString(DatabaseConstants.MOCK_PATH);
            resourceUUID = results.getString(DatabaseConstants.UUID_FIELD);
            path = mockPath+";"+resourceUUID;

            if (path != null && (!uuidList.contains(path))) {
                uuidList.add(path);
            }
        }

        String[] paths = uuidList.toArray(new String[uuidList.size()]);
        return new CollectionImpl(paths);
    }

}
