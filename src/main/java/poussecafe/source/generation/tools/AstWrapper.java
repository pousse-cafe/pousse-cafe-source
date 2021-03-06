package poussecafe.source.generation.tools;

import java.util.Collection;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import poussecafe.source.analysis.ClassName;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("unchecked")
public class AstWrapper {

    public AstWrapper(AST ast) {
        requireNonNull(ast);
        this.ast = ast;
    }

    private AST ast;

    public AST ast() {
        return ast;
    }

    public SimpleType newSimpleType(Class<?> typeClass) {
        return newSimpleType(typeClass.getSimpleName());
    }

    public SimpleType newSimpleType(String name) {
        return ast.newSimpleType(ast.newSimpleName(name));
    }

    public SimpleType newSimpleType(ClassName name) {
        return ast.newSimpleType(name.toJdomName(ast));
    }

    public TypeLiteral newTypeLiteral(ClassName name) {
        var typeLiteral = ast.newTypeLiteral();
        typeLiteral.setType(newSimpleType(name.getIdentifier()));
        return typeLiteral;
    }

    public TypeDeclarationBuilder newTypeDeclarationBuilder() {
        return new TypeDeclarationBuilder(ast);
    }

    public MethodDeclaration newPublicConstructor(ClassName typeName) {
        var constructor = ast.newMethodDeclaration();
        constructor.setConstructor(true);
        constructor.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));

        String simpleTypeName = typeName.getIdentifier().toString();
        constructor.setName(ast.newSimpleName(simpleTypeName));

        return constructor;
    }

    public SingleVariableDeclaration newSimpleMethodParameter(String simpleTypeName, String parameterName) {
        var constructorParameter = ast.newSingleVariableDeclaration();
        constructorParameter.setType(ast.newSimpleType(ast.newSimpleName(simpleTypeName)));
        constructorParameter.setName(ast.newSimpleName(parameterName));
        return constructorParameter;
    }

    public SimpleName newVariableAccess(String variableName) {
        return ast.newSimpleName(variableName);
    }

    public ParameterizedType newParameterizedType(Class<?> typeClass) {
        return ast.newParameterizedType(newSimpleType(typeClass));
    }

    public ParameterizedType newParameterizedType(String typeClass) {
        return ast.newParameterizedType(newSimpleType(typeClass));
    }

    public ParameterizedType newParameterizedType(ClassName typeName) {
        return ast.newParameterizedType(newSimpleType(typeName));
    }

    public TypeParameter newExtendingTypeParameter(String name, ClassName supertype) {
        var parameter = ast.newTypeParameter();
        parameter.setName(ast.newSimpleName(name));
        parameter.typeBounds().add(newSimpleType(supertype));
        return parameter;
    }

    public MarkerAnnotation newOverrideAnnotation() {
        var annotation = ast.newMarkerAnnotation();
        annotation.setTypeName(ast.newSimpleName(Override.class.getSimpleName()));
        return annotation;
    }

    public SingleMemberAnnotation newSuppressWarningsAnnotation(String... value) {
        var annotation = ast.newSingleMemberAnnotation();
        annotation.setTypeName(ast.newSimpleName(SuppressWarnings.class.getSimpleName()));
        if(value.length == 1) {
            var singleString = ast.newStringLiteral();
            singleString.setLiteralValue(value[0]);
            annotation.setValue(singleString);
        } else if(value.length > 1) {
            var stringArray = newStringArrayInitializer(asList(value));
            annotation.setValue(stringArray);
        }
        return annotation;
    }

    public ArrayInitializer newStringArrayInitializer(Collection<String> values) {
        var stringArray = ast.newArrayInitializer();
        for(String string : values) {
            var singleString = ast.newStringLiteral();
            singleString.setLiteralValue(string);
            stringArray.expressions().add(singleString);
        }
        return stringArray;
    }

    public SimpleName newSimpleName(ClassName name) {
        return ast.newSimpleName(name.getIdentifier().toString());
    }

    public StringLiteral newStringLiteral(String string) {
        var literal = ast.newStringLiteral();
        literal.setLiteralValue(string);
        return literal;
    }

    public ReturnStatement newReturnNullStatement() {
        var statement = ast.newReturnStatement();
        statement.setExpression(ast.newNullLiteral());
        return statement;
    }
}
