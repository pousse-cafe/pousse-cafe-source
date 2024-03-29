package poussecafe.source.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import poussecafe.source.Source;
import poussecafe.source.analysis.SafeClassName;

import static java.util.Objects.requireNonNull;
import static poussecafe.util.Equality.referenceEquals;

@SuppressWarnings("serial")
public class TypeComponent implements Serializable, Documented {

    private Source source;

    public Source source() {
        return source;
    }

    private SafeClassName typeName;

    public SafeClassName typeName() {
        return typeName;
    }

    private Documentation documentation = Documentation.empty();

    @Override
    public Documentation documentation() {
        return documentation;
    }

    public List<TypeReference> references() {
        return Collections.unmodifiableList(references);
    }

    private List<TypeReference> references = new ArrayList<>();

    public static class Builder {

        private TypeComponent component = new TypeComponent();

        public TypeComponent build() {
            requireNonNull(component.typeName);
            requireNonNull(component.source);
            requireNonNull(component.documentation);
            return component;
        }

        public Builder name(SafeClassName typeName) {
            component.typeName = typeName;
            return this;
        }

        public Builder source(Source source) {
            component.source = source;
            return this;
        }

        public Builder documentation(Documentation documentation) {
            component.documentation = documentation;
            return this;
        }

        public Builder reference(TypeReference reference) {
            component.references.add(reference);
            return this;
        }

        public Builder references(Collection<TypeReference> references) {
            references.forEach(this::reference);
            return this;
        }
    }

    private TypeComponent() {

    }

    @Override
    public boolean equals(Object obj) {
        return referenceEquals(this, obj).orElse(other -> new EqualsBuilder()
                .append(source, other.source)
                .append(typeName, other.typeName)
                .append(documentation, other.documentation)
                .append(references, other.references)
                .build());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(source)
                .append(typeName)
                .append(documentation)
                .append(references)
                .build();
    }
}
