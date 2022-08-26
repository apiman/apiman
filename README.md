![Verify Build Workflow](https://github.com/Apiman/apiman/workflows/Verify%20Build%20Workflow/badge.svg)
[![Apiman Cypress](https://img.shields.io/endpoint?url=https://dashboard.cypress.io/badge/simple/tpeh3n/master&style=flat&logo=cypress)](https://dashboard.cypress.io/projects/tpeh3n/runs)

# The Apiman project (Open Source API Management)

## 📖 Requirements

- Java 11+
- To build Apiman with tests: Docker or an equivalent container engine. This is for running tests with [testcontainers](https://www.testcontainers.org/supported_docker_environment/). 
- Naturally, if you're using an Apiman container, you'll need it for runtime too!

## 🏃‍♂️ Quickstart

You can look at the Apiman [quickstarts on apiman.io](https://www.apiman.io).

Or, you can build and run our 'server-all' quickstart to try everything out immediately.

Here's what you need to do:

```bash
echo "Cloning Apiman"
git clone --recurse-submodules https://github.com/apiman/apiman.git && cd apiman

echo "Building Apiman"
./fastbuild.sh

echo "Starting a Keycloak container with a demo Apiman realm"
cd tools/server-all/target/docker
docker compose up -d

echo "Starting Apiman WildFly Quickstart" && cd ../
./wildfly-dev-server/bin/standalone.sh
```

Once Apiman has started up, and if all went well, you can point your browser to the API Manager via http://localhost:8080/apimanui/ and log in (either register a new user or log in as the admin):

* Username: `admin`
* Password: `admin123!`

You can log into the Keycloak admin console via http://localhost:8085/admin (same credentials as above).

This quickstart seeds a bunch of content into apiman. This is not strictly necessary, but it does populate the API Manager with some data so that it doesn't feel so lonely the first time you log in.

There's also a handy echo service available at http://localhost:8080/services/echo - it's useful for demos. You're welcome!

⚠️ Please don't use Apiman's default usernames, passwords, keys, certificates, etc., in production.

## ⚒️ Build Apiman

### Fast and easy

The easiest way to build quickly (without tests) is to run our [fastbuild script](https://github.com/apiman/apiman/blob/master/fastbuild.sh). It's in the root of the project. If you have [mvnd](https://github.com/apache/maven-mvnd/) the build will be faster.

```shell
git clone --recurse-submodules https://github.com/apiman/apiman.git && cd apiman
./fastbuild.sh 
```

### I want to run the tests

First, build the parent pom (it's in `/parent`), then you can build the main project from the top level.

```shell
echo "Building Apiman Parent..."
cd parent
../mvnw clean install

cd ..
echo "Building the main Apiman project..."
./mvnw clean install
```

## 👷 Contribute to Apiman

Apiman is open source, and we welcome anybody who wants to participate and contribute!

If you want to fix a bug or make any changes, please log an issue in [GitHub Issues](https://github.com/apiman/apiman/issues) describing the bug or new feature. 

You can also [join our discussion forums](https://github.com/apiman/apiman/discussions) if you want help, or to discuss a more complex issue.

There are [lots of different workflows for contributing](https://docs.github.com/en/github/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request). Feel free to use one that suits you. We're endeavouring to pull together a more detailed contribution document that we'll upload soon 🙌.

## 🔎 Developer Portal

Apiman also comes with a developer portal. There you can allow developers to access your APIs. Developers can view and test your APIs to develop their own apps.\
Check it out here: [Apiman Developer-Portal](https://github.com/apiman/apiman-developer-portal)

## Looking for support?

Apiman is a non-commercial project and it is supported downstream by multiple commercial sponsors that offer support and SaaS.

 > [Scheer PAS](https://www.scheer-pas.com/en/) is the platform for flexible end-to-end support of individual processes. It stands for digitization and automation, regardless of how many people, systems or companies need to be integrated.

[Scheer PAS API Management](https://www.scheer-pas.com/en/api-management/) is based on Apiman and offers you also enterprise support.\
You may visit [Scheer PAS documentation](https://doc.scheer-pas.com/display/HOME) for further details.
