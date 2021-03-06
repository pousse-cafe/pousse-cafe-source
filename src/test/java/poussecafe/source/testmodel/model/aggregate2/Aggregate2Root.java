package poussecafe.source.testmodel.model.aggregate2;

import poussecafe.discovery.*;
import poussecafe.domain.EntityAttributes;
import poussecafe.source.testmodel.model.events.Event2;
import poussecafe.source.testmodel.model.events.Event3;
import poussecafe.source.testmodel.process.Process1;

public class Aggregate2Root extends poussecafe.domain.AggregateRoot<Identifier2, Aggregate2Root.Attributes> {

    @MessageListener(processes = {Process1.class}, runner = Process1Listener2Runner.class)
    @ProducesEvent(value = Event3.class, required = false, consumedByExternal = "External2")
    public void process1Listener2(Event2 event) {

    }

    public static interface Attributes extends EntityAttributes<Identifier2> {

    }
}
