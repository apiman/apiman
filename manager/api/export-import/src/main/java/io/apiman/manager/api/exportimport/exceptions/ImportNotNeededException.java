package io.apiman.manager.api.exportimport.exceptions;

/**
 * If no import is needed this exception can be thrown to abort/cancel the import
 */
public class ImportNotNeededException extends Exception {

    /**
     * Constructor
     * @param message the error message
     */
    public ImportNotNeededException (String message){
        super(message);
    }
}
