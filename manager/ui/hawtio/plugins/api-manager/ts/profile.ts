/// <reference path="apimanPlugin.ts"/>
/// <reference path="rpc.ts"/>
module Apiman {
    
    export var UserProfileController = _module.controller("Apiman.UserProfileController",
        ['$q', '$rootScope', '$scope', '$location', 'CurrentUserSvcs', 'PageLifecycle',
        ($q, $rootScope, $scope, $location, CurrentUserSvcs, PageLifecycle) => {
            var pageData = {
                user: $q(function(resolve, reject) {
                    CurrentUserSvcs.get({ what: 'info' }, resolve, reject);
                })
            };

            $rootScope.isDirty = false;
            $scope.isValid = true;
            $scope.updatedUser = {
                fullName: undefined,
                email: undefined
            };
            
            $scope.$watch('updatedUser', function(newValue) {
                var dirty = false;
                var valid = true;
                if (!newValue.fullName) {
                    valid = false;
                }
                if (!newValue.email) {
                    valid = false;
                }
                if (newValue.fullName != $scope.user.fullName) {
                    dirty = true;
                }
                if (newValue.email != $scope.user.email) {
                    dirty = true;
                }
                
                $rootScope.isDirty = dirty;
                $scope.isValid = valid;
            }, true);
            
            $scope.save = function() {
                $scope.updateButton.state = 'in-progress';
                CurrentUserSvcs.update({ what: 'info' }, $scope.updatedUser, function() {
                    $scope.updateButton.state = 'complete';
                    $scope.user.fullName = $scope.updatedUser.fullName;
                    $scope.user.email = $scope.updatedUser.email;
                    $scope.isValid = true;
                    $rootScope.isDirty = false;
                }, PageLifecycle.handleError);
            }
            
            PageLifecycle.loadPage('UserProfile', undefined, pageData, $scope, function() {
                $scope.updatedUser.fullName = $scope.user.fullName;
                $scope.updatedUser.email = $scope.user.email;
                PageLifecycle.setPageTitle('user-profile');
            });
        }]);

}
