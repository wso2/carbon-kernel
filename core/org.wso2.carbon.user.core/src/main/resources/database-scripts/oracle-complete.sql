
CREATE TABLE REG_CONTENT (
                    CONTENT_ID VARCHAR2 (50),
                    CONTENT_DATA BLOB,
                    PRIMARY KEY (CONTENT_ID))
/
CREATE TABLE REG_RESOURCE (
                    RID VARCHAR2 (50),
                    PATH VARCHAR2 (2000) NOT NULL,
                    MEDIA_TYPE VARCHAR2 (500),
                    COLLECTION INTEGER NOT NULL,
                    CREATOR VARCHAR2 (500),
                    CREATED_TIME TIMESTAMP,
                    LAST_UPDATOR VARCHAR2 (500),
                    LAST_UPDATED_TIME TIMESTAMP,
                    DESCRIPTION VARCHAR2 (4000),
                    CONTENT_ID VARCHAR2 (50),
                    EQUIVALENT_VERSION INTEGER NOT NULL,
                    ASSOCIATED_SNAPSHOT_ID INTEGER NOT NULL,
                    PRIMARY KEY (RID),
                    FOREIGN KEY (CONTENT_ID) REFERENCES REG_CONTENT (CONTENT_ID))
/
CREATE INDEX REG_RESOURCE_PATH ON REG_RESOURCE (PATH)
/
CREATE TABLE REG_DEPENDENCY (
                    DEPENDENCY_ID INTEGER,
                    PARENT_RID VARCHAR2 (50) NOT NULL,
                    CHILD_RID VARCHAR2 (50) NOT NULL,
                    PRIMARY KEY (DEPENDENCY_ID),
                    UNIQUE (PARENT_RID, CHILD_RID),
                    FOREIGN KEY (PARENT_RID) REFERENCES REG_RESOURCE (RID) ON DELETE CASCADE,
                    FOREIGN KEY (CHILD_RID) REFERENCES REG_RESOURCE (RID) ON DELETE CASCADE)
/
CREATE SEQUENCE REG_DEPENDENCY_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER REG_DEPENDENCY_TRIGGER
                    BEFORE INSERT
                    ON REG_DEPENDENCY
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT REG_DEPENDENCY_SEQUENCE.nextval INTO :NEW.DEPENDENCY_ID FROM dual;
                    END;
/
CREATE TABLE REG_PROPERTY (
                    PROPERTY_ID INTEGER,
                    RID VARCHAR2 (50) NOT NULL,
                    NAME VARCHAR2 (100) NOT NULL,
                    PROPERTY_VALUE VARCHAR2 (500),
                    PRIMARY KEY (PROPERTY_ID),
                    FOREIGN KEY (RID) REFERENCES REG_RESOURCE (RID) ON DELETE CASCADE)
/
CREATE SEQUENCE REG_PROPERTY_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER REG_PROPERTY_TRIGGER
                    BEFORE INSERT
                    ON REG_PROPERTY
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT REG_PROPERTY_SEQUENCE.nextval INTO :NEW.PROPERTY_ID FROM dual;
                    END;
/
CREATE TABLE REG_ASSOCIATION (
                    ASSOCIATION_ID INTEGER,
                    SOURCEPATH VARCHAR2 (2000) NOT NULL,
                    TARGETPATH VARCHAR2 (2000) NOT NULL,
                    ASSOCIATION_TYPE VARCHAR2 (2000) NOT NULL,
                    PRIMARY KEY (ASSOCIATION_ID))
/
CREATE SEQUENCE REG_ASSOCIATION_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER REG_ASSOCIATION_TRIGGER
                    BEFORE INSERT
                    ON REG_ASSOCIATION
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT REG_ASSOCIATION_SEQUENCE.nextval INTO :NEW.ASSOCIATION_ID FROM dual;
                    END;
/
CREATE INDEX INDEX_ASSOCIATION_SOURCEPATH ON REG_ASSOCIATION (SOURCEPATH)
/
CREATE INDEX INDEX_ASSOCIATION_TARGETPATH ON REG_ASSOCIATION (TARGETPATH)
/
CREATE TABLE REG_TAG (
                    TAG_ID INTEGER,
                    TAG_NAME VARCHAR2 (500) NOT NULL,
                    RID VARCHAR2 (50) NOT NULL,
                    USER_ID VARCHAR2 (20) NOT NULL,
                    TAGGED_TIME TIMESTAMP NOT NULL,
                    PRIMARY KEY (TAG_ID),
                    UNIQUE (TAG_NAME, RID, USER_ID))
