/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServiceImplController = _module.controller("Apiman.ServiceImplController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams', 'EntityStatusService',
        ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs, $routeParams, EntityStatusService) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'impl';
            $scope.version = params.version;
            $scope.typeOptions = ["rest", "soap"];
            $scope.updatedService = new Object();

            var pageData = ServiceEntityLoader.getCommonData($scope, $location);
            if (params.version != null) {
                pageData = angular.extend(pageData, {
                    gateways: $q(function(resolve, reject) {
                        ApimanSvcs.query({ entityType: 'gateways' }, resolve, reject);
                    })
                });
            }

            $scope.$watch('updatedService', function(newValue) {
                if ($scope.version) {
                    var dirty = false;
                    if (newValue.endpoint != $scope.version.endpoint || newValue.endpointType != $scope.version.endpointType) {
                        dirty = true;
                    } else if (newValue.gateways && newValue.gateways.length > 0) {
                        Logger.debug('Dirty because of gateways!');
                        dirty = true;
                    }
                    $scope.isDirty = dirty;
                }
            }, true);

            $scope.$watch('selectedGateway', function(newValue) {
                Logger.info('New gateway selected: {0}', newValue);
                var alreadySet = false;
                if ($scope.version.gateways[0].gatewayId == newValue.id) {
                    alreadySet = true;
                }
                if (!alreadySet) {
                    $scope.updatedService.gateways = [ { gatewayId: newValue.id } ];
                } else {
                    delete $scope.updatedService.gateways;
                }
            });

            $scope.reset = function() {
                $scope.updatedService.endpoint = $scope.version.endpoint;
                $scope.updatedService.endpointType = $scope.version.endpointType;
                delete $scope.updatedService.gateways;
                angular.forEach($scope.gateways, function(gateway) {
                    // TODO support multiple gateway assignments here
                    if (gateway.id == $scope.version.gateways[0].gatewayId) {
                        $scope.selectedGateway = gateway;
                    }
                });
                $scope.isDirty = false;
            };

            $scope.saveService = function() {
                $scope.saveButton.state = 'in-progress';
                OrgSvcs.update({ organizationId: params.org, entityType: 'services', entityId:params.service, versionsOrActivity: 'versions', version: params.version }, $scope.updatedService, function(reply) {
                    $scope.isDirty = false;
                    $scope.saveButton.state = 'complete';
                    $scope.version = reply;
                    EntityStatusService.setEntityStatus(reply.status);
                }, PageLifecycle.handleError);
            };

            PageLifecycle.loadPage('ServiceImpl', pageData, $scope, function() {
                $scope.reset();
                PageLifecycle.setPageTitle('service-impl', [ $scope.service.name ]);
            });
        }])

}
