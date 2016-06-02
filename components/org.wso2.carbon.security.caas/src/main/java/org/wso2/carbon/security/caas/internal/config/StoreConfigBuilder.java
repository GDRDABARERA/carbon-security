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

package org.wso2.carbon.security.caas.internal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.security.caas.api.util.CarbonSecurityConstants;
import org.wso2.carbon.security.caas.user.core.config.AuthorizationConnectorConfig;
import org.wso2.carbon.security.caas.user.core.config.CredentialConnectorConfig;
import org.wso2.carbon.security.caas.user.core.config.IdentityConnectorConfig;
import org.wso2.carbon.security.caas.user.core.config.StoreConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration builder for stores.
 *
 * @since 1.0.0
 */
public class StoreConfigBuilder {

    private static final Logger log = LoggerFactory.getLogger(StoreConfigBuilder.class);

    public static StoreConfig buildStoreConfigs() {

        StoreConfig storeConfig = new StoreConfig();
        Map<String, StoreConnectorConfigEntry> externalConfigEntries = getExternalConfigEntries();

        Path file = Paths.get(CarbonSecurityConstants.getCarbonHomeDirectory().toString(), "conf", "security",
                              CarbonSecurityConstants.STORE_CONFIG_FILE);

        StoreConfigFile storeConfigFile;
        if (Files.exists(file)) {
            try (Reader in = new InputStreamReader(Files.newInputStream(file), StandardCharsets.ISO_8859_1)) {
                Yaml yaml = new Yaml();
                yaml.setBeanAccess(BeanAccess.FIELD);
                storeConfigFile = new Yaml().loadAs(in, StoreConfigFile.class);
            } catch (IOException e) {
                throw new RuntimeException("Error while loading " + CarbonSecurityConstants.STORE_CONFIG_FILE + " " +
                                           "configuration file", e);
            }
        } else {
            throw new RuntimeException("Configuration file " + CarbonSecurityConstants.STORE_CONFIG_FILE + "' is not " +
                                       "available.");
        }

        if (storeConfigFile == null || storeConfigFile.getCredentialStore() == null
            || storeConfigFile.getAuthorizationStore() == null || storeConfigFile.getIdentityStore() == null) {
            throw new IllegalArgumentException("Invalid or missing configurations in the file - " +
                                               CarbonSecurityConstants.STORE_CONFIG_FILE);
        }

        storeConfig.setEnableCache(storeConfigFile.isEnableCache());

        if (storeConfigFile.getCredentialStore().getConnector() != null && !storeConfigFile.getCredentialStore()
                .getConnector().trim().isEmpty()) {

            storeConfig.setEnableCacheForCredentialStore(storeConfigFile.getCredentialStore().isEnableCache());

            Map<String, StoreConnectorConfigEntry> credentialConnectorMap =
                    getStoreConnectorsMap(storeConfigFile.getCredentialStore().getConnector(),
                                          storeConfigFile.getCredentialStore(), externalConfigEntries,
                                          storeConfigFile.getStoreConnectors());

            if (credentialConnectorMap.size() > 0) {
                credentialConnectorMap.entrySet().forEach(
                        entry -> storeConfig.addCredentialStoreConfig(entry.getKey
                                (), new CredentialConnectorConfig(entry.getValue().getConnectorType(),
                                                              entry.getValue().getProperties()))
                );
            }
        } else {
            new RuntimePermission("Valid credentialStore configuration is not available in " +
                                  CarbonSecurityConstants.STORE_CONFIG_FILE);
        }

        if (storeConfigFile.getIdentityStore().getConnector() != null && !storeConfigFile.getIdentityStore()
                .getConnector().trim().isEmpty()) {

            storeConfig.setEnableCacheForIdentityStore(storeConfigFile.getIdentityStore().isEnableCache());

            Map<String, StoreConnectorConfigEntry> identityStoreConnectorMap =
                    getStoreConnectorsMap(storeConfigFile.getIdentityStore().getConnector(),
                                          storeConfigFile.getIdentityStore(), externalConfigEntries,
                                          storeConfigFile.getStoreConnectors());

            if (identityStoreConnectorMap.size() > 0) {
                identityStoreConnectorMap.entrySet().forEach(
                        entry -> storeConfig.addIdentityStoreConfig(entry.getKey
                                (), new IdentityConnectorConfig(entry.getValue().getConnectorType(),
                                                            entry.getValue().getProperties()))
                );
            }
        } else {
            new RuntimePermission("Valid identityStore configuration is not available in " +
                                  CarbonSecurityConstants.STORE_CONFIG_FILE);
        }

        if (storeConfigFile.getAuthorizationStore().getConnector() != null && !storeConfigFile.getAuthorizationStore()
                .getConnector().trim().isEmpty()) {

            storeConfig.setEnableCacheForAuthorizationStore(storeConfigFile.getAuthorizationStore().isEnableCache());

            Map<String, StoreConnectorConfigEntry> authorizationStoreConnectorMap =
                    getStoreConnectorsMap(storeConfigFile.getAuthorizationStore().getConnector(),
                                          storeConfigFile.getAuthorizationStore(), externalConfigEntries,
                                          storeConfigFile.getStoreConnectors());

            if (authorizationStoreConnectorMap.size() > 0) {
                authorizationStoreConnectorMap.entrySet().forEach(
                        entry -> storeConfig.addAuthorizationStoreConfig(entry.getKey
                                (), new AuthorizationConnectorConfig(entry.getValue().getConnectorType(),
                                                                 entry.getValue().getProperties()))
                );
            }
        } else {
            new RuntimePermission("Valid authorizationStore configuration is not available in " +
                                  CarbonSecurityConstants.STORE_CONFIG_FILE);
        }

        return storeConfig;
    }

