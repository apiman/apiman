/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var UserServicesController = _module.controller("Apiman.UserServicesController",
        ['$q', '$scope', '$location', 'UserSvcs', 'PageLifecycle', 
        ($q, $scope, $location, UserSvcs, PageLifecycle) => {
            var params = $location.search();
            $scope.tab = 'services';

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
                services: $q(function(resolve, reject) {
                    UserSvcs.query({ user: params.user, entityType: 'services' }, function(userServices) {
                        resolve(userServices);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('UserServices', promise, $scope);
    }])

}
