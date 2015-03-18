/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var UserOrgsController = _module.controller("Apiman.UserOrgsController",
        ['$q', '$scope', '$location', 'UserSvcs', 'PageLifecycle', 
        ($q, $scope, $location, UserSvcs, PageLifecycle) => {
            var params = $location.search();
            $scope.tab = 'organizations';

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
                organizations: $q(function(resolve, reject) {
                    UserSvcs.query({ user: params.user, entityType: 'organizations' }, function(userOrgs) {
                        resolve(userOrgs);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('UserOrgs', promise, $scope);
    }])

}