/
CREATE SEQUENCE REG_TAG_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER REG_TAG_TRIGGER
                    BEFORE INSERT
                    ON REG_TAG
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT REG_TAG_SEQUENCE.nextval INTO :NEW.TAG_ID FROM dual;
                    END;
/
CREATE TABLE REG_COMMENT (
                    COMMENT_ID INTEGER,
                    RID VARCHAR2 (50) NOT NULL,
                    USER_ID VARCHAR2 (20) NOT NULL,
                    COMMENT_TEXT VARCHAR2 (500) NOT NULL,
                    COMMENTED_TIME TIMESTAMP NOT NULL,
                    PRIMARY KEY (COMMENT_ID))
/
CREATE SEQUENCE REG_COMMENT_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER REG_COMMENT_TRIGGER
                    BEFORE INSERT
                    ON REG_COMMENT
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT REG_COMMENT_SEQUENCE.nextval INTO :NEW.COMMENT_ID FROM dual;
                    END;
/
CREATE TABLE REG_RATING (
                    RATING_ID INTEGER,
                    RID VARCHAR2 (50) NOT NULL,
                    USER_ID VARCHAR2 (20) NOT NULL,
                    RATING INTEGER NOT NULL,
                    RATED_TIME TIMESTAMP NOT NULL,
                    PRIMARY KEY (RATING_ID))
/
CREATE SEQUENCE REG_RATING_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER REG_RATING_TRIGGER
                    BEFORE INSERT
                    ON REG_RATING
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT REG_RATING_SEQUENCE.nextval INTO :NEW.RATING_ID FROM dual;
                    END;
/
CREATE TABLE REG_LOG (
                    LOG_ID INTEGER,
                    PATH VARCHAR2 (2000),
                    USER_ID VARCHAR2 (20) NOT NULL,
                    LOGGED_TIME TIMESTAMP NOT NULL,
                    ACTION INTEGER NOT NULL,
                    ACTION_DATA VARCHAR2 (500),
                    PRIMARY KEY (LOG_ID))
/
CREATE SEQUENCE REG_LOG_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER REG_LOG_TRIGGER
                    BEFORE INSERT
                    ON REG_LOG
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT REG_LOG_SEQUENCE.nextval INTO :NEW.LOG_ID FROM dual;
                    END;
/
CREATE TABLE REG_CONTENT_VERSION (
                    CONTENT_VERSION_ID VARCHAR2 (50),
                    CONTENT_DATA BLOB,
                    PRIMARY KEY (CONTENT_VERSION_ID))
/
CREATE TABLE REG_RESOURCE_VERSION (
                    RESOURCE_VERSION_ID INTEGER,
                    RID VARCHAR2 (50) NOT NULL,
                    VERSION INTEGER NOT NULL,
                    PATH VARCHAR2 (2000) NOT NULL,
                    MEDIA_TYPE VARCHAR2 (500),
                    COLLECTION INTEGER NOT NULL,
                    CREATOR VARCHAR2 (500),
                    CREATED_TIME TIMESTAMP,
                    LAST_UPDATOR VARCHAR2 (500),
                    LAST_UPDATED_TIME TIMESTAMP,
                    DESCRIPTION VARCHAR2 (4000),
                    CONTENT_ID VARCHAR2 (50),
                    ASSOCIATED_SNAPSHOT_ID INTEGER NOT NULL,
                    FOREIGN KEY (CONTENT_ID) REFERENCES REG_CONTENT_VERSION (CONTENT_VERSION_ID),
                    PRIMARY KEY (RESOURCE_VERSION_ID),
                    UNIQUE(RID, VERSION))
/
CREATE SEQUENCE REG_RESOURCE_VERSION_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER REG_RESOURCE_VERSION_TRIGGER
                    BEFORE INSERT
                    ON REG_RESOURCE_VERSION
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT REG_RESOURCE_VERSION_SEQUENCE.nextval INTO :NEW.RESOURCE_VERSION_ID FROM dual;
                    END;
/
CREATE TABLE REG_DEPENDENCY_VERSION (
                    DEPENDENCY_VERSION_ID INTEGER,
                    PARENT_RID VARCHAR2 (50) NOT NULL,
                    PARENT_VERSION INTEGER NOT NULL,
                    CHILD_RID VARCHAR2 (50) NOT NULL,
                    PRIMARY KEY (DEPENDENCY_VERSION_ID),
                    UNIQUE (PARENT_RID, PARENT_VERSION, CHILD_RID))
