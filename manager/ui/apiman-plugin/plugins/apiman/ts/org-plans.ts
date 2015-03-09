/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var OrgPlansController = _module.controller("Apiman.OrgPlansController",
        ['$q', '$scope', '$location', 'Logger', 'OrgSvcs', 'PageLifecycle', ($q, $scope, $location, Logger, OrgSvcs, PageLifecycle) => {
            var params = $location.search();
            var promise = $q.all({
                org: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: '' }, function(org) {
                        Logger.log("(org-plans) :: Loaded org.");
                        resolve(org);
                    }, function(error) {
                        reject(error);
                    });
                }),
                members: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function(members) {
                        Logger.log("(org-plans) :: Loaded members.");
                        resolve(members);
                    }, function(error) {
                        reject(error);
                    });
                }),
                plans: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'plans' }, function(plans) {
                        Logger.log("(org-plans) :: Loaded plans.");
                        resolve(plans);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('OrgPlans', promise, $scope);
        }]);

}
