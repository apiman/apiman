/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServicePoliciesController = _module.controller("Apiman.ServicePoliciesController",
        ['$q', '$scope', '$location', '$route', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs', 'Dialogs',
        ($q, $scope, $location, $route, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs, Dialogs) => {
            var params = $location.search();
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
                    }, function(error) {
                        if (error.status == 409) {
                            $location.url('apiman/error-409.html');
                        } else {
                            alert("ERROR=" + error.status + " " + error.statusText);
                        }
                    });
                });
            };
            
            var dataLoad = ServiceEntityLoader.getCommonData($scope, $location);
            dataLoad = angular.extend(dataLoad, {
                policies: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies' }, function(policies) {
                        resolve(policies);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            var promise = $q.all(dataLoad);

            PageLifecycle.loadPage('ServicePolicies', promise, $scope);
        }])

}
