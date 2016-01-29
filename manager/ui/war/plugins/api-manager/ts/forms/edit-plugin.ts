/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var EditPluginController = _module.controller("Apiman.EditPluginController",
        ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', 'Dialogs', '$routeParams', 'Logger',
        ($q, $scope, $location, ApimanSvcs, PageLifecycle, Dialogs, $routeParams, Logger) => {
            var params = $routeParams;
            
            var pageData = {
                plugin: $q(function(resolve, reject) {
                    ApimanSvcs.get({ entityType: 'plugins', secondaryType: params.plugin }, function(plugin) {
                        plugin.isSnapshot = plugin.version.indexOf("-SNAPSHOT", plugin.version.length - "-SNAPSHOT".length) !== -1;
                        resolve(plugin);
                    }, reject);
                })
            };
            
            $scope.reload = function() {
                Logger.debug("Now reloading the plugin!");
                $scope.reloadButton.state = 'in-progress';
                var body = {
                    groupId : $scope.plugin.groupId,
                    artifactId : $scope.plugin.artifactId,
                    version : $scope.plugin.version,
                    classifier : $scope.plugin.classifier,
                    type : $scope.plugin.type
                };
                ApimanSvcs.save({ entityType: 'plugins' }, body, function(reply) {
                     PageLifecycle.redirectTo('/admin/plugins');
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('EditPlugin', 'admin', pageData, $scope, function() {
                PageLifecycle.setPageTitle('plugin-details');
            });
    }])

}
