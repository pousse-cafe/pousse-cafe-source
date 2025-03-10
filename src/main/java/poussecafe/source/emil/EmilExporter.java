package poussecafe.source.emil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import poussecafe.source.generation.NamingConventions;
import poussecafe.source.model.Cardinality;
import poussecafe.source.model.Message;
import poussecafe.source.model.MessageListener;
import poussecafe.source.model.MessageType;
import poussecafe.source.model.ProducedEvent;
import poussecafe.source.model.SourceModel;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class EmilExporter {

    public String toEmil() {
        if(emil == null) {
            generateEmil();
        }
        return emil;
    }

    private String emil;

    private void generateEmil() {
        builder = new FormattedEmilTokenStreamBuilder();
        builder.appendHeader(processName);
        while(!targetListeners.isEmpty()) {
            MessageListener next = next();
            appendTopListener(next);

            targetListeners.remove(next);
            if(!targetListeners.isEmpty()) {
                builder.appendNewLine();
            }
        }
        emil = builder.toString();
    }

    private FormattedEmilTokenStreamBuilder builder;

    private List<MessageListener> targetListeners;

    private MessageListener next() {
        Optional<MessageListener> topListener = findNextTopListener();
        if(topListener.isEmpty()) {
            return targetListeners.get(0);
        } else {
            return topListener.get();
        }
    }

    private Optional<MessageListener> findNextTopListener() {
        return targetListeners.stream()
                .filter(listener -> !processProducedEvents.contains(listener.consumedMessage()))
                .findFirst();
    }

    private Set<Message> processProducedEvents = new HashSet<>();

    private void appendTopListener(MessageListener rootListener) {
        builder.resetIndent();
        if(rootListener.consumesFromExternal().isEmpty()) {
            appendMessageConsumption(Optional.empty(), rootListener.consumedMessage(), true, Optional.empty());
        } else {
            rootListener.consumesFromExternal().forEach(consumesFromExternal ->
                appendMessageConsumption(Optional.of(consumesFromExternal), rootListener.consumedMessage(), true, Optional.empty()));
        }
    }

    private void appendMessageConsumption(Optional<String> consumesFromExternal, Message message, boolean required, Optional<String> noteIfNoListeners) {
        appendMessage(consumesFromExternal, message, required);

        var consumers = findAndRemoveConsumers(message);
        if(consumers.isEmpty()) {
            appendNoListener(noteIfNoListeners);
        } else if(consumers.size() == 1) {
            appendSingleListener(consumers);
        } else {
            appendMultipleListeners(consumers);
        }
    }

    private void appendMessage(Optional<String> consumesFromExternal, Message message, boolean required) {
        if(message.type() == MessageType.COMMAND) {
            builder.appendCommandIdentifier(message.name());
        } else if(message.type() == MessageType.DOMAIN_EVENT) {
            if(consumesFromExternal.isPresent()) {
                builder.appendOpeningNote(consumesFromExternal.get());
            }
            builder.appendDomainEventIdentifier(message.name());
        }
        if(!required) {
            builder.appendOptionalOperator();
        }
    }

    private Consumers findAndRemoveConsumers(Message event) {
        var consumers = new Consumers();

        Iterator<MessageListener> iterator = targetListeners.iterator();
        while(iterator.hasNext()) {
            MessageListener listener = iterator.next();
            if(listener.consumedMessage().equals(event)) {
                consumers.listeners.add(listener);
                iterator.remove();
            }
        }

        if(processName.isPresent()) {
            var uniqueOrderedProcessNames = new TreeSet<String>();
            model.messageListeners().stream()
                    .filter(listener -> !listener.processNames().contains(processName.get()))
                    .filter(listener -> listener.consumedMessage().equals(event))
                    .forEach(listener -> uniqueOrderedProcessNames.addAll(listener.processNames()));
            consumers.otherProcesses.addAll(uniqueOrderedProcessNames);
        }

        return consumers;
    }

    class Consumers {

        List<MessageListener> listeners = new ArrayList<>();

        List<String> otherProcesses = new ArrayList<>();

        public boolean isEmpty() {
            return listeners.isEmpty()
                    && otherProcesses.isEmpty();
        }

        public int size() {
            return listeners.size() + otherProcesses.size();
        }
    }

    private Optional<String> processName;

    private void appendNoListener(Optional<String> noteIfNoListeners) {
        builder.appendEndOfConsumption();
        if(noteIfNoListeners.isPresent()) {
            builder.appendClosingNote(noteIfNoListeners.get());
        }
        builder.appendNewLine();
    }

    private void appendSingleListener(Consumers consumers) {
        builder.appendConsumptionToken();
        if(consumers.listeners.size() == 1) {
            appendListener(consumers.listeners.get(0));
        } else {
            appendProcess(consumers.otherProcesses.get(0));
        }
    }

    private void appendListener(MessageListener listener) {
        if(listener.container().type().isFactory()) {
            appendFactoryListener(listener);
        } else if(listener.container().type().isRepository()) {
            appendRepositoryListener(listener);
        } else if(listener.container().type().isRoot()) {
            appendAggregateRootListener(listener);
        } else {
            builder.appendEndOfConsumption();
            builder.appendClosingNote("external");
        }
    }

    private void appendMultipleListeners(Consumers consumers) {
        builder.appendOpeningConsumptionToken();
        builder.appendNewLine();
        builder.incrementIndent();
        for(MessageListener listener : consumers.listeners) {
            builder.indent();
            builder.appendClosingConsumptionToken();
            appendListener(listener);
        }
        for(String process : consumers.otherProcesses) {
            builder.indent();
            builder.appendClosingConsumptionToken();
            appendProcess(process);
        }
        builder.decrementIndent();
    }

    private void appendFactoryListener(MessageListener listener) {
        appendFactoryName(listener);
        var listenerProducedEvents = listener.producedEvents();
        if(!listenerProducedEvents.isEmpty()) {
            appendAggregateMessageConsumptions(listenerProducedEvents);
        } else {
            builder.appendNewLine();
        }
    }

    private void appendFactoryName(MessageListener listener) {
        var factoryIdentifier = listener.container().containerIdentifier();
        builder.appendFactoryIdentifier(factoryIdentifier);
        builder.appendInlineNote(listener.methodName());
        appendCardinality(listener);
    }

    private void appendCardinality(MessageListener listener) {
        var cardinality = listener.returnTypeCardinality();
        if(cardinality.isPresent()) {
            if(cardinality.get() == Cardinality.OPTIONAL) {
                builder.appendOptionalOperator();
            } else if(cardinality.get() == Cardinality.SEVERAL) {
                builder.appendSeveralOperator();
            }
        }
    }

    private SourceModel model;

    private Optional<String> consumedByExternalNote(ProducedEvent producedEvent) {
        if(producedEvent.consumedByExternal().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(producedEvent.consumedByExternal().stream().collect(joining(", ")));
        }
    }

    private void appendRepositoryListener(MessageListener listener) {
        appendRepositoryName(listener);
        var listenerProducedEvents = listener.producedEvents();
        if(!listenerProducedEvents.isEmpty()) {
            appendAggregateMessageConsumptions(listenerProducedEvents);
        } else {
            builder.appendNewLine();
        }
    }

    private void appendRepositoryName(MessageListener listener) {
        var repositoryIdentifier = listener.container().containerIdentifier();
        builder.appendRepositoryIdentifier(repositoryIdentifier);
        builder.appendInlineNote(listener.methodName());
        appendCardinality(listener);
    }

    private void appendAggregateRootListener(MessageListener listener) {
        appendRunnerAndAggregateRoot(listener);
        var listenerProducedEvents = aggregateRootListenerProducedEvents(listener);
        if(!listenerProducedEvents.isEmpty()) {
            appendAggregateMessageConsumptions(listenerProducedEvents);
        } else {
            builder.appendNewLine();
        }
        builder.decrementIndent();
    }

    private List<ProducedEvent> aggregateRootListenerProducedEvents(MessageListener listener) {
        var listenerProducedEvents = new ArrayList<>(listener.producedEvents());
        listenerProducedEvents.sort((e1, e2) -> e1.message().name().compareTo(e2.message().name()));
        return listenerProducedEvents;
    }

    private void appendRunnerAndAggregateRoot(MessageListener listener) {
        builder.appendRunnerIdentifier(listener.runnerName().orElseThrow());
        builder.appendNewLine();
        builder.incrementIndent();
        builder.indent();
        var aggregateRootName = listener.container().containerIdentifier();
        builder.appendAggregateRootIdentifier(aggregateRootName);
        builder.appendInlineNote(listener.methodName());
    }

    private void appendAggregateMessageConsumptions(List<ProducedEvent> events) {
        builder.appendOpenRelation();
        builder.appendNewLine();
        builder.incrementIndent();
        for(ProducedEvent producedEvent : events) {
            builder.indent();
            builder.appendCloseRelation();
            Optional<String> note = consumedByExternalNote(producedEvent);
            appendMessageConsumption(Optional.empty(), producedEvent.message(), producedEvent.required(), note);
        }
        builder.indent();
        builder.appendEndOfRelations();
        builder.appendNewLine();
        builder.decrementIndent();
    }

    private void appendProcess(String processName) {
        builder.appendProcessIdentifier(processName);
        builder.appendNewLine();
    }

    public static class Builder {

        private EmilExporter exporter = new EmilExporter();

        public EmilExporter build() {
            requireNonNull(exporter.model);
            requireNonNull(exporter.processName);

            if(exporter.processName.isPresent()) {
                exporter.targetListeners = new LinkedList<>(exporter.model.processListeners(
                        exporter.processName.orElseThrow()));
            } else {
                exporter.targetListeners = new LinkedList<>(exporter.model.messageListeners());
            }
            exporter.targetListeners.sort(new EmilListenersComparator());

            exporter.processProducedEvents = new HashSet<>();
            exporter.targetListeners.stream().forEach(listener -> addToProducedEvents(listener.producedEvents()));

            return exporter;
        }

        private void addToProducedEvents(Collection<ProducedEvent> producedEvents) {
            var messages = producedEvents.stream().map(ProducedEvent::message).collect(toList());
            exporter.processProducedEvents.addAll(messages);
        }

        public Builder model(SourceModel model) {
            exporter.model = model;
            return this;
        }

        public Builder processName(Optional<String> processName) {
            exporter.processName = processName;
            return this;
        }
    }

    private EmilExporter() {

    }
}
