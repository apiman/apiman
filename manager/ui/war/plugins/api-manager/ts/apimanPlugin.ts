// // Browserify imports
import * as $ from 'jquery'
// @ts-ignore
window.jQuery = $;
// @ts-ignore
window.$ = $;
import 'bootstrap/dist/js/bootstrap.js';
import 'bootstrap-select/dist/js/bootstrap-select.js';
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

import 'sugar';
import 'ng-file-upload';
import 'moment';
import 'moment/min/locales';
import * as dayjs from 'dayjs';
const relativeTime = require('dayjs/plugin/relativeTime');
dayjs.extend(relativeTime)
// Markdown editor and code highlight plugin (for editor)
import '@toast-ui/editor';
import '@toast-ui/editor-plugin-code-syntax-highlight/dist/toastui-editor-plugin-code-syntax-highlight-all';
import 'prismjs';

// End browserify imports

import angular = require("angular");
import {ApimanGlobals} from "./apimanGlobals";
import {IModule} from "angular";

import './rpc.ts'
import './filters.ts'
import './logger.ts'
import './configuration.ts'
import './translation.ts'
import './lifecycle.ts'
import './currentuser.ts'
import './modals.ts'

/** CSS Imports **/
import 'bootstrap-select/dist/css/bootstrap-select.css'
import 'patternfly/dist/css/patternfly.css'
import 'patternfly/dist/css/patternfly-additions.css'
import 'ng-sortable/dist/ng-sortable.css'
import 'c3/c3.css'
import 'angular-xeditable-npm/dist/css/xeditable.css'
import 'angular-scrollable-table/scrollable-table.css'
import 'angular-ui-bootstrap/ui-bootstrap-csp.css'
import 'ui-select/dist/select.css'
import 'select2/select2.css'
import 'prismjs/themes/prism.css'
import '@toast-ui/editor-plugin-code-syntax-highlight/dist/toastui-editor-plugin-code-syntax-highlight.css'
import '@toast-ui/editor/dist/toastui-editor.css'
import 'tui-color-picker/dist/tui-color-picker.css'
import '@toast-ui/editor-plugin-color-syntax/dist/toastui-editor-plugin-color-syntax.css'

/** Load these last to ensure they don't get overriden **/
import '../css/apiman.css';
import '../css/apiman-responsive.css';

export const _module: IModule = angular.module(ApimanGlobals.pluginName, [
    'angular-clipboard',
    'ngRoute',
    'ngSanitize',
    'ui.bootstrap',
    'ui.select',
    'ui.sortable',
    'xeditable',
    'ngFileUpload',
    'ngAnimate',
    'scrollable-table',
    // Apiman
    'ApimanRPC',
    'ApimanFilters',
    'ApimanLogger',
    'ApimanConfiguration',
    'ApimanTranslation',
    'ApimanPageLifecycle',
    'ApimanCurrentUser',
    'ApimanModals'
]);


