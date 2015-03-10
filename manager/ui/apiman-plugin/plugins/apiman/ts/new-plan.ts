/// <reference path="apimanPlugin.ts"/>
module Apiman {

    export var NewPlanController = _module.controller("Apiman.NewPlanController",
        ['$q', '$location', '$scope', 'UserSvcs', 'OrgSvcs', 'PageLifecycle', ($q, $location, $scope, UserSvcs, OrgSvcs, PageLifecycle) => {
            var promise = $q.all({
                organizations: $q(function(resolve, reject) {
                    UserSvcs.query({ entityType: 'organizations' }, function(userOrgs) {
                        $scope.selectedOrg = userOrgs[0];
                        resolve(userOrgs);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            
            $scope.setOrg = function(org) {
                $scope.selectedOrg = org;
            };
            $scope.saveNewPlan = function() {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: $scope.selectedOrg.id, entityType: 'plans' }, $scope.plan, function(reply) {
                    $location.path(Apiman.pluginName + '/plan-overview.html').search('org', reply.organization.id).search('plan', reply.name).search('version', $scope.plan.initialVersion);
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        $scope.createButton.state = 'error';
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            };
            
            // Initialize the model - the default initial version for a new plan is always 1.0
            $scope.plan = {
                initialVersion: '1.0'
            };
            
            PageLifecycle.loadPage('NewPlan', promise, $scope);
        }]);

}
