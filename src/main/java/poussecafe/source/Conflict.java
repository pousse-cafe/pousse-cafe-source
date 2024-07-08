package poussecafe.source;

import java.util.Objects;

import poussecafe.source.analysis.ClassName;

public class Conflict {

    public ClassName outerModuleClass() {
        return outerModuleClass;
    }

    private ClassName outerModuleClass;

    public ClassName innerModuleClass() {
        return innerModuleClass;
    }

    private ClassName innerModuleClass;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Conflict conflict = new Conflict();

        public Conflict build() {
            Objects.requireNonNull(conflict.outerModuleClass);
            Objects.requireNonNull(conflict.innerModuleClass);
            return conflict;
        }

        public Builder outerModuleClass(ClassName outerModuleClass) {
            conflict.outerModuleClass = outerModuleClass;
            return this;
        }

        public Builder innerModuleClass(ClassName innerModuleClass) {
            conflict.innerModuleClass = innerModuleClass;
            return this;
        }
    }
}
