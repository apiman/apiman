import {_module} from "./apimanPlugin";

_module
    .controller("Apiman.DashController",
    ['$scope', 'PageLifecycle', 'CurrentUser', 'Configuration',
    function ($scope, PageLifecycle, CurrentUser, Configuration) {
        PageLifecycle.loadPage('Dash', undefined, undefined, $scope, function() {
            PageLifecycle.setPageTitle('dashboard');
        });
    }]);


