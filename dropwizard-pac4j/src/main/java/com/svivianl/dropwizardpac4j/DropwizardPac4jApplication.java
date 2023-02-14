package com.svivianl.dropwizardpac4j;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.session.SessionHandler;
import org.pac4j.core.authorization.authorizer.ProfileAuthorizer;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.HttpActionHelper;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.dropwizard.Pac4jFactory;
import org.pac4j.jax.rs.features.JaxRsConfigProvider;
import org.pac4j.jax.rs.features.Pac4JSecurityFeature;
import org.pac4j.jax.rs.jersey.features.Pac4JValueFactoryProvider;
import org.pac4j.jax.rs.servlet.features.ServletJaxRsContextFactoryProvider;
import org.pac4j.jee.filter.CallbackFilter;
import org.pac4j.jee.filter.LogoutFilter;
import org.pac4j.jee.filter.SecurityFilter;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.List;


/**
 * A ready-to-go example service with core libraries at the ready.
 */
public class DropwizardPac4jApplication extends Application<DropwizardPac4jConfiguration> {

    public static void main(String[] args) throws Exception {
        new DropwizardPac4jApplication().run(args);
    }

    @Override
    public String getName() {
        return "dropwizard-pac4j";
    }

    @Override
    public void initialize(Bootstrap<DropwizardPac4jConfiguration> bootstrap) {
        // Allow environment variable substitution to work in the configuration
        // ref: https://www.dropwizard.io/en/latest/manual/core.html#environment-variables
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    }

    @Override
    public void run(DropwizardPac4jConfiguration configuration, Environment environment) {
        if (configuration.getSsoClientId() != null) {
            setSsoFilters(configuration, environment);
        }

        ApiResources apiResources = new ApiResources();

        environment.jersey().register(new Pac4JValueFactoryProvider.Binder());
        environment.jersey().register(apiResources);

        environment.jersey()
                .register(new ServletJaxRsContextFactoryProvider());
        environment.jersey().register(new Pac4JSecurityFeature());
        environment.jersey()
                .register(new Pac4JValueFactoryProvider.Binder());

        setupJettySession(environment, configuration);
    }

    /**
     * Override if needed, but prefer to exploit
     * {@link Pac4jFactory#setSessionEnabled(boolean)} first.
     *
     * @param environment
     *            the dropwizard {@link Environment}
     * @since 1.1.0
     */
    protected void setupJettySession(Environment environment, DropwizardPac4jConfiguration configuration) {
        MutableServletContextHandler contextHandler = environment.getApplicationContext();
        if (contextHandler.getSessionHandler() == null) {
            contextHandler.setSessionHandler(new SessionHandler());
        }

        environment.getApplicationContext().getServletContext().getSessionCookieConfig().setName(configuration.getSsoSessionKey());

    }


    public void setSsoFilters(DropwizardPac4jConfiguration configuration, Environment environment) {

        Config securityConfig = new SecurityConfigFactory(configuration).build();
        environment.jersey().register(new JaxRsConfigProvider(securityConfig));

        // https://github.com/pac4j/javalin-pac4j/blob/master/src/test/java/org/pac4j/javalin/example/CustomAuthorizer.java
        // https://developer.okta.com/blog/2017/10/31/add-authentication-to-play-framework-with-oidc
        securityConfig.addAuthorizer("securityHeaders", new ProfileAuthorizer() {
            @Override
            protected boolean isProfileAuthorized(WebContext webContext, SessionStore sessionStore, UserProfile userProfile) {
                return userProfile != null;
            }

            @Override
            public boolean isAuthorized(WebContext webContext, SessionStore sessionStore, List<UserProfile> list) {
                return isAnyAuthorized(webContext, sessionStore, list);
            }
        });

        CorsFilter corsFilter = new CorsFilter();
        environment.servlets().addFilter(corsFilter.getClass().getName(), corsFilter)
                .addMappingForUrlPatterns(
                        EnumSet.allOf(DispatcherType.class), true, "/*");

        String callbackPath = configuration.getSsoCallbackPath();
/*                // "/callback";
                "/login/oauth2/code/okta";*/
        CallbackFilter callbackFilter = new CallbackFilter();
        callbackFilter.setDefaultUrl("/");
        callbackFilter.setRenewSession(true);
        // https://www.linkedin.com/pulse/apache-zeppelin-oidc-single-sign-on-using-pac4j-oliveira-he-him-
        callbackFilter.setCallbackLogic(new DefaultCallbackLogic() {
            @Override
            public HttpAction redirectToOriginallyRequestedUrl(final WebContext context, final SessionStore sessionStore,
                                                               final String defaultUrl) {
                // from the context we can get the client that is logged in
                String defaultUrl2 = "http://localhost:3000";
                if (CommonHelper.isNotBlank(defaultUrl2) &&
                        !Pac4jConstants.DEFAULT_URL_VALUE.equals(defaultUrl2)) {
                    return HttpActionHelper.buildRedirectUrlAction(context, (new FoundAction(defaultUrl2)).getLocation());
                /*if (CommonHelper.isNotBlank(defaultUrl) &&
                        !Pac4jConstants.DEFAULT_URL_VALUE.equals(defaultUrl)) {
                    return HttpActionHelper.buildRedirectUrlAction(context, (new FoundAction(defaultUrl)).getLocation());*/
                } else {
                    return super.redirectToOriginallyRequestedUrl(context, sessionStore, "");
                }
            }
        });
        callbackFilter.setConfig(securityConfig);
        environment.servlets().addFilter(callbackFilter.getClass().getName(), callbackFilter)
                .addMappingForUrlPatterns(
                        EnumSet.of(DispatcherType.REQUEST), true, callbackPath + "/*");

        SecurityFilter securityFilter = new SecurityFilter();
        securityFilter.setClients("oidcClient,headerClient");

        // The authorizer 'securityHeaders' must be defined in the security configuration
        securityFilter.setAuthorizers("securityHeaders");

        //securityFilter.setRenewSession(true);
        securityFilter.setConfig(securityConfig);

        environment.servlets()
                .addFilter(securityFilter.getClass().getName(), securityFilter)
                .addMappingForUrlPatterns(
                        EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), true,
                        // "/*"
                        "/api/private", "/api/groups"
                );

        LogoutFilter logoutFilter = new LogoutFilter();
        logoutFilter.setLocalLogout(false);
        logoutFilter.setCentralLogout(true);
        logoutFilter.setConfig(securityConfig);
        environment.servlets().addFilter(logoutFilter.getClass().getName(), logoutFilter)
                .addMappingForUrlPatterns(
                        EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), true, "/logout");
    }
}