# APIMan UI

The APIMan frontend is built with TypeScript and AngularJS. NPM and TSD are used to manage dependencies.

#### Installation

To install dependencies on the front-end, run the following:
 
 `npm install`
 
This will install NPM dependencies and TS definitions (through an NPM `postinstall` script). No need to install any 
dependencies globally!
 
#### Get Started

To build and run the app:

`gulp`

The above script will also monitor for changes, and will rebuild the app accordingly. To see exactly which files are monitored, check the `watch` task in the Gulpfile, located in `/apiman/manager/ui/hawtio/gulpfile.js`.
