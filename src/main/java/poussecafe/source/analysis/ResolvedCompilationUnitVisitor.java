package poussecafe.source.analysis;

public interface ResolvedCompilationUnitVisitor {

    default boolean visit(ResolvedCompilationUnit unit) {
        return false;
    }

    default boolean visit(ResolvedTypeDeclaration type) {
        return false;
    }

    default void endVisit(ResolvedTypeDeclaration type) {

    }

    default boolean visit(ResolvedMethod method) {
        return false;
    }

    default void forget(String sourceId) {

    }

    default boolean visit(ResolvedEnumDeclaration resolvedEnumDeclaration) {
        return false;
    }
}