/
CREATE SEQUENCE REG_DEP_VERSION_SEQ START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER REG_DEPENDENCY_VERSION_TRIGGER
                    BEFORE INSERT
                    ON REG_DEPENDENCY_VERSION
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT REG_DEP_VERSION_SEQ.nextval INTO :NEW.DEPENDENCY_VERSION_ID FROM dual;
                    END;
/
CREATE TABLE REG_PROPERTY_VERSION (
                    PROPERTY_VERSION_ID INTEGER,
                    RID VARCHAR2 (50) NOT NULL,
                    VERSION INTEGER NOT NULL,
                    NAME VARCHAR2 (100) NOT NULL,
                    PROPERTY_VALUE VARCHAR2 (500),
                    PRIMARY KEY (PROPERTY_VERSION_ID),
                    FOREIGN KEY (RID, VERSION) REFERENCES REG_RESOURCE_VERSION (RID, VERSION))
/
CREATE SEQUENCE REG_PROPERTY_VERSION_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER REG_PROPERTY_VERSION_TRIGGER
                    BEFORE INSERT
                    ON REG_PROPERTY_VERSION
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT REG_PROPERTY_VERSION_SEQUENCE.nextval INTO :NEW.PROPERTY_VERSION_ID FROM dual;
                    END;
/
CREATE TABLE REG_SNAPSHOT (
                    SNAPSHOT_ID INTEGER,
                    ROOT_ID VARCHAR2 (50) NOT NULL,
                    PRIMARY KEY (SNAPSHOT_ID),
                    UNIQUE (SNAPSHOT_ID, ROOT_ID))
/
CREATE SEQUENCE REG_SNAPSHOT_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER REG_SNAPSHOT_TRIGGER
                    BEFORE INSERT
                    ON REG_SNAPSHOT
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT REG_SNAPSHOT_SEQUENCE.nextval INTO :NEW.SNAPSHOT_ID FROM dual;
                    END;
/
CREATE INDEX INDEX_SNAPSHOT_ROOT_ID ON REG_SNAPSHOT (ROOT_ID)
/
CREATE TABLE REG_SNAPSHOT_RESOURCE_VERSION (
                    SRV_ID INTEGER,
                    SNAPSHOT_ID INTEGER NOT NULL,
                    RID VARCHAR2 (50) NOT NULL,
                    VERSION INTEGER NOT NULL,
                    PRIMARY KEY (SRV_ID),
                    UNIQUE (SNAPSHOT_ID, RID, VERSION))
/
CREATE SEQUENCE REG_SNAP_RESOURCE_VER_SEQ START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER REG_SNAP_RESOURCE_VER_TRIGGER
                    BEFORE INSERT
                    ON REG_SNAPSHOT_RESOURCE_VERSION
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT REG_SNAP_RESOURCE_VER_SEQ.nextval INTO :NEW.SRV_ID FROM dual;
                    END;
/
CREATE TABLE UM_USERS (
                    ID INTEGER,
                    USER_NAME VARCHAR2(255) NOT NULL,
                    USER_PASSWORD VARCHAR2(255) NOT NULL,
                    PRIMARY KEY (ID),
                    UNIQUE(USER_NAME))
/
CREATE SEQUENCE UM_USERS_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER UM_USERS_TRIGGER
		            BEFORE INSERT
                    ON UM_USERS
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT UM_USERS_SEQUENCE.nextval INTO :NEW.ID FROM dual;
                    END;
/
CREATE TABLE UM_USER_ATTRIBUTES (
                    ID INTEGER,
                    ATTR_NAME VARCHAR2(255) NOT NULL,
                    ATTR_VALUE VARCHAR2(255),
                    USER_ID INTEGER,
                    FOREIGN KEY (USER_ID) REFERENCES UM_USERS(ID) ON DELETE CASCADE,
                    PRIMARY KEY (ID))
/
CREATE SEQUENCE UM_USER_ATTRIBUTES_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER UM_USER_ATTRIBUTES_TRIGGER
                    BEFORE INSERT
                    ON UM_USER_ATTRIBUTES
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT UM_USER_ATTRIBUTES_SEQUENCE.nextval INTO :NEW.ID FROM dual;
                    END;
/
CREATE TABLE UM_ROLES (
                    ID INTEGER,
                    ROLE_NAME VARCHAR2(255) NOT NULL,
                    PRIMARY KEY (ID),
                    UNIQUE(ROLE_NAME))
