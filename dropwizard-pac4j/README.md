# Service Bootstrap

1. copy `config.yml` file as, for example, `override.yml`
2. make the necessary changes to the file and override the required variables (variables whose prefix is `SSO_`)
3. build the application: `mvn clean package`
4. run `java -jar target/dropwizard-pac4j-1.0.0-SNAPSHOT.jar server override.yml`


**General**

Required variables:

| **Variable**      | **default** | **example**                                             |
| ----------------- | ----------- |---------------------------------------------------------|
| HTTP_PORT         | 8080        |                                                         |
| HTTP_ADMIN_PORT   | 8081        |                                                         |
| SSO_CLIENT_ID     |             |                                                         |
| SSO_CLIENT_SECRET |             |                                                         |
| SSO_DOMAIN        |             | `https://dev-<YOUR_OKTA_DEV_ACCOUNT_NUMBER>.okta.com`   |
| SSO_CALLBACK_PATH |             | ex: `/login/oauth2/code/okta`                           |
| SSO_SESSION_KEY   |             |                                                         |

