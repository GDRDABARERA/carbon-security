/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.security.usercore.constant;

/**
 * Connector related constants.
 */
public class ConnectorConstants {

    public static final String DATA_SOURCE = "DataSource";
    public static final String DATABASE_TYPE = "DatabaseType";
    public static final String SQL_QUERIES = "SqlStatements";
    public static final String USERSTORE_ID = "UserstoreId";
    public static final java.lang.String USERSTORE_NAME = "UserstoreName";

    public static final class QueryTypes {

        public static final String SQL_QUERY_GET_GROUP = "sql_query_get_group";
        public static final String SQL_QUERY_COMPARE_PASSWORD_HASH = "sql_query_compare_password_hash";
        public static final String SQL_QUERY_GET_USER_FROM_ID = "sql_query_get_user_from_id";
        public static final String SQL_QUERY_GET_USER_FROM_USERNAME = "sql_query_get_user_from_username";
        public static final String SQL_QUERY_GET_USER_CLAIMS = "sql_query_get_user_claims";
    }
}
