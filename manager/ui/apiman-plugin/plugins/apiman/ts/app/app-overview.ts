/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AppOverviewController = _module.controller("Apiman.AppOverviewController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', ($q, $scope, $location, PageLifecycle, AppEntityLoader) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            $scope.tab = 'overview';
            $scope.version = params.version;
            var dataLoad = AppEntityLoader.getCommonData($scope, $location);
            var promise = $q.all(dataLoad);
            PageLifecycle.loadPage('AppOverview', promise, $scope);
        }])

}
