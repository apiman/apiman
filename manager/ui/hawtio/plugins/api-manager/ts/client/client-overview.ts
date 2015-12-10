/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var ClientOverviewController = _module.controller("Apiman.ClientOverviewController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ClientEntityLoader', '$routeParams', 'Configuration',
        ($q, $scope, $location, PageLifecycle, ClientEntityLoader, $routeParams, Configuration) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'overview';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            var pageData = ClientEntityLoader.getCommonData($scope, $location);
            PageLifecycle.loadPage('ClientOverview', 'clientView', pageData, $scope, function() {
                PageLifecycle.setPageTitle('client-overview', [ $scope.client.name ]);
            });
        }])

}
