/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var NewPluginController = _module.controller("Apiman.NewPluginController",
        ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', '$routeParams',
        ($q, $scope, $location, ApimanSvcs, PageLifecycle, $routeParams) => {
            $scope.plugin = {};
            var params = $routeParams;
            if (params && params.groupId) {
                $scope.plugin = {
                    groupId: params.groupId,
                    artifactId: params.artifactId,
                    version: params.version
                };
                if (params.classifier) {
                    $scope.plugin.classifier = params.classifier;
                }
                if (params.type) {
                    $scope.plugin.type = params.type;
                }
            }
            
            var validate = function() {
                var valid = true;
                if (!$scope.plugin.groupId) {
                    valid = false;
                }
                if (!$scope.plugin.artifactId) {
                    valid = false;
                }
                if (!$scope.plugin.version) {
                    valid = false;
                }
                $scope.isValid = valid;
            };

            $scope.$watch('plugin', function(newValue) {
                validate();
            }, true);
            
            $scope.addPlugin  = function() {
                $scope.addButton.state = 'in-progress';
                ApimanSvcs.save({ entityType: 'plugins' }, $scope.plugin, function(reply) {
                     PageLifecycle.redirectTo('/admin/plugins');
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('NewPlugin', 'admin', undefined, $scope, function() {
                PageLifecycle.setPageTitle('new-plugin');
                $('#apiman-group-id').focus();
            });
            
    }])

}
