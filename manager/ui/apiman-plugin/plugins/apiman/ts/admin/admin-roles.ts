/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AdminRolesController = _module.controller("Apiman.AdminRolesController",
        ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', 
        ($q, $scope, ApimanSvcs, PageLifecycle) => {
            
            $scope.filterRoles = function(value) {
                if (!value) {
                    $scope.filteredRoles = $scope.roles;
                } else {
                    var filtered = [];
                    for (var i = 0; i < $scope.roles.length; i++) {
                        var role = $scope.roles[i];
                        if (role.name.toLowerCase().indexOf(value) > -1) {
                            filtered.push(role);
                        }
                    }
                    $scope.filteredRoles = filtered;
                }
            };
            
            var promise = $q.all({
                roles: $q(function(resolve, reject) {
                    ApimanSvcs.query({ entityType: 'roles' }, function(adminRoles) {
                        $scope.filteredRoles = adminRoles;
                        resolve(adminRoles);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('AdminRoles', promise, $scope, function() {
                PageLifecycle.setPageTitle('admin-roles');
            });
    }])

}
