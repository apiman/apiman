/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var PlanPoliciesController = _module.controller("Apiman.PlanPoliciesController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'ApimanSvcs', 'Logger', 'PageLifecycle', 'PlanEntityLoader', 
        ($q, $scope, $location, OrgSvcs, ApimanSvcs, Logger, PageLifecycle, PlanEntityLoader) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            $scope.tab = 'policies';

            $scope.removePolicy = function(policy) {
                alert("policyId=" + policy.id);
                OrgSvcs.delete({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies', policyId: policy.id }, function(reply) {
                    $location.path(Apiman.pluginName + '/plan-overview.html').search('org', reply.organization.id).search('plan', reply.name).search('version', $scope.plan.initialVersion);
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            };

            var dataLoad = PlanEntityLoader.getCommonData($scope, $location);
            angular.extend(dataLoad, {
                policies: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies' }, function(policies) {
                        resolve(policies);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            var promise = $q.all(dataLoad);
            PageLifecycle.loadPage('PlanPolicies', promise, $scope);
        }])


}
