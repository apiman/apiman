/// <reference path="apimanPlugin.ts"/>
module Apiman {

  export var DashController = _module.controller("Apiman.DashController", ['$scope', 'Logger', ($scope, Logger) => {
      Logger.log("Showing the dashboard!");
  }]);

}
