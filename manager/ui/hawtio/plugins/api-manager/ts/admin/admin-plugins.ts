/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AdminPluginsController = _module.controller("Apiman.AdminPluginsController",
        ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', ($q, $scope, ApimanSvcs, PageLifecycle) => {
            var pageData = {
                plugins: $q(function(resolve, reject) {
                    ApimanSvcs.query({ entityType: 'plugins' }, function(adminPlugins) {
                        resolve(adminPlugins);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('AdminPlugins', pageData, $scope, function() {
                PageLifecycle.setPageTitle('admin-plugins');
            });
    }])

}
