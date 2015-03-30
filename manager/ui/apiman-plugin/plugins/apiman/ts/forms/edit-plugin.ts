/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var EditPluginController = _module.controller("Apiman.EditPluginController",
        ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', 'Dialogs',
        ($q, $scope, $location, ApimanSvcs, PageLifecycle, Dialogs) => {
            
            var params = $location.search();
            
            var promise = $q.all({
                plugin: $q(function(resolve, reject) {
                    ApimanSvcs.get({ entityType: 'plugins', secondaryType: params.plugin }, function(plugin) {
                        resolve(plugin);
                    }, reject);
                })
            });
            
            $scope.deletePlugin  = function() {
                $scope.deleteButton.state = 'in-progress';
                Dialogs.confirm('Confirm Delete Plugin', 'Do you really want to delete this plugin?', function() {
                    ApimanSvcs.delete({ entityType: 'plugins', secondaryType: $scope.plugin.id }, function(reply) {
                         $location.url(pluginName + '/admin-plugins.html');
                    }, PageLifecycle.handleError);
                }, function() {
                    $scope.deleteButton.state = 'complete';
                });
            }
            
            PageLifecycle.loadPage('EditPlugin', promise, $scope, function() {
                PageLifecycle.setPageTitle('plugin-details');
            });
    }])

}
