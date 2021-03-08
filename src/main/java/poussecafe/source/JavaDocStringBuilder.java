package poussecafe.source;

import java.util.List;
import org.eclipse.jdt.core.dom.IDocElement;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;

public class JavaDocStringBuilder {

    public String build() {
        return builder.toString();
    }

    private StringBuilder builder = new StringBuilder();

    public JavaDocStringBuilder withJavadoc(Javadoc javadoc) {
        @SuppressWarnings("unchecked")
        List<TagElement> tags = javadoc.tags();
        tags.forEach(this::withTagElement);
        return this;
    }

    public JavaDocStringBuilder withTagElement(TagElement tag) {
        @SuppressWarnings("unchecked")
        List<IDocElement> docElements = tag.fragments();
        docElements.forEach(this::withDocElement);
        return this;
    }

    public JavaDocStringBuilder withDocElement(IDocElement tag) {
        if(tag instanceof TextElement) {
            TextElement textElement = (TextElement) tag;
            builder.append(textElement.getText());
        }
        return this;
    }
}
