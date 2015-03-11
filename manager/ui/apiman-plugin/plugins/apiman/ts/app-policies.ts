/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var AppPoliciesController = _module.controller("Apiman.AppPoliciesController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', ($q, $scope, $location, PageLifecycle, AppEntityLoader) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            $scope.tab = 'policies';
            $scope.version = params.version;
            var dataLoad = AppEntityLoader.getCommonData($scope, $location);
            var promise = $q.all(dataLoad);
            PageLifecycle.loadPage('AppPolicies', promise, $scope);
        }])

}
