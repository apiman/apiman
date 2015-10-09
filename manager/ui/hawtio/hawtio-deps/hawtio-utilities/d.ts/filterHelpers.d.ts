/// <reference path="baseHelpers.d.ts" />
declare module FilterHelpers {
    var log: Logging.Logger;
    function search(object: any, filter: string, maxDepth?: number, and?: boolean): boolean;
    /**
     * Tests if an object contains the text in "filter".  The function
     * only checks the values in an object and ignores keys altogether,
     * can also work with strings/numbers/arrays
     * @param object
     * @param filter
     * @returns {boolean}
     */
    function searchObject(object: any, filter: string, maxDepth?: number, depth?: number): boolean;
}
