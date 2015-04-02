/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {
    
    export var UserProfileController = _module.controller("Apiman.UserProfileController",
        ['$q', '$scope', '$location', 'CurrentUserSvcs', 'PageLifecycle',
        ($q, $scope, $location, CurrentUserSvcs, PageLifecycle) => {
            var promise = $q.all({
                user: $q(function(resolve, reject) {
                    CurrentUserSvcs.get({ what: 'info' }, resolve, reject);
                })
            });

            $scope.isDirty = false;
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
                
                $scope.isDirty = dirty;
                $scope.isValid = valid;
            }, true);
            
            $scope.save = function() {
                $scope.updateButton.state = 'in-progress';
                CurrentUserSvcs.update({ what: 'info' }, $scope.updatedUser, function() {
                    $scope.updateButton.state = 'complete';
                    $scope.user.fullName = $scope.updatedUser.fullName;
                    $scope.user.email = $scope.updatedUser.email;
                    $scope.isValid = true;
                    $scope.isDirty = false;
                }, PageLifecycle.handleError);
            }
            
            PageLifecycle.loadPage('UserProfile', promise, $scope, function() {
                $scope.updatedUser.fullName = $scope.user.fullName;
                $scope.updatedUser.email = $scope.user.email;
                PageLifecycle.setPageTitle('user-profile');
            });
        }]);

}
