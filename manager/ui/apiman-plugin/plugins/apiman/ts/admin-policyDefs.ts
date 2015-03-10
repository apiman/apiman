/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var AdminPolicyDefsController = _module.controller("Apiman.AdminPolicyDefsController",
        ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', ($q, $scope, ApimanSvcs, PageLifecycle) => {
            var promise = $q.all({
                policyDefs: $q(function(resolve, reject) {
                    ApimanSvcs.query({ entityType: 'policyDefs' }, function(policyDefs) {
                        resolve(policyDefs);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('AdminPolicyDefs', promise, $scope);
    }])

}
