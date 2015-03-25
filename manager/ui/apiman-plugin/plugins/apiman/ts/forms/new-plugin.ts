/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var NewPluginController = _module.controller("Apiman.NewPluginController",
        ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', 'Dialogs',
        ($q, $scope, $location, ApimanSvcs, PageLifecycle, Dialogs) => {
            $scope.plugin = {};
            
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
                     $location.path(pluginName + '/admin-plugins.html');
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                    $scope.addButton.state = 'error';
                });
            }
            
            PageLifecycle.loadPage('NewPlugin', undefined, $scope);
            $('#NewPluginController').focus();
    }])

}
