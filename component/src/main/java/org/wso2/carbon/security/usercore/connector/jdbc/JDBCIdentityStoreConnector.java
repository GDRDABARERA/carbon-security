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

package org.wso2.carbon.security.usercore.connector.jdbc;

import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.security.usercore.bean.Group;
import org.wso2.carbon.security.usercore.bean.User;
import org.wso2.carbon.security.internal.config.IdentityStoreConfig;
import org.wso2.carbon.security.usercore.constant.ConnectorConstants;
import org.wso2.carbon.security.usercore.connector.IdentityStoreConnector;
import org.wso2.carbon.security.usercore.constant.DatabaseColumnNames;
import org.wso2.carbon.security.usercore.exception.IdentityStoreException;
import org.wso2.carbon.security.usercore.util.DatabaseUtil;
import org.wso2.carbon.security.usercore.util.NamedPreparedStatement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Identity store connector for JDBC based stores.
 */
public class JDBCIdentityStoreConnector implements IdentityStoreConnector {

    private DataSource dataSource;
    private IdentityStoreConfig identityStoreConfig;
    private Map<String, String> sqlStatements;
    private String userStoreId;
    private String userStoreName;

    @Override
    public void init(IdentityStoreConfig identityStoreConfig) throws IdentityStoreException {

        Properties properties = identityStoreConfig.getStoreProperties();

        this.sqlStatements = (Map<String, String>) properties.get(ConnectorConstants.SQL_QUERIES);
        this.userStoreId = properties.getProperty(ConnectorConstants.USERSTORE_ID);
        this.userStoreName = properties.getProperty(ConnectorConstants.USERSTORE_NAME);
        this.identityStoreConfig = identityStoreConfig;
        try {
            dataSource = DatabaseUtil.getInstance()
                    .getDataSource(properties.getProperty(ConnectorConstants.DATA_SOURCE));
        } catch (DataSourceException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getUserStoreName() {
        return userStoreName;
    }

    @Override
    public String getUserStoreID() {
        return userStoreId;
    }

    @Override
    public User getUserFromId(String userID) throws IdentityStoreException {

        try (Connection connection = dataSource.getConnection()) {

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(connection,
                    sqlStatements.get(ConnectorConstants.QueryTypes.SQL_QUERY_GET_USER_FROM_ID));
            namedPreparedStatement.setString("userId", userID);
            ResultSet resultSet = namedPreparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new IdentityStoreException("No user for given id");
            }

            String username = resultSet.getString(DatabaseColumnNames.User.USERNAME);
            return new User(userID, userStoreId, username);
        } catch (SQLException e) {
            throw new IdentityStoreException("Error occurred while retrieving user from database", e);
        }
    }

    @Override
    public User getUser(String username) throws IdentityStoreException {

        try (Connection connection = dataSource.getConnection()) {

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(connection,
                    sqlStatements.get(ConnectorConstants.QueryTypes.SQL_QUERY_GET_USER_FROM_USERNAME));
            namedPreparedStatement.setString("username", username);
            ResultSet resultSet = namedPreparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new IdentityStoreException("No user for given id");
            }

            String userId = resultSet.getString(DatabaseColumnNames.User.USER_ID);
            return new User(userId, userStoreId, username);
        } catch (SQLException e) {
            throw new IdentityStoreException("Error occurred while retrieving user from database", e);
        }
    }

    @Override
    public List<User> listUsers(String filterPattern, int offset, int length) throws IdentityStoreException {
        return null;
    }

    @Override
    public Map<String, String> getUserClaimValues(String userId) throws IdentityStoreException {

        try (Connection connection = dataSource.getConnection()) {

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(connection,
                    sqlStatements.get(ConnectorConstants.QueryTypes.SQL_QUERY_GET_USER_CLAIMS));
            namedPreparedStatement.setString("userId", userId);
            ResultSet resultSet = namedPreparedStatement.executeQuery();

            Map<String, String> userClaims = new HashMap<>();
            while (resultSet.next()) {
                String claimUri = resultSet.getString("claimUri");
                String claimValue = resultSet.getString("claimValue");
                userClaims.put(claimUri, claimValue);
            }
            return userClaims;
        } catch (SQLException e) {
            throw new IdentityStoreException("Error occurred while retrieving user claims from database", e);
        }

    }

    @Override
    public Map<String, String> getUserClaimValues(String userID, Set<String> claimURIs) throws IdentityStoreException {
        return null;
    }

    @Override
    public Group getGroupById(String groupId) throws IdentityStoreException {

        try (Connection connection = dataSource.getConnection()) {

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(connection,
                    sqlStatements.get(ConnectorConstants.QueryTypes.SQL_QUERY_GET_GROUP));
            namedPreparedStatement.setString(DatabaseColumnNames.Group.GROUP_UNIQUE_ID, groupId);
            ResultSet resultSet = namedPreparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new IdentityStoreException("No group for given id");
            }

            String groupName = resultSet.getString(DatabaseColumnNames.Group.GROUP_NAME);
            return new Group(groupId, userStoreId, groupName);
        } catch (SQLException e) {
            throw new IdentityStoreException("Internal error occurred while communicating with database", e);
        }
    }

    @Override
    public Group getGroup(String groupName) throws IdentityStoreException {

        try (Connection connection = dataSource.getConnection()) {

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(connection,
                    sqlStatements.get(ConnectorConstants.QueryTypes.SQL_QUERY_GET_GROUP));
            namedPreparedStatement.setString(DatabaseColumnNames.Group.GROUP_NAME, groupName);
            ResultSet resultSet = namedPreparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new IdentityStoreException("No group for given name");
            }

            String groupId = resultSet.getString(DatabaseColumnNames.Group.GROUP_UNIQUE_ID);
            return new Group(groupId, userStoreId, groupName);
        } catch (SQLException e) {
            throw new IdentityStoreException("Internal error occurred while communicating with database", e);
        }
    }

    @Override
    public List<Group> listGroups(String attribute, String filter, int maxItemLimit) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<Group> getGroupsOfUser(String userID) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<User> getUsersOfGroup(String groupID) throws IdentityStoreException {
        return null;
    }

    @Override
    public User addUser(Map<String, String> claims, Object credential, List<String> groupList)
            throws IdentityStoreException {
        return null;
    }

    @Override
    public Group addGroup(String groupName) throws IdentityStoreException {
        return null;
    }

    @Override
    public void assignGroupsToUser(String userId, List<Group> groups) throws IdentityStoreException {

    }

    @Override
    public void assingUsersToGroup(String groupId, List<User> identities) throws IdentityStoreException {

    }

    @Override
    public void updateCredential(String userID, Object newCredential) throws IdentityStoreException {

    }

    @Override
    public void updateCredential(String userID, Object oldCredential, Object newCredential)
            throws IdentityStoreException {

    }

    @Override
    public void setUserAttributeValues(String userID, Map<String, String> attributes) throws IdentityStoreException {

    }

    @Override
    public void deleteUserAttributeValues(String userID, List<String> attributes) throws IdentityStoreException {

    }

    @Override
    public void deleteUser(String userID) throws IdentityStoreException {

    }

    @Override
    public void deleteGroup(String groupId) throws IdentityStoreException {

    }

    @Override
    public boolean isReadOnly() throws IdentityStoreException {
        return false;
    }

    @Override
    public IdentityStoreConfig getIdentityStoreConfig() {
        return identityStoreConfig;
    }
}
