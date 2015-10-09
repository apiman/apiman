// Browserify Entry Point
// This file includes a list of dependencies we want to use on the client, but must be
// installed server-side with npm. Browserify will check all requires(), subsequently
// bundling them into a single file, which we can then use on the client.

global.jQuery = global.$ = require('jquery');
var bootstrap = require('patternfly/components/bootstrap/dist/js/bootstrap.js');
var bootstrapSelect = require('patternfly/components/bootstrap-select/bootstrap-select.js');
var jsonEditor = require('json-editor');
var patternfly = require('patternfly/dist/js/patternfly.js');
var angular = require('angular');
var ngResource = require('angular-resource');
var ngSortable = require('ng-sortable/dist/ng-sortable.js');
var d3 = require('d3');
global.c3 = require('patternfly/components/c3/c3.js');
var ngXEditable = require('angular-xeditable/dist/js/xeditable.js');
var ngSanitize = require('angular-sanitize');
global.Logger = require('js-logger');
global._ = require('lodash');
var ngRoute = require('angular-route');
var URI = require('urijs');
var ngSchemaForm = require('angular-schema-form');
global.SwaggerUi = require('swagger-ui-browserify');
global.SwaggerClient = require('swagger-client');
global.SwaggerAuthorizations = require('swagger-ui-browserify/node_modules/swagger-ui/dist/lib/swagger-oauth.js');
