package com.svivianl.dropwizardpac4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.SimpleServerFactory;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.direct.AnonymousClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.http.ajax.DefaultAjaxRequestResolver;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;

import java.util.Optional;
import java.util.stream.Stream;

// https://developer.okta.com/blog/2018/09/12/secure-java-ee-rest-api#use-pac4j-to-lock-down-your-java-rest-api
public class SecurityConfigFactory implements ConfigFactory {
    private final JwtAuthenticator jwtAuthenticator = new JwtAuthenticator();
    private final ObjectMapper mapper = new ObjectMapper();

    private final String clientId;
    private final String clientSecret;
    private final String discoveryURI;
    private final String callbackPath;
    private final String jwtUrl;
    private final int port;

    public SecurityConfigFactory(DropwizardPac4jConfiguration configuration) {
        this.clientId = configuration.getSsoClientId();
        this.clientSecret = configuration.getSsoClientSecret();
        // "/oauth2/default/.well-known/openid-configuration";
        this.discoveryURI = configuration.getSsoDomain() + "/oauth2/default/.well-known/openid-configuration";
                // configuration.getSsoDiscoveryUri();
        this.callbackPath = configuration.getSsoCallbackPath();
        this.jwtUrl = configuration.getSsoDomain() + "/oauth2/default/v1/keys";

        Stream<ConnectorFactory> connectors = configuration.getServerFactory() instanceof DefaultServerFactory
                ? ((DefaultServerFactory)configuration.getServerFactory()).getApplicationConnectors().stream()
                : Stream.of((SimpleServerFactory)configuration.getServerFactory()).map(SimpleServerFactory::getConnector);

        this.port = connectors.filter(connector -> connector.getClass().isAssignableFrom(HttpConnectorFactory.class))
                .map(connector -> (HttpConnectorFactory) connector)
                .mapToInt(HttpConnectorFactory::getPort)
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    @Override
    public Config build(final Object... parameters) {
        System.out.print("++++++++++++++++++++++++++++++\n");
        System.out.print("Building Security configuration...\n");

        String callbackUrl = "http://localhost:" + this.port + this.callbackPath;
        System.out.print("callback url: " + callbackUrl + "\n");

        final OidcConfiguration oidcConfiguration = new OidcConfiguration();
        oidcConfiguration.setClientId(clientId);
        oidcConfiguration.setSecret(clientSecret);
        oidcConfiguration.setDiscoveryURI(discoveryURI);
        oidcConfiguration.setScope("openid profile email");
        oidcConfiguration.setUseNonce(true);

        // https://developer.okta.com/docs/reference/api/oidc/#authorize
        // https://www.pac4j.org/docs/clients/openid-connect.html
        // select display mode: page, popup, touch, and wap
        oidcConfiguration.addCustomParam("display", "page");
        final OidcClient oidcClient = new OidcClient(oidcConfiguration);
        // https://devforum.okta.com/t/cannot-see-custom-attributes/16083

        oidcClient.setAuthorizationGenerator((ctx, sessionStore, profile) -> {
        /*oidcClient.setAuthorizationGenerator((ctx, profile) -> {*/
/*            String roles = profile.getAttribute("roles");
            for (String role: roles.split(",")) {
                profile.addRole(role);
            }
            return Optional.of(profile);*/
            profile.addRole("ROLE_USER");
            System.out.print("************************* profile: " + profile.getAttribute("name") + "\n");

            return Optional.of(profile);
        });
        oidcClient.setCallbackUrl(callbackUrl);
        oidcClient.setMultiProfile(true);

        DefaultAjaxRequestResolver ajaxRequestResolver = new DefaultAjaxRequestResolver();
        ajaxRequestResolver.setAddRedirectionUrlAsHeader(true);
        oidcClient.setAjaxRequestResolver(ajaxRequestResolver);
        /*oidcClient.getRedirectionAction()*/
        oidcClient.init();

        /*HeaderClient headerClient = new HeaderClient("Authorization", "Bearer ", oidcClient.getProfileCreator());*/
        HeaderClient headerClient = new HeaderClient("Authorization", "Bearer ", (credentials, ctx, sessionStore) -> {
            // it does not stop here
            String token = ((TokenCredentials) credentials).getToken();
            if (CommonHelper.isNotBlank(token)) {
                CommonProfile profile = new CommonProfile();
                profile.setId(token);
                credentials.setUserProfile(profile);
                return Optional.of(credentials.getUserProfile());
            }
            return Optional.empty();
        });

        final Clients clients = new Clients(callbackUrl, oidcClient,
                // direct client
                // if we want to call indirect client, headerClient should be commented out
                 headerClient,
                new AnonymousClient());
        clients.init();
        Config config = new Config(clients);
        PathMatcher matcher = new PathMatcher();
        config.getMatchers().put("pathMatcher", matcher);
        /*config.setProfileManagerFactory(profileManagerFactory);*/
        return config;
    }
}