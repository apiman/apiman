![Verify Build Workflow](https://github.com/Apiman/apiman/workflows/Verify%20Build%20Workflow/badge.svg)
[![Apiman Cypress](https://img.shields.io/endpoint?url=https://dashboard.cypress.io/badge/simple/tpeh3n/master&style=flat&logo=cypress)](https://dashboard.cypress.io/projects/tpeh3n/runs)

# The apiman project (Open Source API Management)

## Summary


This is the official Git repository for the apiman project:  http://apiman.io/

The apiman project is a standalone API Management system that can be either run as a separate system or
embedded within existing frameworks and platforms.

## Get the code

The easiest way to get started with the code is to [create your own fork](http://help.github.com/forking/)
of this repository, and then clone your fork:

	$ git clone git@github.com:<you>/apiman.git
	$ cd apiman
	$ git remote add upstream git://github.com/apiman/apiman.git

At any time, you can pull changes from the upstream and merge them onto your master:

	$ git checkout master               # switches to the 'master' branch
	$ git pull upstream master          # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
	$ git push origin                   # pushes all the updates to your fork, which should be in-sync with 'upstream'

The general idea is to keep your 'master' branch in-sync with the 'upstream/master'.

## Building apiman

### Requirements
- Maven 3.x
- Java 8+ (your application server must also support the version of Java you intend to deploy on)
- Docker (for running tests with [testcontainers](https://www.testcontainers.org/supported_docker_environment/))

The following command compiles all the code, installs the JARs into your local Maven repository, and runs all of the unit tests:

	$ mvn clean install
	
You can skip the test if you do not have docker installed:

	$ mvn clean install	-DskipTests

## Quickstart (i.e. How To Run It)

The project can be built and deployed on a variety of runtime platforms, but if you want to see it in
action as quickly as possible try this:

    $ mvn clean install -Pinstall-all-wildfly
    $ cd tools/server-all/target/wildfly-dev-server
    $ ./bin/standalone.sh

The above maven command will do the following:

1. A full build of apiman
2. Download WildFly
3. Unpack and configure WildFly
4. Deploy the WildFly version of Apiman to WildFly

Once WildFly has started up, and if all went well, you can point your browser to the
[API Manager](http://localhost:8080/apimanui/) and log in (either register a new user
or log in as the admin):

* admin/admin123!

Note that this quickstart seeds a bunch of content into apiman. This is not strictly necessary, but it
does populate the API Manager with some data so that it doesn't feel so lonely the first time you log in.
You're welcome.

## Contribute fixes and features

apiman is open source, and we welcome anybody who wants to participate and contribute!

If you want to fix a bug or make any changes, please log an issue in [GitHub Issues](https://github.com/apiman/apiman/issues) describing the bug
or new feature. Then we highly recommend making the changes on a topic branch named with the GitHub issue number. For example, this command creates
a branch for the #317 issue:

	$ git checkout -b apiman-317

After you're happy with your changes and a full build (with unit tests) runs successfully, commit your
changes on your topic branch. Then it's time to check for and pull any recent changes that were made in
the official repository:

	$ git checkout master               # switches to the 'master' branch
	$ git pull upstream master          # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
	$ git checkout apiman-317           # switches to your topic branch
	$ git rebase master                 # reapplies your changes on top of the latest in master
	                                      (i.e., the latest from master will be the new base for your changes)

If the pull grabbed a lot of changes, you should rerun your build to make sure your changes are still good.
You can then either [create patches](http://progit.org/book/ch5-2.html) (one file per commit, saved in `~/apiman-317`) with

	$ git format-patch -M -o ~/apiman-317 origin/master

and upload them to the JIRA issue, or you can push your topic branch and its changes into your public fork repository

	$ git push origin apiman-317        # pushes your topic branch into your public fork of apiman

and [generate a pull-request](http://help.github.com/pull-requests/) for your changes.

We prefer pull-requests, because we can review the proposed changes, comment on them,
discuss them with you, and likely merge the changes right into the official repository.

Please try to create one commit per feature or fix, generally the easiest way to do this is via [git squash](https://git-scm.com/book/en/v2/Git-Tools-Rewriting-History#Squashing-Commits).
This makes reverting changes easier, and avoids needlessly polluting the repository history with checkpoint commits.

## Code Formatting

When you are hacking on some apiman code, we'd really appreciate it if you followed the
apiman coding standards.  If you're using Eclipse, you can find a code formatter config
file here:

tools/src/eclipse/apiman-eclipse-formatter.xml

You should be able to import that guy straight into Eclipse by going to
*Window->Preferences :: Java/Code Style/Formatter*

## Developer Portal

Apiman also comes with a developer portal. There you can allow developers to access your APIs. Developers can view and test your APIs to develop their own apps.\
Check it out here: [Apiman Developer-Portal](https://github.com/apiman/apiman-developer-portal)

## Looking for support?

 > [Scheer PAS](https://www.scheer-pas.com/en/) is the platform for flexible end-to-end support of individual processes. It stands for digitization and automation, regardless of how many people, systems or companies need to be integrated.

[Scheer PAS API Management](https://www.scheer-pas.com/en/api-management/) is based on Apiman and offers you also enterprise support.\
You may visit [Scheer PAS documentation](https://doc.scheer-pas.com/display/HOME) for further details.
