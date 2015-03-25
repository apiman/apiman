/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServiceOverviewController = _module.controller("Apiman.ServiceOverviewController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', ($q, $scope, $location, PageLifecycle, ServiceEntityLoader) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            $scope.tab = 'overview';
            $scope.version = params.version;
            var dataLoad = ServiceEntityLoader.getCommonData($scope, $location);
            var promise = $q.all(dataLoad);
            PageLifecycle.loadPage('ServiceOverview', promise, $scope);
        }])

}
