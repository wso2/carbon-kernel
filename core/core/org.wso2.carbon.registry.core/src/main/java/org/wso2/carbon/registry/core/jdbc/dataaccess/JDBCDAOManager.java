/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.jdbc.dataaccess;

import org.wso2.carbon.registry.core.dao.*;
import org.wso2.carbon.registry.core.dataaccess.DAOManager;
import org.wso2.carbon.registry.core.jdbc.dao.*;

/**
 * An implementation of {@link DAOManager} to obtain access to the object representations of various
 * data stored on a back-end JDBC-based database.
 */
public class JDBCDAOManager implements DAOManager {

    private AssociationDAO associationDAO;
    private CommentsDAO commentsDAO;
    private CommentsDAO commentsVersionDAO;
    private RatingsDAO ratingsDAO;
    private RatingsDAO ratingsVersionDAO;
    private TagsDAO tagsDAO;
    private TagsDAO tagsVersionDAO;
    private LogsDAO logsDAO;
    private ResourceDAO resourceDAO;
    private ResourceVersionDAO resourceVersionDAO;

    public JDBCDAOManager() {
        this.resourceDAO = new JDBCResourceDAO();
        this.associationDAO = new JDBCAssociationDAO();
        this.logsDAO = new JDBCLogsDAO();

        // All data access objects below need an instance of a resource data access object to be
        // present on the DAO manager.
        this.commentsDAO = new JDBCCommentsDAO(this);
        this.commentsVersionDAO = new JDBCCommentsVersionDAO(this);
        this.ratingsDAO = new JDBCRatingsDAO(this);
        this.ratingsVersionDAO = new JDBCRatingsVersionDAO(this);
        this.tagsDAO = new JDBCTagsDAO(this);
        this.tagsVersionDAO = new JDBCTagsVersionDAO(this);

        // All data access object below needs multiple instances of data access objects created
        // above to be present on the DAO manager.
        this.resourceVersionDAO = new JDBCResourceVersionDAO(this);
    }

    public AssociationDAO getAssociationDAO() {
        return associationDAO;
    }

    public CommentsDAO getCommentsDAO(boolean isVersioned) {
        return isVersioned ? commentsVersionDAO : commentsDAO;
    }

    public RatingsDAO getRatingsDAO(boolean isVersioned) {
        return isVersioned ? ratingsVersionDAO : ratingsDAO;
    }

    public TagsDAO getTagsDAO(boolean isVersioned) {
        return isVersioned ? tagsVersionDAO : tagsDAO;
    }

    public LogsDAO getLogsDAO() {
        return logsDAO;
    }

    public ResourceDAO getResourceDAO() {
        return resourceDAO;
    }

    public ResourceVersionDAO getResourceVersionDAO() {
        return resourceVersionDAO;
    }
}
