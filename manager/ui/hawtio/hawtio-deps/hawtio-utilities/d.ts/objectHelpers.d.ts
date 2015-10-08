/// <reference path="includes.d.ts" />
/**
 * Module that provides functions related to working with javascript objects
 */
declare module ObjectHelpers {
    /**
     * Convert an array of 'things' to an object, using 'index' as the attribute name for that value
     * @param arr
     * @param index
     * @param decorator
     */
    function toMap(arr: Array<any>, index: string, decorator?: (any) => void): any;
}
