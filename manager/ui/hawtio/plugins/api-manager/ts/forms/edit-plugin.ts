/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var EditPluginController = _module.controller("Apiman.EditPluginController",
        ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', 'Dialogs', '$routeParams',
        ($q, $scope, $location, ApimanSvcs, PageLifecycle, Dialogs, $routeParams) => {
            var params = $routeParams;
            
            var pageData = {
                plugin: $q(function(resolve, reject) {
                    ApimanSvcs.get({ entityType: 'plugins', secondaryType: params.plugin }, function(plugin) {
                        resolve(plugin);
                    }, reject);
                })
            };
            
            PageLifecycle.loadPage('EditPlugin', pageData, $scope, function() {
                PageLifecycle.setPageTitle('plugin-details');
            });
    }])

}
