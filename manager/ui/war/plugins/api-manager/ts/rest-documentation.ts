import { _module } from './apimanPlugin';
import * as SwaggerUI from 'swagger-ui';
import 'swagger-ui/dist/swagger-ui.css';

_module.controller("Apiman.ManagerRestApiDefController",
    ['$q', '$rootScope', '$scope', 'PageLifecycle', 'Configuration',
        function ($q, $rootScope, $scope, PageLifecycle, Configuration) {

            PageLifecycle.loadPage('MangerRestApiDef', undefined, undefined, $scope, function() {

                PageLifecycle.setPageTitle('manager-rest-def');

                $scope.definitionUrl = Configuration.api.endpoint + '/swagger.yaml';

                if (SwaggerUI) {
                    $scope.definitionStatus = 'loading';
                    let ui;
                    let swaggerOptions: SwaggerUI.SwaggerUIOptions = {
                        url: $scope.definitionUrl,
                        dom_id: "#swagger-ui-container",
                        validatorUrl: "https://online.swagger.io/validator",
                        layout: "BaseLayout",
                        operationsSorter: "alpha",
                        tryItOutEnabled: true,
                        supportedSubmitMethods: [
                            'get',
                            'put',
                            'post',
                            'delete',
                            'options',
                            'head',
                            'patch',
                            'trace'
                        ],


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

                    ui = SwaggerUI(swaggerOptions);
                    $scope.hasDefinition = true;
                } else {
                    $scope.hasDefinition = false;
                }
            });
        }]);
