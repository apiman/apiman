/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AdminPluginsController = _module.controller("Apiman.AdminPluginsController",
        ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', 'Dialogs', 
        ($q, $scope, ApimanSvcs, PageLifecycle, Dialogs) => {
            $scope.tab = 'plugins';
            $scope.filterAvailablePlugins = function(value) {
                if (!value) {
                    $scope.filteredAvailablePlugins = $scope.availablePlugins;
                } else {
                    var filtered = [];
                    angular.forEach($scope.availablePlugins, function(plugin) {
                        if (plugin.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(plugin);
                        }
                    });
                    $scope.filteredAvailablePlugins = filtered;
                }
            };
            var pageData = {
                plugins: $q(function(resolve, reject) {
                    ApimanSvcs.query({ entityType: 'plugins' }, resolve, reject);
                }),
                availablePlugins: $q(function(resolve, reject) {
                    ApimanSvcs.query({ entityType: 'plugins', secondaryType: 'availablePlugins' }, function(plugins) {
                        $scope.filteredAvailablePlugins = plugins;
                        resolve(plugins);
                    }, reject);
                })
            };
            var getInstalledPlugin = function(plugin) {
                var rval = null;
                angular.forEach($scope.plugins, function(p) {
                    if (plugin.groupId == p.groupId && plugin.artifactId == p.artifactId) {
                        rval = p;
                    }
                });
                return rval;
            };
            
            var removePlugin = function(plugin) {
                var index = -1;
                var i = 0;
                angular.forEach($scope.plugins, function(p, i) {
                    if (p === plugin) {
                        index = i;
                    }
                });
                if (index >= 0) {
                    $scope.plugins.splice(index, 1);
                }
            };
            
            $scope.uninstallPlugin = function(plugin) {
                plugin.deleting = true;
                Dialogs.confirm('Confirm Uninstall Plugin', 'Do you really want to uninstall this plugin?  Any policies it provided will no longer be available.', function() {
                    ApimanSvcs.delete({ entityType: 'plugins', secondaryType: plugin.id }, function(reply) {
                        removePlugin(plugin);
                        refreshPlugins();
                    }, PageLifecycle.handleError);
                }, function() {
                    delete plugin.deleting;
                });
            }
            
            var refreshPlugins = function() {
                angular.forEach($scope.availablePlugins, function(plugin) {
                    var ip = getInstalledPlugin(plugin);
                    if (ip) {
                        plugin.isInstalled = true;
                        plugin.installedVersion = ip.version;
                        ip.latestVersion = plugin.version;
                        ip.needsUpgrade = plugin.version != ip.version;
                    }
                });
            };
            
            PageLifecycle.loadPage('AdminPlugins', pageData, $scope, function() {
                PageLifecycle.setPageTitle('admin-plugins');
                refreshPlugins();
            });
    }])

}
