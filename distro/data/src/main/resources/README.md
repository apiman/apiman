# Initial data and configuration

## About

* At first startup all files (in bootstrap directory) will be imported
* Initial data will not be imported again until you change the `exportedOn` date
  * The `exportedOn` field represents the date of the initial data
  * The system data will be updated if the `exportedOn` field changes to a newer date
  * The system saves the metadata on each import

### The Benefit

This allows you to change system data with new apiman releases.
E.g. If you want to add a new policy definition you will add them into the file and change the `exportedOn` date.
After building a release the system will be updated at first startup

## Change or Update Configuration between Releases

1. Change the `exportedOn` field
2. Change the initial data. Be careful this may overwrite you actual apiman config

## Disable Initial Config

You can disable the import of initial configuration completely with the property `-Dapiman.bootstrap.disabled=true`
