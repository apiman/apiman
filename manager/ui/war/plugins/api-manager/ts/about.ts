/// <reference path="apimanPlugin.ts"/>
module Apiman {

    export var DashController = _module.controller("Apiman.AboutController",
        ['$scope', 'PageLifecycle', 'CurrentUser', 'Configuration',
        ($scope, PageLifecycle, CurrentUser, Configuration) => {
            PageLifecycle.loadPage('About', undefined, undefined, $scope, function() {
                $scope.github = "http://github.com/apiman/apiman";
                $scope.site = "http://apiman.io/";
                $scope.userGuide = "http://www.apiman.io/latest/user-guide.html";
                $scope.tutorials = "http://www.apiman.io/latest/tutorials.html";
                $scope.version = Configuration.apiman.version;
                $scope.builtOn = Configuration.apiman.builtOn;
                $scope.apiEndpoint = Configuration.api.endpoint;
                PageLifecycle.setPageTitle('about');
            });
        }]);

}
