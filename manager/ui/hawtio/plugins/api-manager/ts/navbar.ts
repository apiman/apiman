/// <reference path="apimanPlugin.ts"/>
module Apiman {

    export var NavbarController = _module.controller("Apiman.NavbarController", 
            ['$scope', 'Logger', 'Configuration', ($scope, Logger, Configuration) => 
    {
        Logger.log("Current user is {0}.", Configuration.user.username);
        $scope.username = Configuration.user.username;
    }]);

}
