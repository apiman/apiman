/// <reference path="../../includes.ts"/>
module ApimanPageLifecycle {

    export var pageTitles = {
        "page.title.admin-gateways": "apiman - Admin - Gateways",
        "page.title.admin-plugins": "apiman - Admin - Plugins",
        "page.title.admin-roles": "apiman - Admin - Roles",
        "page.title.admin-policyDefs": "apiman - Admin - Policy Definitions",
        "page.title.admin-export": "apiman - Admin - Export/Import",
        "page.title.api-catalog": "apiman - API Catalog",
        "page.title.api-catalog-def": "apiman - API Definition",
        "page.title.client-activity": "apiman - {0} (Change Log)",
        "page.title.client-apis": "apiman - {0} (APIs)",
        "page.title.client-contracts": "apiman - {0} (Contracts)",
        "page.title.client-metrics": "apiman - {0} (Metrics)",
        "page.title.client-overview": "apiman - {0} (Overview)",
        "page.title.client-policies": "apiman - {0} (Policies)",
        "page.title.consumer-org": "apiman - Organization {0}",
        "page.title.consumer-orgs": "apiman - Organizations",
        "page.title.consumer-api": "apiman - API {0}",
        "page.title.consumer-api-def": "apiman - API {0} - Definition",
        "page.title.consumer-apis": "apiman - APIs",
        "page.title.dashboard": "apiman - Home",
        "page.title.about": "apiman - About",
        "page.title.edit-gateway": "apiman - Edit Gateway",
        "page.title.edit-policy": "apiman - Edit Policy",
        "page.title.edit-policyDef": "apiman - Edit Policy Definition",
        "page.title.edit-role": "apiman - Edit Role",
        "page.title.import-policyDefs": "apiman - Import Policy Definition(s)",
        "page.title.import-apis": "apiman - Import API(s)",
        "page.title.new-client": "apiman - New Client App",
        "page.title.new-client-version": "apiman - New Client App Version",
        "page.title.new-contract": "apiman - New Contract",
        "page.title.new-gateway": "apiman - New Gateway",
        "page.title.new-member": "apiman - Add Member",
        "page.title.new-org": "apiman - New Organization",
        "page.title.new-plan": "apiman - New Plan",
        "page.title.new-plan-version": "apiman - New Plan Version",
        "page.title.new-plugin": "apiman - Add Plugin",
        "page.title.new-policy": "apiman - Add Policy",
        "page.title.new-role": "apiman - New Role",
        "page.title.new-api": "apiman - New API",
        "page.title.new-api-version": "apiman - New API Version",
        "page.title.org-activity": "apiman - {0} (Change Log)",
        "page.title.org-clients": "apiman - {0} (Client Apps)",
        "page.title.org-manage-members": "apiman - {0} (Manage Members)",
        "page.title.org-members": "apiman - {0} (Members)",
        "page.title.org-plans": "apiman - {0} (Plans)",
        "page.title.org-apis": "apiman - {0} (APIs)",
        "page.title.plan-activity": "apiman - {0} (Change Log)",
        "page.title.plan-overview": "apiman - {0} (Overview)",
        "page.title.plan-policies": "apiman - {0} (Policies)",
        "page.title.plugin-details": "apiman - Plugin Details",
        "page.title.policy-defs": "apiman - Admin - Policy Definitions",
        "page.title.api-activity": "apiman - {0} (Change Log)",
        "page.title.api-contracts": "apiman - {0} (Contracts)",
        "page.title.api-endpoint": "apiman - {0} (Endpoint)",
        "page.title.api-metrics": "apiman - {0} (Metrics)",
        "page.title.api-impl": "apiman - {0} (Implementation)",
        "page.title.api-def": "apiman - {0} (Definition)",
        "page.title.api-overview": "apiman - {0} (Overview)",
        "page.title.api-plans": "apiman - {0} (Plans)",
        "page.title.api-policies": "apiman - {0} (Policies)",
        "page.title.user-activity": "apiman - {0} (Change Log)",
        "page.title.user-clients": "apiman - {0} (Client Apps)",
        "page.title.user-orgs": "apiman - {0} (Organizations)",
        "page.title.user-profile": "apiman - User Profile",
        "page.title.user-apis": "apiman - {0} (APIs)",
        "page.title.error": "apiman - {0} Error",
    };
    
    var formatMessage = function(theArgs) {
        var now = new Date();
        var msg = theArgs[0];
        if (theArgs.length > 1) {
            for (var i = 1; i < theArgs.length; i++) {
                msg = msg.replace('{'+(i-1)+'}', theArgs[i]);
            }
        }
        return msg;
    };

