# The Overlord APIMan project (API Management)

## Summary

This is the official Git repository for the Overlord APIMan project, which is intended to be a part of the [JBoss Overlord](http://www.jboss.org/overlord) umbrella project.

The APIMan project is a standalone API Management system that can be either run as a separate system or embedded within existing frameworks.

## Get the code

The easiest way to get started with the code is to [create your own fork](http://help.github.com/forking/) of this repository, and then clone your fork:

	$ git clone git@github.com:<you>/apiman.git
	$ cd apiman
	$ git remote add upstream git://github.com/Governance/apiman.git
	
At any time, you can pull changes from the upstream and merge them onto your master:

	$ git checkout master               # switches to the 'master' branch
	$ git pull upstream master          # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
	$ git push origin                   # pushes all the updates to your fork, which should be in-sync with 'upstream'

The general idea is to keep your 'master' branch in-sync with the 'upstream/master'.

## Building APIMan

We use Maven 3.x to build our software. The following command compiles all the code, installs the JARs into your local Maven repository, and runs all of the unit tests:

	$ mvn clean install
	
## Quickstart (i.e. How To Run It)

The project can be built and deployed on a variety of runtime platforms, but if you want to see it in 
action as quickly as possible try this:

    $ mvn clean install -Prun-all-wildfly8

This maven command will do the following:

1. A full build of apiman
2. Download WildFly 8.x
3. Unpack and configure WildFly 8.x
4. Deploy the WildFly 8.x version of apiman to WildFly
5. Launch WildFly

If all goes well, you can point your browser to the [API Management UI](http://localhost:8080/apiman-dt-ui/) and log in as
any of the following users:

* admin/admin123!
* bwayne/bwayne123!
* ckent/ckent123!
* dprince/dprince123!

## Contribute fixes and features

APIMan is open source, and we welcome anybody who wants to participate and contribute!

If you want to fix a bug or make any changes, please log an issue in the [APIMan JIRA](http://issues.jboss.org/browse/APIMAN) describing the bug
or new feature. Then we highly recommend making the changes on a topic branch named with the JIRA issue number. For example, this command creates
a branch for the APIMAN-1234 issue:

	$ git checkout -b apiman-1234

After you're happy with your changes and a full build (with unit tests) runs successfully, commit your changes on your topic branch
(using [really good comments](http://community.jboss.org/wiki/OverlordDevelopmentGuidelines#Commits)). Then it's time to check for
and pull any recent changes that were made in the official repository:

	$ git checkout master               # switches to the 'master' branch
	$ git pull upstream master          # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
	$ git checkout apiman-1234           # switches to your topic branch
	$ git rebase master                 # reapplies your changes on top of the latest in master
	                                      (i.e., the latest from master will be the new base for your changes)

If the pull grabbed a lot of changes, you should rerun your build to make sure your changes are still good.
You can then either [create patches](http://progit.org/book/ch5-2.html) (one file per commit, saved in `~/apiman-1234`) with 

	$ git format-patch -M -o ~/apiman-1234 orgin/master

and upload them to the JIRA issue, or you can push your topic branch and its changes into your public fork repository

	$ git push origin apiman-1234         # pushes your topic branch into your public fork of APIMan

and [generate a pull-request](http://help.github.com/pull-requests/) for your changes. 

We prefer pull-requests, because we can review the proposed changes, comment on them,
discuss them with you, and likely merge the changes right into the official repository.

Please also read the guidelines for contributors: https://github.com/Governance/overlord/wiki/Contributor-Guidelines
