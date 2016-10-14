package org.wso2.carbon.security.caas.internal.config.domain;

import java.util.List;

/**
 * Domain config.
 */
public class DomainConfigFile {

    /**
     * List of domain configuration entries.
     */
    private List<DomainConfigEntry> domains;

    /**
     * Get Domain configuration entries.
     *
     * @return List<DomainConfigEntry> - List of domain configuration entries
     */
    public List<DomainConfigEntry> getDomains() {
        return domains;
    }

    /**
     * Set Domain configuration entries.
     *
     * @param domains List<DomainConfigEntry> - List of domain configuration entries
     */
    public void setDomains(List<DomainConfigEntry> domains) {
        this.domains = domains;
    }
}
