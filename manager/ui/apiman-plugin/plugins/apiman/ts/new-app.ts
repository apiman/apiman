/// <reference path="apimanPlugin.ts"/>
module Apiman {

  export var NewAppController = _module.controller("Apiman.NewAppController", ['$location', '$scope', 'UserSvcs', 'OrgSvcs',  ($location,$scope, UserSvcs, OrgSvcs) => {
    UserSvcs.query(function(userOrgs) {
        $scope.organizations = userOrgs;
        $scope.selectedOrg = $scope.organizations[0];
    }, function(error) {
        alert("ERROR=" + error);
    });
    $scope.setOrg = function(org) {
      $scope.selectedOrg = org;
    };
    $scope.saveNewApp = function() {
        OrgSvcs.save({organizationId: $scope.selectedOrg.id}, $scope.app, function(reply) {
           $location.path(Apiman.pluginName + '/app-overview.html').search('org',$scope.selectedOrg.id).search('app',$scope.app.name).search('version',$scope.app.initialVersion);
        }, function(error) {
           if (error.status == 409) {
              $location.path('apiman/error-409.html');          
           } else {
              alert("ERROR=" + error.status + " " + error.statusText);
           }
        });
    };
  }]);

}
