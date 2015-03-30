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
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('NewOrg', undefined, $scope, function() {
                PageLifecycle.setPageTitle('new-org');
                $scope.$applyAsync(function() {
                    $('#apiman-entityname').focus();
                });
            });
        }]);

}
