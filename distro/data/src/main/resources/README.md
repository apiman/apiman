# Apiman: Open Source API Management Platform

The main Apiman documentation is available on https://www.apiman.io.

## Keycloak IDM (WildFly distro)

Apiman is usually combined with an IDM system, such as Keycloak, to manage users, secure login, roles, etc.

If you are unable to log in, it is likely because Keycloak is not running, or the URL for Apiman to connect to Keycloak is not configured correctly.

Historically, a Keycloak server was bundled with the Apiman WildFly overlay; this is no longer the case since Apiman 3.

* Start a [standalone Keycloak server](https://www.keycloak.org).

* [Bootstrap Keycloak with the demonstration Apiman realm definition](https://www.keycloak.org/server/importExport) in `data/apiman-realm-for-keycloak.json`.

* Set the `APIMAN_AUTH_URL` environment variable to point to the running Keycloak instance; for example `export APIMAN_AUTH_URL=http://localhost:5/auth`.

You can modify details such as secrets, trust manager settings, etc, in `standalone/configuration/standalone-apiman.xml`. 
Look for the Keycloak subsystem `<subsystem xmlns="urn:jboss:domain:keycloak:1.1">...</subsystem>`.

The Apiman Docker Compose distribution may provide useful examples. 

**For production use, please change the default realm passwords and secrets.**

## Initial data and configuration (bootstrapping)

* At first startup all files in the bootstrap directory will be imported
* Initial data will not be imported again until you change the `exportedOn` date
  * The `exportedOn` field represents the date of the initial data
  * The system data will be updated if the `exportedOn` field changes to a newer date
  * The system saves the metadata on each import

This allows you to change system data with new Apiman releases.
E.g. If you want to add a new policy definition you will add them into the file and change the `exportedOn` date.
After building a release the system will be updated at first startup.

### Change or Update Configuration between Releases

1. Change the `exportedOn` field
2. Change the initial data. Be careful this may overwrite your actual Apiman config

### Disable Initial Config

You can disable the import of initial configuration completely with the property `-Dapiman.bootstrap.disabled=true`
