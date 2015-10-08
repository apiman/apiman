/// <reference path="includes.d.ts" />
/**
 * @module UI
 */
declare module UI {
    var scrollBarWidth: number;
    function findParentWith($scope: any, attribute: any): any;
    function getIfSet(attribute: any, $attr: any, def: any): any;
    function observe($scope: any, $attrs: any, key: any, defValue: any, callbackFunc?: any): void;
    function getScrollbarWidth(): number;
}
