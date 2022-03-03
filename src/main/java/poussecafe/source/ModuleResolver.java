package poussecafe.source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import poussecafe.source.analysis.ClassName;

public class ModuleResolver {

    public ModuleResolver(Collection<ClassName> modules) {
        sortedModules = new ClassName[modules.size()];
        modules.toArray(sortedModules);
        Arrays.sort(sortedModules, moduleComparator);
    }

    private ClassName[] sortedModules;

    private Comparator<ClassName> moduleComparator = (m1, m2) -> m1.qualifier().compareTo(m2.qualifier());

    public List<Conflict> detectConflicts() {
        var conflicts = new ArrayList<Conflict>();
        for(int i = sortedModules.length - 1; i >= 1; --i) {
            if(sortedModules[i].qualifier().startsWith(sortedModules[i - 1].qualifier())) {
                conflicts.add(Conflict.builder()
                        .outerModuleClass(sortedModules[i - 1])
                        .innerModuleClass(sortedModules[i])
                        .build());
            }
        }
        return conflicts;
    }

    public Optional<ClassName> findModule(ClassName componentClass) {
        var componentQualifiedClassName = componentClass.qualified();
        for(int i = sortedModules.length - 1; i >= 0; --i) {
            if(componentQualifiedClassName.startsWith(sortedModules[i].qualifier() + ".")) {
                return Optional.of(sortedModules[i]);
            }
        }
        return Optional.empty();
    }
}
