package poussecafe.source.validation.names;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import poussecafe.collection.Pair;
import poussecafe.source.Conflict;
import poussecafe.source.ModuleResolver;
import poussecafe.source.analysis.ClassName;
import poussecafe.source.validation.ValidationMessage;
import poussecafe.source.validation.ValidationMessageType;
import poussecafe.source.validation.model.Module;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class Modules {

    public Modules(Collection<Module> modules) {
        modulesMap = modules.stream().collect(toMap(Module::className, module -> module));
        resolver = new ModuleResolver(modules.stream().map(Module::className).collect(toList()));
    }

    private Map<ClassName, Module> modulesMap = new HashMap<>();

    private ModuleResolver resolver;

    public List<ValidationMessage> checkModulesPartition() {
        return resolver.detectConflicts().stream()
                .map(conflict -> new Pair<Conflict, Module>(conflict, modulesMap.get(conflict.innerModuleClass())))
                .filter(pair -> pair.two() != null && pair.two().sourceLine().isPresent())
                .map(pair -> new ValidationMessage.Builder()
                        .location(pair.two().sourceLine().orElseThrow())
                        .type(ValidationMessageType.ERROR)
                        .message("Base package is a subpackage of module " + pair.one().outerModuleClass())
                        .build())
                .collect(toList());
    }

    public ClassName qualifyName(NamedComponent component) {
        var moduleClass = resolver.findModule(component.className());
        if(moduleClass.isPresent()) {
            return new ClassName(moduleClass.orElseThrow().simple(), component.name());
        } else {
            return new ClassName(component.name());
        }
    }
}
