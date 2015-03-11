/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var AppActivityController = _module.controller("Apiman.AppActivityController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', ($q, $scope, $location, PageLifecycle, AppEntityLoader) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            $scope.tab = 'activity';
            $scope.version = params.version;
            var dataLoad = AppEntityLoader.getCommonData($scope, $location);
            var promise = $q.all(dataLoad);
            PageLifecycle.loadPage('AppActivity', promise, $scope);
        }])

}
