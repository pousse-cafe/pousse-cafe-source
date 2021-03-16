package poussecafe.source.testmodel.model.aggregate1;

import poussecafe.attribute.Attribute;
import poussecafe.attribute.entity.EntityAttribute;

public class Aggregate1Attributes implements Aggregate1.Root.Attributes {

    @Override
    public Attribute<Identifier1> identifier() {
        return null;
    }

    @Override
    public Attribute<ValueObject1> valueObject1() {
        return null;
    }

    @Override
    public EntityAttribute<Entity1> entity1() {
        return null;
    }
}
