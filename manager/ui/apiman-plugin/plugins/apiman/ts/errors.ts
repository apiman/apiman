/// <reference path="apimanPlugin.ts"/>
module Apiman {

    _module.controller("Apiman.Error400Controller",
        ['$scope', '$rootScope', 'PageLifecycle',
        ($scope, $rootScope, PageLifecycle) => {
            PageLifecycle.loadPage('Error', undefined, $scope, function() {
                PageLifecycle.setPageTitle('error', 400);
            });
        }]);
    
    _module.controller("Apiman.Error403Controller",
        ['$scope', '$rootScope', 'PageLifecycle',
        ($scope, $rootScope, PageLifecycle) => {
            PageLifecycle.loadPage('Error', undefined, $scope, function() {
                PageLifecycle.setPageTitle('error', 403);
            });
        }]);

    _module.controller("Apiman.Error404Controller",
        ['$scope', '$rootScope', 'PageLifecycle',
        ($scope, $rootScope, PageLifecycle) => {
            PageLifecycle.loadPage('Error', undefined, $scope, function() {
                PageLifecycle.setPageTitle('error', 404);
            });
        }]);

    _module.controller("Apiman.Error409Controller",
        ['$scope', '$rootScope', 'PageLifecycle',
        ($scope, $rootScope, PageLifecycle) => {
            PageLifecycle.loadPage('Error', undefined, $scope, function() {
                PageLifecycle.setPageTitle('error', 409);
            });
        }]);

    _module.controller("Apiman.Error500Controller",
        ['$scope', '$rootScope', 'PageLifecycle', 'Logger',
        ($scope, $rootScope, PageLifecycle, Logger) => {
            $scope.error = $rootScope.pageError;
            PageLifecycle.loadPage('Error', undefined, $scope, function() {
                PageLifecycle.setPageTitle('error', 500);
            });
        }]);

}
