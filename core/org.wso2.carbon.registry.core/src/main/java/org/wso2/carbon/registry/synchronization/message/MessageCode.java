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
package org.wso2.carbon.registry.synchronization.message;

/**
 * An enumeration of various codes used by the messages involved in various operations of the API,
 * and some others designed for the use of 3rd parties using the API. This includes message codes
 * for informative messages, error messages, and messages requesting a confirmation from a user
 * before performing an operation.
 */
@SuppressWarnings("unused")
public enum MessageCode {

    ////////////////////////////////////////////////////////
    // Info messages for API operations
    ////////////////////////////////////////////////////////

    /**
     * The file or directory has been added.
     */
    ADDED,

    /**
     * The file or directory has been sent.
     */
    SENT,

    /**
     * The file or directory has been deleted.
     */
    DELETED,

    /**
     * The file or directory has not been deleted.
     */
    NOT_DELETED,

    /**
     * The file or directory is in a conflicted state.
     */
    CONFLICTED,

    /**
     * The file or directory has been updated.
     */
    UPDATED,

    /**
     * The file or directory has been overwritten.
     */
    OVERWRITTEN,

    /**
     * The file or directory has not been overwritten.
     */
    NON_OVERWRITTEN,

    ////////////////////////////////////////////////////////
    // Info messages for non-API (3rd party) operations
    ////////////////////////////////////////////////////////

    /**
     * Generic help on using a client.
     */
    HELP,

    /**
     * Operation successfully completed.
     */
    SUCCESS,

    /**
     * The check-in operation has been aborted.
     */
    CHECK_IN_OPERATION_ABORTED,

    /**
     * A successful transmission.
     */
    TRANSMIT_SUCCESS,

    /**
     * A successful addition.
     */
    ADDED_SUCCESS,

    /**
     * A successful update.
     */
    UPDATED_SUCCESS,

    /**
     * A failure due to conflict.
     */
    CONFLICTED_FAILURE,

    /**
     * A successful deletion.
     */
    DELETED_SUCCESS,

    /**
     * Total number of files overwritten.
     */
    OVERWRITTEN_FINAL,

    /**
     * Total number of files not overwritten.
     */
    NON_OVERWRITTEN_FINAL,

    /**
     * No files have been added.
     */
    NO_FILES_ADDED,

    /**
     * Total number of files that were not deleted.
     */
    NOT_DELETED_FINAL,

    /**
     * No files have been updated.
     */
    NO_FILES_UPDATED,

    /**
     * No files are in conflict.
     */
    NO_FILES_CONFLICTED,

    /**
     * No files have been deleted.
     */
    NO_FILES_DELETED,

    ////////////////////////////////////////////////////////
    // Generic Validation errors preventing API operations
    ////////////////////////////////////////////////////////

    /**
     * The path from which the check-out must be done is missing.
     */
    CO_PATH_MISSING,

    /**
     * Resolve conflicts before executing operation.
     */
    RESOLVE_CONFLICTS,

    /**
     * The dump request was made to a resource instead of a collection.
     */
    DUMPING_NON_COLLECTION,

    /**
     * The creation of the file failed.
     */
    FILE_CREATION_FAILED,

    /**
     * Unable to write content to the file.
     */
    PROBLEM_IN_CREATING_CONTENT,

    /**
     * Output to a file is not supported for the given operation.
     */
    OUTPUT_FILE_NOT_SUPPORTED,

    /**
     * A check-out must exist before trying to execute an update operation.
     */
    CHECKOUT_BEFORE_UPDATE,

    /**
     * A collection and a resource by the same name. This happens when we try to execute an update,
     * and when we attempt to replace a file with a collection or directory with a resource.
     */
    COLLECTION_AND_RESOURCE_SAME_NAME,

    /**
     * The file to be read is not found.
     */
    FILE_TO_READ_IS_NOT_FOUND,

    /**
     * The length of the file is too large.
     */
    FILE_LENGTH_IS_TOO_LARGE,

    /**
     * The dump format is not supported.
     */
    UNSUPPORTED_DUMP_FORMAT,

    /**
     * The given carbon version is not supported by the synchronization API.
     */
    CHECKOUT_OLD_VERSION,

    /**
     * Invalid dump to create a meta file.
     */
    INVALID_DUMP_CREATE_META_FILE,

    /**
     * A check-out must be done before check-in.
     */
    CHECKOUT_BEFORE_CHECK_IN,

    ////////////////////////////////////////////////////////
    // Errors while performing API operations
    ////////////////////////////////////////////////////////

    /**
     * An error occurred during restoration.
     */
    ERROR_IN_RESTORING,

    /**
     * An error occurred while dumping due to resource not found or user not having sufficient
     * permissions.
     */
    ERROR_IN_DUMPING_NO_RESOURCE_OR_NO_PERMISSION,

    /**
     * An error occurred while dumping due to the user not having sufficient permissions.
     */
    ERROR_IN_DUMPING_AUTHORIZATION_FAILED,

    /**
     * An error occurred while dumping the path.
     */
    ERROR_IN_DUMPING,

    /**
     * An error occurred while creating meta file.
     */
    ERROR_CREATING_META_FILE,

    /**
     * An error occurred while writing to the meta file.
     */
    ERROR_WRITING_TO_META_FILE,

    /**
     * An error occurred while reading the file.
     */
    ERROR_IN_READING,

    /**
     * An error occurred due to being unable to read whole file.
     */
    ERROR_IN_COMPLETELY_READING,

    /**
     * An error occurred while reading meta file.
     */
    ERROR_IN_READING_META_FILE,

