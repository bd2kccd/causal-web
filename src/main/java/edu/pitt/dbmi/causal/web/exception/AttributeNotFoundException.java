package edu.pitt.dbmi.ccd.annotations.exception;

/**
 * Mark Silvis (marksilvis@pitt.edu)
 */
public class AttributeNotFoundException extends NotFoundException {

    private static final String ATTRIB = "attribute";
    private static final String VOCAB = "specification";
    private static final String ID = "id";

    public AttributeNotFoundException(Long id) {
        super(ATTRIB, ID, id);
    }

    public AttributeNotFoundException(Long vocabulary, Long id) {
        super(ATTRIB, new String[]{VOCAB, ID}, new Object[]{vocabulary, id});
    }
}
