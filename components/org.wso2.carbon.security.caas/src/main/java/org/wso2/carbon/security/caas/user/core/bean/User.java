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

package org.wso2.carbon.security.caas.user.core.bean;

import org.wso2.carbon.security.caas.user.core.exception.AuthorizationStoreException;
import org.wso2.carbon.security.caas.user.core.exception.IdentityStoreException;
import org.wso2.carbon.security.caas.user.core.exception.StoreException;
import org.wso2.carbon.security.caas.user.core.store.AuthorizationStore;
import org.wso2.carbon.security.caas.user.core.store.IdentityStore;

import java.util.List;
import java.util.Map;

/**
 * Represents a user in the user core. All of the user related identity operations can be
 * done through this class.
 */
public class User {

    private String userID;
    private String identityStoreID;
    private String credentialStoreId;
    private String tenantDomain;
    private String userName;

    private IdentityStore identityStore;
    private AuthorizationStore authorizationStore;

    private User(String userName, String userID, String identityStoreID, String credentialStoreId,
                 String tenantDomain, IdentityStore identityStore,
                 AuthorizationStore authorizationStore) {

        this.userName = userName;
        this.userID = userID;
        this.identityStoreID = identityStoreID;
        this.credentialStoreId = credentialStoreId;
        this.tenantDomain = tenantDomain;
        this.identityStore = identityStore;
        this.authorizationStore = authorizationStore;
    }

    /**
     * Get the fully qualified name of this user.
     * @return Fully qualified name as a String.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Get user id.
     * @return User id.
     */
    public String getUserId() {
        return userID;
    }

    /**
     * Get the identity store id.
     * @return Identity store id.
     */
    public String getIdentityStoreId() {
        return identityStoreID;
    }

    /**
     * Get the credential store id.
     * @return Credential store id.
     */
    public String getCredentialStoreId() {
        return credentialStoreId;
    }

    /**
     * Get tenant domain.
     * @return Tenant domain.
     */
    public String getTenantDomain() {
        return tenantDomain;
    }

    /**
     * Get claims of this user.
     * @return Map of User claims.
     * @throws IdentityStoreException Identity store exception.
     */
    public Map<String, String> getClaims() throws IdentityStoreException {
        return identityStore.getUserAttributeValues(userID, identityStoreID);
    }

    /**
     * Get claims of this user for given URIs.
     * @param claimURIs Claim URIs that needs to be retrieved.
     * @return Map of User claims.
     * @throws IdentityStoreException Identity store exception.
     */
    public Map<String, String> getClaims(List<String> claimURIs) throws IdentityStoreException {
        return identityStore.getUserAttributeValues(userID, claimURIs, identityStoreID);
    }

    /**
     * Get the groups assigned to this user.
     * @return List of Groups assigned to this user.
     * @throws IdentityStoreException Identity store exception.
     */
    public List<Group> getGroups() throws IdentityStoreException {
        return identityStore.getGroupsOfUser(userID, identityStoreID);
    }

    /**
     * Get the roles assigned to this user.
     * @return List of Roles assigned to this user.
     * @throws AuthorizationStoreException Authorization store exception,
     */
    public List<Role> getRoles() throws AuthorizationStoreException {
        return authorizationStore.getRolesOfUser(userID, identityStoreID);
    }

    /**
     * Checks whether this user is authorized for given Permission.
     * @param permission Permission that should check on this user.
     * @return True if authorized.
     * @throws AuthorizationStoreException Authorization store exception.
     * @throws IdentityStoreException Identity store exception.
     */
    public boolean isAuthorized(Permission permission) throws AuthorizationStoreException, IdentityStoreException {
        return authorizationStore.isUserAuthorized(userID, permission, identityStoreID);
    }

    /**
     * Checks whether this User is in the given Role.
     * @param roleName Name of the Role.
     * @return True if this user is in the Role.
     * @throws AuthorizationStoreException Authorization store exception.
     */
    public boolean isInRole(String roleName) throws AuthorizationStoreException {
        return authorizationStore.isUserInRole(userID, identityStoreID, roleName);
    }

