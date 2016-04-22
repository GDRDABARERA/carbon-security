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

package org.wso2.carbon.security.internal.config;

import java.util.Collections;
import java.util.List;

/**
 * StoreConfigs Bean
 *
 * @since 1.0.0
 */
public class StoreConfigs {

    private StoreConfig credentialStore;

    private StoreConfig identityStore;

    private StoreConfig authorizationStore;

    private List<StoreConnectorConfig> storeConnectors;

    public StoreConfig getCredentialStore() {
        return credentialStore;
    }

    public void setCredentialStore(StoreConfig credentialStore) {
        this.credentialStore = credentialStore;
    }

    public StoreConfig getIdentityStore() {
        return identityStore;
    }

    public void setIdentityStore(StoreConfig identityStore) {
        this.identityStore = identityStore;
    }

    public StoreConfig getAuthorizationStore() {
        return authorizationStore;
    }

    public void setAuthorizationStore(StoreConfig authorizationStore) {
        this.authorizationStore = authorizationStore;
    }

    public List<StoreConnectorConfig> getStoreConnectors() {
        if (storeConnectors == null) {
            return Collections.EMPTY_LIST;
        }
        return storeConnectors;
    }

    public void setStoreConnectors(List<StoreConnectorConfig> storeConnectors) {
        this.storeConnectors = storeConnectors;
    }

}

