/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewPlanVersionController = _module.controller("Apiman.NewPlanVersionController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'PageLifecycle', '$routeParams',
        ($q, $location, $scope, OrgSvcs, PageLifecycle, $routeParams) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.planversion = {
                clone: true,
                cloneVersion: params.version
            };
            $scope.saveNewPlanVersion = function() {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: ''}, $scope.planversion, function(reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/plans/{1}/{2}', params.org, params.plan, reply.version);
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('NewPlanVersion', 'planEdit', undefined, $scope, function() {
                PageLifecycle.setPageTitle('new-plan-version');
                $scope.$applyAsync(function() {
                    $('#apiman-version').focus();
                });
            });
        }]);

}
