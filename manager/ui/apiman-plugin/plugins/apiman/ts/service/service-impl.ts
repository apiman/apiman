/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServiceImplController = _module.controller("Apiman.ServiceImplController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs',
        ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            $scope.tab = 'impl';
            $scope.version = params.version;
            $scope.typeOptions = ["rest","soap"];
            $scope.updatedService = new Object();
            
            var dataLoad = ServiceEntityLoader.getCommonData($scope, $location);
            if (params.version != null) {
                dataLoad = angular.extend(dataLoad, {
                    serviceVersion: $q(function(resolve, reject) {
                        OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version }, resolve, reject);
                    }),
                    gateways: $q(function(resolve, reject) {
                        ApimanSvcs.query({ entityType: 'gateways' }, resolve, reject);
                    })
                });
            }
            var promise = $q.all(dataLoad);
            
            $scope.$watch('updatedService', function(newValue) {
                if ($scope.serviceVersion) {
                    if (newValue.endpoint == $scope.serviceVersion.endpoint && newValue.endpointType == $scope.serviceVersion.endpointType) {
                        $scope.isDirty = false;
                    } else {
                        $scope.isDirty = true;
                    }
                }
            }, true);
            
            $scope.reset = function() {
                $scope.updatedService.endpoint = $scope.serviceVersion.endpoint;
                $scope.updatedService.endpointType = $scope.serviceVersion.endpointType;
                $scope.isDirty = false;
            };
             
            $scope.saveService = function() {
                $scope.saveButton.state = 'in-progress';
                OrgSvcs.update({ organizationId: params.org, entityType: 'services', entityId:params.service, versionsOrActivity: 'versions', version: params.version }, $scope.updatedService, function(reply) {
                    $scope.isDirty = false;
                    $scope.saveButton.state = 'complete';
                    $scope.serviceVersion.endpoint = $scope.updatedService.endpoint;
                    $scope.serviceVersion.endpointType = $scope.updatedService.endpointType;
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('ServiceImpl', promise, $scope, function() {
                $scope.reset();
                PageLifecycle.setPageTitle('service-impl', [ $scope.service.name ]);
            });
        }])

}
