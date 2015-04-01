/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var PlanOverviewController = _module.controller("Apiman.PlanOverviewController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'PlanEntityLoader', '$routeParams',
        ($q, $scope, $location, PageLifecycle, PlanEntityLoader, $routeParams) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'overview';
            var dataLoad = PlanEntityLoader.getCommonData($scope, $location);
            var promise = $q.all(dataLoad);
            PageLifecycle.loadPage('PlanOverview', promise, $scope, function() {
                PageLifecycle.setPageTitle('plan-overview', [ $scope.plan.name ]);
            });
        }])


}
