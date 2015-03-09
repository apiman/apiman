/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var OrgAppsController = _module.controller("Apiman.OrgAppsController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', ($q, $scope, $location, OrgSvcs, PageLifecycle) => {
            var params = $location.search();
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
                apps: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'applications' }, function(apps) {
                        resolve(apps);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('OrgApps', promise, $scope);
        }])

}
