package poussecafe.spring.mongo.storage.source;

import java.util.List;
import poussecafe.source.generation.AggregatePackage;
import poussecafe.source.validation.SourceLine;
import poussecafe.source.validation.ValidationMessage;
import poussecafe.source.validation.ValidationMessageType;
import poussecafe.source.validation.types.StorageTypesValidator;
import poussecafe.spring.mongo.storage.SpringMongoDbStorage;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class MongoTypesValidator extends StorageTypesValidator {

    @Override
    protected String storageName() {
        return SpringMongoDbStorage.NAME;
    }

    @Override
    public List<ValidationMessage> validateAdditionalTypes(SourceLine sourceLine, AggregatePackage aggregatePackage) {
        var typeName = MongoStorageAdaptersCodeGenerator.aggregateMongoRepositoryTypeName(aggregatePackage);
        if(classResolver().loadClass(typeName).isEmpty()) {
            return asList(new ValidationMessage.Builder()
                    .location(sourceLine)
                    .type(ValidationMessageType.WARNING)
                    .message("Spring Data Mongo repository is missing, misplaced or not following naming convention")
                    .build());
        } else {
            return emptyList();
        }
    }
}
