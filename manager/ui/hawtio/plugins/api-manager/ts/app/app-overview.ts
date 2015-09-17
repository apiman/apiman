/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AppOverviewController = _module.controller("Apiman.AppOverviewController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', '$routeParams', 'Configuration',
        ($q, $scope, $location, PageLifecycle, AppEntityLoader, $routeParams, Configuration) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'overview';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            var pageData = AppEntityLoader.getCommonData($scope, $location);
            PageLifecycle.loadPage('AppOverview', pageData, $scope, function() {
                PageLifecycle.setPageTitle('app-overview', [ $scope.app.name ]);
            });
        }])

}