_module.config([
    '$locationProvider',
    '$routeProvider',
    'uiSelectConfig', ($locationProvider,
                       $routeProvider,
                       uiSelectConfig) => {
        var path = 'plugins/api-manager/html/';
        var prefix = '/api-manager';

        uiSelectConfig.theme = 'select2';
        uiSelectConfig.searchEnabled = false;

        // Define Routes

        $routeProvider
            .when(prefix + '/', {
                templateUrl: path + 'dash.html'
            })
            .when(prefix + '/about', {
                templateUrl: path + 'about.html'
            })
            .when(prefix + '/profile', {
                templateUrl: path + 'user/user-profile.html'
            })
            .when(prefix + '/admin/gateways', {
                templateUrl: path + 'admin/admin-gateways.html'
            })
            .when(prefix + '/admin/plugins', {
                templateUrl: path + 'admin/admin-plugins.html'
            })
            .when(prefix + '/admin/policyDefs', {
                templateUrl: path + 'admin/admin-policyDefs.html'
            })
            .when(prefix + '/admin/roles', {
                templateUrl: path + 'admin/admin-roles.html'
            })
            .when(prefix + '/admin/export', {
                templateUrl: path + 'admin/admin-export.html'
            })
            .when(prefix + '/admin/gateways/:gateway', {
                templateUrl: path + 'forms/edit-gateway.html'
            })
            .when(prefix + '/admin/plugins/:plugin', {
                templateUrl: path + 'forms/edit-plugin.html'
            })
            .when(prefix + '/admin/policyDefs/:policyDef', {
                templateUrl: path + 'forms/edit-policyDef.html'
            })
            .when(prefix + '/admin/roles/:role', {
                templateUrl: path + 'forms/edit-role.html'
            })
            .when(prefix + '/notifications', {
                templateUrl: path + 'notifications/notifications-dash.html'
            })
            .when(prefix + '/orgs/:org/:type/:id/:ver/policies/:policy', {
                templateUrl: path + 'forms/edit-policy.html'
            })
            .when(prefix + '/orgs/:org/:type/:id/:ver/new-policy', {
                templateUrl: path + 'forms/new-policy.html'
            })
            .when(prefix + '/orgs/:org/clients/:client', {
                templateUrl: path + 'client/client.html'
            })
            .when(prefix + '/orgs/:org/clients/:client/:version', {
                templateUrl: path + 'client/client-overview.html'
            })
            .when(prefix + '/orgs/:org/clients/:client/:version/contracts', {
                templateUrl: path + 'client/client-contracts.html'
            })
            .when(prefix + '/orgs/:org/clients/:client/:version/apis', {
                templateUrl: path + 'client/client-apis.html'
            })
            .when(prefix + '/orgs/:org/clients/:client/:version/metrics', {
                templateUrl: path + 'client/client-metrics.html'
            })
            .when(prefix + '/orgs/:org/clients/:client/:version/policies', {
                templateUrl: path + 'client/client-policies.html'
            })
            .when(prefix + '/orgs/:org/clients/:client/:version/activity', {
                templateUrl: path + 'client/client-activity.html'
            })
            .when(prefix + '/orgs/:org/clients/:client/:version/new-version', {
                templateUrl: path + 'forms/new-clientversion.html'
            })
            .when(prefix + '/orgs/:org/plans/:plan', {
                templateUrl: path + 'plan/plan.html'
            })
            .when(prefix + '/orgs/:org/plans/:plan/:version', {
                templateUrl: path + 'plan/plan-overview.html'
            })
            .when(prefix + '/orgs/:org/plans/:plan/:version/policies', {
                templateUrl: path + 'plan/plan-policies.html'
            })
            .when(prefix + '/orgs/:org/plans/:plan/:version/activity', {
                templateUrl: path + 'plan/plan-activity.html'
            })
            .when(prefix + '/orgs/:org/plans/:plan/:version/new-version', {
                templateUrl: path + 'forms/new-planversion.html'
            })
            .when(prefix + '/orgs/:org/apis/:api', {
                templateUrl: path + 'api/api.html'
            })
            .when(prefix + '/orgs/:org/apis/:api/:version', {
                templateUrl: path + 'api/api-overview.html'
            })
            .when(prefix + '/orgs/:org/apis/:api/:version/impl', {
                templateUrl: path + 'api/api-impl.html'
            })
            .when(prefix + '/orgs/:org/apis/:api/:version/def', {
                templateUrl: path + 'api/api-def.html'
            })
            .when(prefix + '/orgs/:org/apis/:api/:version/plans', {
                templateUrl: path + 'api/api-plans.html'
            })
            .when(prefix + '/orgs/:org/apis/:api/:version/devportal', {
                templateUrl: path + 'api/api-devportal.html'
            })
            .when(prefix + '/orgs/:org/apis/:api/:version/policies', {
                templateUrl: path + 'api/api-policies.html'
            })
            .when(prefix + '/orgs/:org/apis/:api/:version/endpoint', {
                templateUrl: path + 'api/api-endpoint.html'
            })
            .when(prefix + '/orgs/:org/apis/:api/:version/contracts', {
                templateUrl: path + 'api/api-contracts.html'
            })
            .when(prefix + '/orgs/:org/apis/:api/:version/metrics', {
                templateUrl: path + 'api/api-metrics.html'
            })
            .when(prefix + '/orgs/:org/apis/:api/:version/activity', {
                templateUrl: path + 'api/api-activity.html'
            })
            .when(prefix + '/orgs/:org/apis/:api/:version/new-version', {
                templateUrl: path + 'forms/new-apiversion.html'
            })
            .when(prefix + '/orgs/:org/import/apis', {
                templateUrl: path + 'api/import-apis.html'
            })
            .when(prefix + '/catalog/api-catalog', {
                templateUrl: path + 'catalog/api-catalog.html'
            })
            .when(prefix + '/catalog/api-catalog/:name/def', {
                templateUrl: path + 'catalog/api-catalog-def.html'
            })
            .when(prefix + '/browse/orgs', {
                templateUrl: path + 'consumer/consumer-orgs.html'
            })
            .when(prefix + '/browse/apis', {
                templateUrl: path + 'consumer/consumer-apis.html'
            })
            .when(prefix + '/browse/orgs/:org', {
                templateUrl: path + 'consumer/consumer-org.html'
            })
            .when(prefix + '/browse/orgs/:org/:api', {
                templateUrl: path + 'consumer/consumer-api-redirect.html'
            })
            .when(prefix + '/browse/orgs/:org/:api/:version', {
                templateUrl: path + 'consumer/consumer-api.html'
            })
            .when(prefix + '/browse/orgs/:org/:api/:version/def', {
                templateUrl: path + 'consumer/consumer-api-def.html'
            })
            .when(prefix + '/new-client', {
                templateUrl: path + 'forms/new-client.html'
            })
            .when(prefix + '/new-contract', {
                templateUrl: path + 'forms/new-contract.html'
            })
            .when(prefix + '/new-gateway', {
                templateUrl: path + 'forms/new-gateway.html'
            })
            .when(prefix + '/new-org', {
                templateUrl: path + 'forms/new-org.html'
            })
            .when(prefix + '/new-plan', {
                templateUrl: path + 'forms/new-plan.html'
            })
            .when(prefix + '/new-plugin', {
                templateUrl: path + 'forms/new-plugin.html'
            })
            .when(prefix + '/new-role', {
                templateUrl: path + 'forms/new-role.html'
            })
            .when(prefix + '/new-api', {
                templateUrl: path + 'forms/new-api.html'
            })
            .when(prefix + '/import-policyDefs', {
                templateUrl: path + 'forms/import-policyDefs.html'
            })
            .when(prefix + '/orgs/:org', {
                templateUrl: path + 'org/org.html'
            })
            .when(prefix + '/orgs/:org/plans', {
                templateUrl: path + 'org/org-plans.html'
            })
            .when(prefix + '/orgs/:org/apis', {
                templateUrl: path + 'org/org-apis.html'
            })
            .when(prefix + '/orgs/:org/clients', {
                templateUrl: path + 'org/org-clients.html'
            })
            .when(prefix + '/orgs/:org/members', {
                templateUrl: path + 'org/org-members.html'
            })
            .when(prefix + '/orgs/:org/manage-members', {
                templateUrl: path + 'org/org-manage-members.html'
            })
            .when(prefix + '/orgs/:org/activity', {
                templateUrl: path + 'org/org-activity.html'
            })
            .when(prefix + '/orgs/:org/new-member', {
                templateUrl: path + 'org/org-new-member.html'
            })
            .when(prefix + '/users/:user', {
                templateUrl: path + 'user/user.html'
            })
            .when(prefix + '/users/:user/activity', {
                templateUrl: path + 'user/user-activity.html'
            })
            .when(prefix + '/users/:user/clients', {
                templateUrl: path + 'user/user-clients.html'
            })
            .when(prefix + '/users/:user/orgs', {
                templateUrl: path + 'user/user-orgs.html'
            })
            .when(prefix + '/users/:user/apis', {
                templateUrl: path + 'user/user-apis.html'
            })
            .when(prefix + '/errors/invalid_server', {
                templateUrl: path + 'errors/invalid_server.html'
            })
            .when(prefix + '/errors/400', {
                templateUrl: path + 'errors/400.html'
            })
            .when(prefix + '/errors/403', {
                templateUrl: path + 'errors/403.html'
            })
            .when(prefix + '/errors/404', {
                templateUrl: path + 'errors/404.html'
            })
            .when(prefix + '/errors/409', {
                templateUrl: path + 'errors/409.html'
            })
            .when(prefix + '/errors/409-8002', {
                templateUrl: path + 'errors/409-8002.html'
            })
            .when(prefix + '/errors/500', {
                templateUrl: path + 'errors/500.html'
            })
            .when(prefix + '/def', {
                templateUrl: path + 'rest-documentation.html'
            })
            .otherwise({redirectTo: prefix + '/'});

        $locationProvider.html5Mode(true);
    }]);

