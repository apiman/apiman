/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

 export var ServiceImplController = _module.controller("Apiman.ServiceImplController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs',
         ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            $scope.tab = 'impl';
            $scope.version = params.version;
            $scope.typeOptions = ["rest","soap"];
            
            var dataLoad = ServiceEntityLoader.getCommonData($scope, $location);
            if (params.version != null) {
                dataLoad = angular.extend(dataLoad, {
                    selectedService: $q(function(resolve, reject) {
                        OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version }, function(selectedService) {
                            $scope.gatewayId = selectedService.gateways[0].gatewayId;
                            resolve(selectedService);
                            
                        }, function(error) {
                            reject(error);
                        });
                    }),
                    gateways: $q(function(resolve, reject) {
                    ApimanSvcs.query({ entityType: 'gateways' }, function(gateways) {
                        resolve(gateways);
                    }, function(error) {
                        reject(error);
                    });
                })
                });
            }
            var promise = $q.all(dataLoad);
             
            $scope.saveService = function() {
                //$scope.saveButton.state = 'Saving...';
                var updatedService:any = {};
                updatedService.endpoint = $scope.selectedService.endpoint;
                updatedService.gateways = $scope.selectedService.gateways; //TBD
                updatedService.plans = $scope.selectedService.plans;
                updatedService.endpointType = $scope.selectedService.endpointType;
                updatedService.publicService = $scope.selectedService.publicService;
                
                OrgSvcs.update({ organizationId: params.org, entityType: 'services', entityId:params.service, versionsOrActivity: 'versions', version: params.version }, updatedService, function(reply) {
                    //$scope.saveButton.state = 'Save';
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        //$scope.saveButton.state = 'error';
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            };
            
            PageLifecycle.loadPage('ServiceImpl', promise, $scope);
        }])

}
