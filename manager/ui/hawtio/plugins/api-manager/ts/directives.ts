/// <reference path="../../includes.ts"/>
module Apiman {

    _module.directive('apimanActionBtn',
        ['Logger', function(Logger) {
            return {
                restrict: 'A',
                link: function(scope, element, attrs) {
                    var actionVar = attrs.field;
                    var actionText = attrs.placeholder;
                    var icon = attrs.icon;
                    Logger.debug("Action button initializing state variable [{0}].", actionVar);
                    scope[actionVar] = {
                        state: 'ready',
                        html: $(element).html(),
                        actionHtml: '<i class="fa fa-spin ' + icon + '"></i> ' + actionText
                    };
                    scope.$watch(actionVar + '.state', function() {
                        var newVal = scope[actionVar];
                        if (newVal.state == 'in-progress') {
                            $(element).prop('disabled', true);
                            $(element).html(newVal.actionHtml);
                        } else {
                            $(element).prop('disabled', false);
                            $(element).html(newVal.html);
                        }
                    });
                }
            };
        }]);


    _module.directive('apimanSelectPicker',
        ['Logger', '$timeout', '$parse', 'TranslationService',
        function(Logger, $timeout, $parse, TranslationService) {
            return {
                restrict: 'A',
                link: function(scope, element, attrs) {
                    function refresh(newVal) {
                        scope.$applyAsync(function() {
                            $(element)['selectpicker']('refresh');
                        });
                    }

                    $timeout(function() {
                        $(element)['selectpicker']();
                        $(element)['selectpicker']('refresh');
                    });

                    if (attrs.ngOptions && / in /.test(attrs.ngOptions)) {
                        var refreshModel = attrs.ngOptions.split(' in ')[1].split(' ')[0];
                        Logger.debug('Watching model {0} for {1}.', refreshModel, attrs.ngModel);
                        scope.$watch(refreshModel, function() {
                            scope.$applyAsync(function() {
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
                    scope.$on('$destroy', function() {
                        $timeout(function() {
                            $(element)['selectpicker']('destroy');
                        });
                    });

                    $timeout(function() {
                        $(element)['selectpicker']('refresh');
                    }, 200);
                }
            };
        }]);


    _module.directive('apimanPermission',
        ['Logger', 'CurrentUser',
        (Logger, CurrentUser) => {
            return {
                restrict: 'A',
                link: function($scope, element, attrs) {
                    var refresh = function(newValue) {
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
        ['Logger', 'EntityStatusService', 
        function(Logger, EntityStatusService) {
            return {
                restrict: 'A',
                link: function(scope, element, attrs) {
                    scope.$watch(function($scope) {
                        return EntityStatusService.getEntityStatus();
                    }, function(newValue, oldValue) {
                        var entityStatus = newValue;
                        var elem = element;
                        if (entityStatus) {
                            var validStatuses = attrs.apimanStatus.split(',');
                            var statusIsValid = false;
                            Logger.debug('Checking status {0} against valid statuses {1}:  {2}', entityStatus, '' + validStatuses,
                                    element[0].outerHTML);
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

    _module.factory('EntityStatusService', 
        ['$rootScope',
        function($rootScope) {
            var entityStatus = null;

            return {
                setEntityStatus: function(status) {
                    entityStatus = status;
                },

                getEntityStatus: function() {
                    return entityStatus;
                }
            };
        }]);

    _module.directive('apimanEntityStatus',
        ['Logger', 'EntityStatusService', 
        function(Logger, EntityStatusService) {
            return {
                restrict: 'A',
                link: function(scope, element, attrs) {
                    scope.$watch(function($scope) {
                        return EntityStatusService.getEntityStatus();
                    }, function(newValue, oldValue) {
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
        ['Logger', 'TranslationService', 
        function(Logger, TranslationService) {
            return {
                restrict: 'E',
                replace: true,
                templateUrl: 'plugins/api-manager/html/directives/searchBox.html',
                scope: {
                    searchFunction: '=function'
                },
                link: function(scope, element, attrs) {
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
                        var translatedValue = TranslationService.translate(translationKey, defaultValue);
                        scope.placeholder = translatedValue;
                    }

                    scope.doSearch = function() {
                        $(element).find('button i').removeClass('fa-search');
                        $(element).find('button i').removeClass('fa-close');
                        if (scope.value) {
                            $(element).find('button i').addClass('fa-close');
                        } else {
                            $(element).find('button i').addClass('fa-search');
                        }
                        scope.searchFunction(scope.value);
                    };
                    scope.onClick = function() {
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

    _module.directive('apimanConfirmModal',
        ['Logger', 
        function(Logger) {
            return {
                templateUrl: 'plugins/api-manager/html/directives/confirmModal.html',
                replace: true,
                restrict: 'E',
                transclude: true,
                link: function(scope, element, attrs) {
                    scope.title = attrs.modalTitle;
                    $(element).on('hidden.bs.modal', function() {
                        $(element).remove();
                    });
                }
            };
        }]);

    _module.directive('apimanSelectServiceModal',
        ['Logger', 
        function(Logger) {
            return {
                templateUrl: 'plugins/api-manager/html/directives/selectServiceModal.html',
                replace: true,
                restrict: 'E',
                link: function(scope, element, attrs) {
                    scope.title = attrs.modalTitle;
                    $(element).on('hidden.bs.modal', function() {
                        $(element).remove();
                    });
                }
            };
        }]);

    var entryTypeClasses = {
        Organization : 'fa-shield',
        Application : 'fa-gears',
        Plan : 'fa-bar-chart-o',
        Service : 'fa-puzzle-piece'
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
                link: function(scope, element, attrs) {
                    scope.pluginName = $rootScope.pluginName;
                    scope.hasMore = true;
                    scope.getEntryIcon = function(entry) {
                        return entryTypeClasses[entry.entityType];
                    };
                    scope.getMore = function() {
                        scope.getMoreButton.state = 'in-progress';
                        scope.next(function(newEntries) {
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
        function(Logger, $rootScope) {
            return {
                restrict: 'E',
                scope: {
                    entry: '=model'
                },
                link: function(scope, element, attrs) {
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
                require : 'ngModel',
                scope: {
                    ngModel: '='
                },
                link: function($scope, $elem, $attrs, ngModel) {
                    $elem.on('dragover', function(e) {
                        e.preventDefault();
                        if (e.dataTransfer) {
                            e.dataTransfer.effectAllowed = 'copy';
                        }
                        if (!$elem.hasClass('dropping')) {
                            $elem.addClass('dropping');
                        }
                        return false;
                    });
                    $elem.on('dragenter', function(e) {
                        e.preventDefault();
                        if (e.dataTransfer) {
                            e.dataTransfer.effectAllowed = 'copy';
                        }
                        $elem.addClass('dropping');
                        return false;
                    });
                    $elem.on('dragleave', function(e) {
                        e.preventDefault();
                        $elem.removeClass('dropping');
                        return false;
                    });

                    $elem.on('drop', function(e) {
                        e.preventDefault();
                        $elem.removeClass('dropping');
                        if (e.originalEvent.dataTransfer && e.originalEvent.dataTransfer.files.length){
                            if (e.preventDefault) e.preventDefault();
                            if (e.stopPropagation) e.stopPropagation();

                            var firstFile = e.originalEvent.dataTransfer.files[0];
                            var reader = new FileReader();

                            reader.onload = (function(theFile) {
                                return function(result) {
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
        function(Logger) {
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

                        orderChanged: function(event) {
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
        ['Logger', 
        function(Logger) {
            return {
                restrict: 'E',
                scope: {
                    descr: '=description',
                    callback: '='
                },
                controller: ['$scope', ($scope) => {
                }],
                link: function($scope, $elem, $attrs) {
                    $scope.defaultValue = $attrs.defaultValue;

                    var elem = null;
                    var previousRows = 1;

                    $scope.topPosition = 0;
                    $scope.leftPosition = 0;
                    $scope.height = 60;
                    
                    console.log('$scope.desc: ' + $scope.desc);

                    // If description is updated, call updateFunction.
                    $scope.$watch(function() {
                        //console.log('Description: ' + $scope.org.description);
                        return $scope.descr;
                    },
                    function(new_value, old_value) {
                        if (old_value !== new_value && typeof new_value !== 'undefined') {
                             $scope.callback(new_value || '');
                         }
                    });

                    $scope.focusOnDescription = function(event) {
                        elem = event.target;
                        elem.value = $scope.descr || '';

                        $(elem).css('height', 'auto');
                        $(elem).height(elem.scrollHeight);
                    };

                    $scope.changeOnDescription = function() {
                        $(elem).css('height', 'auto');
                        $(elem).height(elem.scrollHeight);
                    };

                    $scope.descriptionMouseOver = function(event) {
                        $scope.showPencil = true;
                        var elem = event.target;
                        var position = elem.getBoundingClientRect();

                        // Calculate position of pen
                        // console.log("elem.top " + position.top);
                        // console.log("elem.bottom " + position.bottom);
                        // console.log("elem.left " + position.left);
                        // console.log("elem.right " + position.right);

                        if (position.right != 0) {
                            $scope.leftPosition = (position.right - position.left) - 15;
                            $scope.height = (position.bottom - position.top);
                        }
                    }

                    $scope.descriptionMouseOut = function(event) {
                        $scope.showPencil = false;
                    }

                },
                templateUrl: 'plugins/api-manager/html/directives/editDescription.html'
            }
        }]);

    _module.run(['editableOptions', 'editableThemes', function(editableOptions, editableThemes) {
      editableOptions.theme = 'default';

      // overwrite templates
      editableThemes['default'].submitTpl = '<button class="btn btn-default inline-save-btn" type="submit"><i class="fa fa-check fa-fw"></i></button>';
      editableThemes['default'].cancelTpl = '<button class="btn btn-default" type="button" ng-click="$form.$cancel()"><i class="fa fa-times fa-fw"></i></button>';
      editableThemes['default'].buttonsTpl = '<div></div>';
      editableThemes['default'].formTpl = '<form class="editable-wrap apiman-inline-edit"></form>';
    }]);

    _module.directive('apimanI18nKey',
        ['Logger', 'TranslationService',
        function(Logger, TranslationService) {
            return {
                restrict: 'A',
                link: function(scope, element, attrs) {
                    if (!attrs.apimanI18nKey) {
                        return;
                    }

                    var translationKey, defaultValue, translatedValue;

                    // Process the text of the element only if it has no child elements
                    if ($(element).children().length == 0) {
                        translationKey = attrs.apimanI18nKey;
                        defaultValue = $(element).text();
                        translatedValue = TranslationService.translate(translationKey, defaultValue);
                        $(element).text(translatedValue);
                    }

                    // Now process the placeholder attribute.
                    if ($(element).attr('placeholder')) {
                        translationKey = attrs.apimanI18nKey + '.placeholder';
                        defaultValue = $(element).attr('placeholder');
                        translatedValue = TranslationService.translate(translationKey, defaultValue);
                        Logger.debug('Translating placeholder attr.  Key: {2}  default value: {0}  translated: {1}', defaultValue, translatedValue, translationKey);
                        $(element).prop('placeholder', translatedValue);
                        $(element).attr('placeholder', translatedValue);
                    }

                    // Now process the title attribute.
                    if ($(element).attr('title')) {
                        translationKey = attrs.apimanI18nKey + '.title';
                        defaultValue = $(element).attr('title');
                        translatedValue = TranslationService.translate(translationKey, defaultValue);
                        Logger.debug('Translating title attr.  Key: {2}  default value: {0}  translated: {1}', defaultValue, translatedValue, translationKey);
                        $(element).prop('title', translatedValue);
                        $(element).attr('title', translatedValue);
                    }
                }
            };
        }]);

}
