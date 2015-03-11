/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var AppContractsController = _module.controller("Apiman.AppContractsController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', ($q, $scope, $location, PageLifecycle, AppEntityLoader) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            $scope.tab = 'contracts';
            $scope.version = params.version;
            var dataLoad = AppEntityLoader.getCommonData($scope, $location);
            var promise = $q.all(dataLoad);
            PageLifecycle.loadPage('AppContracts', promise, $scope);
        }])

}
