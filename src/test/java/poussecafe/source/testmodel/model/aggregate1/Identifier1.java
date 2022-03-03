package poussecafe.source.testmodel.model.aggregate1;

import poussecafe.domain.ValueObject;
import poussecafe.util.StringId;

public class Identifier1 extends StringId implements ValueObject {

    public Identifier1(String value) {
        super(value);
    }

    public Identifier1 build() {
        return null;
    }
}
