package poussecafe.source.generation.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import poussecafe.source.analysis.ClassName;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class TypeDeclarationEditor {

    public TypeDeclarationEditor setName(String name) {
        rewrite.set(TypeDeclaration.NAME_PROPERTY, rewrite.ast().newSimpleName(name));
        return this;
    }

    public TypeDeclarationEditor setName(ClassName name) {
        setName(name.getIdentifier().toString());
        return this;
    }

    public TypeDeclarationEditor setSuperclass(Type superclassType) {
        rewrite.set(TypeDeclaration.SUPERCLASS_TYPE_PROPERTY, superclassType);
        return this;
    }

    public InnerTypeDeclarationEditor declaredType(String name) {
        return declaredType(name, DefaultInsertionMode.LAST);
    }

    public InnerTypeDeclarationEditor declaredType(String name, DefaultInsertionMode insertionMode) {
        var existingTypeDeclaration = findTypeDeclarationByName(name);
        if(existingTypeDeclaration.isPresent()) {
            return editExistingType(existingTypeDeclaration.get());
        } else {
            var newTypeDeclaration = rewrite.ast().newTypeDeclaration();
            if(insertionMode == DefaultInsertionMode.LAST) {
                rewrite.listRewrite(TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(newTypeDeclaration, null);
            } else if(insertionMode == DefaultInsertionMode.FIRST) {
                rewrite.listRewrite(TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(newTypeDeclaration, null);
            } else {
                throw new UnsupportedOperationException();
            }
            return editNewType(name, newTypeDeclaration);
        }
    }

    public Optional<AbstractTypeDeclaration> findTypeDeclarationByName(String name) {
        for(Object declarationObject : typeDeclaration.bodyDeclarations()) {
            if(declarationObject instanceof TypeDeclaration) {
                TypeDeclaration declaration = (TypeDeclaration) declarationObject;
                if(declaration.getName().getIdentifier().equals(name)) {
                    return Optional.of(declaration);
                }
            }
        }
        return Optional.empty();
    }

    public InnerTypeDeclarationEditor editExistingType(AbstractTypeDeclaration existingType) {
        var nodeRewriter = new NodeRewrite(rewrite.rewrite(), existingType);
        return new InnerTypeDeclarationEditor(nodeRewriter, this, false);
    }

    private InnerTypeDeclarationEditor editNewType(String typeName, AbstractTypeDeclaration newTypeDeclaration) {
        newTypeDeclaration.setName(rewrite.ast().newSimpleName(typeName));
        var nodeRewriter = new NodeRewrite(rewrite.rewrite(), newTypeDeclaration);
        return new InnerTypeDeclarationEditor(nodeRewriter, this, true);
    }

    public InnerTypeDeclarationEditor newTypeDeclarationBefore(String typeName, AbstractTypeDeclaration nextType) {
        var newTypeDeclaration = rewrite.ast().newTypeDeclaration();
        rewrite.listRewrite(TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertBefore(newTypeDeclaration, nextType, null);
        return editNewType(typeName, newTypeDeclaration);
    }

    public InnerTypeDeclarationEditor newTypeDeclarationAfter(String typeName, AbstractTypeDeclaration nextType) {
        var newTypeDeclaration = rewrite.ast().newTypeDeclaration();
        rewrite.listRewrite(TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertAfter(newTypeDeclaration, nextType, null);
        return editNewType(typeName, newTypeDeclaration);
    }

    public InnerTypeDeclarationEditor newTypeDeclarationLast(String typeName) {
        var newTypeDeclaration = rewrite.ast().newTypeDeclaration();
        rewrite.listRewrite(TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(newTypeDeclaration, null);
        return editNewType(typeName, newTypeDeclaration);
    }

    public TypeDeclarationEditor setInterface(boolean isInterface) {
        rewrite.set(TypeDeclaration.INTERFACE_PROPERTY, isInterface);
        return this;
    }

    public TypeDeclarationEditor addSuperinterface(Type superinterfaceType) {
        if(!alreadyImplements(superinterfaceType)) {
            rewrite.listRewrite(TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY).insertLast(superinterfaceType, null);
        }
        return this;
    }

    private boolean alreadyImplements(Type superinterfaceType) {
        return containsNode(typeDeclaration.superInterfaceTypes(), superinterfaceType);
    }

    public TypeDeclarationEditor addSuperinterfaceFirst(SimpleType superinterfaceType) {
        if(!alreadyImplements(superinterfaceType)) {
            rewrite.listRewrite(TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY).insertFirst(superinterfaceType, null);
        }
        return this;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean containsNode(List list, Object node) {
        return list.stream().anyMatch(listNode -> listNode.toString().equals(node.toString()));
    }

    private TypeDeclaration typeDeclaration;

    public TypeDeclarationEditor setTypeParameter(int index, TypeParameter parameter) {
        var listRewrite = rewrite.listRewrite(TypeDeclaration.TYPE_PARAMETERS_PROPERTY);
        while(listRewrite.getRewrittenList().size() <= index) {
            var typeParameter = rewrite.ast().newTypeParameter();
            listRewrite.insertLast(typeParameter, null);
        }
        listRewrite.replace((ASTNode) listRewrite.getRewrittenList().get(index), parameter, null);
        return this;
    }

    public TypeDeclarationEditor addField(FieldDeclaration field) {
        rewrite.listRewrite(TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(field, null);
        return this;
    }

    public ModifiersEditor modifiers() {
        return new ModifiersEditor(rewrite, TypeDeclaration.MODIFIERS2_PROPERTY);
    }

    public List<MethodDeclarationEditor> method(String methodName) {
        var methods = findMethods(methodName);
        var newMethod = createMethodIfAbsent(methodName, methods);
        return methods.stream()
                .map(method -> editMethod(method, method == newMethod))
                .collect(toList());
    }

    public MethodDeclarationEditor editMethod(MethodDeclaration methodDeclaration, boolean newMethod) {
        return new MethodDeclarationEditor(new NodeRewrite(rewrite.rewrite(), methodDeclaration), newMethod);
    }

    private MethodDeclaration createMethodIfAbsent(String methodName, List<MethodDeclaration> methods) {
        MethodDeclaration newMethod = null;
        if(methods.isEmpty()) {
            newMethod = rewrite.ast().newMethodDeclaration();
            newMethod.setName(rewrite.ast().newSimpleName(methodName));
            rewrite.listRewrite(TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(newMethod, null);
            methods.add(newMethod);
        }
        return newMethod;
    }

    public MethodDeclarationEditor insertNewMethodBefore(ASTNode referenceNode) {
        MethodDeclaration newMethod = rewrite.ast().newMethodDeclaration();
        rewrite.listRewrite(TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertBefore(newMethod, referenceNode, null);
        return editMethod(newMethod, true);
    }

    public MethodDeclarationEditor insertNewMethodAfter(ASTNode referenceNode) {
        MethodDeclaration newMethod = rewrite.ast().newMethodDeclaration();
        rewrite.listRewrite(TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertAfter(newMethod, referenceNode, null);
        return editMethod(newMethod, true);
    }

    public MethodDeclarationEditor insertNewMethodFirst() {
        MethodDeclaration newMethod = rewrite.ast().newMethodDeclaration();
        rewrite.listRewrite(TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(newMethod, null);
        return editMethod(newMethod, true);
    }

    public MethodDeclarationEditor insertNewMethodLast() {
        MethodDeclaration newMethod = rewrite.ast().newMethodDeclaration();
        rewrite.listRewrite(TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(newMethod, null);
        return editMethod(newMethod, true);
    }

    public List<MethodDeclaration> findMethods(String methodName) {
        return methods().stream()
                .filter(method -> method.getName().getIdentifier().equals(methodName))
                .collect(toList());
    }

    public List<MethodDeclaration> methods() {
        var rewrittenMethods = new ArrayList<MethodDeclaration>();
        var objectsList = rewrite.listRewrite(TypeDeclaration.BODY_DECLARATIONS_PROPERTY).getRewrittenList();
        for(Object declaration : objectsList) {
            if(declaration instanceof MethodDeclaration) {
                MethodDeclaration methodDeclaration = (MethodDeclaration) declaration;
                rewrittenMethods.add(methodDeclaration);
            }
        }
        return rewrittenMethods;
    }

    public List<FieldDeclarationEditor> field(String fieldName) {
        var fields = findFields(fieldName);
        if(fields.isEmpty()) {
            var fragment = rewrite.ast().newVariableDeclarationFragment();
            fragment.setName(rewrite.ast().newSimpleName(fieldName));

            var method = rewrite.ast().newFieldDeclaration(fragment);
            rewrite.listRewrite(TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(method, null);
            fields.add(method);
        }

        return fields.stream()
                .map(method -> new FieldDeclarationEditor(new NodeRewrite(rewrite.rewrite(), method)))
                .collect(toList());
    }

    private List<FieldDeclaration> findFields(String fieldName) {
        var fields = new ArrayList<FieldDeclaration>();
        for(Object declaration : typeDeclaration.bodyDeclarations()) {
            if(declaration instanceof FieldDeclaration) {
                FieldDeclaration methodDeclaration = (FieldDeclaration) declaration;
                if(isSingleField(methodDeclaration, fieldName)) {
                    fields.add(methodDeclaration);
                }
            }
        }
        return fields;
    }

    private boolean isSingleField(FieldDeclaration fieldDeclaration, String methodName) {
        if(fieldDeclaration.fragments().size() == 1) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
            return fragment.getName().getIdentifier().equals(methodName);
        } else {
            return false;
        }
    }

    public boolean hasField(String fieldName) {
        return !findFields(fieldName).isEmpty();
    }

    public List<MethodDeclarationEditor> constructors(String typeName) {
        var methods = findConstructors();
        var newConstructor = createConstructorIfAbsent(typeName, methods);
        return methods.stream()
                .map(method -> new MethodDeclarationEditor(new NodeRewrite(rewrite.rewrite(), method), method == newConstructor))
                .collect(toList());
    }

    private MethodDeclaration createConstructorIfAbsent(String typeName, List<MethodDeclaration> methods) {
        MethodDeclaration newConstructor = null;
        if(methods.isEmpty()) {
            newConstructor = rewrite.ast().newMethodDeclaration();
            newConstructor.setConstructor(true);
            newConstructor.setName(rewrite.ast().newSimpleName(typeName));
            rewrite.listRewrite(TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(newConstructor, null);
            methods.add(newConstructor);
        }
        return newConstructor;
    }

    private List<MethodDeclaration> findConstructors() {
        var methods = new ArrayList<MethodDeclaration>();
        for(Object declaration : typeDeclaration.bodyDeclarations()) {
            if(declaration instanceof MethodDeclaration) {
                MethodDeclaration methodDeclaration = (MethodDeclaration) declaration;
                if(methodDeclaration.isConstructor()) {
                    methods.add(methodDeclaration);
                }
            }
        }
        return methods;
    }

    public TypeDeclarationEditor(NodeRewrite rewrite, boolean newType) {
        requireNonNull(rewrite);
        this.rewrite = rewrite;

        typeDeclaration = (TypeDeclaration) rewrite.node();

        this.newType = newType;
    }

    private NodeRewrite rewrite;

    public boolean isNewType() {
        return newType;
    }

    private boolean newType;

    public ASTRewrite rewrite() {
        return rewrite.rewrite();
    }
}
