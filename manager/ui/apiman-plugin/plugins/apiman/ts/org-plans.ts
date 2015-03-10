/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var OrgPlansController = _module.controller("Apiman.OrgPlansController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', 'Logger', ($q, $scope, $location, OrgSvcs, PageLifecycle, Logger) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            var promise = $q.all({
                org: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: '' }, function(org) {
                        resolve(org);
                    }, function(error) {
                        reject(error);
                    });
                }),
                members: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function(members) {
                        resolve(members);
                    }, function(error) {
                        reject(error);
                    });
                }),
                plans: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'plans' }, function(plans) {
                        resolve(plans);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('OrgPlans', promise, $scope);
        }]);

}