_module.factory('authInterceptor',
    ['$q', '$timeout', 'Configuration', 'Logger',
        ($q, $timeout, Configuration, Logger) => {
            var refreshBearerToken = function () {
                Logger.info('Refreshing bearer token now.');
                // Note: we need to use jquery directly for this call, otherwise we will have
                // a circular dependency in angular.
                $.get('rest/tokenRefresh', function (reply) {
                    Logger.info('Bearer token successfully refreshed: {0}', reply);
                    Configuration.api.auth.bearerToken.token = reply.token;
                    var refreshPeriod = reply.refreshPeriod;
                    if (!refreshPeriod || refreshPeriod < 1) {
                        Logger.info('Refresh period was invalid! (using 60s)');
                        refreshPeriod = 60;
                    }
                    $timeout(refreshBearerToken, refreshPeriod * 1000);
                }).fail(function (error) {
                    Logger.error('Failed to refresh bearer token: {0}', error);
                });
            };
            if (Configuration.api.auth.type == 'bearerToken') {
                var refreshPeriod = Configuration.api.auth.bearerToken.refreshPeriod;
                $timeout(refreshBearerToken, refreshPeriod * 1000);
            }
            var requestInterceptor = {
                request: function (config) {
                    var authHeader = Configuration.getAuthorizationHeader();
                    if (authHeader) {
                        config.headers.Authorization = authHeader;
                    }
                    return config;
                }
            };
            return requestInterceptor;
        }]);


