import {_module} from "../apimanPlugin";

_module.controller("Apiman.NewOrgController",
        ['$q', '$location', '$rootScope', '$scope', 'OrgSvcs', 'PageLifecycle', 'CurrentUser', 'Logger',
        function ($q, $location, $rootScope, $scope, OrgSvcs, PageLifecycle, CurrentUser, Logger) {
            $scope.saveNewOrg = function() {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save($scope.org, function(reply) {
                    CurrentUser.clear();
                    PageLifecycle.redirectTo('/orgs/{0}/plans', reply.id);
                }, PageLifecycle.handleError);
            };

            PageLifecycle.loadPage('NewOrg', undefined, undefined, $scope, function() {

                // Using this instead the existing permissions' system, as this is a global setting rather than an org based one
                if ($rootScope.noCreateOrg === true) {
                    Logger.info('Only administrators are allowed to create new organizations')
                    PageLifecycle.handleError({status: 404})
                }

                PageLifecycle.setPageTitle('new-org');
                $scope.$applyAsync(function() {
                    $('#apiman-entityname').focus();
                });
            });
        }]);
