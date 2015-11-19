/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var UserRedirectController = _module.controller("Apiman.UserRedirectController",
        ['$q', '$scope', '$location', 'PageLifecycle', '$routeParams',
        ($q, $scope, $location, PageLifecycle, $routeParams) => {
            PageLifecycle.loadPage('UserRedirect', undefined, undefined, $scope, function() {
                PageLifecycle.forwardTo('/users/{0}/orgs', $routeParams.user);
            });
    }])

}
