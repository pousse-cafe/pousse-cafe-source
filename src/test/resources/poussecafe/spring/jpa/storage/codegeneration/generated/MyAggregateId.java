package poussecafe.spring.jpa.storage.codegeneration.generated;

import poussecafe.domain.ValueObject;
import poussecafe.util.StringId;

public class MyAggregateId extends StringId implements ValueObject {

    public MyAggregateId(String value) {
        super(value);
    }
}