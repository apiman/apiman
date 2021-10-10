import {_module} from "../apimanPlugin";

_module.controller("Apiman.UserRedirectController",
      ['$q', '$scope', '$location', 'PageLifecycle', '$routeParams',
      function ($q, $scope, $location, PageLifecycle, $routeParams) {
          PageLifecycle.loadPage('UserRedirect', undefined, undefined, $scope, function() {
              PageLifecycle.forwardTo('/users/{0}/orgs', $routeParams.user);
          });
  }]);
