/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var AdminPluginsController = _module.controller("Apiman.AdminPluginsController",
        [
            '$q',
            '$scope',
            '$uibModal',
            'ApimanSvcs',
            'PageLifecycle',
            'Logger',
        ($q, $scope, $uibModal, ApimanSvcs, PageLifecycle, Logger) => {
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
                    ApimanSvcs.query({ entityType: 'plugins' }, function(plugins) {
                        angular.forEach(plugins, function(p) {
                            p.isSnapshot = p.version.indexOf("-SNAPSHOT", this.length - "-SNAPSHOT".length) !== -1;
                        });
                        resolve(plugins);
                    }, reject);
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
            
            $scope.uninstallPlugin = function(plugin, size) {
                plugin.deleting = true;

                var options = {
                    message: 'Do you really want to uninstall this plugin?  Any policies it provided will no longer be available.',
                    title: 'Confirm Uninstall Plugin'
                };

                $scope.animationsEnabled = true;

                $scope.toggleAnimation = function () {
                    $scope.animationsEnabled = !$scope.animationsEnabled;
                };

                var modalInstance = $uibModal.open({
                    animation: $scope.animationsEnabled,
                    templateUrl: 'confirmModal.html',
                    controller: 'ModalConfirmCtrl',
                    size: size,
                    resolve: {
                        options: function () {
                            return options;
                        }
                    }
                });

                modalInstance.result.then(function () {
                    ApimanSvcs.delete({ entityType: 'plugins', secondaryType: plugin.id }, function(reply) {
                        removePlugin(plugin);
                        refreshPlugins();
                    }, PageLifecycle.handleError);
                }, function () {
                    //console.log('Modal dismissed at: ' + new Date());
                });
            };
            
            $scope.upgradePlugin = function(plugin, size) {
                var options = {
                    initialVersion: plugin.latestVersion,
                    label: 'New Plugin Version',
                    message: 'Do you really want to upgrade this plugin?  Any published APIs already using the plugin will continue to use the old version.  All new policies will use the newly upgraded version.',
                    title: 'Confirm Upgrade Plugin'
                };

                $scope.animationsEnabled = true;

                $scope.toggleAnimation = function () {
                    $scope.animationsEnabled = !$scope.animationsEnabled;
                };

                var modalInstance = $uibModal.open({
                    animation: $scope.animationsEnabled,
                    templateUrl: 'getValueModal.html',
                    controller: 'ModalGetValueCtrl',
                    size: size,
                    resolve: {
                        options: function () {
                            return options;
                        }
                    }
                });

                modalInstance.result.then(function (value) {
                    var uplugin = {
                        groupId: plugin.groupId,
                        artifactId: plugin.artifactId,
                        classifier: plugin.classifier,
                        type: plugin.type,
                        version: value,
                        upgrade: true
                    };

                    plugin.upgrading = true;

                    ApimanSvcs.save({ entityType: 'plugins' }, uplugin, function(reply) {
                        delete plugin.upgrading;
                        plugin.version = value;
                        refreshPlugins();
                    }, PageLifecycle.handleError);
                }, function () {
                    //console.log('Modal dismissed at: ' + new Date());
                });
            };
            
            var refreshPlugins = function() {
                angular.forEach($scope.plugins, function(ip) {
                  delete ip.latestVersion;
                  ip.hasAvailableVersion = false;
                  ip.canUpgrade = true;
                });

                angular.forEach($scope.availablePlugins, function(plugin) {
                    var ip = getInstalledPlugin(plugin);

                    if (ip) {
                        plugin.isInstalled = true;
                        plugin.installedVersion = ip.version;
                        ip.latestVersion = plugin.version;
                        ip.canUpgrade = plugin.version != ip.version;
                        ip.hasAvailableVersion = true;
                    } else {
                        plugin.isInstalled = false;
                        delete plugin.installedVersion;
                    }
                });
            };
            
            PageLifecycle.loadPage('AdminPlugins', 'admin', pageData, $scope, function() {
                PageLifecycle.setPageTitle('admin-plugins');
                refreshPlugins();
            });
    }])

}
