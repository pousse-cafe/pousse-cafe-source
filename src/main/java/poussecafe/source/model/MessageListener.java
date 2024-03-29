package poussecafe.source.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import poussecafe.discovery.DefaultProcess;
import poussecafe.source.Source;
import poussecafe.source.analysis.MessageListenerMethod;
import poussecafe.source.analysis.ResolvedType;
import poussecafe.source.analysis.ResolvedTypeName;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static poussecafe.util.Equality.referenceEquals;

@SuppressWarnings("serial")
public class MessageListener implements Serializable, Documented {

    public MessageListenerContainer container() {
        return container;
    }

    private MessageListenerContainer container;

    public String methodName() {
        return methodName;
    }

    private String methodName;

    public Message consumedMessage() {
        return consumedMessage;
    }

    private Message consumedMessage;

    public List<String> processNames() {
        return processNames;
    }

    private List<String> processNames = singletonList(DefaultProcess.class.getSimpleName());

    public List<ProducedEvent> producedEvents() {
        return producedEvents;
    }

    private List<ProducedEvent> producedEvents = new ArrayList<>();

    public Optional<String> runnerName() {
        return Optional.ofNullable(runnerName);
    }

    private String runnerName;

    public Optional<String> runnerClass() {
        return Optional.ofNullable(runnerClass);
    }

    private String runnerClass;

    public List<String> consumesFromExternal() {
        return Collections.unmodifiableList(consumesFromExternal);
    }

    private List<String> consumesFromExternal = new ArrayList<>();

    public Optional<Cardinality> returnTypeCardinality() {
        return Optional.ofNullable(returnTypeCardinality);
    }

    private Cardinality returnTypeCardinality;

    public boolean isLinkedToAggregate() {
        return container().aggregateName().isPresent();
    }

    public String aggregateName() {
        return container().aggregateName().orElseThrow();
    }

    public Source source() {
        return source;
    }

    private Source source;

    public String id() {
        return aggregateName() + "." + methodName + "(" + consumedMessage.name() + ")";
    }

    @Override
    public Documentation documentation() {
        return documentation;
    }

    private Documentation documentation = Documentation.empty();

    public static class Builder {

        private MessageListener messageListener = new MessageListener();

        public MessageListener build() {
            requireNonNull(messageListener.container);
            requireNonNull(messageListener.methodName);
            requireNonNull(messageListener.consumedMessage);
            requireNonNull(messageListener.source);
            requireNonNull(messageListener.documentation);

            if(messageListener.container.type().isFactory()
                    && messageListener.returnTypeCardinality == null) {
                throw new IllegalStateException("Production type must be present with factory listeners");
            }

            if(!processNames.isEmpty()) {
                messageListener.processNames = processNames;
            }

            return messageListener;
        }

        public Builder withContainer(MessageListenerContainer container) {
            messageListener.container = container;
            return this;
        }

        public Builder withMethodDeclaration(MessageListenerMethod method) {
            String methodName = method.name();

            messageListener.methodName = methodName;
            messageListener.consumedMessage = Message.ofTypeName(method.consumedMessage().orElseThrow());

            messageListener.consumesFromExternal.addAll(method.consumesFromExternal());
            List<ResolvedTypeName> processes = method.processes();
            processes.stream().map(ResolvedTypeName::simpleName).forEach(processNames::add);

            messageListener.producedEvents = method.producedEvents().stream()
                    .map(annotation -> new ProducedEvent.Builder()
                            .withAnnotation(annotation)
                            .build())
                    .collect(toList());

            messageListener.runnerName = method.runner().map(ResolvedTypeName::simpleName).orElse(null);
            messageListener.runnerClass = method.runner().map(ResolvedTypeName::qualifiedName).orElse(null);

            Optional<ResolvedType> returnType = method.returnType();
            if(returnType.isPresent()
                    && !returnType.get().isPrimitive()) {
                messageListener.returnTypeCardinality = returnTypeCardinality(returnType.get());
            }

            messageListener.documentation = method.documentation();

            return this;
        }

        private Cardinality returnTypeCardinality(ResolvedType returnType) {
            ResolvedTypeName typeName = returnType.genericTypeName();
            if(typeName.instanceOf(Collection.class.getCanonicalName())) {
                return Cardinality.SEVERAL;
            } else if(typeName.instanceOf(Optional.class.getCanonicalName())) {
                return Cardinality.OPTIONAL;
            } else {
                return Cardinality.SINGLE;
            }
        }

        public Builder withMethodName(String methodName) {
            messageListener.methodName = methodName;
            return this;
        }

        public Builder withConsumedMessage(Message consumedMessage) {
            messageListener.consumedMessage = consumedMessage;
            return this;
        }

        public Builder withReturnTypeCardinality(Optional<Cardinality> productionType) {
            messageListener.returnTypeCardinality = productionType.orElse(null);
            return this;
        }

        public Builder withRunnerName(Optional<String> runnerName) {
            messageListener.runnerName = runnerName.orElse(null);
            return this;
        }

        public Builder withRunnerClass(Optional<String> runnerClass) {
            messageListener.runnerClass = runnerClass.orElse(null);
            return this;
        }

        public Builder withProducedEvent(ProducedEvent producedEvent) {
            messageListener.producedEvents.add(producedEvent);
            return this;
        }

        public Builder withProducedEvents(List<ProducedEvent> producedEvents) {
            messageListener.producedEvents.addAll(producedEvents);
            return this;
        }

        public Builder withConsumesFromExternal(List<String> consumesFromExternal) {
            messageListener.consumesFromExternal.addAll(consumesFromExternal);
            return this;
        }

        public Builder withProcessName(String processName) {
            processNames.add(processName);
            return this;
        }

        private List<String> processNames = new ArrayList<>();

        public Builder withProcessNames(List<String> processNames) {
            this.processNames.addAll(processNames);
            return this;
        }

        public Builder withSource(Source source) {
            messageListener.source = source;
            return this;
        }

        public Builder withDocumentation(Documentation documentation) {
            messageListener.documentation = documentation;
            return this;
        }
    }

    private MessageListener() {

    }

    @Override
    public boolean equals(Object obj) {
        return referenceEquals(this, obj).orElse(other -> new EqualsBuilder()
                .append(consumedMessage, other.consumedMessage)
                .append(consumesFromExternal, other.consumesFromExternal)
                .append(container, other.container)
                .append(methodName, other.methodName)
                .append(processNames, other.processNames)
                .append(producedEvents, other.producedEvents)
                .append(returnTypeCardinality, other.returnTypeCardinality)
                .append(runnerClass, other.runnerClass)
                .append(source, other.source)
                .append(documentation, other.documentation)
                .build());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(consumedMessage)
                .append(consumesFromExternal)
                .append(container)
                .append(methodName)
                .append(processNames)
                .append(producedEvents)
                .append(returnTypeCardinality)
                .append(runnerClass)
                .append(source)
                .append(documentation)
                .build();
    }
}
