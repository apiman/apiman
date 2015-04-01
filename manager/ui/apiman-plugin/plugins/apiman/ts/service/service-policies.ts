/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServicePoliciesController = _module.controller("Apiman.ServicePoliciesController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'Dialogs', '$routeParams',
        ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, Dialogs, $routeParams) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'policies';
            $scope.version = params.version;

            var removePolicy = function(policy) {
                angular.forEach($scope.policies, function(p, index) {
                    if (policy === p) {
                        $scope.policies.splice(index, 1);
                    }
                });
            };

            $scope.removePolicy = function(policy) {
                Dialogs.confirm('Confirm Remove Policy', 'Do you really want to remove this policy from the service?', function() {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'services', entityId:params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies', policyId: policy.id }, function(reply) {
                        removePolicy(policy);
                    }, PageLifecycle.handleError);
                });
            };
            
            var dataLoad = ServiceEntityLoader.getCommonData($scope, $location);
            dataLoad = angular.extend(dataLoad, {
                policies: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies' }, resolve, reject);
                })
            });
            var promise = $q.all(dataLoad);

            PageLifecycle.loadPage('ServicePolicies', promise, $scope, function() {
                PageLifecycle.setPageTitle('service-policies', [ $scope.service.name ]);
            });
        }])

}
