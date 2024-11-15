![Verify Build Workflow](https://github.com/Apiman/apiman/workflows/Verify%20Build%20Workflow/badge.svg)
[![Apiman Cypress](https://img.shields.io/endpoint?url=https://dashboard.cypress.io/badge/simple/tpeh3n/master&style=flat&logo=cypress)](https://dashboard.cypress.io/projects/tpeh3n/runs)

# Apiman (Open Source API Management)

**⚡️⚡️ Apiman 4 is under active development ⚡️⚡️**

Apiman is a flexible and open-source API Management platform.

With just a few clicks, you can get Apiman running, putting a platform at your fingertips that covers the whole API Management lifecycle. Whether you want to offer existing APIs to external consumers securely or have a centralized location to discover and govern APIs, Apiman has you covered.

Apiman is designed to be easy to customize, allowing you to implement your functionality by writing simple Java plugins.

## 📖 Requirements


- Java 11+
- To build Apiman with tests: Docker or an equivalent container engine (for running tests with [testcontainers](https://www.testcontainers.org/supported_docker_environment/))
- Naturally, if you're using an Apiman container, you'll need it for runtime too!


## 🏃‍♂️ Quickstart


You can explore the [quickstarts on apiman.io](https://www.apiman.io).

Alternatively, build and run the 'server-all' quickstart to try everything out immediately. Here are the steps:

```bash
git clone https://github.com/apiman/apiman.git && cd apiman
./fastbuild.sh
cd tools/server-all/target/docker
docker compose up -d
./wildfly-dev-server/bin/standalone.sh
```

Once Apiman has started, you can access the API Manager at [http://localhost:8080/apimanui/](http://localhost:8080/apimanui/) and log in using the following credentials:

* Username: `admin`
* Password: `admin123!`
Keycloak admin console is available at [http://localhost:8085/admin](http://localhost:8085/admin) with the same credentials.

### ⚒️ Build Apiman
The easiest way to build quickly (without tests) is to use the [fastbuild script](https://github.com/apiman/apiman/blob/master/fastbuild.sh):

```shell
git clone https://github.com/apiman/apiman.git && cd apiman
./fastbuild.sh
```

### I want to run the tests

First, build the parent pom (located in `/parent`), and then build the main project from the top level:

```shell
echo "Building Apiman Parent..."
cd parent
../mvnw clean install

cd ..
echo "Building the main Apiman project..."
./mvnw clean install
```



## 👷 Contribute to Apiman


Apiman is open-source, and contributions are welcome! Log an issue in [GitHub Issues](https://github.com/apiman/apiman/issues) for bugs or new features. Join our [discussion forums](https://github.com/apiman/apiman/discussions) for complex issues or help.


## 🙋 Looking for support?


Check out [Apiman's support page](https://www.apiman.io/support.html) for expert assistance.

## 🔎 Developer Portal


Apiman comes with a skinnable developer portal that allows external developers to sign up for your APIs in a streamlined marketplace experience.

## 📚 External Documentation

### Quick Links
* [Download Apiman](https://www.apiman.io/download.html)
* [Read the latest Apiman Blogs](https://www.apiman.io/blog/)
* [Get Support](https://www.apiman.io/support.html)
* [Talk Apiman](https://github.com/apiman/apiman/discussions)

### Monorepo
The Apiman monorepo contains the following components:

* [Apiman Containers/Docker Images](https://github.com/apiman/apiman/tree/master/containers)
* [Apiman Plugins](https://github.com/apiman/apiman/tree/master/plugins)
* [Apiman Developer Portal](https://github.com/apiman/apiman/tree/master/portal/ui)
* [Apiman Manager](https://github.com/apiman/apiman/tree/master/manager)
* [Apiman Gateway](https://github.com/apiman/apiman/tree/master/gateway)
* [Apiman Distributions (packaged binaries we ship)](https://github.com/apiman/apiman/tree/master/distro)
