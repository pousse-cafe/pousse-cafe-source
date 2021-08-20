package poussecafe.source.testmodel.model.aggregate2;

import poussecafe.domain.ValueObject;
import poussecafe.util.StringId;

public class Identifier2 extends StringId implements ValueObject {

    public Identifier2(String value) {
        super(value);
    }
}
