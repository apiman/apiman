/// <reference path="apimanPlugin.ts"/>
module Apiman {

    _module.controller('Apiman.Error400Controller',
        ['$scope', '$rootScope', 'PageLifecycle',
        ($scope, $rootScope, PageLifecycle) => {
            PageLifecycle.loadErrorPage('Error', $scope, function() {
                PageLifecycle.setPageTitle('error', 400);
            });
        }]);
    
    _module.controller('Apiman.Error403Controller',
        ['$scope', '$rootScope', 'PageLifecycle',
        ($scope, $rootScope, PageLifecycle) => {
            PageLifecycle.loadErrorPage('Error', $scope, function() {
                PageLifecycle.setPageTitle('error', 403);
            });
        }]);

    _module.controller('Apiman.Error404Controller',
        ['$scope', '$rootScope', 'PageLifecycle',
        ($scope, $rootScope, PageLifecycle) => {
            PageLifecycle.loadErrorPage('Error', $scope, function() {
                PageLifecycle.setPageTitle('error', 404);
            });
        }]);

    _module.controller('Apiman.Error409Controller',
        ['$scope', '$rootScope', 'PageLifecycle',
        ($scope, $rootScope, PageLifecycle) => {
            PageLifecycle.loadErrorPage('Error', $scope, function() {
                PageLifecycle.setPageTitle('error', 409);
            });
        }]);

    _module.controller('Apiman.Error500Controller',
        ['$scope', '$rootScope', 'PageLifecycle', 'Logger',
        ($scope, $rootScope, PageLifecycle, Logger) => {
            $scope.error = $rootScope.pageError;
            PageLifecycle.loadErrorPage('Error', $scope, function() {
                PageLifecycle.setPageTitle('error', 500);
            });
        }]);

    _module.controller('Apiman.ErrorInvalidServerController',
        ['$scope', '$rootScope', 'PageLifecycle', 'Logger', 'Configuration',
        ($scope, $rootScope, PageLifecycle, Logger, Configuration) => {
            $scope.error = $rootScope.pageError;
            PageLifecycle.loadErrorPage('Error', $scope, function() {
                $scope.installGuide = 'http://www.apiman.io/latest/installation-guide.html';
                $scope.version = Configuration.apiman.version;
                $scope.builtOn = Configuration.apiman.builtOn;
                $scope.apiEndpoint = Configuration.api.endpoint;
                $scope.cors = 'https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS';
                PageLifecycle.setPageTitle('error', 'Invalid Server');
            });
        }]);

}
