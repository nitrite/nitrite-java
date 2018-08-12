/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.exceptions;

import lombok.Getter;

import static org.dizitart.no2.exceptions.ErrorCodes.*;

/**
 * Represents Nitrite error message.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public class ErrorMessage {
    static final String PREFIX = "NO2.";

    /**
     * Gets the error message.
     *
     * @return the error message.
     * */
    @Getter
    private String message;

    /**
     * Gets the error code.
     *
     * @return the error code.
     * */
    @Getter
    private String errorCode;

    private ErrorMessage(String message, int errorCode) {
        this.message = message;
        this.errorCode = PREFIX + errorCode;
    }

    /**
     * Checks if an error code corresponds to an {@link ErrorMessage}.
     *
     * @param code the error code
     * @return `true` if equal; `false` otherwise.
     */
    public boolean isEqual(int code) {
        return this.errorCode.equals(PREFIX + code);
    }

    /**
     * Creates a new {@link ErrorMessage}.
     *
     * @param message   the message
     * @param errorCode the error code
     * @return the new {@link ErrorMessage}
     */
    public static ErrorMessage errorMessage(String message, int errorCode) {
        return new ErrorMessage(message, errorCode);
    }

    public static final ErrorMessage NO_USER_MAP_FOUND =
            new ErrorMessage("no user map found in the database",
                    SE_NO_USER_MAP_FOUND);

    public static final ErrorMessage USER_MAP_SHOULD_NOT_EXISTS =
            new ErrorMessage("user map found unexpectedly",
                    SE_USER_MAP_SHOULD_NOT_EXISTS);

    public static final ErrorMessage NULL_USER_CREDENTIAL =
            new ErrorMessage("no username or password is stored in the database",
                    SE_NULL_USER_CREDENTIAL);

    public static final ErrorMessage INVALID_USER_PASSWORD =
            new ErrorMessage("username or password is invalid",
                    SE_INVALID_USER_PASSWORD);

    public static final ErrorMessage USER_ID_IS_EMPTY =
            new ErrorMessage("user id can not be empty",
                    SE_USER_ID_EMPTY);

    public static final ErrorMessage PASSWORD_IS_EMPTY =
            new ErrorMessage("password can not be empty",
                    SE_PASSWORD_EMPTY);

    public static final ErrorMessage DATABASE_OPENED_IN_OTHER_PROCESS =
            new ErrorMessage("database is already opened in other process",
                    NIOE_DATABASE_OPENED);

    public static final ErrorMessage UNABLE_TO_REPAIR_DB =
            new ErrorMessage("unable to repair database",
                    NIOE_REPAIR_FAILED);

    public static final ErrorMessage UNABLE_TO_CREATE_IN_MEMORY_DB =
            new ErrorMessage("unable to create in-memory database",
                    NIOE_IN_MEMORY_FAILED);

    public static final ErrorMessage UNABLE_TO_CREATE_DB_FILE =
            new ErrorMessage("unable to create database file",
                    NIOE_FILE_CREATE_FAILED);

    public static final ErrorMessage STORE_IS_CLOSED =
            new ErrorMessage("store is closed",
                    NIOE_STORE_CLOSED);

    public static final ErrorMessage COLLECTION_IS_DROPPED =
            new ErrorMessage("collection has been dropped",
                    NIOE_COLLECTION_DROPPED);

    public static final ErrorMessage IMPORT_READER_ERROR =
            new ErrorMessage("I/O error while creating parser from reader",
                    NIOE_IMPORT_READER_ERROR);

    public static final ErrorMessage IMPORT_READ_ERROR =
            new ErrorMessage("error while importing data",
                    NIOE_IMPORT_READ_ERROR);

    public static final ErrorMessage EXPORT_WRITER_ERROR =
            new ErrorMessage("I/O error while writing data with writer",
                    NIOE_EXPORT_WRITER_ERROR);

    public static final ErrorMessage EXPORT_WRITE_ERROR =
            new ErrorMessage("error while exporting data",
                    NIOE_EXPORT_WRITE_ERROR);

    public static final ErrorMessage INVALID_AND_FILTER =
            new ErrorMessage("invalid AND filter",
                    FE_AND_INVALID);

    public static final ErrorMessage VALUE_IS_NOT_COMPARABLE =
            new ErrorMessage("value is not comparable",
                    FE_VALUE_NOT_COMPARABLE);

    public static final ErrorMessage NESTED_ELEM_MATCH_NOT_SUPPORTED =
            new ErrorMessage("nested elemMatch filter is not supported",
                    FE_ELEM_MATCH_NESTED);

    public static final ErrorMessage FULL_TEXT_ELEM_MATCH_NOT_SUPPORTED =
            new ErrorMessage("full-text search is not supported in elemMatch filter",
                    FE_ELEM_MATCH_FULL_TEXT);

    public static final ErrorMessage ELEM_MATCH_SUPPORTED_ON_ARRAY_ONLY =
            new ErrorMessage("elemMatch filter only applies to array or iterable",
                    FE_ELEM_MATCH_NO_ARRAY);

    public static final ErrorMessage INVALID_OR_FILTER =
            new ErrorMessage("invalid OR filter",
                    FE_OR_INVALID);

    public static final ErrorMessage STAR_NOT_A_VALID_SEARCH_STRING =
            new ErrorMessage("* is not a valid search string",
                    FE_FTS_STAR_NOT_VALID);

    public static final ErrorMessage FILTERED_FIND_OPERATION_FAILED =
            new ErrorMessage("find operation failed",
                    FE_FILTERED_FIND_FAILED);

    public static final ErrorMessage FILTERED_FIND_WITH_OPTIONS_OPERATION_FAILED =
            new ErrorMessage("find operation failed",
                    FE_FILTERED_FIND_WITH_OPTION_FAILED);

    public static final ErrorMessage NESTED_OBJ_ELEM_MATCH_NOT_SUPPORTED =
            new ErrorMessage("nested elemMatch filter is not supported",
                    FE_OBJ_ELEM_MATCH_NESTED);

    public static final ErrorMessage FULL_TEXT_OBJ_ELEM_MATCH_NOT_SUPPORTED =
            new ErrorMessage("full-text search is not supported in elemMatch filter",
                    FE_OBJ_ELEM_MATCH_FULL_TEXT);

    public static final ErrorMessage OBJ_ELEM_MATCH_SUPPORTED_ON_ARRAY_ONLY =
            new ErrorMessage("elemMatch filter only applies to array or iterable",
                    FE_OBJ_ELEM_MATCH_NO_ARRAY);

    public static final ErrorMessage CAN_NOT_SEARCH_NON_COMPARABLE_ON_INDEXED_FIELD =
            new ErrorMessage("can not search non-comparable value on indexed field",
                    FE_INDEX_NON_COMPARABLE_SEARCH);

    public static final ErrorMessage INVALID_SEARCH_TERM_LEADING_STAR =
            new ErrorMessage("invalid search term '*'",
                    FE_SEARCH_TERM_INVALID_LEADING_STAR);

    public static final ErrorMessage INVALID_SEARCH_TERM_TRAILING_STAR =
            new ErrorMessage("invalid search term '*'",
                    FE_SEARCH_TERM_INVALID_TRAILING_STAR);

    public static final ErrorMessage MULTIPLE_WORDS_WITH_WILD_CARD =
            new ErrorMessage("multiple words with wildcard is not supported",
                    FE_MULTIPLE_WORDS_WITH_WILDCARD);

    public static final ErrorMessage NON_STRING_VALUE_IN_FULL_TEXT_INDEX =
            new ErrorMessage("value must be of string data type",
                    IE_FULL_TEXT_NON_STRING_VALUE);

    public static final ErrorMessage FAILED_TO_QUERY_FTS_DATA =
            new ErrorMessage("could not search on full-text index",
                    IE_FAILED_TO_QUERY_FTS_DATA);

    public static final ErrorMessage CAN_NOT_COMPARE_WITH_NULL_ID =
            new ErrorMessage("can not compare with null id",
                    IIE_COMPARISON_WITH_NULL_ID);

    public static final ErrorMessage ID_CAN_NOT_BE_NULL =
            new ErrorMessage("id can not be null",
                    IIE_NULL_ID);

    public static final ErrorMessage ID_FILTER_VALUE_CAN_NOT_BE_NULL =
            new ErrorMessage("id value can not be null",
                    IIE_NULL_ID_FILTER_VALUE);

    public static final ErrorMessage ID_FIELD_IS_NOT_ACCESSIBLE =
            new ErrorMessage("id field is not accessible",
                    IIE_ID_FIELD_NOT_ACCESSIBLE);

    public static final ErrorMessage FAILED_TO_CREATE_AUTO_ID =
            new ErrorMessage("failed to auto generate nitrite id",
                    IIE_FAILED_TO_CREATE_AUTO_ID);

    public static final ErrorMessage ID_VALUE_CAN_NOT_BE_EMPTY_STRING =
            new ErrorMessage("id value can not be empty string",
                    IIE_ID_VALUE_EMPTY_STRING);

    public static final ErrorMessage CANNOT_ACCESS_AUTO_ID =
            new ErrorMessage("auto generated id value can not be accessed",
                    IIE_CANNOT_ACCESS_AUTO_ID);

    public static final ErrorMessage AUTO_ID_ALREADY_SET =
            new ErrorMessage("auto generated id should not be set manually",
                    IIE_AUTO_ID_ALREADY_SET);

    public static final ErrorMessage PAGINATION_SIZE_CAN_NOT_BE_NEGATIVE =
            new ErrorMessage("pagination size can not be negative",
                    VE_NEGATIVE_PAGINATION_SIZE);

    public static final ErrorMessage PAGINATION_OFFSET_CAN_NOT_BE_NEGATIVE =
            new ErrorMessage("pagination offset can not be negative",
                    VE_NEGATIVE_PAGINATION_OFFSET);

    public static final ErrorMessage PAGINATION_OFFSET_GREATER_THAN_SIZE =
            new ErrorMessage("pagination offset is greater than total size",
                    VE_OFFSET_GREATER_THAN_SIZE);

    public static final ErrorMessage REPOSITORY_NOT_INITIALIZED =
            new ErrorMessage("repository has not been initialized properly",
                    VE_REPOSITORY_NOT_INITIALIZED);

    public static final ErrorMessage CAN_NOT_PROJECT_TO_PRIMITIVE =
            new ErrorMessage("can not project to primitive type",
                    VE_PROJECTION_PRIMITIVE_TYPE);

    public static final ErrorMessage CAN_NOT_PROJECT_TO_INTERFACE =
            new ErrorMessage("can not project to interface",
                    VE_PROJECTION_INTERFACE);

    public static final ErrorMessage CAN_NOT_PROJECT_TO_ARRAY =
            new ErrorMessage("can not project to array",
                    VE_PROJECTION_ARRAY);

    public static final ErrorMessage CAN_NOT_PROJECT_TO_ABSTRACT =
            new ErrorMessage("can not project to abstract type",
                    VE_PROJECTION_ABSTRACT_TYPE);

    public static final ErrorMessage CAN_NOT_PROJECT_TO_EMPTY_TYPE =
            new ErrorMessage("can not project to empty type",
                    VE_PROJECT_EMPTY_TYPE);

    public static final ErrorMessage SYNC_NO_REMOTE_COLLECTION =
            new ErrorMessage("remote collection is not set",
                    VE_SYNC_NO_REMOTE);

    public static final ErrorMessage OBJ_INVALID_EMBEDDED_FIELD =
            new ErrorMessage("invalid embedded field provided",
                    VE_OBJ_INVALID_EMBEDDED_FIELD);

    public static final ErrorMessage INVALID_EMBEDDED_FIELD =
            new ErrorMessage("invalid embedded field provided",
                    VE_INVALID_EMBEDDED_FIELD);

    public static final ErrorMessage DOC_GET_TYPE_NULL =
            new ErrorMessage("type can not be null",
                    VE_INVALID_EMBEDDED_FIELD);

    public static final ErrorMessage PROJECTION_WITH_NOT_NULL_VALUES =
            new ErrorMessage("projection contains not null values",
                    VE_PROJECTION_WITH_NOT_NULL_VALUE);

    public static final ErrorMessage UNABLE_TO_CREATE_IN_MEMORY_READONLY_DB =
            new ErrorMessage("unable create readonly in-memory database",
                    IOE_IN_MEMORY_READONLY_DB);

    public static final ErrorMessage FAILED_TO_CREATE_IN_MEMORY_READONLY_DB =
            new ErrorMessage("can not create readonly in-memory database",
                    NIOE_IN_MEMORY_READONLY_DB);

    public static final ErrorMessage UNABLE_TO_SORT_ON_ARRAY =
            new ErrorMessage("can not sort on array or collection objects",
                    IOE_SORT_ON_ARRAY_TYPE);

    public static final ErrorMessage REMOVE_ON_DOCUMENT_ITERATOR_NOT_SUPPORTED =
            new ErrorMessage("remove on cursor is not supported",
                    IOE_REMOVE_DOCUMENT_ITERATOR);

    public static final ErrorMessage OBJ_REMOVE_ON_OBJECT_ITERATOR_NOT_SUPPORTED =
            new ErrorMessage("remove on cursor is not supported",
                    IOE_OBJ_REMOVE_OBJECT_ITERATOR);

    public static final ErrorMessage OBJ_REMOVE_ON_PROJECTED_OBJECT_ITERATOR_NOT_SUPPORTED =
            new ErrorMessage("remove on cursor is not supported",
                    IOE_OBJ_REMOVE_PROJECTED_OBJECT_ITERATOR);

    public static final ErrorMessage OBJ_REMOVE_ON_JOINED_OBJECT_ITERATOR_NOT_SUPPORTED =
            new ErrorMessage("remove on cursor is not supported",
                    IOE_OBJ_REMOVE_JOINED_OBJECT_ITERATOR);

    public static final ErrorMessage REPLICATOR_ALREADY_RUNNING =
            new ErrorMessage("replicator is already running",
                    IOE_REPLICATOR_RUNNING);

    public static final ErrorMessage OBJ_MULTI_UPDATE_WITH_JUST_ONCE =
            new ErrorMessage("cannot update multiple items as justOnce is set to true",
                    IOE_OBJ_MULTI_UPDATE_WITH_JUST_ONCE);

    public static final ErrorMessage REMOVE_FAILED_AS_NO_ID_FOUND =
            new ErrorMessage("remove operation failed as no id value found for the document",
                    NIE_REMOVE_FAILED_FOR_NO_ID);

    public static final ErrorMessage UPDATE_FAILED_AS_NO_ID_FOUND =
            new ErrorMessage("update operation failed as no id value found for the document",
                    NIE_UPDATE_FAILED_FOR_NO_ID);

    public static final ErrorMessage OBJ_REMOVE_FAILED_AS_NO_ID_FOUND =
            new ErrorMessage("remove operation failed as no id value found for the object",
                    NIE_OBJ_REMOVE_FAILED_FOR_NO_ID);

    public static final ErrorMessage OBJ_UPDATE_FAILED_AS_NO_ID_FOUND =
            new ErrorMessage("update operation failed as no id value found for the object",
                    NIE_OBJ_UPDATE_FAILED_FOR_NO_ID);

    public static final ErrorMessage OBJ_MULTIPLE_ID_FOUND =
            new ErrorMessage("multiple id fields found for the type",
                    NIE_OBJ_MULTIPLE_ID);

    public static final ErrorMessage JSON_SERIALIZATION_FAILED =
            new ErrorMessage("failed to serialize object to json",
                    OME_SERIALIZE_TO_JSON_FAILED);

    public static final ErrorMessage SYNC_ACCOUNT_CREATE_REMOTE_ERROR =
            new ErrorMessage("remote error while creating new sync user",
                SYE_CREATE_ACCOUNT_REMOTE_ERROR);

    public static final ErrorMessage SYNC_ACCOUNT_UPDATE_REMOTE_ERROR =
        new ErrorMessage("remote error while updating sync user",
            SYE_UPDATE_ACCOUNT_REMOTE_ERROR);

    public static final ErrorMessage SYNC_GET_SIZE_REMOTE_ERROR =
        new ErrorMessage("remote error while getting size of collection",
            SYE_GET_SIZE_REMOTE_ERROR);

    public static final ErrorMessage SYNC_CLEAR_REMOTE_ERROR =
        new ErrorMessage("remote error while clearing the collection",
            SYE_CLEAR_REMOTE_ERROR);

    public static final ErrorMessage SYNC_FETCH_REMOTE_ERROR =
        new ErrorMessage("error while fetching documents from remote collection",
            SYE_FETCH_REMOTE_ERROR);

    public static final ErrorMessage SYNC_CHANGE_SINCE_REMOTE_ERROR =
            new ErrorMessage("error while fetching changes from remote collection",
                    SYE_CHANGE_SINCE_REMOTE_ERROR);

    public static final ErrorMessage SYNC_CHANGE_REMOTE_ERROR =
            new ErrorMessage("error while submitting changes to remote collection",
                    SYE_CHANGE_REMOTE_ERROR);

    public static final ErrorMessage SYNC_TRY_LOCK_REMOTE_ERROR =
            new ErrorMessage("error while acquiring lock to remote collection",
                    SYE_TRY_LOCK_REMOTE_ERROR);

    public static final ErrorMessage SYNC_RELEASE_LOCK_REMOTE_ERROR =
            new ErrorMessage("remote error while releasing lock from collection",
                    SYE_RELEASE_LOCK_REMOTE_ERROR);
}
