/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AdminRolesController = _module.controller("Apiman.AdminRolesController",
        ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', 
        ($q, $scope, ApimanSvcs, PageLifecycle) => {
            $scope.tab = 'roles';
            $scope.filterRoles = function(value) {
                if (!value) {
                    $scope.filteredRoles = $scope.roles;
                } else {
                    var filtered = [];
                    angular.forEach($scope.roles, function(role) {
                        if (role.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(role);
                        }
                    });
                    $scope.filteredRoles = filtered;
                }
            };
            
            var pageData = {
                roles: $q(function(resolve, reject) {
                    ApimanSvcs.query({ entityType: 'roles' }, function(adminRoles) {
                        $scope.filteredRoles = adminRoles;
                        resolve(adminRoles);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('AdminRoles', 'admin', pageData, $scope, function() {
                PageLifecycle.setPageTitle('admin-roles');
            });
    }])

}
