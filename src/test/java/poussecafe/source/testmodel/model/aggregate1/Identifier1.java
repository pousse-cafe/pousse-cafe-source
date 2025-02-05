package poussecafe.source.testmodel.model.aggregate1;

import poussecafe.annotations.Trivial;
import poussecafe.domain.ValueObject;
import poussecafe.util.StringId;

@Trivial
public class Identifier1 extends StringId implements ValueObject {

    public Identifier1(String value) {
        super(value);
    }

    public Identifier1 build() {
        return null;
    }
}
