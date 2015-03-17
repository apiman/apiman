/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var ServiceEntityLoader = _module.factory('ServiceEntityLoader', 
        ['$q', 'OrgSvcs', 'Logger', '$rootScope', ($q, OrgSvcs, Logger, $rootScope) => {
            return {
                getCommonData: function($scope, $location) {
                    var params = $location.search();
                    return {
                        service: $q(function(resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service }, function(service) {
                                resolve(service);
                            }, function(error) {
                                reject(error);
                            });
                        }),
                        versions: $q(function(resolve, reject) {
                            OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions' }, function(versions) {
                                resolve(versions);
                                if (params.version != null) {
                                    for (var i = 0; i < versions.length; i++) {
                                        if (params.version == versions[i].version) {
                                            $scope.selectedServiceVersion = versions[i];
                                            break;
                                        }
                                    }
                                } else {
                                    $scope.selectedServiceVersion = versions[0];
                                }
                                $scope.version = $scope.selectedServiceVersion.version;
                                $scope.entityStatus = $scope.selectedServiceVersion.status;
                            }, function(error) {
                                reject(error);
                            });
                        })
                    };
                }
            }
        }]);

    export var ServiceEntityController = _module.controller("Apiman.ServiceEntityController",
        ['$q', '$scope', '$location', 'ActionSvcs', 'Logger', ($q, $scope, $location, ActionSvcs, Logger) => {
            var params = $location.search();
            
            $scope.setVersion = function(service) {
                $scope.selectedServiceVersion = service;
                $location.path(Apiman.pluginName + "/service-overview.html").search('org', params.org).search('service', params.service).search('version', service.version);
            };

//            $scope.registerApp = function(app) {
//                $scope.registerButton.state = 'in-progress';
//                var registerAction = {
//                    type: 'registerApp',
//                    entityId: app.id,
//                    organizationId: app.organizationId,
//                    entityVersion: app.version
//                };
//                ActionSvcs.save(registerAction, function(reply) {
//                    $scope.selectedAppVersion.status = 'Registered';
//                    $scope.registerButton.state = 'complete';
//                    $scope.entityStatus = $scope.selectedAppVersion.status;
//                }, function(error) {
//                    $scope.registerButton.state = 'error';
//                    alert("ERROR=" + error);
//                });
//            };
//            
//            $scope.unregisterApp = function(app) {
//                $scope.unregisterButton.state = 'in-progress';
//                var unregisterAction = {
//                    type: 'unregisterApp',
//                    entityId: app.id,
//                    organizationId: app.organizationId,
//                    entityVersion: app.version
//                };
//                ActionSvcs.save(unregisterAction, function(reply) {
//                    $scope.selectedAppVersion.status = 'Retired';
//                    $scope.unregisterButton.state = 'complete';
//                    $scope.entityStatus = $scope.selectedAppVersion.status;
//                }, function(error) {
//                    $scope.unregisterButton.state = 'error';
//                    alert("ERROR=" + error);
//                });
//            };
            
        }])

}
