import {_module} from "../apimanPlugin";

_module.controller("Apiman.OrgRedirectController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'CurrentUser', '$routeParams',
        function ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, CurrentUser, $routeParams) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            if (!CurrentUser.isMember(params.org)) {
              delete $rootScope['currentUser'];
            }
            PageLifecycle.loadPage('OrgRedirect', 'orgView', undefined, $scope, function() {
                var orgId = $routeParams.org;
                var tab = 'members';
                if (CurrentUser.hasPermission(orgId, 'planEdit')) {
                    tab = 'plans';
                } else if (CurrentUser.hasPermission(orgId, 'apiEdit')) {
                    tab = 'apis';
                } else if (CurrentUser.hasPermission(orgId, 'clientEdit')) {
                    tab = 'clients';
                }
                PageLifecycle.forwardTo('/orgs/{0}/{1}', orgId, tab);
            });
        }]);