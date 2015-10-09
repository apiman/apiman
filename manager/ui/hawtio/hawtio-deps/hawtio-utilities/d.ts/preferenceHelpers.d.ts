/// <reference path="includes.d.ts" />
declare module Core {
    /**
    * Parsers the given value as JSON if it is define
    */
    function parsePreferencesJson(value: any, key: any): any;
    function initPreferenceScope($scope: any, localStorage: any, defaults: any): void;
    /**
     * Returns true if there is no validFn defined or if its defined
     * then the function returns true.
     *
     * @method isValidFunction
     * @for Perspective
     * @param {Core.Workspace} workspace
     * @param {Function} validFn
     * @param {string} perspectiveId
     * @return {Boolean}
     */
    function isValidFunction(workspace: any, validFn: any, perspectiveId: any): any;
}