_module.config(['$httpProvider', function ($httpProvider) {
    $httpProvider.interceptors.push('authInterceptor');
}]);

_module.run([
    '$rootScope',
    'SystemSvcs',
    'Configuration',
    '$location', ($rootScope,
                  SystemSvcs,
                  Configuration,
                  $location) => {

        $rootScope.isDirty = false;

        $rootScope.$on('$locationChangeStart', function (event, newUrl, oldUrl) {
            if ($rootScope.isDirty) {
                if (confirm('You have unsaved changes. Are you sure you would like to navigate away from this page? You will lose these changes.') != true) {
                    event.preventDefault();
                }
            }
        });

        $rootScope.pluginName = ApimanGlobals.pluginName;
    }]);

// Load the configuration jsonp script
$.getScript('apiman/config.js')
    .done((script, textStatus) => {
        ApimanGlobals.Logger.info('Loaded the config.js config!');
    })
    .fail((response) => {
        ApimanGlobals.Logger.debug('Error fetching configuration: ', response);
    })
    .always(() => {
        // Load the i18n jsonp script
        $.getScript('apiman/translations.js').done((script, textStatus) => {
            ApimanGlobals.Logger.info('Loaded the translations.js bundle!');

            angular.element(document).ready(function () {
                console.log("Angular bootstrap running");
                angular.bootstrap(document, ['api-manager']);
            });
        }).fail((response) => {
            ApimanGlobals.Logger.debug('Error fetching translations: ', response);
        });
    });


