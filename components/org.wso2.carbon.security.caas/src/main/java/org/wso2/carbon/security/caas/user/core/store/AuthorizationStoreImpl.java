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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.utils.LambdaExceptionUtils;
import org.wso2.carbon.security.caas.internal.CarbonSecurityDataHolder;
import org.wso2.carbon.security.caas.user.core.bean.Action;
import org.wso2.carbon.security.caas.user.core.bean.Group;
import org.wso2.carbon.security.caas.user.core.bean.Permission;
import org.wso2.carbon.security.caas.user.core.bean.Resource;
import org.wso2.carbon.security.caas.user.core.bean.Role;
import org.wso2.carbon.security.caas.user.core.bean.User;
import org.wso2.carbon.security.caas.user.core.config.AuthorizationConnectorConfig;
import org.wso2.carbon.security.caas.user.core.constant.UserCoreConstants;
import org.wso2.carbon.security.caas.user.core.exception.AuthorizationStoreException;
import org.wso2.carbon.security.caas.user.core.exception.IdentityStoreException;
import org.wso2.carbon.security.caas.user.core.exception.PermissionNotFoundException;
import org.wso2.carbon.security.caas.user.core.exception.RoleNotFoundException;
import org.wso2.carbon.security.caas.user.core.exception.StoreException;
import org.wso2.carbon.security.caas.user.core.service.RealmService;
import org.wso2.carbon.security.caas.user.core.store.connector.AuthorizationStoreConnector;
import org.wso2.carbon.security.caas.user.core.store.connector.AuthorizationStoreConnectorFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a virtual authorization store to abstract the underlying stores.
 * @since 1.0.0
 */
