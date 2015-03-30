/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AdminPluginsController = _module.controller("Apiman.AdminPluginsController",
        ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', ($q, $scope, ApimanSvcs, PageLifecycle) => {
            var promise = $q.all({
                plugins: $q(function(resolve, reject) {
                    ApimanSvcs.query({ entityType: 'plugins' }, function(adminPlugins) {
                        resolve(adminPlugins);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('AdminPlugins', promise, $scope, function() {
                PageLifecycle.setPageTitle('admin-plugins');
            });
    }])

}
