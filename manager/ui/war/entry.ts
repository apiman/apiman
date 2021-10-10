// Browserify Entry Point
// This file includes a list of dependencies we want to use on the client, but must be
// installed server-side with npm. Browserify will check all requires(), subsequently
// bundling them into a single file, which we can then use on the client.

import * as $ from 'jquery';
// @ts-ignore
window.jQuery = $;
// @ts-ignore
window.$ = $;
import 'bootstrap/dist/js/bootstrap.js';
import 'bootstrap-select/dist/js/bootstrap-select.js';
import 'json-editor';
import 'patternfly/dist/js/patternfly.js';
import 'angular';
import 'angular-ui-bootstrap/ui-bootstrap.js';
import 'angular-ui-bootstrap/ui-bootstrap-tpls.js';
import 'ui-select/dist/select.js';
import 'angular-clipboard';
import 'angular-resource';
import 'ng-sortable/dist/ng-sortable.js';
import 'd3';
import 'c3/c3.js';
import 'angular-xeditable-npm/dist/js/xeditable.js';
import 'angular-sanitize';
import 'angular-animate';
import 'js-logger';
import 'lodash';
import 'angular-route';
import 'urijs';
import 'angular-schema-form';
import 'angular-scrollable-table/angular-scrollable-table.js';
import 'swagger-ui-dist/swagger-ui-bundle.js';
import 'swagger-client';

import 'sugar';
import 'ng-file-upload';
import 'moment/min/moment.min.js';
// Markdown editor and code highlight plugin (for editor)
import '@toast-ui/editor';
import '@toast-ui/editor-plugin-code-syntax-highlight/dist/toastui-editor-plugin-code-syntax-highlight-all';
import 'prismjs';
import '@toast-ui/editor-plugin-color-syntax';