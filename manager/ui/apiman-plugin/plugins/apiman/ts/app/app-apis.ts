/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AppApisController = _module.controller("Apiman.AppApisController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', ($q, $scope, $location, PageLifecycle, AppEntityLoader) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            $scope.tab = 'apis';
            $scope.version = params.version;
            var dataLoad = AppEntityLoader.getCommonData($scope, $location);
            var promise = $q.all(dataLoad);
            PageLifecycle.loadPage('AppApis', promise, $scope);
        }])

}
