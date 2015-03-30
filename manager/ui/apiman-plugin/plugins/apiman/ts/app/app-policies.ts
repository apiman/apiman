/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AppPoliciesController = _module.controller("Apiman.AppPoliciesController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', 'OrgSvcs', 'Dialogs', 
        ($q, $scope, $location, PageLifecycle, AppEntityLoader, OrgSvcs, Dialogs) => {
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
                Dialogs.confirm('Confirm Remove Policy', 'Do you really want to remove this policy from the application?', function() {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies', policyId: policy.id }, function(reply) {
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
            
            var dataLoad = AppEntityLoader.getCommonData($scope, $location);
            dataLoad = angular.extend(dataLoad, {
                policies: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies' }, function(policies) {
                        resolve(policies);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            var promise = $q.all(dataLoad);

            PageLifecycle.loadPage('AppPolicies', promise, $scope, function() {
                PageLifecycle.setPageTitle('app-policies', [ $scope.app.name ]);
            });
        }])

}
