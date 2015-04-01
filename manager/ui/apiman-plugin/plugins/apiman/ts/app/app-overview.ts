/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AppOverviewController = _module.controller("Apiman.AppOverviewController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', '$routeParams',
        ($q, $scope, $location, PageLifecycle, AppEntityLoader, $routeParams) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'overview';
            $scope.version = params.version;
            var dataLoad = AppEntityLoader.getCommonData($scope, $location);
            var promise = $q.all(dataLoad);
            PageLifecycle.loadPage('AppOverview', promise, $scope, function() {
                PageLifecycle.setPageTitle('app-overview', [ $scope.app.name ]);
            });
        }])

}
