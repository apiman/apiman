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

    
    _module.directive('apimanPermission',
        ['Logger', 'CurrentUser', function(Logger, CurrentUser) {
            return {
                restrict: 'A',
                link: function(scope, element, attrs) {
                    scope.$watch('organizationId', function(newValue, oldValue) {
                        var orgId = newValue;
                        if (orgId) {
                            var permission = attrs.apimanPermission;
                            Logger.debug('Checking authorization :: permission {0}/{1}.', orgId, permission);
                            if (!CurrentUser.hasPermission(orgId, permission)) {
                                $(element).hide();
                            }
                        } else {
                            Logger.debug('Missing organizationId from $scope - authorization disabled.');
                        }
                    });
                }
            };
        }]);

}
