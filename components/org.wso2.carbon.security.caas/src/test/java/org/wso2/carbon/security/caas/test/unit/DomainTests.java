///*
//*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//*
//*  WSO2 Inc. licenses this file to you under the Apache License,
//*  Version 2.0 (the "License"); you may not use this file except
//*  in compliance with the License.
//*  You may obtain a copy of the License at
//*
//*    http://www.apache.org/licenses/LICENSE-2.0
//*
//* Unless required by applicable law or agreed to in writing,
//* software distributed under the License is distributed on an
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//* KIND, either express or implied.  See the License for the
//* specific language governing permissions and limitations
//* under the License.
//*/
//
//package org.wso2.carbon.security.caas.test.unit;
//
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.testng.PowerMockTestCase;
//import org.testng.Assert;
//import org.testng.annotations.AfterMethod;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Test;
//import org.wso2.carbon.security.caas.api.util.CarbonSecurityConstants;
//import org.wso2.carbon.security.caas.internal.CarbonSecurityDataHolder;
//import org.wso2.carbon.security.caas.internal.config.StoreConfigBuilder;
//import org.wso2.carbon.security.caas.user.core.bean.Domain;
//import org.wso2.carbon.security.caas.user.core.bean.User;
//import org.wso2.carbon.security.caas.user.core.claim.ClaimManager;
//import org.wso2.carbon.security.caas.user.core.config.StoreConfig;
//import org.wso2.carbon.security.caas.user.core.context.AuthenticationContext;
//import org.wso2.carbon.security.caas.user.core.exception.AuthenticationFailure;
//import org.wso2.carbon.security.caas.user.core.exception.CredentialStoreException;
//import org.wso2.carbon.security.caas.user.core.exception.IdentityStoreException;
//import org.wso2.carbon.security.caas.user.core.exception.UserNotFoundException;
//import org.wso2.carbon.security.caas.user.core.store.AuthorizationStore;
//import org.wso2.carbon.security.caas.user.core.store.CredentialStore;
//import org.wso2.carbon.security.caas.user.core.store.CredentialStoreImpl;
//import org.wso2.carbon.security.caas.user.core.store.IdentityStore;
//import org.wso2.carbon.security.caas.user.core.store.IdentityStoreImpl;
//import org.wso2.carbon.security.caas.user.core.store.connector.CredentialStoreConnector;
//import org.wso2.carbon.security.caas.user.core.store.connector.CredentialStoreConnectorFactory;
//import org.wso2.carbon.security.caas.user.core.store.connector.IdentityStoreConnector;
//import org.wso2.carbon.security.caas.user.core.store.connector.IdentityStoreConnectorFactory;
//
//import javax.security.auth.callback.Callback;
//import javax.security.auth.callback.NameCallback;
//import javax.security.auth.callback.PasswordCallback;
//import java.net.URL;
//
///**
// * Tests specific for the domain model implementation.
// */
//@PrepareForTest(CarbonSecurityDataHolder.class)
//public class DomainTests extends PowerMockTestCase {
//
//    @Mock
//    private CarbonSecurityDataHolder carbonSecurityDataHolder;
//
//    /**
//     * Carbon home directory name.
//     */
//    public static final String CARBON_HOME_DIRECTORY = "carbon_home";
//
//    /**
//     * Initialise mocks at test start.
//     */
//    @BeforeClass
//    public void setup() {
//
//        MockitoAnnotations.initMocks(this);
//    }
//
//    /**
//     * Initialise instances which are required by the tests.
//     *
//     * @throws CredentialStoreException Exception when an error occurs in credential store config
//     * @throws IdentityStoreException   Exception when an error occurs in identity store config
//     */
//    @BeforeMethod
//    public void init() throws CredentialStoreException, IdentityStoreException {
//
//        initCarbonSecurityDataHolder();
//    }
//
//    /**
//     * Reset mock instances.
//     */
//    @AfterMethod
//    public void resetMocks() {
//
//        Mockito.reset(carbonSecurityDataHolder);
//    }
//
//
//    /**
//     * Authenticate a user.
//     *
//     * @throws CredentialStoreException Exception in the credential store
//     * @throws AuthenticationFailure    Exception upon failing to authenticate
//     * @throws UserNotFoundException    Exception when the appropriate user instance is not found
//     * @throws IdentityStoreException   Exception when an error occurs in identity store config
//     */
//    @Test
//    public void authenticateUser()
//            throws CredentialStoreException, AuthenticationFailure,
//            UserNotFoundException, IdentityStoreException {
//
//        StoreConfig storeConfig = StoreConfigBuilder.buildStoreConfigs();
//        Domain domain = new Domain("D1", storeConfig);
//
//        // User builder initialisation
//        User.UserBuilder userBuilder = initUserBuilder("admin", domain);
//
//        Mockito.when(identityStoreConnector.getUserBuilder(Mockito.any(Callback[].class)))
//                .thenReturn(userBuilder);
//
//        Callback[] callbacks = new Callback[2];
//        PasswordCallback passwordCallback = new PasswordCallback("password", false);
//        NameCallback nameCallback = new NameCallback("username");
//
//        nameCallback.setName("admin");
//        passwordCallback.setPassword(new char[]{'a', 'd', 'm', 'i', 'n'});
//
//        callbacks[0] = passwordCallback;
//        callbacks[1] = nameCallback;
//
//        AuthenticationContext authenticationContext = credentialStore.authenticate(callbacks);
//
//        Assert.assertNotNull(authenticationContext);
//    }
//
//    /**
//     * Set carbon home directory.
//     */
//    private void setCarbonHomeDirectory() {
//
//        URL carbonHomeURL = getClass().getClassLoader().getResource(CARBON_HOME_DIRECTORY);
//        String carbonHome = carbonHomeURL != null
//                ? carbonHomeURL.getPath()
//                : "";
//
//        System.setProperty(CarbonSecurityConstants.CARBON_HOME, carbonHome);
//    }
//
//    /**
//     * Initialise carbon security data holder.
//     */
//    private void initCarbonSecurityDataHolder() {
//
//        PowerMockito.mockStatic(CarbonSecurityDataHolder.class);
//        Mockito.when(CarbonSecurityDataHolder.getInstance()).thenReturn(carbonSecurityDataHolder);
//    }
//
//    /**
//     * Initialises a user builder instance.
//     *
//     * @param userId id of the user
//     * @param domain domain of the domain in which the user belongs
//     * @return User.UserBuilder instance
//     */
//    private User.UserBuilder initUserBuilder(String userId, Domain domain) {
//
//        User.UserBuilder userBuilder = Mockito.mock(User.UserBuilder.class);
//        Mockito.when(userBuilder.setIdentityStore(Mockito.any(IdentityStore.class)))
//                .thenReturn(userBuilder);
//        Mockito.when(userBuilder.setAuthorizationStore(Mockito.any(AuthorizationStore.class)))
//                .thenReturn(userBuilder);
//        Mockito.when(userBuilder.setClaimManager(Mockito.any(ClaimManager.class)))
//                .thenReturn(userBuilder);
//
//        User user = Mockito.mock(User.class);
//        Mockito.when(user.getUserId()).thenReturn(userId);
//        Mockito.doReturn(user).when(userBuilder).build();
//        Mockito.doReturn(domain).when(user).getDomain();
//
//        return userBuilder;
//    }
//}
