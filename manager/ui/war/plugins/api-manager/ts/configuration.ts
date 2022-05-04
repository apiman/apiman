import angular = require("angular");

angular.module('ApimanConfiguration', [])
    .factory('Configuration', ['$window',
    function ($window) {
        var cdata:any = {};

        if ($window['APIMAN_CONFIG_DATA']) {
            cdata = angular.copy($window['APIMAN_CONFIG_DATA']);

            //delete $window['APIMAN_CONFIG_DATA'];
        } else {
            console.log('***  Missing variable: APIMAN_CONFIG_DATA  ***');
        }

        cdata.getAuthorizationHeader = function () {
            var authHeader = null;

            if (cdata.api.auth.type == 'basic') {
                var username = cdata.api.auth.basic.username;
                var password = cdata.api.auth.basic.password;
                var enc = btoa(username + ':' + password);

                authHeader = 'Basic ' + enc;
            } else if (cdata.api.auth.type == 'bearerToken') {
                if (cdata.api.auth.bearerToken && cdata.api.auth.bearerToken.token) {
                    var token = cdata.api.auth.bearerToken.token;

                    authHeader = 'Bearer ' + token;
                } else {
                    console.log('** Auth type was ' + cdata.api.auth.type + ' but no bearer-token was found! **');
                }
            } else if (cdata.api.auth.type == 'authToken') {
                var token = cdata.api.auth.bearerToken.token;

                authHeader = 'AUTH-TOKEN ' + token;
            }

            return authHeader;
        };

        if (!cdata.ui) {
            cdata.ui = {
                header: false,
                metrics: true
            };
        }

        if (cdata.ui.metrics == undefined) {
            cdata.ui.metrics = true;
        }
        if (cdata.ui.platform == undefined) {
            cdata.ui.platform = "community";
        }
        if (cdata.ui.adminOnlyOrgCreation == undefined) {
            cdata.ui.adminOnlyOrgCreation = false;
        }
        if (cdata.ui.notifications == undefined) {
            cdata.ui.notifications = false;
        }

        return cdata;
    }]);