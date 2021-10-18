import angular = require("angular");
import {ApimanGlobals} from "./apimanGlobals";

export var pageTitles = {
        "page.title.admin-gateways": "Apiman - Admin - Gateways",
        "page.title.admin-plugins": "Apiman - Admin - Plugins",
        "page.title.admin-roles": "Apiman - Admin - Roles",
        "page.title.admin-policyDefs": "Apiman - Admin - Policy Definitions",
        "page.title.admin-export": "Apiman - Admin - Export/Import",
        "page.title.api-catalog": "Apiman - API Catalog",
        "page.title.api-catalog-def": "Apiman - API Definition",
        "page.title.client-activity": "Apiman - {0} (Change Log)",
        "page.title.client-apis": "Apiman - {0} (APIs)",
        "page.title.client-contracts": "Apiman - {0} (Contracts)",
        "page.title.client-metrics": "Apiman - {0} (Metrics)",
        "page.title.client-overview": "Apiman - {0} (Overview)",
        "page.title.client-policies": "Apiman - {0} (Policies)",
        "page.title.consumer-org": "Apiman - Organization {0}",
        "page.title.consumer-orgs": "Apiman - Organizations",
        "page.title.consumer-api": "Apiman - API {0}",
        "page.title.consumer-api-def": "Apiman - API {0} - Definition",
        "page.title.consumer-apis": "Apiman - APIs",
        "page.title.dashboard": "Apiman - Home",
        "page.title.about": "Apiman - About",
        "page.title.edit-gateway": "Apiman - Edit Gateway",
        "page.title.edit-policy": "Apiman - Edit Policy",
        "page.title.edit-policyDef": "Apiman - Edit Policy Definition",
        "page.title.edit-role": "Apiman - Edit Role",
        "page.title.import-policyDefs": "Apiman - Import Policy Definition(s)",
        "page.title.import-apis": "Apiman - Import API(s)",
        "page.title.new-client": "Apiman - New Client App",
        "page.title.new-client-version": "Apiman - New Client App Version",
        "page.title.new-contract": "Apiman - New Contract",
        "page.title.new-gateway": "Apiman - New Gateway",
        "page.title.new-member": "Apiman - Add Member",
        "page.title.new-org": "Apiman - New Organization",
        "page.title.new-plan": "Apiman - New Plan",
        "page.title.new-plan-version": "Apiman - New Plan Version",
        "page.title.new-plugin": "Apiman - Add Plugin",
        "page.title.new-policy": "Apiman - Add Policy",
        "page.title.new-role": "Apiman - New Role",
        "page.title.new-api": "Apiman - New API",
        "page.title.new-api-version": "Apiman - New API Version",
        "page.title.manager-rest-def": "Apiman - REST API",
        "page.title.org-activity": "Apiman - {0} (Change Log)",
        "page.title.org-clients": "Apiman - {0} (Client Apps)",
        "page.title.org-manage-members": "Apiman - {0} (Manage Members)",
        "page.title.org-members": "Apiman - {0} (Members)",
        "page.title.org-plans": "Apiman - {0} (Plans)",
        "page.title.org-apis": "Apiman - {0} (APIs)",
        "page.title.plan-activity": "Apiman - {0} (Change Log)",
        "page.title.plan-overview": "Apiman - {0} (Overview)",
        "page.title.plan-policies": "Apiman - {0} (Policies)",
        "page.title.plugin-details": "Apiman - Plugin Details",
        "page.title.policy-defs": "Apiman - Admin - Policy Definitions",
        "page.title.api-activity": "Apiman - {0} (Change Log)",
        "page.title.api-contracts": "Apiman - {0} (Contracts)",
        "page.title.api-endpoint": "Apiman - {0} (Endpoint)",
        "page.title.api-metrics": "Apiman - {0} (Metrics)",
        "page.title.api-impl": "Apiman - {0} (Implementation)",
        "page.title.api-def": "Apiman - {0} (Definition)",
        "page.title.api-overview": "Apiman - {0} (Overview)",
        "page.title.api-plans": "Apiman - {0} (Plans)",
        "page.title.api-devportal": "Apiman - {0} (v. {1}) (Dev Portal)",
        "page.title.api-policies": "Apiman - {0} (Policies)",
        "page.title.user-activity": "Apiman - {0} (Change Log)",
        "page.title.user-clients": "Apiman - {0} (Client Apps)",
        "page.title.user-orgs": "Apiman - {0} (Organizations)",
        "page.title.user-profile": "Apiman - User Profile",
        "page.title.user-apis": "Apiman - {0} (APIs)",
        "page.title.error": "Apiman - {0} Error", 
        "page.title.notification-dash": "Apiman - {0} (Notifications Dashboard)"
    };

    const formatMessage = function(theArgs) {
        const now = new Date();
        var msg = theArgs[0];
        if (theArgs.length > 1) {
            for (var i = 1; i < theArgs.length; i++) {
                msg = msg.replace('{'+(i-1)+'}', theArgs[i]);
            }
        }
        return msg;
    };

    const _module = angular.module("ApimanPageLifecycle", []);

    _module.factory('PageLifecycle',
        ['$q', '$timeout', 'Logger', '$rootScope', '$location', 'CurrentUserSvcs', 'Configuration', 'TranslationSvc', '$window', 'CurrentUser',
        ($q, $timeout, Logger, $rootScope, $location, CurrentUserSvcs, Configuration, TranslationSvc, $window, CurrentUser) => {
            var header = 'community';
            if (Configuration.ui && Configuration.ui.header) {
                header = Configuration.ui.header;
            }
            if (header == 'apiman') {
                header = 'community';
            }
            $rootScope.headerInclude = 'plugins/api-manager/html/headers/' + header + '.include';
            console.log('Using header: ' + $rootScope.headerInclude);

            let redirectWrongPermission = function () {
                Logger.info('Detected a 404 error.');
                $location.url(ApimanGlobals.pluginName + '/errors/404').replace();
                return;
            };

            var processCurrentUser = function(currentUser) {
                $rootScope.currentUser = currentUser;
                var permissions = {};
                var memberships = {};
                if (currentUser.permissions) {
                    for (var i = 0; i < currentUser.permissions.length; i++) {
                        var perm = currentUser.permissions[i];
                        var permid = perm.organizationId + '||' + perm.name;
                        permissions[permid] = true;
                        memberships[perm.organizationId] = true;
                    }
                }
                Logger.info('Updating permissions now {0}', permissions);
                $rootScope.permissions = permissions;
                $rootScope.memberships = memberships;
                $rootScope.isAdmin = currentUser.admin;
            };
            var handleError = function(error) {
                $rootScope.pageState = 'error';
                $rootScope.pageError = error;
                if (error.status == 400) {
                    Logger.info('Detected an error {0}, redirecting to 400.', error.status);
                    $location.url(ApimanGlobals.pluginName + '/errors/400').replace();
                } else if (error.status == 401) {
                    Logger.info('Detected an error 401, reloading the page.');
                    $window.location.reload();
                } else if (error.status == 403) {
                    Logger.info('Detected an error {0}, redirecting to 403.', error.status);
                    $location.url(ApimanGlobals.pluginName + '/errors/403').replace();
                } else if (error.status == 404) {
                    Logger.info('Detected an error {0}, redirecting to 404.', error.status);
                    $location.url(ApimanGlobals.pluginName + '/errors/404').replace();
                } else if (error.status == 409) {
                    Logger.info('Detected an error {0}, redirecting to 409.', error.status);
                    var errorUri = '409';
                    Logger.info('=====> {0}', error);
                    Logger.info('=====> error code: {0}', error.data.errorCode);
                    if (error.data.errorCode && error.data.errorCode == 8002) {
                        errorUri = '409-8002';
                    }
                    $location.url(ApimanGlobals.pluginName + '/errors/' + errorUri).replace();
                } else if (error.status == 0) {
                    Logger.info('Detected an error {0}, redirecting to CORS error page.', error.status);
                    $location.url(ApimanGlobals.pluginName + '/errors/invalid_server').replace();
                } else {
                    // TODO: if the error data starts with <html> then redirect to a more generic html-into-div based error page
                    Logger.info('Detected an error {0}, redirecting to 500.', error.status);
                    $location.url(ApimanGlobals.pluginName + '/errors/500').replace();
                }
            };
            return {
                setPageTitle: function(titleKey, params) {
                    let key = 'page.title.' + titleKey;
                    let pattern = pageTitles[key];
                    pattern = TranslationSvc.translate(key, pattern);
                    if (pattern) {
                        let args = [];
                        args.push(pattern);
                        args = args.concat(params);
                        document.title = formatMessage(args);
                    } else {
                        document.title = pattern;
                    }
                },
                handleError: handleError,
                forwardTo: function() {
                    var path = '/' + ApimanGlobals.pluginName + formatMessage(arguments);
                    Logger.info('Forwarding to page {0}', path);
                    $location.url(path).replace();
                },
                redirectTo: function() {
                    var path = '/' + ApimanGlobals.pluginName + formatMessage(arguments);
                    Logger.info('Redirecting to page {0}', path);
                    $location.url(path);
                },
                loadPage: function(pageName, requiredPermission, pageData, $scope, handler) {
                    Logger.log("|{0}| >> Loading page.", pageName);
                    $rootScope.pageState = 'loading';
                    $rootScope.isDirty = false;

                    var currentUser = $q(function(resolve, reject) {
                        if ($rootScope.currentUser) {
                            Logger.log("|{0}| >> Using cached current user from $rootScope.", pageName);
                            resolve($rootScope.currentUser);
                        } else {
                            return CurrentUserSvcs.get({ what: 'info' }, function(currentUser) {
                                processCurrentUser(currentUser);
                                resolve(currentUser);
                            }, reject);
                        }
                    });

                    // Now resolve the data as a promise (wait for all data packets to be fetched)
                    return currentUser.then(function (){
                        return $q.all(pageData);
                    }).then(function(data) {
                        // Make sure the user has permission to view this page.
                        if ( (requiredPermission && requiredPermission == 'orgView' && !CurrentUser.isMember($scope.organizationId)) ||
                             ( requiredPermission && requiredPermission != 'orgView' && !CurrentUser.hasPermission($scope.organizationId, requiredPermission)) )
                        {
                            redirectWrongPermission();
                        }

                        // Now process all the data packets and bind them to the $scope.
                        var count = 0;
                        angular.forEach(data, function(value, key) {
                            Logger.info("|{0}| >> Binding {1} to $scope.", pageName, key);
                            this[key] = value;
                            count++;
                        }, $scope);

                        $timeout(function() {
                            $rootScope.pageState = 'loaded';
                            Logger.log("|{0}| >> Page successfully loaded: {1} data packets loaded", pageName, count);
                            if (handler) {
                                $timeout(function() {
                                    Logger.log("|{0}| >> Calling Page onLoaded handler", pageName);
                                    handler();
                                }, 20);
                            }
                        }, 50);
                    }, function(reason) {
                        Logger.error("|{0}| >> Page load failed: {1}", pageName, reason);
                        handleError(reason);
                    });
                },
                loadErrorPage: function(pageName, $scope, handler) {
                    Logger.log("|{0}| >> Loading error page.", pageName);
                    $rootScope.pageState = 'loading';

                    // Nothing to do asynchronously for the error pages!
                    $rootScope.pageState = 'loaded';
                    if (handler) {
                        handler();
                    }
                    Logger.log("|{0}| >> Error page successfully loaded", pageName);
                }
            }
        }]);

