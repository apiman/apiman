/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var OrgServicesController = _module.controller("Apiman.OrgServicesController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', ($q, $scope, $location, OrgSvcs, PageLifecycle) => {
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
                services: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'services' }, function(services) {
                        resolve(services);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('OrgSvcs', promise, $scope);
        }])

}
