/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var UserServicesController = _module.controller("Apiman.UserServicesController",
        ['$q', '$scope', 'UserSvcs', 'PageLifecycle', ($q, $scope, UserSvcs, PageLifecycle) => {
            var promise = $q.all({
                services: $q(function(resolve, reject) {
                    UserSvcs.query({ entityType: 'services' }, function(userServices) {
                        resolve(userServices);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('UserServices', promise, $scope);
    }])

}
