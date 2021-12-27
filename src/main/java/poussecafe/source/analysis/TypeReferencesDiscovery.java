package poussecafe.source.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import poussecafe.source.Ignore;
import poussecafe.source.model.ComponentType;
import poussecafe.source.model.TypeReference;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class TypeReferencesDiscovery {

    public List<TypeReference> references() {
        if(ValueObjectClass.isValueObject(resolvedTypeDeclaration)) {
            return methodReturnTypeReferences(resolvedTypeDeclaration);
        } else if(EntityClass.isEntity(resolvedTypeDeclaration)) {
            var attributes = resolvedTypeDeclaration.innerTypes().stream()
                    .filter(item -> item.implementsInterface(CompilationUnitResolver.ENTITY_ATTRIBUTES_INTERFACE))
                    .findFirst();
            if(attributes.isPresent()) {
                return attributesTypeReferences(attributes.orElseThrow());
            } else {
                return emptyList();
            }
        } else {
            return emptyList();
        }
    }

    private List<TypeReference> methodReturnTypeReferences(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        return resolvedTypeDeclaration.methods().stream()
                .map(this::typeReference)
                .filter(Optional::isPresent)
                .map(Optional::orElseThrow)
                .collect(toList());
    }

    private Optional<TypeReference> typeReference(ResolvedMethod method) {
        var returnType = method.returnType();
        if(method.modifiers().hasVisibility(Visibility.PUBLIC)
                && returnType.isPresent()) {
            var componentType = componentType(returnType.orElseThrow());
            if(componentType.isEmpty()) {
                return Optional.empty();
            } else {
                var referenceBuilder = new TypeReference.Builder();
                var typeClassName = typeClassName(returnType.orElseThrow());
                referenceBuilder.typeClassName(typeClassName);
                referenceBuilder.ignored(method.asAnnotatedElement().findAnnotation(Ignore.class.getCanonicalName()).isPresent());
                referenceBuilder.type(componentType.orElseThrow());
                return Optional.of(referenceBuilder.build());
            }
        } else {
            return Optional.empty();
        }
    }

    private Optional<ComponentType> componentType(ResolvedType typeName) {
        if(typeName.isSimpleType()) {
            return componentType(typeName.toTypeName());
        } else if(typeName.isParametrized()) {
            var typeParameters = typeName.typeParameters();
            if(typeParameters.size() == 1) {
                return componentType(typeParameters.get(0));
            } else if(typeParameters.size() == 2
                    && typeName.genericTypeName().instanceOf(CompilationUnitResolver.ENTITY_MAP_ATTRIBUTE_INTERFACE)) {
                return componentType(typeParameters.get(1));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    private Optional<ComponentType> componentType(ResolvedTypeName typeName) {
        if(typeName.instanceOf(CompilationUnitResolver.VALUE_OBJECT_INTERFACE)
                || typeName.instanceOf(Enum.class.getCanonicalName())) {
            return Optional.of(ComponentType.VALUE_OBJECT);
        } else if(typeName.instanceOf(CompilationUnitResolver.ENTITY_CLASS)) {
            return Optional.of(ComponentType.ENTITY);
        } else {
            return Optional.empty();
        }
    }

    private ClassName typeClassName(ResolvedType typeName) {
        if(typeName.isSimpleType()) {
            return typeName.toTypeName().qualifiedClassName();
        } else if(typeName.isParametrized()) {
            var typeParameters = typeName.typeParameters();
            if(typeParameters.size() == 1) {
                return typeParameters.get(0).toTypeName().qualifiedClassName();
            } else if(typeParameters.size() == 2
                    && typeName.genericTypeName().instanceOf(CompilationUnitResolver.ENTITY_MAP_ATTRIBUTE_INTERFACE)) {
                return typeParameters.get(1).toTypeName().qualifiedClassName();
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private List<TypeReference> attributesTypeReferences(ResolvedTypeDeclaration typeDeclaration) {
        var references = new ArrayList<TypeReference>();
        attributesIdentifier(typeDeclaration).ifPresent(references::add);
        references.addAll(methodReturnTypeReferences(typeDeclaration));
        return references;
    }

    private Optional<TypeReference> attributesIdentifier(ResolvedTypeDeclaration typeDeclaration) {
        var entityAttributesType = typeDeclaration.superInterfaceTypes().stream()
            .filter(item -> item.toTypeName().isClass(CompilationUnitResolver.ENTITY_ATTRIBUTES_INTERFACE))
            .findFirst();
        if(entityAttributesType.isPresent()) {
            var identifierType = entityAttributesType.orElseThrow().typeParameters().get(0);
            var identifierComponentType = componentType(identifierType);
            if(identifierComponentType.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(new TypeReference.Builder()
                        .typeClassName(typeClassName(identifierType))
                        .ignored(false)
                        .type(identifierComponentType.orElseThrow())
                        .build());
            }
        } else {
            return Optional.empty();
        }
    }

    public TypeReferencesDiscovery(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        this.resolvedTypeDeclaration = resolvedTypeDeclaration;
    }

    private ResolvedTypeDeclaration resolvedTypeDeclaration;
}
