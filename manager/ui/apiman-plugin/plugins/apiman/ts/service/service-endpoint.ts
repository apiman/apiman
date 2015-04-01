/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServiceEndpointController = _module.controller("Apiman.ServiceEndpointController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams',
        ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs, $routeParams) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'endpoint';
            $scope.version = params.version;
            
            var dataLoad = ServiceEntityLoader.getCommonData($scope, $location);
            if (params.version != null) {
                dataLoad = angular.extend(dataLoad, {
                    managedEndpoint: $q(function(resolve, reject) {
                        OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'endpoint' }, resolve, reject);
                    })
                });
            }
            var promise = $q.all(dataLoad);

            PageLifecycle.loadPage('ServiceEndpoint', promise, $scope, function() {
                PageLifecycle.setPageTitle('service-endpoint', [ $scope.service.name ]);
            });
        }])

}
