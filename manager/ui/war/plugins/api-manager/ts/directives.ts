/// <reference path="../../includes.ts"/>

module Apiman {

    _module.directive('apimanActionBtn',
        ['Logger', function (Logger) {
            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                    var actionVar = attrs.field;
                    var actionText = attrs.placeholder;
                    var icon = attrs.icon;
                    var disabledExpr = attrs.ngDisabled;
                    scope[actionVar] = {
                        state: 'ready',
                        html: $(element).html(),
                        actionHtml: '<i class="fa fa-spin ' + icon + '"></i> ' + actionText
                    };
                    scope.$watch(actionVar + '.state', function () {
                        var newVal = scope[actionVar];
                        if (newVal.state == 'in-progress') {
                            $(element).prop('disabled', true);
                            $(element).html(newVal.actionHtml);
                        } else {
                            var isDisabled = false;
                            if (disabledExpr) {
                                var exprVal = scope.$eval(disabledExpr);
                                isDisabled = new Boolean(exprVal).valueOf();
                            }
                            $(element).prop('disabled', isDisabled);
                            $(element).html(newVal.html);
                        }
                    });
                }
            };
        }]);

    _module.directive('apimanApiModal',
        ['Logger', function (Logger) {
            return {
                templateUrl: 'plugins/api-manager/html/client/apiModal.html',
                replace: true,
                restrict: 'E',
                link: function (scope, element, attrs) {
                    $(element).on('hidden.bs.modal', function () {
                        $(element).remove();
                    });

                    // Called if copy-to-clipboard functionality was successful
                    scope.copySuccess = function () {
                        //console.log('Copied!');
                    };

                    // Called if copy-to-clipboard functionality was unsuccessful
                    scope.copyFail = function (err) {
                        //console.error('Error!', err);
                    };
                }
            };
        }]);

    _module.directive('apimanSelectPicker',
        ['Logger', '$timeout', '$parse', 'TranslationSvc',
            function (Logger, $timeout, $parse, TranslationSvc) {
                return {
                    restrict: 'A',
                    link: function (scope, element, attrs) {
                        function refresh(newVal) {
                            scope.$applyAsync(function () {
                                $(element)['selectpicker']('refresh');
                            });
                        }
                        $timeout(function () {
                            $(element)['selectpicker']();
                            $(element)['selectpicker']('refresh');
                        });

                        if (attrs.ngOptions && / in /.test(attrs.ngOptions)) {
                            var refreshModel = attrs.ngOptions.split(' in ')[1].split(' ')[0];
                            Logger.debug('Watching model {0} for {1}.', refreshModel, attrs.ngModel);
                            scope.$watch(refreshModel, function () {
                                scope.$applyAsync(function () {
                                    Logger.debug('Refreshing {0} due to watch model update.', attrs.ngModel);
                                    $(element)['selectpicker']('refresh');
                                });
                            }, true);
                        }

                        if (attrs.apimanSelectPicker) {
                            Logger.debug('Watching {0}.', attrs.apimanSelectPicker);
                            scope.$watch(attrs.apimanSelectPicker + '.length', refresh, true);
                        }
                        if (attrs.ngModel) {
                            scope.$watch(attrs.ngModel, refresh, true);
                        }
                        if (attrs.ngDisabled) {
                            scope.$watch(attrs.ngDisabled, refresh, true);
                        }
                        scope.$on('$destroy', function () {
                            $timeout(function () {
                                $(element)['selectpicker']('destroy');
                            });
                        });
                    }
                };
            }]);


    _module.directive('apimanPermission',
        ['Logger', 'CurrentUser',
            (Logger, CurrentUser) => {
                return {
                    restrict: 'A',
                    link: function ($scope, element, attrs) {
                        var refresh = function (newValue) {
                            var orgId = $scope.organizationId;
                            if (orgId) {
                                var permission = attrs.apimanPermission;
                                $(element).removeClass('apiman-not-permitted');
                                if (!CurrentUser.hasPermission(orgId, permission)) {
                                    $(element).addClass('apiman-not-permitted');
                                }
                            } else {
                                Logger.error('Missing organizationId from $scope - authorization disabled.');
                            }
                        };
                        $scope.$watch('organizationId', refresh);
                        $scope.$watch('permissions', refresh);
                    }
                };
            }]);


    _module.directive('apimanStatus',
        ['Logger', 'EntityStatusSvc',
            function (Logger, EntityStatusSvc) {
                return {
                    restrict: 'A',
                    link: function (scope, element, attrs) {
                        scope.$watch(function ($scope) {
                            return EntityStatusSvc.getEntityStatus();
                        }, function (newValue, oldValue) {
                            var entityStatus = newValue;
                            var elem = element;

                            if (entityStatus) {
                                var validStatuses = attrs.apimanStatus.split(',');
                                var statusIsValid = false;
                                //                            Logger.debug('Checking status {0} against valid statuses {1}:  {2}', entityStatus, '' + validStatuses,
                                //                                    element[0].outerHTML);
                                for (var i = 0; i < validStatuses.length; i++) {
                                    if (validStatuses[i] == entityStatus) {
                                        statusIsValid = true;
                                        break;
                                    }
                                }
                                $(element).removeClass('apiman-wrong-status');
                                if (!statusIsValid) {
                                    $(element).addClass('apiman-wrong-status');
                                }
                            } else {
                                Logger.error('Missing entityStatus from $scope - hide/show based on entity status feature is disabled.');
                            }
                        });
                    }
                };
            }]);

    _module.factory('EntityStatusSvc',
        ['$rootScope', 'Logger',
            function ($rootScope, Logger) {
                var entity = null;
                var entityType = null;

                return {
                    setEntity: function (theEntity, type) {
                        Logger.debug('Setting the entity: {0} type={1}', theEntity, type);
                        entity = theEntity;
                        entityType = type;
                    },
                    getEntity: function () {
                        return entity;
                    },
                    getEntityStatus: function () {
                        if (entity) {
                            return entity.status;
                        }
                    },
                    getEntityType: function () {
                        if (!entity) {
                            Logger.debug('Entity is null!');
                            return 'n/a';
                        }
                        return entityType;
                    },
                    setEntityStatus: function (status) {
                        if (entity) {
                            entity.status = status;
                        }
                    },
                    isEntityDisabled: function () {
                        if (entity) {
                            if (entityType == 'client' || entityType == 'clients') {
                                return entity.status == 'Retired';
                            } else if (entityType == 'api' || entityType == 'apis') {
                                if (entity.publicAPI) {
                                    return entity.status == 'Retired';
                                } else {
                                    return (entity.status !== 'Created' && entity.status !== 'Ready');
                                }
                            } else {
                                return (entity.status !== 'Created' && entity.status !== 'Ready');
                            }
                        }
                    }
                };
            }]);

    _module.directive('apimanEntityStatus',
        ['Logger', 'EntityStatusSvc',
            function (Logger, EntityStatusSvc) {
                return {
                    restrict: 'A',
                    link: function (scope, element, attrs) {
                        scope.$watch(function ($scope) {
                            return EntityStatusSvc.getEntityStatus();
                        }, function (newValue, oldValue) {
                            var entityStatus = newValue;
                            if (entityStatus) {
                                $(element).html(entityStatus);
                                $(element).removeClass();
                                $(element).addClass('apiman-label');

                                if (entityStatus == 'Created' || entityStatus == 'Ready') {
                                    $(element).addClass('apiman-label-warning');
                                } else if (entityStatus == 'Retired') {
                                    $(element).addClass('apiman-label-default');
                                } else {
                                    $(element).addClass('apiman-label-success');
                                }
                            }
                        });
                    }
                };
            }]);

    export var sb_counter = 0;

    _module.directive('apimanSearchBox',
        ['Logger', 'TranslationSvc',
            function (Logger, TranslationSvc) {
                return {
                    restrict: 'E',
                    replace: true,
                    templateUrl: 'plugins/api-manager/html/directives/searchBox.html',
                    scope: {
                        searchFunction: '=function'
                    },
                    link: function (scope, element, attrs) {
                        scope.placeholder = attrs.placeholder;
                        if (attrs['id']) {
                            scope.filterId = attrs['id'] + '-f';
                            scope.buttonId = attrs['id'] + '-b';
                        } else {
                            var cid = 'search-box-' + sb_counter;
                            sb_counter = sb_counter + 1;
                            scope.filterId = cid + '-filter';
                            scope.buttonId = cid + '-button';
                        }

                        if (attrs.apimanI18nKey) {
                            var translationKey = attrs.apimanI18nKey + ".placeholder";
                            var defaultValue = scope.placeholder;
                            var translatedValue = TranslationSvc.translate(translationKey, defaultValue);
                            scope.placeholder = translatedValue;
                        }

                        scope.doSearch = function () {
                            $(element).find('button i').removeClass('fa-search');
                            $(element).find('button i').removeClass('fa-close');
                            if (scope.value) {
                                $(element).find('button i').addClass('fa-close');
                            } else {
                                $(element).find('button i').addClass('fa-search');
                            }
                            scope.searchFunction(scope.value);
                        };
                        scope.onClick = function () {
                            if (scope.value) {
                                scope.value = '';
                                $(element).find('button i').removeClass('fa-search');
                                $(element).find('button i').removeClass('fa-close');
                                $(element).find('button i').addClass('fa-search');
                            }
                            scope.searchFunction(scope.value);
                        };
                    }
                };
            }]);

    var entryTypeClasses = {
        Organization: 'fa-shield',
        Client: 'fa-gears',
        Plan: 'fa-bar-chart-o',
        Api: 'fa-puzzle-piece'
    };

    _module.directive('apimanActivity',
        ['Logger', '$rootScope', 'PageLifecycle',
            (Logger, $rootScope, PageLifecycle) => {
                return {
                    templateUrl: 'plugins/api-manager/html/directives/activity.html',
                    restrict: 'E',
                    replace: true,
                    scope: {
                        auditEntries: '=model',
                        next: '=next'
                    },
                    link: function (scope, element, attrs) {
                        scope.pluginName = $rootScope.pluginName;
                        scope.hasMore = true;
                        scope.getEntryIcon = function (entry) {
                            return entryTypeClasses[entry.entityType];
                        };
                        scope.getMore = function () {
                            scope.getMoreButton.state = 'in-progress';
                            scope.next(function (newEntries) {
                                scope.auditEntries = scope.auditEntries.concat(newEntries);
                                scope.hasMore = newEntries.length >= 20;
                                scope.getMoreButton.state = 'complete';
                            }, PageLifecycle.handleError);
                        };
                    }
                };
            }]);

    _module.directive('apimanAuditEntry',
        ['Logger', '$rootScope',
            function (Logger, $rootScope) {
                return {
                    restrict: 'E',
                    scope: {
                        entry: '=model'
                    },
                    link: function (scope, element, attrs) {
                        scope.pluginName = $rootScope.pluginName;
                        scope.template = 'plugins/api-manager/html/directives/audit/' + scope.entry.entityType + '/audit' + scope.entry.what + '.html';
                        if (scope.entry.data) {
                            scope.data = JSON.parse(scope.entry.data);
                        }
                    },
                    template: '<div ng-include="template"></div>'
                };
            }]);

    _module.directive('apimanDropText',
        ['Logger',
            (Logger) => {
                return {
                    restrict: 'A',
                    require: 'ngModel',
                    scope: {
                        ngModel: '='
                    },
                    link: function ($scope, $elem, $attrs, ngModel) {
                        $elem.on('dragover', function (e) {
                            e.preventDefault();
                            if (e.dataTransfer) {
                                e.dataTransfer.effectAllowed = 'copy';
                            }
                            if (!$elem.hasClass('dropping')) {
                                $elem.addClass('dropping');
                            }
                            return false;
                        });
                        $elem.on('dragenter', function (e) {
                            e.preventDefault();
                            if (e.dataTransfer) {
                                e.dataTransfer.effectAllowed = 'copy';
                            }
                            $elem.addClass('dropping');
                            return false;
                        });
                        $elem.on('dragleave', function (e) {
                            e.preventDefault();
                            $elem.removeClass('dropping');
                            return false;
                        });

                        $elem.on('drop', function (e) {
                            e.preventDefault();
                            $elem.removeClass('dropping');
                            if (e.originalEvent.dataTransfer && e.originalEvent.dataTransfer.files.length) {
                                if (e.preventDefault) e.preventDefault();
                                if (e.stopPropagation) e.stopPropagation();

                                var firstFile = e.originalEvent.dataTransfer.files[0];
                                var reader = new FileReader();

                                reader.onload = (function (theFile) {
                                    return function (result) {
                                        $elem.val(result.target.result);
                                        ngModel.$setViewValue(result.target.result);
                                        $scope.$emit('afterdrop', { element: $elem, value: result.target.result });
                                    };
                                })(firstFile);

                                reader.readAsText(firstFile);
                            }
                        });
                    }
                }
            }]);

    _module.directive('apimanPolicyList',
        ['Logger',
            function (Logger) {
                return {
                    restrict: 'E',
                    scope: {
                        policies: "=ngModel",
                        remove: "=removeFunction",
                        reorder: "=reorderFunction",
                        type: "@",
                        org: "@orgId",
                        id: "@pageId",
                        version: "@"
                    },
                    controller: ['$scope', ($scope) => {
                        $scope.policyListOptions = {
                            containerPositioning: 'relative',

                            orderChanged: function (event) {
                                Logger.debug("Reordered as: {0}", $scope.ctrl.policies);
                                $scope.ctrl.reorder($scope.ctrl.policies);
                            }
                        };

                        $scope.pluginName = $scope.$parent.pluginName;
                    }],
                    controllerAs: 'ctrl',
                    bindToController: true,
                    templateUrl: 'plugins/api-manager/html/directives/policyList.html'
                }
            }
        ]);

    _module.directive('apimanEditableDescription',
        ['TranslationSvc', 'Logger',
            function (TranslationSvc, Logger) {
                return {
                    restrict: 'E',
                    scope: {
                        descr: '=description',
                        callback: '='
                    },
                    link: function ($scope, $elem, $attrs) {
                        $scope.defaultValue = $attrs.defaultValue;
                        var elem = null;

                        $scope.beforeSaveDescription = function (value?: string) {
                            if (value && value.length !== 0) {
                                Logger.debug('$scope.descr is {0}', $scope.descr)
                                var TEXT_VALIDATION = /^[0-9A-z_\- .,]+$/;

                                if (!TEXT_VALIDATION.test(value)) {
                                    return TranslationSvc.translate('text-error', 'The value is not valid!');
                                }
                            }

                            return true
                        };

                        $scope.saveDescription = function () {
                            $scope.callback($scope.descr);
                        };

                        $scope.focusOnDescription = function (event) {
                            elem = event.target;
                            elem.value = $scope.descr || '';

                            $(elem).css('height', 'auto');
                            $(elem).height(elem.scrollHeight);
                        };

                        $scope.changeOnDescription = function () {
                            $(elem).css('height', 'auto');
                            $(elem).height(elem.scrollHeight);
                        };

                        $scope.descriptionMouseOver = function () {
                            $scope.showPencil = true;
                        };

                        $scope.descriptionMouseOut = function () {
                            $scope.showPencil = false;
                        };

                    },
                    templateUrl: 'plugins/api-manager/html/directives/editDescription.html'
                }
            }]);

    _module.run(['editableOptions', 'editableThemes', function (editableOptions, editableThemes) {
        editableOptions.theme = 'default';

        // overwrite templates
        editableThemes['default'].submitTpl = '<button class="btn btn-default inline-save-btn" type="submit"><i class="fa fa-check fa-fw"></i></button>';
        editableThemes['default'].cancelTpl = '<button class="btn btn-default" type="button" ng-click="$form.$cancel()"><i class="fa fa-times fa-fw"></i></button>';
        editableThemes['default'].buttonsTpl = '<div class="editable-options"></div>';
        editableThemes['default'].formTpl = '<form class="editable-wrap apiman-inline-edit"></form>';
    }]);

    _module.directive('apimanI18nKey',
        ['Logger', 'TranslationSvc',
            function (Logger, TranslationSvc) {
                return {
                    restrict: 'A',
                    link: function (scope, element, attrs) {
                        if (!attrs.apimanI18nKey) {
                            return;
                        }

                        var translationKey, defaultValue, translatedValue;

                        // Process the text of the element only if it has no child elements
                        if ($(element).children().length == 0) {
                            translationKey = attrs.apimanI18nKey;
                            defaultValue = $(element).text();
                            translatedValue = TranslationSvc.translate(translationKey, defaultValue);
                            $(element).text(translatedValue);
                        }

                        // Now process the placeholder attribute.
                        if ($(element).attr('placeholder')) {
                            translationKey = attrs.apimanI18nKey + '.placeholder';
                            defaultValue = $(element).attr('placeholder');
                            translatedValue = TranslationSvc.translate(translationKey, defaultValue);
                            Logger.debug('Translating placeholder attr.  Key: {2}  default value: {0}  translated: {1}', defaultValue, translatedValue, translationKey);
                            $(element).prop('placeholder', translatedValue);
                            $(element).attr('placeholder', translatedValue);
                        }

                        // Now process the title attribute.
                        if ($(element).attr('title')) {
                            translationKey = attrs.apimanI18nKey + '.title';
                            defaultValue = $(element).attr('title');
                            translatedValue = TranslationSvc.translate(translationKey, defaultValue);
                            Logger.debug('Translating title attr.  Key: {2}  default value: {0}  translated: {1}', defaultValue, translatedValue, translationKey);
                            $(element).prop('title', translatedValue);
                            $(element).attr('title', translatedValue);
                        }
                    }
                };
            }]);

    _module.directive('clickOutside', function ($parse, $timeout) {
        return {
            link: function (scope, element, attrs: any) {
                function handler(event) {
                    if (!$(event.target).closest(element).length) {
                        scope.$apply(function () {
                            $parse(attrs.clickOutside)(scope);
                        });
                    }
                }

                $timeout(function () {
                    // Timeout is to prevent the click handler from immediately
                    // firing upon opening the popover.
                    $(document).on('click', handler);
                });

                scope.$on('$destroy', function () {
                    $(document).off('click', handler);
                });
            }
        }
    });

    _module.directive('httpVerbsSelect',
        ['Logger', function (Logger) {
            return {
                templateUrl: 'plugins/api-manager/html/directives/requestMethod.html',
                replace: true,
                restrict: 'E',
                link: function (scope, elem, attrs) {
                    // init model
                    scope.resetVerbsSelector = function () {
                        scope.verb = '*';
                    }
                    scope.resetVerbsSelector();
                }
            };
        }]);

    _module.directive('nameValidation', function () {
        return {
            require: 'ngModel',
            link: function (scope, element, attributes, ngModel: any) {
                ngModel.$parsers.push(function (value?: string) {
                    if (value && value.length !== 0) {
                        var NAME_VALIDATION = /^[0-9A-z_\-]+$/;

                        if (NAME_VALIDATION.test(value)) {
                            ngModel.$setValidity('name', true)
                        } else {
                            ngModel.$setValidity('name', false)
                        }
                    } else {
                        ngModel.$setValidity('name', true)
                    }

                    return value;
                });
            }
        };
    });

    _module.directive('pathValidation', function () {
        return {
            require: 'ngModel',
            link: function (scope, element, attributes, ngModel: any) {
                ngModel.$parsers.push(function (value?: string) {
                    if (value && value.length !== 0) {
                        var PATH_VALIDATION = /^[0-9A-z_\-.*/]+$/;

                        if (PATH_VALIDATION.test(value)) {
                            ngModel.$setValidity('path', true)
                        } else {
                            ngModel.$setValidity('path', false)
                        }
                    } else {
                        ngModel.$setValidity('path', true)
                    }

                    return value;
                });
            }
        };
    });

    _module.directive('versionValidation', function () {
        return {
            require: 'ngModel',
            link: function (scope, element, attributes, ngModel: any) {
                ngModel.$parsers.push(function (value?: string) {
                    if (value && value.length !== 0) {
                        var VERSION_VALIDATION = /^[1-9][0-9]*(.[0-9]+)+$/;

                        if (VERSION_VALIDATION.test(value)) {
                            ngModel.$setValidity('version', true)
                        } else {
                            ngModel.$setValidity('version', false)
                        }
                    } else {
                        ngModel.$setValidity('version', true)
                    }

                    return value;
                });
            }
        };
    });

    _module.directive('textValidation', function () {
        return {
            require: 'ngModel',
            link: function (scope, element, attributes, ngModel: any) {
                ngModel.$parsers.push(function (value?: string) {
                    scope.gatewayForm && console.debug(`${scope.gatewayForm.description.$invalid}`)
                    if (value && value.length !== 0) {
                        var TEXT_VALIDATION = /^[0-9A-z_\- .,]+$/;

                        if (TEXT_VALIDATION.test(value)) {
                            ngModel.$setValidity('text', true)
                        } else {
                            ngModel.$setValidity('text', false)
                        }
                    } else {
                        ngModel.$setValidity('text', true)
                    }

                    return value;
                });
            }
        };
    });

    _module.directive('regexpValidation', function () {
        return {
            require: 'ngModel',
            link: function (scope, element, attributes, ngModel: any) {
                ngModel.$parsers.push(function (value?: string) {
                    if (value && value.length !== 0) {
                        try {
                            var regexp = new RegExp(value)
                            ngModel.$setValidity('text', true)
                        } catch(error) {
                            ngModel.$setValidity('text', false)
                        }
                    } else {
                        ngModel.$setValidity('text', true)
                    }

                    return value;
                });
            }
        };
    });

    _module.directive('jsonValidation', function () {
        return {
            require: 'ngModel',
            link: function (scope, element, attributes, ngModel: any) {
                ngModel.$parsers.push(function (value?: string) {
                    if (value && value.length !== 0) {
                        try {
                            var json = JSON.parse(value)
                            ngModel.$setValidity('json', true)
                        } catch(error) {
                            ngModel.$setValidity('json', false)
                        }
                    } else {
                        ngModel.$setValidity('json', true)
                    }

                    return value;
                });
            }
        };
    });

    _module.directive('checkGatewayPassword', function () {
        return {
            require: 'ngModel',
            link: function (scope, element, attributes, ngModel: any) {
                ngModel.$parsers.push(function (value?: string) {
                    if (scope.gatewayForm.password.$viewValue !== scope.gatewayForm.passwordConfirm.$viewValue) {
                        scope.gatewayForm.password.$setValidity('password', false)
                        scope.gatewayForm.passwordConfirm.$setValidity('password', false)
                    } else {
                        scope.gatewayForm.password.$setValidity('password', true)
                        scope.gatewayForm.passwordConfirm.$setValidity('password', true)
                    }

                    return value;
                });
            }
        };
    });

    _module.directive('checkGatewayEndpoint', function () {
        return {
            require: 'ngModel',
            link: function (scope, element, attributes, ngModel: any) {
                ngModel.$parsers.push(function (value?: string) {
                    if (value && value.length !== 0) {
                        var GATEWAY_VALIDATION = /^\$\{apiman\.gateway-endpoint:https?:\/\/[A-z0-9_\-\.]+(:[0-9]+)?\/apiman-gateway-api\}$|^https?:\/\/[A-z0-9_\-\.]+(:[0-9]+)?\/apiman-gateway-api$/

                        if (GATEWAY_VALIDATION.test(value)) {
                            ngModel.$setValidity('gw', true)
                        } else {
                            ngModel.$setValidity('gw', false)
                        }
                    } else {
                        ngModel.$setValidity('gw', true)
                    }

                    return value;
                });
            }
        };
    });

    _module.directive('checkGatewayUsername', function () {
        return {
            require: 'ngModel',
            link: function (scope, element, attributes, ngModel: any) {
                ngModel.$parsers.push(function (value?: string) {
                    if (value && value.length !== 0) {
                        var USERNAME_VALIDATION = /^\$\{apiman\.gateway-endpoint\.username:[a-z_]([a-z0-9_-]{0,31}|[a-z0-9_-]{0,30}\$)\}$|^[a-z_]([a-z0-9_-]{0,31}|[a-z0-9_-]{0,30}\$)$/

                        if (USERNAME_VALIDATION.test(value)) {
                            ngModel.$setValidity('username', true)
                        } else {
                            ngModel.$setValidity('username', false)
                        }
                    } else {
                        ngModel.$setValidity('username', true)
                    }

                    return value;
                });
            }
        };
    });
}