    export var _module = angular.module("ApimanPageLifecycle", []);

    export var PageLifecycle = _module.factory('PageLifecycle', 
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
            };
            var handleError = function(error) {
                $rootScope.pageState = 'error';
                $rootScope.pageError = error;
                if (error.status == 400) {
                    Logger.info('Detected an error {0}, redirecting to 400.', error.status);
                    $location.url(Apiman.pluginName + '/errors/400').replace();
                } else if (error.status == 401) {
                    Logger.info('Detected an error 401, reloading the page.');
                    $window.location.reload();
                } else if (error.status == 403) {
                    Logger.info('Detected an error {0}, redirecting to 403.', error.status);
                    $location.url(Apiman.pluginName + '/errors/403').replace();
                } else if (error.status == 404) {
                    Logger.info('Detected an error {0}, redirecting to 404.', error.status);
                    $location.url(Apiman.pluginName + '/errors/404').replace();
                } else if (error.status == 409) {
                    Logger.info('Detected an error {0}, redirecting to 409.', error.status);
                    var errorUri = '409';
                    Logger.info('=====> {0}', error);
                    Logger.info('=====> error code: {0}', error.data.errorCode);
                    if (error.data.errorCode && error.data.errorCode == 8002) {
                        errorUri = '409-8002';
                    }
                    $location.url(Apiman.pluginName + '/errors/' + errorUri).replace();
                } else if (error.status == 0) {
                    Logger.info('Detected an error {0}, redirecting to CORS error page.', error.status);
                    $location.url(Apiman.pluginName + '/errors/invalid_server').replace();
                } else {
                    // TODO: if the error data starts with <html> then redirect to a more generic html-into-div based error page
                    Logger.info('Detected an error {0}, redirecting to 500.', error.status);
                    $location.url(Apiman.pluginName + '/errors/500').replace();
                }
            };
            return {
                setPageTitle: function(titleKey, params) {
                    var key = 'page.title.' + titleKey;
                    var pattern = pageTitles[key];
                    pattern = TranslationSvc.translate(key, pattern);
                    if (pattern) {
                        var args = [];
                        args.push(pattern);
                        args = args.concat(params);
                        var title = formatMessage(args);
                        document.title = title;
                    } else {
                        document.title = pattern;
                    }
                },
                handleError: handleError,
                forwardTo: function() {
                    var path = '/' + Apiman.pluginName + formatMessage(arguments);
                    Logger.info('Forwarding to page {0}', path);
                    $location.url(path).replace();
                },
                redirectTo: function() {
                    var path = '/' + Apiman.pluginName + formatMessage(arguments);
                    Logger.info('Redirecting to page {0}', path);
                    $location.url(path);
                },
                loadPage: function(pageName, requiredPermission, pageData, $scope, handler) {
                    Logger.log("|{0}| >> Loading page.", pageName);
                    $rootScope.pageState = 'loading';
                    $rootScope.isDirty = false;

                    // Every page gets the current user.
                    var allData = undefined;
                    var commonData = {
                        currentUser: $q(function(resolve, reject) {
                            if ($rootScope.currentUser) {
                                Logger.log("|{0}| >> Using cached current user from $rootScope.", pageName);
                                resolve($rootScope.currentUser);
                            } else {
                                CurrentUserSvcs.get({ what: 'info' }, function(currentUser) {
                                    processCurrentUser(currentUser);
                                    resolve(currentUser);
                                }, reject);
                            }
                        })
                    };
                    
                    // If some additional page data is requested, merge it into the common data
                    if (pageData) {
                        allData = angular.extend({}, commonData, pageData);
                    } else {
                        allData = commonData;
                    }

                    // Now resolve the data as a promise (wait for all data packets to be fetched)
                    var promise = $q.all(allData);
                    promise.then(function(data) {
                        // Make sure the user has permission to view this page.
                        if ( (requiredPermission && requiredPermission == 'orgView' && !CurrentUser.isMember($scope.organizationId)) ||
                             ( requiredPermission && requiredPermission != 'orgView' && !CurrentUser.hasPermission($scope.organizationId, requiredPermission)) )
                        {
                            Logger.info('Detected a 404 error.');
                            $location.url(Apiman.pluginName + '/errors/404').replace();
                            return;
                        }
                        // Now process all the data packets and bind them to the $scope.
                        var count = 0;
                        angular.forEach(data, function(value, key) {
                            Logger.debug("|{0}| >> Binding {1} to $scope.", pageName, key);
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

}