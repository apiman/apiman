/// <reference path="apimanPlugin.ts"/>
module Apiman {

    export var DashController = _module.controller("Apiman.DashController",
        ['$scope', 'PageLifecycle', 'CurrentUser', 
        ($scope, PageLifecycle, CurrentUser) => {
            PageLifecycle.loadPage('Dash', undefined, undefined, $scope, function() {
                $scope.isAdmin = CurrentUser.getCurrentUser().admin;
                $scope.currentUser = CurrentUser.getCurrentUser();
                PageLifecycle.setPageTitle('dashboard');
            });
        }]);

}
