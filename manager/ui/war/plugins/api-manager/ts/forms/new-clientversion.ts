/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewClientVersionController = _module.controller("Apiman.NewClientVersionController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'PageLifecycle', '$routeParams',
        ($q, $location, $scope, OrgSvcs, PageLifecycle, $routeParams) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.clientversion = {
                clone: true,
                cloneVersion: params.version
            };
            $scope.saveNewClientVersion = function() {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: params.org, entityType: 'clients', entityId: params.client, versionsOrActivity: 'versions', version: ''}, $scope.clientversion, function(reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/clients/{1}/{2}', params.org, params.client, reply.version);
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('NewClientVersion', 'clientEdit', undefined, $scope, function() {
                PageLifecycle.setPageTitle('new-client-version');
                $scope.$applyAsync(function() {
                    $('#apiman-version').focus();
                });
            });
        }]);

}
