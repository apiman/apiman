/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AppEntityLoader = _module.factory('AppEntityLoader', 
        ['$q', 'OrgSvcs', 'Logger', '$rootScope', ($q, OrgSvcs, Logger, $rootScope) => {
            return {
                getCommonData: function($scope, $location) {
                    var params = $location.search();
                    $scope.setEntityStatus = function(status) {
                        $scope.entityStatus = status;
                    };
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
                                $scope.setEntityStatus($scope.selectedAppVersion.status);
                            }, function(error) {
                                reject(error);
                            });
                        })
                    };
                }
            }
        }]);

    export var AppEntityController = _module.controller("Apiman.AppEntityController",
        ['$q', '$scope', '$location', 'ActionSvcs', 'Logger', 'Dialogs', 
        ($q, $scope, $location, ActionSvcs, Logger, Dialogs) => {
            var params = $location.search();
            
            $scope.setVersion = function(app) {
                $scope.selectedAppVersion = app;
                $location.search('version', app.version);
            };

            $scope.registerApp = function(app) {
                $scope.registerButton.state = 'in-progress';
                var registerAction = {
                    type: 'registerApplication',
                    entityId: params.app,
                    organizationId: params.org,
                    entityVersion: params.version
                };
                ActionSvcs.save(registerAction, function(reply) {
                    $scope.selectedAppVersion.status = 'Registered';
                    $scope.registerButton.state = 'complete';
                    $scope.setEntityStatus($scope.selectedAppVersion.status);
                }, function(error) {
                    $scope.registerButton.state = 'error';
                    alert("ERROR=" + error);
                });
            };
            
            $scope.unregisterApp = function(app) {
                $scope.unregisterButton.state = 'in-progress';
                Dialogs.confirm('Confirm Unregister App', 'Do you really want to unregister the application?  This cannot be undone.', function() {
                    var unregisterAction = {
                        type: 'unregisterApplication',
                        entityId: params.app,
                        organizationId: params.org,
                        entityVersion: params.version
                    };
                    ActionSvcs.save(unregisterAction, function(reply) {
                        $scope.selectedAppVersion.status = 'Retired';
                        $scope.unregisterButton.state = 'complete';
                        $scope.setEntityStatus($scope.selectedAppVersion.status);
                    }, function(error) {
                        $scope.unregisterButton.state = 'error';
                        alert("ERROR=" + error);
                    });
                }, function() {
                    $scope.unregisterButton.state = 'complete';
                });
            };
            
        }])

}