/** Imitating the old way of magically including everything (not best practice now, but
 * don't have time to manually do the includes in each file yet **/
// import "./filters.ts"
import "./policies.ts"
import "./forms/new-org.ts"
import "./forms/new-gateway.ts"
import "./forms/edit-role.ts"
import "./forms/new-planversion.ts"
import "./forms/new-plugin.ts"
import "./forms/new-clientversion.ts"
import "./forms/new-contract.ts"
import "./forms/edit-plugin.ts"
import "./forms/import-policyDefs.ts"
import "./forms/new-role.ts"
import "./forms/new-apiversion.ts"
import "./forms/new-policy.ts"
import "./forms/new-api.ts"
import "./forms/new-client.ts"
import "./forms/edit-policy.ts"
import "./forms/edit-gateway.ts"
import "./forms/edit-policyDef.ts"
import "./forms/new-plan.ts"
import "./errors.ts"
import "./notification/notification.service.ts"
import "./notification/notification.ts"
import "./notification/notificationmapper.service.ts"
import "./org/org-plans.ts"
import "./org/org-apis.ts"
import "./org/org-members.ts"
import "./org/org-sidebar.ts"
import "./org/org-new-member.ts"
import "./org/org-clients.ts"
import "./org/org.ts"
import "./org/org-activity.ts"
import "./org/org-manage-members.ts"
import "./plan/plan-activity.ts"
import "./plan/plan-overview.ts"
import "./plan/plan.ts"
import "./plan/plan-policies.ts"
import "./catalog/api-catalog.ts"
// import "./modals.ts"
// import "./lifecycle.ts"
// import "./translation.ts"
import "./admin/admin-gateways.ts"
import "./admin/admin-policyDefs.ts"
import "./admin/admin-roles.ts"
import "./admin/admin-export.ts"
import "./admin/admin-plugins.ts"
// import "./rpc.ts"
// import "./directives.ts"
import "./user/user-profile.ts"
import "./user/user-apis.ts"
import "./user/user-orgs.ts"
import "./user/user-activity.ts"
import "./user/user.ts"
import "./user/user-clients.ts"
import "./dash.ts"
import "./about.ts"
// import "./logger.ts"
// import "./currentuser.ts"
import "./navbar.ts"
import "./rest-documentation.ts"
import "./model/checklist.model.ts"
import "./model/blob.model.ts"
import "./model/notifications.model.ts"
import "./model/api.model.ts"
import "./model/contract.model.ts"
import "./apimanGlobals.ts"
import "./api/api-policies.ts"
import "./api/api-devportal.ts"
import "./api/api-overview.ts"
import "./api/api-endpoint.ts"
import "./api/api.ts"
import "./api/api-activity.ts"
import "./api/api-plans.ts"
import "./api/api-contracts.ts"
import "./api/api-def.ts"
import "./api/import-apis.ts"
import "./api/api-metrics.ts"
import "./api/api-impl.ts"
import "./consumer/consumer-orgs.ts"
import "./consumer/consumer-api.ts"
import "./consumer/consumer-org.ts"
import "./consumer/consumer-apis.ts"
import "./services.ts"
import "./client/client-overview.ts"
import "./client/client-contracts.ts"
import "./client/client-policies.ts"
import "./client/client-metrics.ts"
import "./client/client-apis.ts"
import "./client/client.ts"
import "./client/client-activity.ts"
// import "./configuration.ts"
import "./sidebar.ts"