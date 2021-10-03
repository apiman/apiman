# Apiman UI

The Apiman frontend is built with TypeScript and AngularJS. Yarn/NPM and TSD are used to manage dependencies.

## Installation

To install dependencies on the front-end, run the following:

`yarn install`

This will install NPM dependencies and TS definitions (through an NPM `postinstall` script). No need to install any 
dependencies globally!

## Configuration

* Make a copy of *apiman/config.js-SAMPLE* and name it *apiman/config.js*
* Configure *config.js* to work with your API Manager

_Note_: when running the API Manager UI in gulp, we will use simple BASIC authentication
with the credentials configured in the *config.js* file you created above.  The *config.js*
file is local to your machine and not checked into version control.

## Get Started

To build and run the app:

`npx gulp`

The above script will also monitor for changes, and will rebuild the app accordingly. To see exactly which files are monitored, check the `watch` task in the Gulpfile, located in `/apiman/manager/ui/war/gulpfile.js`.

## Contributing

We gladly accept PRs and always appreciate it when community members want to contribute.

If add a new package to `package.json`, ensure you update the yarn lock file.
