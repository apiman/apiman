module Apiman {
    export var ManagerRestApiDefController = _module.controller("Apiman.ManagerRestApiDefController",
        ['$q', '$rootScope', '$scope', 'PageLifecycle', 'Configuration',
            ($q, $rootScope, $scope, PageLifecycle, Configuration) => {

                PageLifecycle.loadPage('MangerRestApiDef', undefined, undefined, $scope, function() {

                    PageLifecycle.setPageTitle('manager-rest-def');

                    $scope.definitionUrl = Configuration.api.endpoint + '/swagger.yaml';

                    if (SwaggerUIBundle) {
                        $scope.definitionStatus = 'loading';
                        let ui;
                        let swaggerOptions = <any>{
                            url: $scope.definitionUrl,
                            dom_id: "#swagger-ui-container",
                            validatorUrl: "https://online.swagger.io/validator",
                            presets: [
                                SwaggerUIBundle.presets.apis
                            ],
                            layout: "BaseLayout",
                            sorter : "alpha",

                            requestInterceptor: function(request) {
                                // Send keycloak token
                                request.headers.Authorization = Configuration.getAuthorizationHeader();
                                return request;
                            },
                            onComplete: function() {
                                $scope.$apply(function() {
                                    $scope.definitionStatus = 'complete';
                                });
                            },
                            // do error handling in the responseInterceptor
                            responseInterceptor: function (response) {
                                if (response.status == 500 && response.ok === false) {
                                    $scope.$apply(function() {
                                        $scope.definitionStatus = 'error';
                                        $scope.hasError = true;
                                    });
                                }
                                return response;
                            }
                        };

                        ui = SwaggerUIBundle(swaggerOptions);
                        $scope.hasDefinition = true;
                    } else {
                        $scope.hasDefinition = false;
                    }
                });
            }]);
}
