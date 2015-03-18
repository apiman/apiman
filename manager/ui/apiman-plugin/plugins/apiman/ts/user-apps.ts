/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var UserAppsController = _module.controller("Apiman.UserAppsController",
        ['$q', '$scope', '$location', 'UserSvcs', 'PageLifecycle', 'Logger',
        ($q, $scope, $location, UserSvcs, PageLifecycle, Logger) => {
            var params = $location.search();
            $scope.tab = 'applications';

            var promise = $q.all({
                user: $q(function(resolve, reject) {
                    UserSvcs.get({ user: params.user }, function(user) {
                        if (!user.fullName) {
                            user.fullName = user.username;
                        }
                        resolve(user);
                    }, function(error) {
                        reject(error);
                    });
                }),
                applications: $q(function(resolve, reject) {
                    UserSvcs.query({ user: params.user, entityType: 'applications' }, function(userApps) {
                        resolve(userApps);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('UserApps', promise, $scope);
    }])

}
