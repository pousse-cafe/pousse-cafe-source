package poussecafe.source.model;

import java.io.Serializable;
import java.util.Optional;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static poussecafe.util.Equality.referenceEquals;

@SuppressWarnings("serial")
public class Documentation implements Serializable {

    public static Documentation empty() {
        return EMPTY_DOCUMENTATION;
    }

    private static final Documentation EMPTY_DOCUMENTATION = new Documentation();

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    private String description;

    public Optional<String> shortDescription() {
        return Optional.ofNullable(shortDescription);
    }

    private String shortDescription;

    public boolean trivial() {
        return trivial;
    }

    private boolean trivial;

    public static class Builder {

        public Documentation build() {
            return documentation;
        }

        private Documentation documentation = new Documentation();

        public Builder description(String description) {
            documentation.description = description;
            return this;
        }

        public Builder shortDescription(String shortDescription) {
            documentation.shortDescription = shortDescription;
            return this;
        }

        public Builder trivial(boolean trivial) {
            documentation.trivial = trivial;
            return this;
        }
    }

    private Documentation() {

    }

    @Override
    public boolean equals(Object obj) {
        return referenceEquals(this, obj).orElse(other -> new EqualsBuilder()
                .append(description, other.description)
                .append(shortDescription, other.shortDescription)
                .append(trivial, other.trivial)
                .build());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(description)
                .append(shortDescription)
                .append(trivial)
                .build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append(description)
                .append(shortDescription)
                .append(trivial)
                .build();
    }
}
