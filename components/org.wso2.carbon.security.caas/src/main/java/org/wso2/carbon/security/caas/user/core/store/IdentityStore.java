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

package org.wso2.carbon.security.caas.user.core.store;

import org.wso2.carbon.security.caas.user.core.bean.Group;
import org.wso2.carbon.security.caas.user.core.bean.User;
import org.wso2.carbon.security.caas.user.core.config.IdentityStoreConfig;
import org.wso2.carbon.security.caas.user.core.exception.GroupNotFoundException;
import org.wso2.carbon.security.caas.user.core.exception.IdentityStoreException;
import org.wso2.carbon.security.caas.user.core.exception.UserNotFoundException;
import org.wso2.carbon.security.caas.user.core.service.RealmService;

import java.util.List;
import java.util.Map;

/**
 * Represents a virtual identity store to abstract the underlying stores.
 * @since 1.0.0
 */
public interface IdentityStore {
    /**
     * Initialize the identity store instance.
     * @param realmService Parent realm service instance.
     * @param identityStoreConfigs Store configs related to the identity store.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void init(RealmService realmService, Map<String, IdentityStoreConfig> identityStoreConfigs)
            throws IdentityStoreException;

    /**
     * Get the user from username.
     * @param username Username of the user.
     * @return User.
     * @throws IdentityStoreException Identity Store Exception.
     * @throws UserNotFoundException User not found exception.
     */
    User getUser(String username) throws IdentityStoreException, UserNotFoundException;

    /**
     * Get the user from user Id.
     * @param userId Id of the user.
     * @param identityStoreId Identity store id of the user.
     * @return User.
     * @throws IdentityStoreException Identity Store Exception.
     */
    User getUserFromId(String userId, String identityStoreId) throws IdentityStoreException;

    /**
     * List all users in User Store according to the filter pattern.
     * @param filterPattern Filter patter to filter users.
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @return List of users match the filter pattern.
     * @throws IdentityStoreException Identity Store Exception.
     */
    List<User> listUsers(String filterPattern, int offset, int length) throws IdentityStoreException;

    /**
     * Get user attribute values.
     * @param userID Id of the user.
     * @param userStoreId Id of the user store which this user belongs.
     * @return Map of user attributes.
     * @throws IdentityStoreException Identity Store Exception.
     */
    Map<String, String> getUserAttributeValues(String userID, String userStoreId) throws IdentityStoreException;

    /**
     * Get user's claim values for the given URIs.
     * @param userID Id of the user.
     * @param attributeNames Attribute names.
     * @param userStoreId Id of the user store which this user belongs.
     * @return Map of user attributes.
     * @throws IdentityStoreException Identity Store Exception.
     */
    Map<String, String> getUserAttributeValues(String userID, List<String> attributeNames, String userStoreId)
            throws IdentityStoreException;

    /**
     * Get the group from name.
     * @param groupName Name of the group.
     * @return Group
     * @throws IdentityStoreException Identity Store Exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    Group getGroup(String groupName) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Get the group from group id.
     * @param groupId Group id.
     * @param identityStoreId Identity store id of the group.
     * @return Group.
     * @throws IdentityStoreException Identity Store Exception.
     */
    Group getGroupFromId(String groupId, String identityStoreId) throws IdentityStoreException;

    /**
     * List groups according to the filter pattern.
     * @param filterPattern Filter pattern for to list groups.
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @return List of groups that matches the filter pattern.
     * @throws IdentityStoreException Identity Store Exception.
     */
    List<Group> listGroups(String filterPattern, int offset, int length) throws IdentityStoreException;

    /**
     * Get the groups assigned to the specified user.
     * @param userId Id of the user.
     * @param userStoreId Id of the user store which this user belongs.
     * @return List of Group assigned to the user.
     * @throws IdentityStoreException Identity Store Exception.
     */
    List<Group> getGroupsOfUser(String userId, String userStoreId) throws IdentityStoreException;

    /**
     * Get the users assigned to the specified group.
     * @param groupID Id of the group.
     * @param userStoreId User store id of this group.
     * @return List of users assigned to the group.
     * @throws IdentityStoreException Identity Store Exception.
     */
    List<User> getUsersOfGroup(String groupID, String userStoreId) throws IdentityStoreException;

    /**
     * Checks whether the user is in the group.
     * @param userId Id of the user.
     * @param groupId Id of the group.
     * @param userStoreId Id of the user store which this user belongs.
     * @return True if the user is in the group.
     * @throws IdentityStoreException Identity Store Exception.
     */
    boolean isUserInGroup(String userId, String groupId, String userStoreId) throws IdentityStoreException;
}
