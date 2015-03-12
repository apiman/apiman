/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var EditRoleController = _module.controller("Apiman.EditRoleController",
        ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', ($q, $scope, $location, ApimanSvcs, PageLifecycle) => {
            
            var params = $location.search();
            
            var allPermissions     = ['orgView','orgEdit','orgAdmin',
                                      'planView','planEdit','planAdmin',
                                      'svcView','svcEdit','svcAdmin',
                                      'appView','appEdit','appAdmin'];
            $scope.rolePermissions = {};
            for (var i=0; i<allPermissions.length; i++) {
                $scope.rolePermissions[allPermissions[i]] = false;
            }
            
            var promise = $q.all({
                role: $q(function(resolve, reject) {
                    ApimanSvcs.get({ entityType: 'roles', secondaryType: params.role }, function(role) {
                        resolve(role);
                        
                        for (var i=0; i<role.permissions.length; i++) {
                            var name = role.permissions[i];
                            $scope.rolePermissions[name]=true;
                        }
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            
            $scope.updateRole  = function() {
                
                var permissions = [];
                for (var key in $scope.rolePermissions) {
                    if ($scope.rolePermissions[key] == true) {
                        permissions.push(key);
                    }
                }
                var role:any = {};
                role.name = $scope.role.name;
                role.description = $scope.role.description;
                role.permissions = permissions;
                role.autoGrant = $scope.role.autoGrant;
                ApimanSvcs.update({ entityType: 'roles', secondaryType: $scope.role.id }, role, function(reply) {
                     $location.path(pluginName + '/admin-roles.html');
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        $scope.createButton.state = 'error';
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            }
            
            $scope.deleteRole  = function() {
                ApimanSvcs.delete({ entityType: 'roles', secondaryType: $scope.role.id }, function(reply) {
                     $location.path(pluginName + '/admin-roles.html');
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        $scope.createButton.state = 'error';
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            }
            
            PageLifecycle.loadPage('EditRole', promise, $scope);
    }])

}