    /**
     * An error occurred while reading meta file stream.
     */
    ERROR_IN_READING_META_FILE_STREAM,

    /**
     * An error occurred while closing meta file stream.
     */
    ERROR_IN_CLOSING_META_FILE_STREAM,

    /**
     * An error occurred while copying content.
     */
    ERROR_IN_COPYING,

    /**
     * An error occurred while encoding resource name.
     */
    ERROR_ENCODING_RESOURCE_NAME,

    /**
     * An error occurred while decoding path.
     */
    ERROR_DECODING_PATH,

    /**
     * An error occurred while deleting file or directory.
     */
    ERROR_IN_DELETING,

    /**
     * An error occurred while cleaning up temporary file or directory.
     */
    ERROR_IN_CLEANING_UP,    

    /**
     * An error occurred while creating temporary file for dump.
     */
    ERROR_IN_CREATING_TEMP_FILE_FOR_DUMP,

    /**
     * An error occurred while reading temporary file for dump.
     */
    ERROR_IN_READING_TEMP_FILE_OF_DUMP,

    /**
     * An error occurred while reading the stream of the temporary file for dump.
     */
    ERROR_IN_READING_STREAM_OF_TEMP_FILE_OF_DUMP,

    /**
     * An error occurred while reading stream that is used to create meta file.
     */
    ERROR_IN_READING_STREAM_TO_CREATE_META_FILE,

    /**
     * An error occurred while creating XML stream writer.
     */
    ERROR_IN_CREATING_XML_STREAM_WRITER,

    /**
     * An error occurred while dumping due to resource not existing on the registry.
     */
    ERROR_DUMP_PATH_RESOURCE_NOT_EXIST,

    /**
     * An error occurred while connecting to the registry.
     */
    ERROR_IN_CONNECTING_REGISTRY,

    /**
     *  An error occurred while adding the new RESOURCE/COLLECTION
     */
    ERROR_IN_ADDING_METADATA,

    ////////////////////////////////////////////////////////
    // User input validation errors for 3rd party clients
    ////////////////////////////////////////////////////////

    /**
     * No options provided.
     */
    NO_OPTIONS_PROVIDED,

    /**
     * Missing username value in argument.
     */
    USERNAME_MISSING,

    /**
     * Missing password value in argument.
     */
    PASSWORD_MISSING,

    /**
     * Missing registry type.
     */
    REGISTRY_TYPE_MISSING,

    /**
     * Missing working directory.
     */
    WORKING_DIR_MISSING,

    /**
     * Wrong working directory.
     */
    WRONG_WORKING_DIR,

    /**
     * Missing dump file.
     */
    DUMP_FILE_MISSING,

    /**
     * Incorrect operation.
     */
    OPERATION_NOT_FOUND,

    /**
     * The required field username has not been provided.
     */
    USERNAME_NOT_PROVIDED,

    /**
     * The restoration URL has not been provided.
     */
    RESTORE_URL_NOT_PROVIDED,

    /**
     * The file does not exist.
     */
    FILE_DOES_NOT_EXIST,

    /**
     * Meta information related to check-in not found.
     */
    CHECK_IN_META_INFO_NOT_FOUND,

    /**
     * A file already exists at the given location.
     */
    FILE_ALREADY_EXISTS,

    /**
     * Meta information related to update not found.
     */
    UPDATE_META_INFO_NOT_FOUND,

    /**
     * Unable to update from a location different to the initial check-out.
     */
    UPDATE_FROM_DIFFERENT_LOCATION,

    /**
     * Malformed URL.
     */
    MALFORMED_URL,

    /**
     * An error occurred while closing the stream.
     */
    ERROR_IN_CLOSING_STREAM,

    ////////////////////////////////////////////////////////
    // Errors in creating an instance of a Registry
    ////////////////////////////////////////////////////////

    /**
     * Error in initializing the realm service.
     */
    REALM_SERVICE_FAILED,

    /**
     * Error in obtaining the registry OSGi service.
     */
    REGISTRY_SERVICE_FAILED,

    /**
     * Error in obtaining an instance of a user registry.
     */
    USER_REGISTRY_FAILED,

    ////////////////////////////////////////////////////////
    // Messages requesting user input
    ////////////////////////////////////////////////////////

    /**
     * Confirmation to delete a directory.
     */
    DIRECTORY_DELETE_CONFIRMATION,

    /**
     * Confirmation to delete a file.
     */
    FILE_DELETE_CONFIRMATION,

    /**
     * Confirmation to overwrite a file.
     */
    FILE_OVERWRITE_CONFIRMATION,

    /**
     * Confirmation to check-in resources.
     */
    CHECK_IN_RESOURCES_CONFIRMATION,

    /**
     * Keep the local deleted file in an update 
     */
    KEEP_DELETED_FILE,

    ////////////////////////////////////////////////////////
    // Messages for resource add operation
    ////////////////////////////////////////////////////////

    /**
     * Adding resource/collection which is already in the registry
     */
    RESOURCE_ALREADY_UNDER_REGISTRY_CONTROL,

    /**
     * Current path of the collection of adding resource/collection which is not in the registry
     */
    CURRENT_COLLECTION_NOT_UNDER_REGISTRY_CONTROL,

    /**
     * Resource metadata corrupted
     */
    RESOURCE_METADATA_CORRUPTED,

    ////////////////////////////////////////////////////////
    // Messages for resource property ser operation
    ////////////////////////////////////////////////////////

    RESOURCE_NOT_UNDER_REGISTRY_CONTROL,


}
