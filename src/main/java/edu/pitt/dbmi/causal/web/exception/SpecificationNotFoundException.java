package edu.pitt.dbmi.ccd.annotations.exception;

/**
 * Mark Silvis (marksilvis@pitt.edu)
 */
public class SpecificationNotFoundException extends NotFoundException {

    private static final String VOCAB = "Vocabulary";
    private static final String NAME = "name";
    private static final String ID = "id";

    public SpecificationNotFoundException(String name) {
        super(VOCAB, NAME, name);
    }

    public SpecificationNotFoundException(Long id) {
        super(VOCAB, ID, id);
    }
}
