/// <reference path="apimanPlugin.ts"/>
module Apiman {

    export var DashController = _module.controller("Apiman.DashController",
        ['$scope', 'PageLifecycle', 'CurrentUser', ($scope, PageLifecycle, CurrentUser) => {
            $scope.isAdmin = CurrentUser.getCurrentUser().admin;
            $scope.currentUser = CurrentUser.getCurrentUser();
            PageLifecycle.loadPage('Dash', undefined, $scope);
        }]);

}
