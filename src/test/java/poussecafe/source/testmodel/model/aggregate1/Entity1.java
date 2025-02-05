package poussecafe.source.testmodel.model.aggregate1;

import poussecafe.annotations.ShortDescription;
import poussecafe.domain.Entity;
import poussecafe.domain.EntityAttributes;

/**
 * Entity1 documentation.
 */
@ShortDescription("Entity1 short")
public class Entity1 extends Entity<String, Entity1.Attributes> {

    public static interface Attributes extends EntityAttributes<String> {

    }
}
