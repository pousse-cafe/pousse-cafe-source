package poussecafe.source.testmodel.model.aggregate1;

import poussecafe.domain.Entity;
import poussecafe.domain.EntityAttributes;
import poussecafe.source.ShortDescription;

/**
 * Entity1 documentation.
 */
@ShortDescription("Entity1 short")
public class Entity1 extends Entity<String, Entity1.Attributes> {

    public static interface Attributes extends EntityAttributes<String> {

    }
}
