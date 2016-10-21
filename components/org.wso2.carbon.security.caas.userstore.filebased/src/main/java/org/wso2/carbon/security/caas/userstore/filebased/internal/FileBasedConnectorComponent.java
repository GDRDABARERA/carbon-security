package org.wso2.carbon.security.caas.userstore.filebased.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.security.caas.user.core.store.connector.AuthorizationStoreConnectorFactory;
import org.wso2.carbon.security.caas.user.core.store.connector.CredentialStoreConnectorFactory;
import org.wso2.carbon.security.caas.user.core.store.connector.IdentityStoreConnectorFactory;
import org.wso2.carbon.security.caas.userstore.filebased.connector.FileBasedAuthorizationStoreConnectorFactory;
import org.wso2.carbon.security.caas.userstore.filebased.connector.FileBasedCredentialStoreConnectorFactory;
import org.wso2.carbon.security.caas.userstore.filebased.connector.FileBasedIdentityStoreConnectorFactory;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * OSGi component for carbon security connectors.
 * @since 1.0.0
 */
@Component(
        name = "org.wso2.carbon.security.userstore.filebased.internal.FileBasedConnectorComponent",
        immediate = true
)
public class FileBasedConnectorComponent {

    private static final Logger log = LoggerFactory.getLogger(FileBasedConnectorComponent.class);

    /**
     * Register user store connectors as OSGi services.
     * @param bundleContext Bundle Context.
     */
    @Activate
    public void registerCarbonSecurityConnectors(BundleContext bundleContext) {

        Dictionary<String, String> connectorProperties = new Hashtable<>();

        connectorProperties.put("connector-type", "FileBasedIdentityStore");
        bundleContext.registerService(IdentityStoreConnectorFactory.class,
                new FileBasedIdentityStoreConnectorFactory(), connectorProperties);

        connectorProperties = new Hashtable<>();
        connectorProperties.put("connector-type", "FileBasedAuthorizationStore");
        bundleContext.registerService(AuthorizationStoreConnectorFactory.class,
                new FileBasedAuthorizationStoreConnectorFactory(), connectorProperties);

        connectorProperties = new Hashtable<>();
        connectorProperties.put("connector-type", "FileBasedCredentialStore");
        bundleContext.registerService(CredentialStoreConnectorFactory.class,
                new FileBasedCredentialStoreConnectorFactory(), connectorProperties);

        log.info("File based user store connector bundle successfully activated.");
    }
}
