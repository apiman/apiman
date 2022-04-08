/// <reference path="apimanPlugin.ts"/>
module Apiman {

    export var DashController = _module.controller("Apiman.DashController",
        ['$scope', 'PageLifecycle', 'CurrentUser', 'Configuration', 
        ($scope, PageLifecycle, CurrentUser, Configuration) => {
            PageLifecycle.loadPage('Dash', undefined, undefined, $scope, function() {
                PageLifecycle.setPageTitle('dashboard');
            });
        }]);

}
