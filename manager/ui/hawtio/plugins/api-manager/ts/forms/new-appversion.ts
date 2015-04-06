/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewAppVersionController = _module.controller("Apiman.NewAppVersionController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'PageLifecycle', '$routeParams',
        ($q, $location, $scope, OrgSvcs, PageLifecycle, $routeParams) => {
            var params = $routeParams;
            $scope.appversion = {
                clone: true,
                cloneVersion: params.version
            };
            $scope.saveNewAppVersion = function() {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: ''}, $scope.appversion, function(reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/apps/{1}/{2}', params.org, params.app, reply.version);
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('NewAppVersion', undefined, $scope, function() {
                PageLifecycle.setPageTitle('new-app-version');
                $scope.$applyAsync(function() {
                    $('#apiman-version').focus();
                });
            });
        }]);

}
