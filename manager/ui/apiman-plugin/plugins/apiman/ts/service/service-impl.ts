/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServiceImplController = _module.controller("Apiman.ServiceImplController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams',
        ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs, $routeParams) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'impl';
            $scope.version = params.version;
            $scope.typeOptions = ["rest","soap"];
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
                    if (newValue.endpoint == $scope.version.endpoint && newValue.endpointType == $scope.version.endpointType) {
                        $scope.isDirty = false;
                    } else {
                        $scope.isDirty = true;
                    }
                }
            }, true);
            
            $scope.reset = function() {
                $scope.updatedService.endpoint = $scope.version.endpoint;
                $scope.updatedService.endpointType = $scope.version.endpointType;
                $scope.isDirty = false;
            };
             
            $scope.saveService = function() {
                $scope.saveButton.state = 'in-progress';
                OrgSvcs.update({ organizationId: params.org, entityType: 'services', entityId:params.service, versionsOrActivity: 'versions', version: params.version }, $scope.updatedService, function(reply) {
                    $scope.isDirty = false;
                    $scope.saveButton.state = 'complete';
                    $scope.version = reply;
                    $scope.entityStatus = reply.status;
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('ServiceImpl', pageData, $scope, function() {
                $scope.reset();
                PageLifecycle.setPageTitle('service-impl', [ $scope.service.name ]);
            });
        }])

}
