/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServiceEndpointController = _module.controller("Apiman.ServiceEndpointController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams', 'Configuration',
        ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs, $routeParams, Configuration) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'endpoint';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;

            var pageData = ServiceEntityLoader.getCommonData($scope, $location);
            pageData = angular.extend(pageData, {
                managedEndpoint: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'endpoint' }, resolve, reject);
                })
            });
            

            PageLifecycle.loadPage('ServiceEndpoint', pageData, $scope, function() {
                PageLifecycle.setPageTitle('service-endpoint', [ $scope.service.name ]);
            });
        }])

}
