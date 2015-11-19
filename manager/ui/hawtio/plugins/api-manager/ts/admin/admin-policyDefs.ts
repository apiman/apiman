/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AdminPolicyDefsController = _module.controller("Apiman.AdminPolicyDefsController",
        ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', ($q, $scope, ApimanSvcs, PageLifecycle) => {
            $scope.tab = 'policyDefs';
            $scope.filterPolicies = function(value) {
                if (!value) {
                    $scope.filteredPolicyDefs = $scope.policyDefs;
                } else {
                    var filtered = [];
                    angular.forEach($scope.policyDefs, function(policyDef) {
                        if (policyDef.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(policyDef);
                        }
                    });
                    $scope.filteredPolicyDefs = filtered;
                }
            };
            
            var pageData = {
                policyDefs: $q(function(resolve, reject) {
                    ApimanSvcs.query({ entityType: 'policyDefs' }, function(policyDefs) {
                        $scope.filteredPolicyDefs = policyDefs;
                        resolve(policyDefs);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('AdminPolicyDefs', 'admin', pageData, $scope, function() {
                PageLifecycle.setPageTitle('admin-policyDefs');
            });
    }])

}
