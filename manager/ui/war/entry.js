// Browserify Entry Point
// This file includes a list of dependencies we want to use on the client, but must be
// installed server-side with npm. Browserify will check all requires(), subsequently
// bundling them into a single file, which we can then use on the client.

global.jQuery = global.$ = require('patternfly/components/jquery/dist/jquery.js');
var bootstrap = require('patternfly/components/bootstrap/dist/js/bootstrap.js');
var bootstrapSelect = require('patternfly/components/bootstrap-select/dist/js/bootstrap-select.js');
var jsonEditor = require('json-editor');
var patternfly = require('patternfly/dist/js/patternfly.js');
var angular = require('angular');
var uiBootstrap = require('angular-ui-bootstrap/ui-bootstrap.js');
var uiBootstrapTpls = require('angular-ui-bootstrap/ui-bootstrap-tpls.js');
var uiSelect = require('ui-select/dist/select.js');
var ngClipboard = require('angular-clipboard');
var ngResource = require('angular-resource');
var ngSortable = require('ng-sortable/dist/ng-sortable.js');
var d3 = require('d3');
global.c3 = require('patternfly/components/c3/c3.js');
var ngXEditable = require('angular-xeditable-npm/dist/js/xeditable.js');
var ngSanitize = require('angular-sanitize');
var ngAnimate = require('angular-animate');
global.Logger = require('js-logger');
global._ = require('lodash');
var ngRoute = require('angular-route');
var URI = require('urijs');
var ngSchemaForm = require('angular-schema-form');
global.SwaggerUi = require('swagger-ui-browserify');
global.SwaggerClient = require('swagger-client');
global.SwaggerAuthorizations = require('swagger-ui-browserify/node_modules/swagger-ui/dist/lib/swagger-oauth.js');

var sugar = require('sugar');
var ngFileUpload = require('ng-file-upload');
