/// <reference path="apimanPlugin.ts"/>
module Apiman {

    export var DashController = _module.controller("Apiman.DashController",
        ['$scope', 'PageLifecycle', 'CurrentUser', 'Configuration', 
        ($scope, PageLifecycle, CurrentUser, Configuration) => {
            PageLifecycle.loadPage('Dash', undefined, undefined, $scope, function() {
                $scope.noCreateOrg = Configuration.ui.platform == 'f8' || Configuration.ui.platform == 'ose' || 
                    (Configuration.ui.adminOnlyOrgCreation == true && !$scope.isAdmin);
                PageLifecycle.setPageTitle('dashboard');
            });
        }]);

}
