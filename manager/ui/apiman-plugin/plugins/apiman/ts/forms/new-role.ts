/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewRoleController = _module.controller("Apiman.NewRoleController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'PageLifecycle', 'CurrentUser', 'Logger', 'ApimanSvcs',
        ($q, $location, $scope, OrgSvcs, PageLifecycle, CurrentUser, Logger, ApimanSvcs) => {
            $scope.role = {};
            $scope.rolePermissions = {};
            $scope.isValid = false;
            
            var validate = function() {
                var valid = true;
                if (!$scope.role.name) {
                    valid = false;
                }
                var atLeastOne = false;
                angular.forEach($scope.rolePermissions, function(value,key) {
                    if (value == true) {
                        atLeastOne = true;
                    }
                });
                if (!atLeastOne) {
                    valid = false;
                }
                $scope.isValid = valid;
            };
            
            $scope.$watch('role', function(newValue) {
                validate();
            }, true);
            $scope.$watch('rolePermissions', function(newValue) {
                validate();
            }, true);
            
            $scope.addRole = function() {
                $scope.createButton.state = 'in-progress';
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
                ApimanSvcs.save({ entityType: 'roles' }, role, function(reply) {
                     $location.url(pluginName + '/admin-roles.html');
                }, function(error) {
                    if (error.status == 409) {
                        $location.url('apiman/error-409.html');
                    } else {
                        $scope.createButton.state = 'error';
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                    $scope.createButton.state = 'error';
                });
            }
            
            PageLifecycle.loadPage('NewRole', undefined, $scope, function() {
                PageLifecycle.setPageTitle('new-role');
                $('#apiman-entityname').focus();
            });
        }]);

}
