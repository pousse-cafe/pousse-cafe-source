package poussecafe.source;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import poussecafe.source.analysis.ClassName;

@Builder
@Value
@Accessors(fluent = true)
public class Conflict {

    private ClassName outerModuleClass;
    
    private ClassName innerModuleClass;
}
