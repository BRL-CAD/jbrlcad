package org.brlcad.geometryservice;

/**
 * Exception that is thrown when there is an issue retrieving geometry
 * from the BRL-CAD geometry service
 */
public class GeometryServiceException extends Exception {


    public GeometryServiceException() {
    }

    public GeometryServiceException(String s) {
        super(s);
    }

    public GeometryServiceException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public GeometryServiceException(Throwable throwable) {
        super(throwable);
    }
}

