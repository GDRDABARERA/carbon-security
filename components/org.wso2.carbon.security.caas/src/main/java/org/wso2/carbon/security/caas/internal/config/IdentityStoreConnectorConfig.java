package org.wso2.carbon.security.caas.internal.config;

import java.util.List;

/**
 * Config entry for IdentityStoreConnector.
 */
public class IdentityStoreConnectorConfig extends CredentialStoreConnectorConfig {

    List<String> uniqueAttributes;

    List<String> otherAttributes;

    public List<String> getUniqueAttributes() {
        return uniqueAttributes;
    }

    public void setUniqueAttributes(List<String> uniqueAttributes) {
        this.uniqueAttributes = uniqueAttributes;
    }

    public List<String> getOtherAttributes() {
        return otherAttributes;
    }

    public void setOtherAttributes(List<String> otherAttributes) {
        this.otherAttributes = otherAttributes;
    }
}
