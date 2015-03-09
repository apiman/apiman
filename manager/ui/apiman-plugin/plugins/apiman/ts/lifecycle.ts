/// <reference path="../../includes.ts"/>
module ApimanPageLifecycle {

    export var _module = angular.module("ApimanPageLifecycle", []);

    export var PageLifecycle = _module.factory('PageLifecycle', ['Logger', '$rootScope', function(Logger, $rootScope) {
        return {
            loadPage: function(pageName, dataPromise, $scope) {
                Logger.log("|{0}| >> Loading page.", pageName);
                $rootScope.isLoaded = false;
                $rootScope.isLoading = true;
                dataPromise.then(function(data) {
                    var count = 0;
                    angular.forEach(data, function(value, key) {
                        Logger.debug("|{0}| >> Binding {1} to $scope.", pageName, key);
                        this[key] = value;
                        count++;
                    }, $scope);
                    $rootScope.isLoaded = true;
                    $rootScope.isLoading = false;
                    $rootScope.isError = false;
                    Logger.log("|{0}| >> Page successfully loaded: {1} data packets loaded", pageName, count);
                }, function(reason) {
                    $rootScope.isError = true;
                    $rootScope.error = reason;
                    alert("Page Load Error: " + reason);
                });
            }
        }
    }]);

}