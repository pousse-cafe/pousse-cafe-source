package poussecafe.spring.jpa.storage.codegeneration.generated.adapters;

import org.springframework.beans.factory.annotation.Autowired;
import poussecafe.discovery.DataAccessImplementation;
import poussecafe.spring.jpa.storage.JpaDataAccess;
import poussecafe.spring.jpa.storage.SpringJpaStorage;
import poussecafe.spring.jpa.storage.codegeneration.generated.MyAggregateId;
import poussecafe.spring.jpa.storage.codegeneration.generated.MyAggregateRepository.DataAccess;
import poussecafe.spring.jpa.storage.codegeneration.generated.MyAggregateRoot;

@DataAccessImplementation(
    aggregateRoot = MyAggregateRoot.class,
    dataImplementation = MyAggregateAttributes.class,
    storageName = SpringJpaStorage.NAME
)
public class MyAggregateSpringJpaDataAccess extends JpaDataAccess<MyAggregateId, MyAggregateAttributes, String>
        implements DataAccess<MyAggregateAttributes> {

    @Override
    protected String convertId(MyAggregateId key) {
        return key.stringValue();
    }

    @Override
    protected MyAggregateAttributesJpaRepository jpaRepository() {
        return repository;
    }

    @Autowired
    private MyAggregateAttributesJpaRepository repository;
}