    private static Map<String, StoreConnectorConfigEntry> getStoreConnectorsMap(
            String connector, StoreConfigEntry storeConfigEntry, Map<String,
            StoreConnectorConfigEntry> externalConfigEntries,
            List<StoreConnectorConfigEntry> storeConnectorConfigEntries) {

        Map<String, StoreConnectorConfigEntry> mergedConfigEntryMap = new HashMap<>();
        Arrays.asList(connector.split(",")).forEach(
                name -> {
                    if (name.startsWith("#")) {
                        String nameWithoutHash = name.substring(1);
                        Properties updatedProperties = new Properties();
                        StoreConnectorConfigEntry mergedConfigEntry = new StoreConnectorConfigEntry();
                        storeConnectorConfigEntries.stream()
                                .filter(configEntry -> nameWithoutHash.equals(configEntry.getName())
                                                  && configEntry.getProperties() != null
                                                  && !configEntry.getProperties().isEmpty())
                                .findFirst()
                                .ifPresent(config -> {
                                    mergedConfigEntry.setConnectorType(config.getConnectorType());
                                    config.getProperties().forEach(updatedProperties::put);
                                });

                        if (storeConfigEntry.getProperties() != null && !storeConfigEntry.getProperties().isEmpty()) {
                            storeConfigEntry.getProperties().forEach(updatedProperties::put);
                        }
                        mergedConfigEntry.setProperties(updatedProperties);
                        mergedConfigEntryMap.put(nameWithoutHash, mergedConfigEntry);
                    } else {
                        StoreConnectorConfigEntry configEntry = externalConfigEntries.get(name);
                        Properties updatedProperties = new Properties();
                        if (configEntry != null && configEntry.getProperties() != null && !configEntry.getProperties()
                                .isEmpty()) {
                            configEntry.getProperties().forEach(updatedProperties::put);
                        }
                        if (storeConfigEntry.getProperties() != null && !storeConfigEntry.getProperties().isEmpty()) {
                            storeConfigEntry.getProperties().forEach(updatedProperties::put);
                        }
                        StoreConnectorConfigEntry mergedConfigEntry = new StoreConnectorConfigEntry();
                        mergedConfigEntry.setConnectorType(configEntry != null ? configEntry.getConnectorType() : null);
                        mergedConfigEntry.setProperties(updatedProperties);
                        mergedConfigEntryMap.put(name, mergedConfigEntry);
                    }
                }
        );

        return mergedConfigEntryMap;
    }

    private static Map<String, StoreConnectorConfigEntry> getExternalConfigEntries() {

        Map<String, StoreConnectorConfigEntry> configEntryMap = new HashMap<>();
        Path path = Paths.get(CarbonSecurityConstants.getCarbonHomeDirectory().toString(), "conf", "security");

        if (Files.exists(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*-connector.yml")) {
                for (Path filePath : stream) {
                    StoreConnectorConfigEntry config = new Yaml().loadAs(Files.newInputStream(filePath),
                                                                         StoreConnectorConfigEntry.class);

                    String name = config != null && config.getName() != null && !config.getName().trim().isEmpty() ?
                                  config.getName().trim() : null;
                    if (name != null) {
                        configEntryMap.put(name, config);
                    } else {
                        log.warn("Connector name is not available in the connector config file: "
                                 + filePath.toString());
                    }
                }
            } catch (DirectoryIteratorException | IOException ex) {
                throw new RuntimeException("Failed to read connector files from path: " + path.toString(), ex);
            }
        }

        return configEntryMap;
    }
}
