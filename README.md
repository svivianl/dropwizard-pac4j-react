# DESCRIPTION

A mix and match of [Use Pac4j to Lock Down Your Java REST API](https://developer.okta.com/blog/2018/09/12/secure-java-ee-rest-api#use-pac4j-to-lock-down-your-java-rest-api) and [Use React and Spring Boot to Build a Simple CRUD App](https://developer.okta.com/blog/2022/06/17/simple-crud-react-and-spring-boot) tutorials.

## OKTA SETUP

1. Create an Okta Application with `OIdC` sign-in method and a `Web Application` application type

- Keep `Authorization code` grant type only
- Sign-in redirect URIs: add `htt://localhost:<HTTP_PORT>/<SSO_CALLBACK_PATH>?client_name=OidcClient`, where `<HTTP_PORT>` and `<SSO_CALLBACK_PATH>` were set up, respectively,  in the `HTTP_PORT` and
  `SSO_CALLBACK_PATH` variables in the `./dropwizard-pac4j/config.yml` or `./dropwizard-pac4j/override.yml` file
- Assignments/Controlled Access: select `Allow everyone in your organization to access

3. In Security/API, add localhost (`http://localhost:3000`) to `Trusted Origins`

# RESOURCES

- [Use Pac4j to Lock Down Your Java REST API](https://developer.okta.com/blog/2018/09/12/secure-java-ee-rest-api#use-pac4j-to-lock-down-your-java-rest-api)
- [Use React and Spring Boot to Build a Simple CRUD App](https://developer.okta.com/blog/2022/06/17/simple-crud-react-and-spring-boot)
- [dropwizard-pac4j](https://github.com/pac4j/dropwizard-pac4j)
- [dropwizard-pac4j-demo](https://github.com/pac4j/dropwizard-pac4j-demo)

# FUTURE FEATURES

- Add PKCE

# KNOWN ISSUES

- When the user has already been authenticated and they press the Login butt, the page will redirect to `/api/private` and they will see the 404 error. To workaround it, the page's cookies must be deleted and the user must go to the `http://localhost:3000` page
