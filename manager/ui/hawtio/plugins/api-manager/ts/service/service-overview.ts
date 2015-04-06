/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServiceOverviewController = _module.controller("Apiman.ServiceOverviewController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', '$routeParams',
        ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, $routeParams) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'overview';
            $scope.version = params.version;
            var pageData = ServiceEntityLoader.getCommonData($scope, $location);
            
            PageLifecycle.loadPage('ServiceOverview', pageData, $scope, function() {
                PageLifecycle.setPageTitle('service-overview', [ $scope.service.name ]);
            });
        }])

}
