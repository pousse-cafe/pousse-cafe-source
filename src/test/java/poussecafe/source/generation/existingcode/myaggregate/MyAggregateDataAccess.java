package poussecafe.source.generation.existingcode.myaggregate;

import poussecafe.domain.EntityDataAccess;

public interface MyAggregateDataAccess<D extends MyAggregate.Attributes> extends EntityDataAccess<MyAggregateId, D> {

}
