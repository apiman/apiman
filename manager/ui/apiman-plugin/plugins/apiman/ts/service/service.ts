/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var ServiceEntityLoader = _module.factory('ServiceEntityLoader', 
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
                            }, reject);
                        }),
                        service: $q(function(resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service }, function(service) {
                                resolve(service);
                            }, reject);
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
                                $scope.setEntityStatus($scope.selectedServiceVersion.status);
                            }, reject);
                        })
                    };
                }
            }
        }]);

    export var ServiceEntityController = _module.controller("Apiman.ServiceEntityController",
        ['$q', '$scope', '$location', 'ActionSvcs', 'Logger', 'Dialogs', 'PageLifecycle',
        ($q, $scope, $location, ActionSvcs, Logger, Dialogs, PageLifecycle) => {
            var params = $location.search();
            $scope.params = params;
            
            $scope.setVersion = function(service) {
                $scope.selectedServiceVersion = service;
                $location.search('version', service.version);
            };

            $scope.publishService = function(service) {
                $scope.publishButton.state = 'in-progress';
                var publishAction = {
                    type: 'publishService',
                    entityId: params.service,
                    organizationId: params.org,
                    entityVersion: params.version
                };
                ActionSvcs.save(publishAction, function(reply) {
                    $scope.selectedServiceVersion.status = 'Published';
                    $scope.publishButton.state = 'complete';
                    $scope.setEntityStatus($scope.selectedServiceVersion.status);
                }, PageLifecycle.handleError);
            };
            
            $scope.retireService = function(service) {
                $scope.retireButton.state = 'in-progress';
                Dialogs.confirm('Confirm Retire Service', 'Do you really want to retire this service?  This action cannot be undone.', function() {
                    var retireAction = {
                        type: 'retireService',
                        entityId: params.service,
                        organizationId: params.org,
                        entityVersion: params.version
                    };
                    ActionSvcs.save(retireAction, function(reply) {
                        $scope.selectedServiceVersion.status = 'Retired';
                        $scope.retireButton.state = 'complete';
                        $scope.setEntityStatus($scope.selectedServiceVersion.status);
                    }, PageLifecycle.handleError);
                }, function() {
                    $scope.retireButton.state = 'complete';
                });
            };
        }])
}
