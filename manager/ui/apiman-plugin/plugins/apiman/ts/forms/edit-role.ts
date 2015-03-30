/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var EditRoleController = _module.controller("Apiman.EditRoleController",
        ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', 'Logger', 'Dialogs',
        ($q, $scope, $location, ApimanSvcs, PageLifecycle, Logger, Dialogs) => {
            var params = $location.search();
            var allPermissions     = ['orgView', 'orgEdit', 'orgAdmin',
                                      'planView','planEdit','planAdmin',
                                      'svcView', 'svcEdit', 'svcAdmin',
                                      'appView', 'appEdit', 'appAdmin'];
            $scope.isValid = true;
            $scope.rolePermissions = {};
            angular.forEach(allPermissions, function(value) {
                $scope.rolePermissions[value] = false;
            });
            
            var validate = function() {
                var atLeastOne = false;
                angular.forEach($scope.rolePermissions, function(value,key) {
                    if (value == true) {
                        atLeastOne = true;
                    }
                });
                return atLeastOne;
            };
            
            $scope.$watch('rolePermissions', function(newValue) {
                $scope.isValid = validate();
            }, true);
            
            var promise = $q.all({
                role: $q(function(resolve, reject) {
                    ApimanSvcs.get({ entityType: 'roles', secondaryType: params.role }, function(role) {
                        angular.forEach(role.permissions, function(name) {
                            $scope.rolePermissions[name] = true;
                        });
                        resolve(role);
                    }, reject);
                })
            });
            
            $scope.updateRole  = function() {
                $scope.updateButton.state = 'in-progress';
                var permissions = [];
                angular.forEach($scope.rolePermissions, function(value,key) {
                    if (value == true) {
                        permissions.push(key);
                    }
                });
                var role:any = {};
                role.name = $scope.role.name;
                role.description = $scope.role.description;
                role.permissions = permissions;
                role.autoGrant = $scope.role.autoGrant;
                ApimanSvcs.update({ entityType: 'roles', secondaryType: $scope.role.id }, role, function(reply) {
                     $location.url(pluginName + '/admin-roles.html');
                }, PageLifecycle.handleError);
            }
            
            $scope.deleteRole  = function() {
                $scope.deleteButton.state = 'in-progress';
                Dialogs.confirm('Confirm Delete Role', 'Do you really want to delete this role?', function() {
                    ApimanSvcs.delete({ entityType: 'roles', secondaryType: $scope.role.id }, function(reply) {
                        $location.url(pluginName + '/admin-roles.html');
                    }, PageLifecycle.handleError);
                }, function() {
                    $scope.deleteButton.state = 'complete';
                });
            }
            
            PageLifecycle.loadPage('EditRole', promise, $scope, function() {
                PageLifecycle.setPageTitle('edit-role');
                $('#apiman-description').focus();
            });
    }])

}
