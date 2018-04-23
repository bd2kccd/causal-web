package edu.pitt.dbmi.ccd.annotations.exception;

/**
 * Mark Silvis (marksilvis@pitt.edu)
 */
public class AnnotationNotFoundException extends NotFoundException {

    private static final String ANNO = "Annotation";
    private static final String ID = "id";

    public AnnotationNotFoundException(Long id) {
        super(ANNO, ID, id);
    }
}