/
CREATE SEQUENCE UM_ROLES_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER UM_ROLES_TRIGGER
                    BEFORE INSERT
                    ON UM_ROLES
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT UM_ROLES_SEQUENCE.nextval INTO :NEW.ID FROM dual;
                    END;
/
CREATE TABLE UM_ROLE_ATTRIBUTES (
                    ID INTEGER,
                    ATTR_NAME VARCHAR2(255) NOT NULL,
                    ATTR_VALUE VARCHAR2(255),
                    ROLE_ID INTEGER,
                    FOREIGN KEY (ROLE_ID) REFERENCES UM_ROLES(ID) ON DELETE CASCADE,
                    PRIMARY KEY (ID))
/
CREATE SEQUENCE UM_ROLE_ATTRIBUTES_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER UM_ROLE_ATTRIBUTES_TRIGGER
		            BEFORE INSERT
                    ON UM_ROLE_ATTRIBUTES
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT UM_ROLE_ATTRIBUTES_SEQUENCE.nextval INTO :NEW.ID FROM dual;
                    END;
/
CREATE TABLE UM_PERMISSIONS (
                    ID INTEGER,
                    RESOURCE_ID VARCHAR2(255) NOT NULL,
                    ACTION VARCHAR2(255) NOT NULL,
                    PRIMARY KEY (ID))
/
CREATE SEQUENCE UM_PERMISSIONS_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER UM_PERMISSIONS_TRIGGER
                    BEFORE INSERT
                    ON UM_PERMISSIONS
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT UM_PERMISSIONS_SEQUENCE.nextval INTO :NEW.ID FROM dual;
                    END;
/
CREATE TABLE UM_ROLE_PERMISSIONS (
		            ID INTEGER,
                    PERMISSION_ID INTEGER NOT NULL,
                    ROLE_ID INTEGER NOT NULL,
                    IS_ALLOWED SMALLINT NOT NULL,
                    UNIQUE (PERMISSION_ID, ROLE_ID),
                    FOREIGN KEY (PERMISSION_ID) REFERENCES UM_PERMISSIONS(ID) ON DELETE  CASCADE,
                    FOREIGN KEY (ROLE_ID) REFERENCES UM_ROLES(ID) ON DELETE CASCADE,
                    PRIMARY KEY (ID))
/
CREATE SEQUENCE UM_ROLE_PERMISSIONS_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER UM_ROLE_PERMISSIONS_TRIGGER
		            BEFORE INSERT
                    ON UM_ROLE_PERMISSIONS
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT UM_ROLE_PERMISSIONS_SEQUENCE.nextval INTO :NEW.ID FROM dual;
                    END;
/
CREATE TABLE UM_USER_PERMISSIONS (
		            ID INTEGER,
		            PERMISSION_ID INTEGER NOT NULL,
                    USER_ID INTEGER NOT NULL,
                    IS_ALLOWED SMALLINT NOT NULL,
                    UNIQUE (PERMISSION_ID, USER_ID),
                    FOREIGN KEY (PERMISSION_ID) REFERENCES UM_PERMISSIONS(ID) ON DELETE CASCADE,
                    FOREIGN KEY (USER_ID) REFERENCES UM_USERS(ID) ON DELETE CASCADE,
                    PRIMARY KEY (ID))
/
CREATE SEQUENCE UM_USER_PERMISSIONS_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER UM_USER_PERMISSIONS_TRIGGER
		            BEFORE INSERT
		            ON UM_USER_PERMISSIONS
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT UM_USER_PERMISSIONS_SEQUENCE.nextval INTO :NEW.ID FROM dual;
                    END;
/
CREATE TABLE UM_USER_ROLES (
		            ID INTEGER,
                    ROLE_ID INTEGER NOT NULL,
                    USER_ID INTEGER NOT NULL,
                    UNIQUE (USER_ID, ROLE_ID),
                    FOREIGN KEY (ROLE_ID) REFERENCES UM_ROLES(ID) ON DELETE CASCADE,
                    FOREIGN KEY (USER_ID) REFERENCES UM_USERS(ID) ON DELETE CASCADE,
                    PRIMARY KEY (ID))
/
CREATE SEQUENCE UM_USER_ROLES_SEQUENCE START WITH 1 INCREMENT BY 1
/
CREATE OR REPLACE TRIGGER UM_USER_ROLES_TRIGGER
                    BEFORE INSERT
                    ON UM_USER_ROLES
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                    SELECT UM_USER_ROLES_SEQUENCE.nextval INTO :NEW.ID FROM dual;
                    END;
/
commit;