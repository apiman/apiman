/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AppRedirectController = _module.controller("Apiman.AppRedirectController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'CurrentUser', '$routeParams',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, CurrentUser, $routeParams) => {
            var orgId = $routeParams.org;
            var appId = $routeParams.app;
            var promise = $q.all({
                versions: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: orgId, entityType: 'applications', entityId: appId, versionsOrActivity: 'versions' }, resolve, reject);
                })
            });
            
            PageLifecycle.loadPage('AppRedirect', promise, $scope, function() {
                var version = $scope.versions[0].version;
                if (!version) {
                    PageLifecycle.handleError({ status: 404 });
                } else {
                    PageLifecycle.forwardTo('/orgs/{0}/apps/{1}/{2}', orgId, appId, version);
                }
            });
        }]);

    export var AppEntityLoader = _module.factory('AppEntityLoader', 
        ['$q', 'OrgSvcs', 'Logger', '$rootScope', '$routeParams',
        ($q, OrgSvcs, Logger, $rootScope, $routeParams) => {
            return {
                getCommonData: function($scope, $location) {
                    var params = $routeParams;
                    $scope.setEntityStatus = function(status) {
                        $scope.entityStatus = status;
                    };
                    return {
                        version: $q(function(resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version }, function(version) {
                                $scope.org = version.application.organization;
                                $scope.app = version.application;
                                $rootScope.mruApp = version;
                                $scope.setEntityStatus(version.status);
                                resolve(version);
                            }, reject);
                        }),
                        versions: $q(function(resolve, reject) {
                            OrgSvcs.query({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions' }, resolve, reject);
                        })
                    };
                }
            }
        }]);

    export var AppEntityController = _module.controller("Apiman.AppEntityController",
        ['$q', '$scope', '$location', 'ActionSvcs', 'Logger', 'Dialogs', 'PageLifecycle', '$routeParams',
        ($q, $scope, $location, ActionSvcs, Logger, Dialogs, PageLifecycle, $routeParams) => {
            var params = $routeParams;
            
            $scope.setVersion = function(app) {
                PageLifecycle.redirectTo('/orgs/{0}/apps/{1}/{2}', params.org, params.app, app.version);
            };

            $scope.registerApp = function() {
                $scope.registerButton.state = 'in-progress';
                var registerAction = {
                    type: 'registerApplication',
                    entityId: params.app,
                    organizationId: params.org,
                    entityVersion: params.version
                };
                ActionSvcs.save(registerAction, function(reply) {
                    $scope.version.status = 'Registered';
                    $scope.registerButton.state = 'complete';
                    $scope.setEntityStatus($scope.version.status);
                }, PageLifecycle.handleError);
            };
            
            $scope.unregisterApp = function() {
                $scope.unregisterButton.state = 'in-progress';
                Dialogs.confirm('Confirm Unregister App', 'Do you really want to unregister the application?  This cannot be undone.', function() {
                    var unregisterAction = {
                        type: 'unregisterApplication',
                        entityId: params.app,
                        organizationId: params.org,
                        entityVersion: params.version
                    };
                    ActionSvcs.save(unregisterAction, function(reply) {
                        $scope.version.status = 'Retired';
                        $scope.unregisterButton.state = 'complete';
                        $scope.setEntityStatus($scope.version.status);
                    }, PageLifecycle.handleError);
                }, function() {
                    $scope.unregisterButton.state = 'complete';
                });
            };
            
        }])

}
