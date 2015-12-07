/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

 export var ApiOverviewController = _module.controller("Apiman.ApiOverviewController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ApiEntityLoader', '$routeParams', 'Configuration',
        ($q, $scope, $location, PageLifecycle, ApiEntityLoader, $routeParams, Configuration) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'overview';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            var pageData = ApiEntityLoader.getCommonData($scope, $location);
            
            PageLifecycle.loadPage('ApiOverview', 'apiView', pageData, $scope, function() {
                PageLifecycle.setPageTitle('api-overview', [ $scope.api.name ]);
            });
        }])

}
