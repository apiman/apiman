/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var EditRoleController = _module.controller("Apiman.EditRoleController",
        [
            '$q', 
            '$scope', 
            '$location',
            '$uibModal',
            'ApimanSvcs', 
            'PageLifecycle', 
            'Logger',  
            '$routeParams',
        ($q, $scope, $location, $uibModal, ApimanSvcs, PageLifecycle, Logger, $routeParams) => {
            var params = $routeParams;
            var allPermissions     = ['orgView', 'orgEdit', 'orgAdmin',
                                      'planView','planEdit','planAdmin',
                                      'apiView', 'apiEdit', 'apiAdmin',
                                      'clientView', 'clientEdit', 'clientAdmin'];
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
            
            var pageData = {
                role: $q(function(resolve, reject) {
                    ApimanSvcs.get({ entityType: 'roles', secondaryType: params.role }, function(role) {
                        angular.forEach(role.permissions, function(name) {
                            $scope.rolePermissions[name] = true;
                        });
                        resolve(role);
                    }, reject);
                })
            };
            
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
                     PageLifecycle.redirectTo('/admin/roles');
                }, PageLifecycle.handleError);
            };
            
            $scope.deleteRole  = function(size) {
                $scope.deleteButton.state = 'in-progress';

                var options = {
                    message: 'Do you really want to delete this role?',
                    title: 'Confirm Delete Role'
                };

                $scope.animationsEnabled = true;

                $scope.toggleAnimation = function () {
                    $scope.animationsEnabled = !$scope.animationsEnabled;
                };

                var modalInstance = $uibModal.open({
                    animation: $scope.animationsEnabled,
                    templateUrl: 'confirmModal.html',
                    controller: 'ModalConfirmCtrl',
                    size: size,
                    resolve: {
                        options: function () {
                            return options;
                        }
                    }
                });

                modalInstance.result.then(function () {
                    ApimanSvcs.delete({ entityType: 'roles', secondaryType: $scope.role.id }, function(reply) {
                        PageLifecycle.redirectTo('/admin/roles');
                    }, PageLifecycle.handleError);
                }, function () {
                    //console.log('Modal dismissed at: ' + new Date());
                    $scope.deleteButton.state = 'complete';
                });
            };
            
            PageLifecycle.loadPage('EditRole', 'admin', pageData, $scope, function() {
                PageLifecycle.setPageTitle('edit-role');
                $('#apiman-description').focus();
            });
    }])

}
