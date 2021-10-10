import {_module} from "../apimanPlugin";

_module.controller("Apiman.UserProfileController",
    ['$q', '$rootScope', '$scope', '$location', 'UserSvcs','CurrentUserSvcs', 'CurrentUser', 'PageLifecycle',
    function ($q, $rootScope, $scope, $location, UserSvcs, CurrentUserSvcs, CurrentUser, PageLifecycle) {
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

            if ($scope.user
                && $scope.user.fullName
                && newValue.fullName != $scope.user.fullName) {
                dirty = true;
            }

            if ($scope.user
                && $scope.user.email
                && newValue.email != $scope.user.email) {
                dirty = true;
            }

            $rootScope.isDirty = dirty;
            $scope.isValid = valid;
        }, true);

        $scope.cancel = function() {
            $rootScope.isDirty = false;
            $location.path($rootScope.pluginName);
        };

        $scope.save = function() {
            $scope.updateButton.state = 'in-progress';
            CurrentUser.getCurrentUser().then(function (currentUser) {
                UserSvcs.update({ user: currentUser.username }, $scope.updatedUser, function() {
                    $scope.updateButton.state = 'complete';
                    $scope.user.fullName = $scope.updatedUser.fullName;
                    $scope.user.email = $scope.updatedUser.email;
                    $scope.isValid = true;
                    $rootScope.isDirty = false;
                }, PageLifecycle.handleError);
            });
        };

        PageLifecycle.loadPage('UserProfile', undefined, pageData, $scope, function() {
            $scope.updatedUser.fullName = $scope.user.fullName;
            $scope.updatedUser.email = $scope.user.email;
            PageLifecycle.setPageTitle('user-profile');
        });
    }]);