    /**
     * Checks whether this user is in the given Group.
     * @param groupName Name of the Group.
     * @return True if this User is in the group.
     * @throws IdentityStoreException Identity store exception.
     */
    public boolean isInGroup(String groupName) throws IdentityStoreException {
        return identityStore.isUserInGroup(userID, groupName, identityStoreID);
    }

    /**
     * Rename this user.
     * @param newUsername New user name.
     */
    public void rename(String newUsername) {
        throw new UnsupportedOperationException("This operation is not supported in platform level.");
    }

    /**
     * Set claims for this User.
     * @param claims List of claims to be set.
     * @throws IdentityStoreException Identity store exception.
     */
    public void setClaims(Map<String, String> claims) throws IdentityStoreException {
        throw new UnsupportedOperationException("This operation is not supported in platform level.");
    }

    /**
     * Add a new Group list by <b>replacing</b> the existing group list. (PUT)
     * @param newGroupList New group list names that needs to replace the existing list.
     * @throws IdentityStoreException Identity store exception.
     */
    public void updateGroups(List<String> newGroupList) throws IdentityStoreException {
        throw new UnsupportedOperationException("This operation is not supported in platform level.");
    }

    /**
     * Assign a new list of Groups to existing list and/or un-assign Groups from existing Groups. (PATCH)
     * @param assignList List to be added to the new list.
     * @param unAssignList List to be removed from the existing list.
     * @throws IdentityStoreException Identity store exception.
     */
    public void updateGroups(List<String> assignList, List<String> unAssignList) throws IdentityStoreException {
        throw new UnsupportedOperationException("This operation is not supported in platform level.");
    }

    /**
     * Add a new Role list by <b>replacing</b> the existing Role list. (PUT)
     * @param newRolesList List of Roles needs to be assigned to this User.
     * @throws AuthorizationStoreException Authorization store exception,
     * @throws IdentityStoreException Identity store exception.
     */
    public void updateRoles(List<Role> newRolesList) throws AuthorizationStoreException, IdentityStoreException {
        authorizationStore.updateRolesInUser(userID, identityStoreID, newRolesList);
    }

    /**
     * Assign a new list of Roles to existing list and/or un-assign Roles from existing list. (PATCH)
     * @param assignList List to be added to the new list.
     * @param unAssignList List to be removed from the existing list.
     */
    public void updateRoles(List<Role> assignList, List<Role> unAssignList) {
        authorizationStore.updateRolesInUser(userID, identityStoreID, assignList, unAssignList);
    }

    /**
     * Builder for the user bean.
     */
    public static class UserBuilder {

        private String userName;
        private String userId;
        private String identityStoreId;
        private String credentialStoreId;
        private String tenantDomain;

        private IdentityStore identityStore;
        private AuthorizationStore authorizationStore;

        public String getUserName() {
            return userName;
        }

        public String getUserId() {
            return userId;
        }

        public String getIdentityStoreId() {
            return identityStoreId;
        }

        public String getCredentialStoreId() {
            return credentialStoreId;
        }

        public String getTenantDomain() {
            return tenantDomain;
        }

        public IdentityStore getIdentityStore() {
            return identityStore;
        }

        public AuthorizationStore getAuthorizationStore() {
            return authorizationStore;
        }

        public UserBuilder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public UserBuilder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public UserBuilder setIdentityStoreId(String identityStoreId) {
            this.identityStoreId = identityStoreId;
            return this;
        }

        public UserBuilder setCredentialStoreId(String credentialStoreId) {
            this.credentialStoreId = credentialStoreId;
            return this;
        }

        public UserBuilder setTenantDomain(String tenantDomain) {
            this.tenantDomain = tenantDomain;
            return this;
        }

        public UserBuilder setIdentityStore(IdentityStore identityStore) {
            this.identityStore = identityStore;
            return this;
        }

        public UserBuilder setAuthorizationStore(AuthorizationStore authorizationStore) {
            this.authorizationStore = authorizationStore;
            return this;
        }

        public User build() {

            if (userName == null || userId == null || identityStoreId == null || credentialStoreId == null ||
                    identityStore == null || tenantDomain == null || authorizationStore == null) {
                throw new StoreException("Required data missing for building user.");
            }

            return new User(userName, userId, identityStoreId, credentialStoreId, tenantDomain, identityStore,
                    authorizationStore);
        }
    }
}
