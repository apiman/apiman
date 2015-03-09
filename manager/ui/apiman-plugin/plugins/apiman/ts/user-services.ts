/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var UserServicesController = _module.controller("Apiman.UserServicesController", ['$scope', 'UserSvcs', ($scope, UserSvcs) => {
        UserSvcs.query({ entityType: 'services' }, function(userServices) {
            $scope.services = userServices;
        }, function(error) {
            alert("ERROR=" + error);
        });
    }])


}
