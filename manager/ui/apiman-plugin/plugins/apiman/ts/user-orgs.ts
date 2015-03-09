/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var UserOrgsController = _module.controller("Apiman.UserOrgsController",
        ['$q', '$scope', 'UserSvcs', 'PageLifecycle', ($q, $scope, UserSvcs, PageLifecycle) => {
            var promise = $q.all({
                organizations: $q(function(resolve, reject) {
                    UserSvcs.query({ entityType: 'organizations' }, function(userOrgs) {
                        resolve(userOrgs);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('UserOrgs', promise, $scope);
    }])

}
