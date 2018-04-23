package edu.pitt.dbmi.ccd.annotations.exception;

/**
 * Mark Silvis (marksilvis@pitt.edu)
 */
public class AnnotationDataNotFoundException extends NotFoundException {

    private static final String DATA = "Annotation Data";
    private static final String ID = "id";

    public AnnotationDataNotFoundException(Long id) {
        super(DATA, ID, id);
    }
}
