/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewPlanController = _module.controller("Apiman.NewPlanController",
        ['$q', '$location', '$scope', 'CurrentUser', 'UserSvcs', 'OrgSvcs', 'PageLifecycle', '$rootScope',
        ($q, $location, $scope, CurrentUser, UserSvcs, OrgSvcs, PageLifecycle, $rootScope) => {
            var recentOrg = $rootScope.mruOrg;

            var pageData = {
                organizations: $q(function(resolve, reject) {
                    return CurrentUser.getCurrentUser().then(function (currentUser) {
                        return UserSvcs.query({ user: currentUser.username, entityType: 'planorgs' }, function(orgs) {
                            if (recentOrg) {
                                $scope.selectedOrg = recentOrg;
                            } else if (orgs.length > 0) {
                                $scope.selectedOrg = orgs[0];
                            }
                            resolve(orgs);
                        }, reject);
                    })
                })
            };
            
            $scope.setOrg = function(org) {
                $scope.selectedOrg = org;
            };
            $scope.saveNewPlan = function() {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: $scope.selectedOrg.id, entityType: 'plans' }, $scope.plan, function(reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/plans/{1}/{2}', reply.organization.id, reply.id, $scope.plan.initialVersion);
                }, PageLifecycle.handleError);
            };
            
            // Initialize the model - the default initial version for a new plan is always 1.0
            $scope.plan = {
                initialVersion: '1.0'
            };
            
            PageLifecycle.loadPage('NewPlan', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('new-plan');
                $scope.$applyAsync(function() {
                    $('#apiman-entityname').focus();
                });
            });
        }]);

}
