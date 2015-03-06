/// <reference path="apimanPlugin.ts"/>
module Apiman {

    export var NewPlanController = _module.controller("Apiman.NewPlanController",
        ['$location', '$scope', 'UserSvcs', 'OrgSvcs', ($location, $scope, UserSvcs, OrgSvcs) => {
            $scope.plan.initialVersion = '1.0';
            
            UserSvcs.query({ entityType: 'organizations' }, function(userOrgs) {
                $scope.organizations = userOrgs;
                $scope.selectedOrg = $scope.organizations[0];
            }, function(error) {
                alert("ERROR=" + error);
            });
            $scope.setOrg = function(org) {
                $scope.selectedOrg = org;
            };
            $scope.saveNewPlan = function() {
                OrgSvcs.save({ organizationId: $scope.selectedOrg.id, entityType: 'plans' }, $scope.plan, function(reply) {
                    $location.path(Apiman.pluginName + '/plan-overview.html').search('org', reply.organization.id).search('plan', reply.name).search('version', $scope.plan.initialVersion);
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            };
        }]);

}
