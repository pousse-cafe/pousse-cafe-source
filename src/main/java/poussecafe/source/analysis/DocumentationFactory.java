package poussecafe.source.analysis;

import java.util.Optional;
import org.eclipse.jdt.core.dom.Javadoc;

import poussecafe.annotations.ShortDescription;
import poussecafe.annotations.Trivial;
import poussecafe.source.JavaDocStringBuilder;
import poussecafe.source.model.Documentation;

public class DocumentationFactory {

    public static Documentation documentation(Javadoc javadoc, AnnotatedElement<?> annotatedElement) {
        var builder = new Documentation.Builder();
        var description = Optional.ofNullable(javadoc)
                .map(string -> new JavaDocStringBuilder().withJavadoc(string).build());
        if(description.isPresent()) {
            builder.description(description.orElseThrow());
        }
        var shortDescription = annotatedElement.findAnnotation(ShortDescription.class.getCanonicalName())
                .map(annotation -> annotation.attribute("value"))
                .filter(Optional::isPresent).map(Optional::orElseThrow)
                .map(ResolvedExpression::asString);
        if(shortDescription.isPresent()) {
            builder.shortDescription(shortDescription.orElseThrow());
        }
        builder.trivial(annotatedElement.findAnnotation(Trivial.class.getCanonicalName()).isPresent());
        return builder.build();
    }

    private DocumentationFactory() {

    }
}
