/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var AppEntityLoader = _module.factory('AppEntityLoader', 
        ['$q', 'OrgSvcs', 'Logger', '$rootScope', ($q, OrgSvcs, Logger, $rootScope) => {
            return {
                getCommonData: function($scope, $location) {
                    var params = $location.search();
                    return {
                        org: $q(function(resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org }, function(org) {
                                resolve(org);
                            }, function(error) {
                                reject(error);
                            });
                        }),
                        app: $q(function(resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org, entityType: 'applications', entityId: params.app }, function(app) {
                                resolve(app);
                            }, function(error) {
                                reject(error);
                            });
                        }),
                        versions: $q(function(resolve, reject) {
                            OrgSvcs.query({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions' }, function(versions) {
                                resolve(versions);
                                if (params.version != null) {
                                    for (var i = 0; i < versions.length; i++) {
                                        if (params.version == versions[i].version) {
                                            $scope.selectedAppVersion = versions[i];
                                            break;
                                        }
                                    }
                                } else {
                                    $scope.selectedAppVersion = versions[0];
                                }
                                $rootScope.mruApp = $scope.selectedAppVersion;
                                $scope.entityStatus = $scope.selectedAppVersion.status;
                            }, function(error) {
                                reject(error);
                            });
                        })
                    };
                }
            }
        }]);

    export var AppEntityController = _module.controller("Apiman.AppEntityController",
        ['$q', '$scope', '$location', 'ActionSvcs', 'Logger', ($q, $scope, $location, ActionSvcs, Logger) => {
            var params = $location.search();
            
            $scope.setVersion = function(app) {
                $scope.selectedAppVersion = app;
                $location.path(Apiman.pluginName + "/app-overview.html").search('org', params.org).search('app', params.app).search('version', app.version);
            };

            $scope.registerApp = function(app) {
                $scope.registerButton.state = 'in-progress';
                var registerAction = {
                    type: 'registerApp',
                    entityId: app.id,
                    organizationId: app.organizationId,
                    entityVersion: app.version
                };
                ActionSvcs.save(registerAction, function(reply) {
                    $scope.selectedAppVersion.status = 'Registered';
                    $scope.registerButton.state = 'complete';
                    $scope.entityStatus = $scope.selectedAppVersion.status;
                }, function(error) {
                    $scope.registerButton.state = 'error';
                    alert("ERROR=" + error);
                });
            };
            
            $scope.unregisterApp = function(app) {
                $scope.unregisterButton.state = 'in-progress';
                var unregisterAction = {
                    type: 'unregisterApp',
                    entityId: app.id,
                    organizationId: app.organizationId,
                    entityVersion: app.version
                };
                ActionSvcs.save(unregisterAction, function(reply) {
                    $scope.selectedAppVersion.status = 'Retired';
                    $scope.unregisterButton.state = 'complete';
                    $scope.entityStatus = $scope.selectedAppVersion.status;
                }, function(error) {
                    $scope.unregisterButton.state = 'error';
                    alert("ERROR=" + error);
                });
            };
            
        }])

}
