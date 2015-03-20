/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    _module.controller("Apiman.RateLimitingFormController",
        ['$scope', 'Logger',
        ($scope, Logger) => {
            var validate = function(config) {
                var valid = true;
                if (!config.limit || config.limit < 1) {
                    valid = false;
                }
                if (!config.granularity) {
                    valid = false;
                }
                if (!config.period) {
                    valid = false;
                }
                $scope.setValid(valid);
            };
            $scope.$watch('config', validate, true);
        }]);

    _module.controller("Apiman.IPListFormController",
        ['$scope', 'Logger',
        ($scope, Logger) => {
            var validate = function(config) {
                var valid = true;
                $scope.setValid(valid);
            };
            $scope.$watch('config', validate, true);
            
            $scope.add = function(ip) {
                if (!$scope.config.ipList) {
                    $scope.config.ipList = [];
                }
                $scope.remove(ip);
                $scope.config.ipList.push(ip);
                $scope.selectedIP =  [ ip ];
                $scope.ipAddress = undefined;
                $('#ip-address').focus();
            };
            
            $scope.remove = function(ips) {
                angular.forEach(ips, function(ip) {
                    var idx = -1;
                    angular.forEach($scope.config.ipList, function(item, index) {
                        if (item == ip) {
                            idx = index;
                        }
                    });
                    if (idx != -1) {
                        $scope.config.ipList.splice(idx, 1);
                    }
                });
                $scope.selectedIP = undefined;
            };
            
            $scope.clear = function() {
                $scope.config.ipList = [];
                $scope.selectedIP = undefined;
            };
        }]);

    _module.controller("Apiman.BASICAuthFormController",
        ['$scope', 'Logger',
        ($scope, Logger) => {
            var validate = function(config) {
                var valid = true;
                $scope.setValid(valid);
            };
            $scope.$watch('config', validate, true);
        }]);

    _module.controller("Apiman.IgnoredResourcesFormController",
        ['$scope', 'Logger',
        ($scope, Logger) => {
            var validate = function(config) {
                var valid = true;
                $scope.setValid(valid);
            };
            $scope.$watch('config', validate, true);
            
            $scope.add = function(path) {
                if (!$scope.config.pathsToIgnore) {
                    $scope.config.pathsToIgnore = [];
                }
                $scope.remove(path);
                $scope.config.pathsToIgnore.push(path);
                $scope.selectedPath =  [ path ];
                $scope.path = undefined;
                $('#path').focus();
            };
            
            $scope.remove = function(paths) {
                angular.forEach(paths, function(path) {
                    var idx = -1;
                    angular.forEach($scope.config.pathsToIgnore, function(item, index) {
                        if (item == path) {
                            idx = index;
                        }
                    });
                    if (idx != -1) {
                        $scope.config.pathsToIgnore.splice(idx, 1);
                    }
                });
                $scope.selectedPath = undefined;
            };
            
            $scope.clear = function() {
                $scope.config.pathsToIgnore = [];
                $scope.selectedPath = undefined;
            };
        }]);

}
