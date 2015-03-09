/// <reference path="apimanPlugin.ts"/>
module Apiman {

    export var DashController = _module.controller("Apiman.DashController",
        ['$scope', 'PageLifecycle', ($scope, PageLifecycle) => {
            PageLifecycle.loadPage('Dash', undefined, $scope);
        }]);

}
