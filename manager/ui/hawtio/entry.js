// Browserify Entry Point
// This file includes a list of dependencies we want to use on the client, but must be
// installed server-side with npm. Browserify will check all requires(), subsequently
// bundling them into a single file, which we can then use on the client.

global.jQuery = global.$ = require('jquery');
var bootstrap = require('bootstrap');
var bootstrapSelect = require('bootstrap-select/dist/js/bootstrap-select.js');
var jsonEditor = require('json-editor');
var patternfly = require('patternfly/dist/js/patternfly.js');
var angular = require('angular');
var ngResource = require('angular-resource');
var ngSortable = require('ng-sortable/dist/ng-sortable.js');
var d3 = require('d3');
var c3 = require('c3');
var ngXEditable = require('angular-xeditable/dist/js/xeditable.js');
var ngSanitize = require('angular-sanitize');
global.Logger = require('js-logger');
global._ = require('lodash');
var ngRoute = require('angular-route');
var URI = require('urijs');
var ngSchemaForm = require('angular-schema-form');
var swaggerUIBrowserify = require('swagger-ui-browserify');
