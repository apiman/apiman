/// <reference path="../../includes.ts"/>
module ApimanPageLifecycle {

    export var pageTitles = {
        "page.title.admin-gateways": "apiman - Admin - Gateways",
        "page.title.admin-plugins": "apiman - Admin - Plugins",
        "page.title.admin-roles": "apiman - Admin - Roles",
        "page.title.admin-policyDefs": "apiman - Admin - Policy Definitions",
        "page.title.app-activity": "apiman - {0} (Activity)",
        "page.title.app-apis": "apiman - {0} (APIs)",
        "page.title.app-contracts": "apiman - {0} (Contracts)",
        "page.title.app-overview": "apiman - {0} (Overview)",
        "page.title.app-policies": "apiman - {0} (Policies)",
        "page.title.consumer-org": "apiman - Organization {0}",
        "page.title.consumer-orgs": "apiman - Organizations",
        "page.title.consumer-service": "apiman - Service {0}",
        "page.title.consumer-services": "apiman - Services",
        "page.title.dashboard": "apiman - Home",
        "page.title.about": "apiman - About",
        "page.title.edit-gateway": "apiman - Edit Gateway",
        "page.title.edit-policy": "apiman - Edit Policy",
        "page.title.edit-policyDef": "apiman - Edit Policy Definition",
        "page.title.edit-role": "apiman - Edit Role",
        "page.title.import-policyDefs": "apiman - Import Policy Definition(s)",
        "page.title.import-services": "apiman - Import Service(s)",
        "page.title.new-app": "apiman - New Application",
        "page.title.new-app-version": "apiman - New Application Version",
        "page.title.new-contract": "apiman - New Contract",
        "page.title.new-gateway": "apiman - New Gateway",
        "page.title.new-member": "apiman - Add Member",
        "page.title.new-org": "apiman - New Organization",
        "page.title.new-plan": "apiman - New Plan",
        "page.title.new-plan-version": "apiman - New Plan Version",
        "page.title.new-plugin": "apiman - Add Plugin",
        "page.title.new-policy": "apiman - Add Policy",
        "page.title.new-role": "apiman - New Role",
        "page.title.new-service": "apiman - New Service",
        "page.title.new-service-version": "apiman - New Service Version",
        "page.title.org-activity": "apiman - {0} (Activity)",
        "page.title.org-apps": "apiman - {0} (Applications)",
        "page.title.org-manage-members": "apiman - {0} (Manage Members)",
        "page.title.org-members": "apiman - {0} (Members)",
        "page.title.org-plans": "apiman - {0} (Plans)",
        "page.title.org-services": "apiman - {0} (Services)",
        "page.title.plan-activity": "apiman - {0} (Activity)",
        "page.title.plan-overview": "apiman - {0} (Overview)",
        "page.title.plan-policies": "apiman - {0} (Policies)",
        "page.title.plugin-details": "apiman - Plugin Details",
        "page.title.policy-defs": "apiman - Admin - Policy Definitions",
        "page.title.service-activity": "apiman - {0} (Activity)",
        "page.title.service-contracts": "apiman - {0} (Contracts)",
        "page.title.service-endpoint": "apiman - {0} (Endpoint)",
        "page.title.service-impl": "apiman - {0} (Implementation)",
        "page.title.service-overview": "apiman - {0} (Overview)",
        "page.title.service-plans": "apiman - {0} (Plans)",
        "page.title.service-policies": "apiman - {0} (Policies)",
        "page.title.user-activity": "apiman - {0} (Activity)",
        "page.title.user-apps": "apiman - {0} (Applications)",
        "page.title.user-orgs": "apiman - {0} (Organizations)",
        "page.title.user-profile": "apiman - User Profile",
        "page.title.user-services": "apiman - {0} (Services)",
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
        ['$q', 'Logger', '$rootScope', '$location', 'CurrentUserSvcs', 'Configuration', 'TranslationService',
        ($q, Logger, $rootScope, $location, CurrentUserSvcs, Configuration, TranslationService) => {
            $rootScope.showHeader = true;
            if (Configuration['ui'] && Configuration.ui.header == false) {
                $rootScope.showHeader = false;
            }

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
                } else if (error.status == 403) {
                    Logger.info('Detected an error {0}, redirecting to 403.', error.status);
                    $location.url(Apiman.pluginName + '/errors/403').replace();
                } else if (error.status == 404) {
                    Logger.info('Detected an error {0}, redirecting to 404.', error.status);
                    $location.url(Apiman.pluginName + '/errors/404').replace();
                } else if (error.status == 409) {
                    Logger.info('Detected an error {0}, redirecting to 409.', error.status);
                    $location.url(Apiman.pluginName + '/errors/409').replace();
                } else {
                    Logger.info('Detected an error {0}, redirecting to 500.', error.status);
                    $location.url(Apiman.pluginName + '/errors/500').replace();
                }
            };
            return {
                setPageTitle: function(titleKey, params) {
                    var key = 'page.title.' + titleKey;
                    var pattern = pageTitles[key];
                    pattern = TranslationService.translate(key, pattern);
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
                loadPage: function(pageName, pageData, $scope, handler) {
                    Logger.log("|{0}| >> Loading page.", pageName);
                    $rootScope.pageState = 'loading';

                    // Every page gets the current user.
                    var allData = undefined;
                    var commonData = {
                        currentUser: $q(function(resolve, reject) {
                            if ($rootScope.currentUser) {
                                Logger.log("|{0}| >> Using cached current user from $rootScope.");
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
                        var count = 0;
                        angular.forEach(data, function(value, key) {
                            Logger.debug("|{0}| >> Binding {1} to $scope.", pageName, key);
                            this[key] = value;
                            count++;
                        }, $scope);
                        $rootScope.pageState = 'loaded';
                        if (handler) {
                            handler();
                        }
                        Logger.log("|{0}| >> Page successfully loaded: {1} data packets loaded", pageName, count);
                    }, function(reason) {
                        Logger.error("|{0}| >> Page load failed: {1}", pageName, reason);
                        handleError(reason);
                    });
                
                
                }
            }
        }]);

}