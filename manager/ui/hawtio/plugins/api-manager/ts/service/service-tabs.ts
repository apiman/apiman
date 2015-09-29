/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServiceTabsController = _module.controller("Apiman.ServiceTabsController",
        ['$scope', '$window',
        ($scope, $window) => {
            function redirect(url) {
                $window.location.href = url;
            }
            
            $scope.check = function(isDirty, url) {
                console.log('isDirty? ' + isDirty);
                console.log('URL: ' + url);
                    
                if(isDirty) {
                    alert('Please save your changes before navigating away from this page.');
                } else {
                    redirect(url);
                }
            };
            
        }]);

}
