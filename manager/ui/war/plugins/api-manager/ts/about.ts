import {_module} from "./apimanPlugin";

_module.controller("Apiman.AboutController",
    ['$scope', 'PageLifecycle', 'CurrentUser', 'Configuration',
    function ($scope, PageLifecycle, CurrentUser, Configuration) {
        PageLifecycle.loadPage('About', undefined, undefined, $scope, function() {
            $scope.github = "https://github.com/apiman/apiman";
            $scope.site = "https://apiman.io/";
            $scope.userGuide = "https://www.apiman.io/latest/user-guide.html";
            $scope.tutorials = "https://www.apiman.io/latest/tutorials.html";
            $scope.version = Configuration.apiman.version;
            $scope.builtOn = Configuration.apiman.builtOn;
            $scope.apiEndpoint = Configuration.api.endpoint;
            PageLifecycle.setPageTitle('about');
        });
    }]);