package com.svivianl.dropwizardpac4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotEmpty;


public class DropwizardPac4jConfiguration extends Configuration {

    @NotEmpty
    private String template;
    @NotEmpty
    private String defaultName = "Stranger";

    // testing SSO
    private String ssoClientId;
    private String ssoClientSecret;
    private String ssoDomain;
    // private String ssoDiscoveryUri;
    private String ssoCallbackPath;
    private String ssoSessionKey;

    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }

    public String getSsoClientId() { return ssoClientId; }
    public String getSsoClientSecret() { return ssoClientSecret; }
    public String getSsoDomain() { return ssoDomain; }
    public String getSsoCallbackPath() { return ssoCallbackPath; }
    //public String getSsoDiscoveryUri() { return ssoDiscoveryUri; }
    public String getSsoSessionKey() { return ssoSessionKey; }

}
