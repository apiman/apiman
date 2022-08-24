[![Verify Build Workflow](https://github.com/apiman/apiman-developer-portal/actions/workflows/verify.yml/badge.svg)](https://github.com/apiman/apiman-developer-portal/actions/workflows/verify.yml)

# Apiman Developer Portal

 > A developer portal for Apiman! Allow developers to access your APIs. Developers can view and test your APIs to develop their own apps.

![Landing](./docu/landing.png)
[Click here to see more pictures](#screenshots)


## Local Development

### Starting the dev mode

* Create a copy of the `src/assets/config.json5` called `src/assets/local-config.json5`
* Adapt the `endpoint` and `auth.url` to match you apiman and keycloak setup
* Execute `npm install && npm run start` or use our provided run configuration (Jetbrains IntelliJ/WebStorm)

#### Linux / Windows
```bash
cp src/assets/config.json5 src/assets/local-config.json5
npm install
npm run start
```

### Building the docker image yourself

```bash
docker build -t apiman/developer-portal:latest .
```

### Cypress E2E Tests
#### Prerequirements
* Create the necessary users (find the credentials in cypress.json) in your IDM with the following roles:
  * cypress.admin - apiadmin
  * cypress.user - apiuser
  * cypress.user2 - devportaluser
* Adjust the following settings in cypress.json if needed:
  * baseUrl -> this url should point to your running 'API Developer Portal'-App 
  * env.apiman_endpoint -> this url should be the API Management REST API

#### Run the tests
* To open cypress and run the test manually in the Cypress Test Runner  `npm run cy:open` or use our provided run configuration (Jetbrains IntelliJ/WebStorm)
* To run all tests automatically `npm run cy:run` or use our provided run configuration (Jetbrains IntelliJ/WebStorm)

## Screenshots
![Detail](./docu/detail.png)
![MY Clients 1](./docu/my-client1.png)
![My Client 2](./docu/my-client2.png)

## Looking for support?

> [Scheer PAS](https://www.scheer-pas.com/en/) is the platform for flexible end-to-end support of individual processes. It stands for digitization and automation, regardless of how many people, systems or companies need to be integrated.

[Scheer PAS API Management](https://www.scheer-pas.com/en/api-management/) is based on Apiman and offers you also
enterprise support.\
You may visit [Scheer PAS documentation](https://doc.scheer-pas.com/display/HOME) for further details.
