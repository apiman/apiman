/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AdminPolicyDefsController = _module.controller("Apiman.AdminPolicyDefsController",
        ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', ($q, $scope, ApimanSvcs, PageLifecycle) => {
            
            $scope.filterPolicies = function(value) {
                if (!value) {
                    $scope.filteredPolicyDefs = $scope.policyDefs;
                } else {
                    var filtered = [];
                    for (var i = 0; i < $scope.policyDefs.length; i++) {
                        var policyDef = $scope.policyDefs[i];
                        if (policyDef.name.toLowerCase().indexOf(value) > -1) {
                            filtered.push(policyDef);
                        }
                    }
                    $scope.filteredPolicyDefs = filtered;
                }
            };
            
            var promise = $q.all({
                policyDefs: $q(function(resolve, reject) {
                    ApimanSvcs.query({ entityType: 'policyDefs' }, function(policyDefs) {
                        $scope.filteredPolicyDefs = policyDefs;
                        resolve(policyDefs);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('AdminPolicyDefs', promise, $scope, function() {
                PageLifecycle.setPageTitle('admin-policyDefs');
            });
    }])

}
