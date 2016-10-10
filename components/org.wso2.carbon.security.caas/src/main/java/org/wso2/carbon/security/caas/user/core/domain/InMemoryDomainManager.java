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

package org.wso2.carbon.security.caas.user.core.domain;

import org.wso2.carbon.security.caas.api.util.CarbonSecurityConstants;
import org.wso2.carbon.security.caas.user.core.bean.Domain;
import org.wso2.carbon.security.caas.user.core.exception.DomainManagerException;
import org.wso2.carbon.security.caas.user.core.store.connector.IdentityStoreConnector;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain manager.
 */
public class InMemoryDomainManager implements DomainManager {

    public InMemoryDomainManager() throws DomainManagerException {

        this.createDefaultDomain();
    }

    /**
     * Domain name to domain mapping.
     */
    private Map<String, Domain> domainNameToDomain = new HashMap<>();

    @Override
    public Domain getDomainFromName(String domainName) throws DomainManagerException {

        Domain domain = domainNameToDomain.get(domainName);

        if (null != domain) {
            return domain;
        } else {
            throw new DomainManagerException("Domain " + domainName + " was not found");
        }
    }

    @Override
    public void addDomain(Domain domain) throws DomainManagerException {

        String domainName = domain.getDomainName();

        if (domainNameToDomain.containsKey(domainName)) {
            throw new DomainManagerException(String
                    .format("Domain %s already exists in the domain map", domainName));
        }

        domainNameToDomain.put(domainName, domain);
    }

    @Override
    public void createDefaultDomain() throws DomainManagerException {

//        return this.addDomain(CarbonSecurityConstants.DEFAULT_DOMAIN_NAME);
    }

    @Override
    public Domain getDefaultDomain() throws DomainManagerException {

        return getDomainFromName(CarbonSecurityConstants.DEFAULT_DOMAIN_NAME);
    }

    // TODO <VIDURA> Add implementation
    @Override
    public Domain getDomainFromUserName(String username) {
        return null;
    }

    @Override
    public IdentityStoreConnector getIdentityStoreConnector(
            String identityStoreConnectorId, String domainName) throws DomainManagerException {

        Domain domain = getDomainFromName(domainName);

        IdentityStoreConnector identityStoreConnector = domain.getIdentityStoreConnectorFromId
                (identityStoreConnectorId);

        if (null != identityStoreConnector) {
            return identityStoreConnector;
        } else {
            throw new DomainManagerException("IdentityStoreConnector " + identityStoreConnectorId +
                    " was not found ");
        }

    }

    @Override
    public Map<String, IdentityStoreConnector> getIdentityStoreConnectorMapForDomain(
            String domainName) throws DomainManagerException {

        return getDomainFromName(domainName)
                .getIdentityStoreConnectorMap();

    }

}
