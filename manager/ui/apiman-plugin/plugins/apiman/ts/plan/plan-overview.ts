/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var PlanOverviewController = _module.controller("Apiman.PlanOverviewController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'PlanEntityLoader', ($q, $scope, $location, PageLifecycle, PlanEntityLoader) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            $scope.tab = 'overview';
            var dataLoad = PlanEntityLoader.getCommonData($scope, $location);
            var promise = $q.all(dataLoad);
            PageLifecycle.loadPage('PlanOverview', promise, $scope);
        }])


}
