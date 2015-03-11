/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var EditPluginController = _module.controller("Apiman.EditPluginController",
        ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', ($q, $scope, $location, ApimanSvcs, PageLifecycle) => {
            
            var params = $location.search();
            
            var promise = $q.all({
                plugin: $q(function(resolve, reject) {
                    ApimanSvcs.get({ entityType: 'plugins', secondaryType: params.plugin }, function(plugin) {
                        resolve(plugin);
                        $scope.configuration = JSON.parse(plugin.configuration);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            
            $scope.deletePlugin  = function() {
                ApimanSvcs.delete({ entityType: 'plugins', secondaryType: $scope.plugin.id }, function(reply) {
                     $location.path(pluginName + '/admin-plugins.html');
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        $scope.createButton.state = 'error';
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            }
            
            PageLifecycle.loadPage('EditPlugin', promise, $scope);
    }])

}
