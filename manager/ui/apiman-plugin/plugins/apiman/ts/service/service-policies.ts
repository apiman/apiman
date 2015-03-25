/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServicePoliciesController = _module.controller("Apiman.ServicePoliciesController",
        ['$q', '$scope', '$location', '$route', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs',
         ($q, $scope, $location, $route, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            $scope.tab = 'policies';
            $scope.version = params.version;
            var lockedPlans = [];
            
            var dataLoad = ServiceEntityLoader.getCommonData($scope, $location);
            if (params.version != null) {
                dataLoad = angular.extend(dataLoad, {
                    policies: $q(function(resolve, reject) {
                        OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies' }, function(policies) {
                            resolve(policies);
                        }, function(error) {
                            reject(error);
                        });
                    })
                });
            }
            var promise = $q.all(dataLoad);
             
            $scope.removePolicy = function( policy ) {
                var policyId = policy.id;
                OrgSvcs.delete({ organizationId: params.org, entityType: 'services', entityId:params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies', policyId: policy.id }, function(reply) {
                    $route.reload();
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        //$scope.saveButton.state = 'error';
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            };
            
            PageLifecycle.loadPage('ServicePolicies', promise, $scope);
        }])

}
