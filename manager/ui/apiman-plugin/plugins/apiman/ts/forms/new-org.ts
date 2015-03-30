/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewOrgController = _module.controller("Apiman.NewOrgController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'PageLifecycle', 'CurrentUser',
        ($q, $location, $scope, OrgSvcs, PageLifecycle, CurrentUser) => {
            $scope.saveNewOrg = function() {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save($scope.org, function(reply) {
                    CurrentUser.refresh(function() {
                        $location.url('apiman/org-plans.html').search('org', reply.id);
                    });
                }, function(error) {
                    if (error.status == 409) {
                        $location.url('apiman/error-409.html');
                    } else {
                        alert("ERROR=" + error.status + " " + error.statusText);
                        $scope.createButton.state = 'error';
                    }
                });
            };
            
            PageLifecycle.loadPage('NewOrg', undefined, $scope, function() {
                PageLifecycle.setPageTitle('new-org');
                $('#apiman-entityname').focus();
            });
        }]);

}
