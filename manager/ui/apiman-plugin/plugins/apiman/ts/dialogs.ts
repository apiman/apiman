/// <reference path="../../includes.ts"/>
module ApimanDialogs {

    export var _module = angular.module("ApimanDialogs", ["ApimanLogger"]);

    export var Dialogs = _module.factory('Dialogs', ['Logger', '$compile', '$rootScope', '$timeout',
        function(Logger, $compile, $rootScope, $timeout) {
            return {
                confirm: function(title, message, yesCallback, noCallback) {
                    var modalScope = $rootScope.$new(true);
                    modalScope.onYes = function() {
                        if (yesCallback) { yesCallback(); }
                    };
                    modalScope.onNo = function() {
                        if (noCallback) { noCallback(); }
                    };
                    modalScope.title = title;
                    modalScope.message = message;
                    $('body').append($compile('<apiman-confirm-modal title="{{ title }}">{{ message }}</apiman-confirm-modal>')(modalScope));
                    $timeout(function() {
                        $('#confirmModal')['modal']({'keyboard': true, 'backdrop': 'static'});
                    }, 1);
                }
            };
        }]);

}