public class AuthorizationStoreImpl implements AuthorizationStore {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationStoreImpl.class);

    private RealmService realmService;
    private Map<String, AuthorizationConnectorConfig> authorizationConnectorConfigs;
    private Map<String, AuthorizationStoreConnector> authorizationStoreConnectors = new HashMap<>();

    @Override
    public void init(RealmService realmService, Map<String, AuthorizationConnectorConfig> authorizationConnectorConfigs)
            throws AuthorizationStoreException {

        this.realmService = realmService;
        this.authorizationConnectorConfigs = authorizationConnectorConfigs;

        if (authorizationConnectorConfigs.isEmpty()) {
            throw new StoreException("At least one authorization store configuration must present.");
        }

        for (Map.Entry<String, AuthorizationConnectorConfig> authorizationStoreConfig :
                authorizationConnectorConfigs.entrySet()) {

            String connectorType = authorizationStoreConfig.getValue().getConnectorType();
            AuthorizationStoreConnectorFactory authorizationStoreConnectorFactory = CarbonSecurityDataHolder
                    .getInstance().getAuthorizationStoreConnectorFactoryMap().get(connectorType);

            if (authorizationStoreConnectorFactory == null) {
                throw new StoreException("No credential store connector factory found for given type.");
            }

            AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectorFactory.getInstance();
            authorizationStoreConnector.init(authorizationStoreConfig.getKey(), authorizationStoreConfig.getValue());

            authorizationStoreConnectors.put(authorizationStoreConfig.getKey(), authorizationStoreConnector);
        }

        if (log.isDebugEnabled()) {
            log.debug("Authorization store successfully initialized.");
        }
    }

    @Override
    public boolean isUserAuthorized(String userId, Permission permission, String identityStoreId)
            throws AuthorizationStoreException, IdentityStoreException {

        // If this user owns this resource, we assume this user has all permissions.
        if (permission.getResource().getOwner().getUserId().equals(userId)) {
            return true;
        }

        // Get the roles directly associated to the user.
        List<Role> roles = new ArrayList<>();
        for (AuthorizationStoreConnector authorizationStoreConnector : authorizationStoreConnectors.values()) {
            roles.addAll(authorizationStoreConnector.getRolesForUser(userId, identityStoreId)
                    .stream()
                    .map(roleBuilder -> roleBuilder
                            .setAuthorizationStore(realmService.getAuthorizationStore())
                            .build())
                    .collect(Collectors.toList()));
        }

        // Get the roles associated through groups.
        List<Group> groups = realmService.getIdentityStore().getGroupsOfUser(userId, identityStoreId);
        for (Group group : groups) {
            roles.addAll(getRolesOfGroup(group.getGroupId(), identityStoreId));
        }

        if (roles.isEmpty()) {
            throw new StoreException("No roles assigned for this user");
        }

        for (Role role : roles) {
            if (isRoleAuthorized(role.getRoleId(), role.getAuthorizationStoreId(), permission)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isGroupAuthorized(String groupId, String identityStoreId, Permission permission)
            throws AuthorizationStoreException {

        List<Role> roles = getRolesOfGroup(groupId, identityStoreId);

        for (Role role : roles) {
            if (isRoleAuthorized(role.getRoleId(), role.getAuthorizationStoreId(), permission)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isRoleAuthorized(String roleId, String authorizationStoreId, Permission permission)
            throws AuthorizationStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                .get(authorizationStoreId);

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    authorizationStoreId));
        }

        List<Permission.PermissionBuilder> permissionBuilders = authorizationStoreConnector
                .getPermissionsForRole(roleId, permission.getResource());

        if (permissionBuilders.isEmpty()) {
            throw new StoreException("No permissions assigned for this role.");
        }

        for (Permission.PermissionBuilder permissionBuilder : permissionBuilders) {
            if (permissionBuilder.build().equals(permission)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isUserInRole(String userId, String identityStoreId, String roleName)
            throws AuthorizationStoreException {

        for (AuthorizationStoreConnector authorizationStoreConnector : authorizationStoreConnectors.values()) {
            if (authorizationStoreConnector.isUserInRole(userId, identityStoreId, roleName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isGroupInRole(String groupId, String identityStoreId, String roleName)
            throws AuthorizationStoreException {

        for (AuthorizationStoreConnector authorizationStoreConnector : authorizationStoreConnectors.values()) {
            if (authorizationStoreConnector.isGroupInRole(groupId, identityStoreId, roleName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Role getRole(String roleName) throws RoleNotFoundException, AuthorizationStoreException {

        RoleNotFoundException roleNotFoundException = new RoleNotFoundException("Role not found for the given name.");

        for (AuthorizationStoreConnector authorizationStoreConnector : authorizationStoreConnectors.values()) {
            try {
                return authorizationStoreConnector.getRole(roleName)
                        .setAuthorizationStore(realmService.getAuthorizationStore())
                        .build();
            } catch (RoleNotFoundException e) {
                roleNotFoundException.addSuppressed(e);
            }
        }
        throw roleNotFoundException;
    }

    @Override
    public Permission getPermission(String resource, String action) throws PermissionNotFoundException,
            AuthorizationStoreException {

        PermissionNotFoundException permissionNotFoundException =
                new PermissionNotFoundException("Permission not found for the given resource id and the action.");

        for (AuthorizationStoreConnector authorizationStoreConnector : authorizationStoreConnectors.values()) {
            try {
                return authorizationStoreConnector.getPermission(new Resource(resource), new Action(action)).build();
            } catch (PermissionNotFoundException e) {
                permissionNotFoundException.addSuppressed(e);
            }
        }
        throw permissionNotFoundException;
    }

    @Override
    public List<Role> getRolesOfUser(String userId, String identityStoreId) throws AuthorizationStoreException {

        List<Role> roles = new ArrayList<>();
        for (AuthorizationStoreConnector authorizationStoreConnector : authorizationStoreConnectors.values()) {
            roles.addAll(authorizationStoreConnector.getRolesForUser(userId, identityStoreId)
                    .stream()
                    .map(roleBuilder -> roleBuilder.setAuthorizationStore(realmService.getAuthorizationStore()).build())
                    .collect(Collectors.toList()));
        }

        return roles;
    }

    @Override
    public List<User> getUsersOfRole(String roleId, String authorizationStoreId) throws AuthorizationStoreException,
            IdentityStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                .get(authorizationStoreId);

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    authorizationStoreId));
        }

        return authorizationStoreConnector.getUsersOfRole(roleId)
                .stream()
                .map(LambdaExceptionUtils.rethrowFunction(userBuilder -> realmService.getIdentityStore()
                        .getUserFromId(userBuilder.getUserId(), userBuilder.getIdentityStoreId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsOfRole(String roleId, String authorizationStoreId) throws AuthorizationStoreException,
            IdentityStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                .get(authorizationStoreId);

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    authorizationStoreId));
        }

        return authorizationStoreConnector.getGroupsOfRole(roleId)
                .stream()
                .map(LambdaExceptionUtils.rethrowFunction(groupBuilder -> realmService.getIdentityStore()
                        .getGroupFromId(groupBuilder.getGroupId(), groupBuilder.getIdentityStoreId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Role> getRolesOfGroup(String groupId, String identityStoreId) throws AuthorizationStoreException {

        List<Role> roles = new ArrayList<>();

        for (AuthorizationStoreConnector authorizationStoreConnector : authorizationStoreConnectors.values()) {
            roles.addAll(authorizationStoreConnector.getRolesForGroup(groupId, identityStoreId)
                    .stream()
                    .map(roleBuilder -> roleBuilder
                            .setAuthorizationStore(realmService.getAuthorizationStore())
                            .build())
                    .collect(Collectors.toList()));
        }

        return roles;
    }

    @Override
    public List<Permission> getPermissionsOfRole(String roleId, String authorizationStoreId, Resource resource)
            throws AuthorizationStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                .get(authorizationStoreId);

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    authorizationStoreId));
        }

        return authorizationStoreConnector.getPermissionsForRole(roleId, resource)
                .stream()
                .map(Permission.PermissionBuilder::build)
                .collect(Collectors.toList());
    }

    @Override
    public List<Permission> getPermissionsOfRole(String roleId, String authorizationStoreId, Action action)
            throws AuthorizationStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                .get(authorizationStoreId);

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    authorizationStoreId));
        }

        return authorizationStoreConnector.getPermissionsForRole(roleId, action)
                .stream()
                .map(Permission.PermissionBuilder::build)
                .collect(Collectors.toList());
    }

    @Override
    public List<Permission> getPermissionsOfRole(String roleId, String authorizationStoreId)
            throws AuthorizationStoreException {

        return getPermissionsOfRole(roleId, authorizationStoreId, Resource.getUniversalResource());
    }

    @Override
    public List<Permission> getPermissionsOfUser(String userId, String identityStoreId, Resource resource)
            throws AuthorizationStoreException {

        return getRolesOfUser(userId, identityStoreId)
                .stream()
                .map(LambdaExceptionUtils.rethrowFunction(role ->
                        getPermissionsOfRole(role.getRoleId(), role.getAuthorizationStoreId(), resource)))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<Permission> getPermissionsOfUser(String userId, String identityStoreId, Action action)
            throws AuthorizationStoreException {

        return getRolesOfUser(userId, identityStoreId)
                .stream()
                .map(LambdaExceptionUtils.rethrowFunction(role ->
                        getPermissionsOfRole(role.getRoleId(), role.getAuthorizationStoreId(), action)))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Role addRole(String roleName, List<Permission> permissions) throws AuthorizationStoreException {

        String authorizationStoreId = getPrimaryAuthorizationStoreId();
        return addRole(roleName, permissions, authorizationStoreId);
    }

    @Override
    public Role addRole(String roleName, List<Permission> permissions, String authorizationStoreId)
            throws AuthorizationStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                .get(authorizationStoreId);

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    authorizationStoreId));
        }

        Role.RoleBuilder roleBuilder = authorizationStoreConnector.addRole(roleName, permissions);

        if (roleBuilder == null) {
            throw new AuthorizationStoreException("Role builder is null.");
        }

        return roleBuilder.setAuthorizationStore(realmService.getAuthorizationStore()).build();
    }

    @Override
    public void deleteRole(Role role) throws AuthorizationStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors.get(role
                .getAuthorizationStoreId());

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    role.getAuthorizationStoreId()));
        }

        authorizationStoreConnector.deleteRole(role.getRoleId());
    }

    @Override
    public Resource addResource(String resourceNamespace, String resourceId, String userId, String identityStoreId)
            throws AuthorizationStoreException {

        String authorizationStoreId = getPrimaryAuthorizationStoreId();
        return addResource(resourceNamespace, resourceId, authorizationStoreId, userId, identityStoreId);
    }

    @Override
    public Resource addResource(String resourceNamespace, String resourceId, String authorizationStoreId, String userId,
                                String identityStoreId) throws AuthorizationStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                .get(authorizationStoreId);

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    authorizationStoreId));
        }

        return authorizationStoreConnector.addResource(resourceNamespace, resourceId, userId, identityStoreId);
    }

    @Override
    public Action addAction(String actionNamespace, String actionName) throws AuthorizationStoreException {

        String authorizationStoreId = getPrimaryAuthorizationStoreId();
        return addAction(actionNamespace, actionName, authorizationStoreId);
    }

    @Override
    public Action addAction(String actionNamespace, String actionName, String authorizationStoreId)
            throws AuthorizationStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                .get(authorizationStoreId);

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    authorizationStoreId));
        }

        return authorizationStoreConnector.addAction(actionNamespace, actionName);
    }

    @Override
    public Permission addPermission(Resource resource, Action action) throws AuthorizationStoreException {

        String authorizationStoreId = getPrimaryAuthorizationStoreId();
        return addPermission(resource, action, authorizationStoreId);
    }

    @Override
    public Permission addPermission(Resource resource, Action action, String authorizationStoreId)
            throws AuthorizationStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                .get(authorizationStoreId);

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    authorizationStoreId));
        }

        return authorizationStoreConnector.addPermission(resource, action).build();
    }

    @Override
    public void deletePermission(Permission permission) throws AuthorizationStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                .get(permission.getAuthorizationStoreId());

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    permission.getAuthorizationStoreId()));
        }

        authorizationStoreConnector.deletePermission(permission.getPermissionId());
    }

    @Override
    public void updateRolesInUser(String userId, String identityStoreId, List<Role> newRoleList)
            throws AuthorizationStoreException {

        if (newRoleList == null || newRoleList.isEmpty()) {
            for (AuthorizationStoreConnector authorizationStoreConnector : authorizationStoreConnectors.values()) {
                authorizationStoreConnector.updateRolesInUser(userId, identityStoreId, newRoleList);
            }
            return;
        }

        Map<String, List<Role>> roleMap = this.getRolesWithAuthorizationStore(newRoleList);

        for (Map.Entry<String, List<Role>> roleEntry : roleMap.entrySet()) {
            AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                    .get(roleEntry.getKey());
            if (authorizationStoreConnector == null) {
                throw new StoreException(String.format("No authorization store found for the given id: %s.",
                        roleEntry.getKey()));
            }
            authorizationStoreConnector.updateRolesInUser(userId, identityStoreId, roleEntry.getValue());
        }
    }

    @Override
    public void updateRolesInUser(String userId, String identityStoreId, List<Role> rolesToBeAssign,
                                  List<Role> rolesToBeUnassign) throws AuthorizationStoreException {

        Map<String, List<Role>> rolesToBeAssignWithStoreId = this.getRolesWithAuthorizationStore(rolesToBeAssign);
        Map<String, List<Role>> rolesToBeUnAssignWithStoreId = this.getRolesWithAuthorizationStore(rolesToBeUnassign);

        Set<String> keys = new HashSet<>();
        keys.addAll(rolesToBeAssignWithStoreId.keySet());
        keys.addAll(rolesToBeUnAssignWithStoreId.keySet());

        for (String key : keys) {

            AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors.get(key);

            if (authorizationStoreConnector == null) {
                throw new StoreException(String.format("No authorization store found for the given id: %s.", key));
            }

            authorizationStoreConnector.updateRolesInUser(userId, identityStoreId, rolesToBeAssignWithStoreId.get(key),
                    rolesToBeUnAssignWithStoreId.get(key));
        }
    }

    @Override
    public void updateUsersInRole(String roleId, String authorizationStoreId, List<User> newUserList)
            throws AuthorizationStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                .get(authorizationStoreId);

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    authorizationStoreId));
        }

        authorizationStoreConnector.updateUsersInRole(roleId, newUserList);
    }

    @Override
    public void updateUsersInRole(String roleId, String authorizationStoreId, List<User> usersToBeAssign,
                                  List<User> usersToBeUnassign) throws AuthorizationStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                .get(authorizationStoreId);

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    authorizationStoreId));
        }

        authorizationStoreConnector.updateUsersInRole(roleId, usersToBeAssign, usersToBeUnassign);
    }

    @Override
    public void updateRolesInGroup(String groupId, String identityStoreId, List<Role> newRoleList)
            throws AuthorizationStoreException {

        if (newRoleList == null || newRoleList.isEmpty()) {
            for (AuthorizationStoreConnector authorizationStoreConnector : authorizationStoreConnectors.values()) {
                authorizationStoreConnector.updateRolesInGroup(groupId, identityStoreId, newRoleList);
            }
            return;
        }

        Map<String, List<Role>> roleMap = this.getRolesWithAuthorizationStore(newRoleList);

        for (Map.Entry<String, List<Role>> roleEntry : roleMap.entrySet()) {
            AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                    .get(roleEntry.getKey());
            if (authorizationStoreConnector == null) {
                throw new StoreException(String.format("No authorization store found for the given id: %s.",
                        roleEntry.getKey()));
            }
            authorizationStoreConnector.updateRolesInGroup(groupId, identityStoreId, roleEntry.getValue());
        }
    }

    @Override
    public void updateRolesInGroup(String groupId, String identityStoreId, List<Role> rolesToBeAssign,
                                   List<Role> rolesToBeUnassigned) throws AuthorizationStoreException {

        Map<String, List<Role>> rolesToBeAssignWithStoreId = this.getRolesWithAuthorizationStore(rolesToBeAssign);
        Map<String, List<Role>> rolesToBeUnAssignWithStoreId = this.getRolesWithAuthorizationStore(rolesToBeUnassigned);

        Set<String> keys = new HashSet<>();
        keys.addAll(rolesToBeAssignWithStoreId.keySet());
        keys.addAll(rolesToBeUnAssignWithStoreId.keySet());

        for (String key : keys) {

            AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors.get(key);

            if (authorizationStoreConnector == null) {
                throw new StoreException(String.format("No authorization store found for the given id: %s.", key));
            }

            authorizationStoreConnector.updateRolesInGroup(groupId, identityStoreId,
                    rolesToBeAssignWithStoreId.get(key), rolesToBeUnAssignWithStoreId.get(key));
        }
    }

    @Override
    public void updateGroupsInRole(String roleId, String authorizationStoreId, List<Group> newGroupList)
            throws AuthorizationStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                .get(authorizationStoreId);

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    authorizationStoreId));
        }

        authorizationStoreConnector.updateGroupsInRole(roleId, newGroupList);
    }

    @Override
    public void updateGroupsInRole(String roleId, String authorizationStoreId, List<Group> groupToBeAssign,
                                   List<Group> groupToBeUnassign) throws AuthorizationStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                .get(authorizationStoreId);

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    authorizationStoreId));
        }

        authorizationStoreConnector.updateGroupsInRole(roleId, groupToBeAssign, groupToBeUnassign);
    }

    @Override
    public void updatePermissionsInRole(String roleId, String authorizationStoreId, List<Permission> newPermissionList)
            throws AuthorizationStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                .get(authorizationStoreId);

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    authorizationStoreId));
        }

        authorizationStoreConnector.updatePermissionsInRole(roleId, newPermissionList);
    }

    @Override
    public void updatePermissionsInRole(String roleId, String authorizationStoreId,
                                        List<Permission> permissionsToBeAssign,
                                        List<Permission> permissionsToBeUnassign) throws AuthorizationStoreException {

        AuthorizationStoreConnector authorizationStoreConnector = authorizationStoreConnectors
                .get(authorizationStoreId);

        if (authorizationStoreConnector == null) {
            throw new StoreException(String.format("No authorization store found for the given id: %s.",
                    authorizationStoreId));
        }

        authorizationStoreConnector.updatePermissionsInRole(roleId, permissionsToBeAssign, permissionsToBeUnassign);
    }

    /**
     * Get the roles with there respective authorization store id.
     * @param roles List of roles.
     * @return Roles grouped from there authorization store id.
     */
    private Map<String, List<Role>> getRolesWithAuthorizationStore(List<Role> roles) {

        Map<String, List<Role>> roleMap = new HashMap<>();

        if (roles == null) {
            return roleMap;
        }

        for (Role role : roles) {
            List<Role> roleList = roleMap.get(role.getAuthorizationStoreId());
            if (roleList == null) {
                roleList = new ArrayList<>();
            }
            roleList.add(role);
            roleMap.put(role.getAuthorizationStoreId(), roleList);
        }

        return roleMap;
    }

    /**
     * Get the primary authorization store id.
     * @return Id of the primary authorization store.
     */
    private String getPrimaryAuthorizationStoreId() {

        // To get the primary authorization store, first check whether the primary property is set to true, if not sort
        // the connectors by there priority. If non of above properties were set, then get the first connector id from
        // the list.
        return authorizationConnectorConfigs.entrySet()
                .stream()
                .filter(config -> Boolean.parseBoolean(config.getValue().getStoreProperties()
                        .getProperty(UserCoreConstants.PRIMARY_USERSTORE)))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(authorizationConnectorConfigs.entrySet()
                        .stream()
                        .sorted((c1, c2) ->
                                Integer.compare(Integer.parseInt(c1.getValue().getStoreProperties()
                                        .getProperty(UserCoreConstants.USERSTORE_PRIORITY)),
                                        Integer.parseInt(c2.getValue().getStoreProperties()
                                                .getProperty(UserCoreConstants.USERSTORE_PRIORITY))))
                        .findFirst()
                        .map(Map.Entry::getKey)
                        .orElse(authorizationConnectorConfigs.entrySet()
                                .stream()
                                .findFirst()
                                .map(Map.Entry::getKey)
                                .orElse(null)));
    }
}
