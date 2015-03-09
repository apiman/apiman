/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var UserAppsController = _module.controller("Apiman.UserAppsController",
        ['$q', '$scope', 'UserSvcs', 'PageLifecycle', ($q, $scope, UserSvcs, PageLifecycle) => {
            var promise = $q.all({
                applications: $q(function(resolve, reject) {
                    UserSvcs.query({ entityType: 'applications' }, function(userApps) {
                        resolve(userApps);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('UserApps', promise, $scope);
    }])

}
