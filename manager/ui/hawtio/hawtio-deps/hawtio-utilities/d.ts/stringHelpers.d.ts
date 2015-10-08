/// <reference path="includes.d.ts" />
declare module StringHelpers {
    function isDate(str: any): boolean;
    /**
     * Convert a string into a bunch of '*' of the same length
     * @param str
     * @returns {string}
     */
    function obfusicate(str: String): String;
    /**
     * Simple toString that obscures any field called 'password'
     * @param obj
     * @returns {string}
     */
    function toString(obj: any): string;
}
