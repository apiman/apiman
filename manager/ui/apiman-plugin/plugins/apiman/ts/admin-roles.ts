/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var AdminRolesController = _module.controller("Apiman.AdminRolesController",
        ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', ($q, $scope, ApimanSvcs, PageLifecycle) => {
            var promise = $q.all({
                roles: $q(function(resolve, reject) {
                    ApimanSvcs.query({ entityType: 'roles' }, function(adminRoles) {
                        resolve(adminRoles);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('AdminRoles', promise, $scope);
    }])

}
