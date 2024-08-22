package io.github.dmitrybdev.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;

public class BoundaryCheck extends AbstractCheck {

    private int minLength = 0;
    private Boundary after = new Boundary(List.of());
    private Boundary before = new Boundary(List.of());
    private int minNewLines = 0;
    private boolean commentsAsNewLines = true;


    public void setMinLength(int minLength) { this.minLength = minLength; }
    public void setAfter(String after) { this.after = parseBoundary(after); }
    public void setBefore(String before) { this.before = parseBoundary(before); }
    public void setMinNewLines(int minNewLines) { this.minNewLines = minNewLines; }
    public void setCommentsAsNewLines(boolean commentsAsNewLines) { this.commentsAsNewLines = commentsAsNewLines; }

    private Boundary parseBoundary(String modifiers) {
        List<ExpectedModifiers> boundary = new ArrayList<>();
        for (String expectedModifiers : modifiers.split(",")) {
            if (expectedModifiers.isBlank()) continue;

            Set<String> expectedModifiersSet = stream(expectedModifiers.split(" "))
                    .filter(not(String::isBlank))
                    .map(String::trim)
                    .collect(toSet());
            boundary.add(new ExpectedModifiers(expectedModifiersSet));
        }
        return new Boundary(boundary);
    }


    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] { CLASS_DEF, INTERFACE_DEF, RECORD_DEF, ANNOTATION_DEF, VARIABLE_DEF, ANNOTATION_FIELD_DEF,
                CTOR_DEF, METHOD_DEF };
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[0];
    }


    @Override
    public void visitToken(DetailAST ast) {
        if (ast.getParent().getType() != OBJBLOCK) return;
        if (ast.getPreviousSibling() == null) return;

        if (!after.matches(getModifiers(ast.getPreviousSibling()))) return;
        if (!before.matches(getModifiers(ast))) return;

        if (getLength(ast) + getLength(ast.getPreviousSibling()) < minLength) return;

        int interval = getInterval(ast);
        if (interval < minNewLines) {
            log(ast.getLineNo(), ast.getColumnNo(),
                    "Current interval ({0} lines) is less than required: {1}", interval, minNewLines);
        };
    }


    private int getLength(DetailAST ast) {
        if (ast.getType() == LCURLY) return 1;
        if (ast.getType() == METHOD_DEF && ast.getLastChild().getType() == SLIST) {
            return ast.getLastChild().getLastChild().getLineNo() - ast.getLineNo() + 1;
        }
        return ast.getLastChild().getLineNo() - ast.getLineNo() + 1;
    }


    private int getInterval(DetailAST ast) {
        if (ast.getLineNo() <= 1) return Integer.MAX_VALUE;

        int spaces = 0;
        for (int i = 0; i < ast.getLineNo(); i++) {
            String line = getLine(ast.getLineNo() - i - 2).trim();

            if (line.startsWith("//") || line.startsWith("/*") || line.startsWith("*")) {
                if (commentsAsNewLines) spaces++;
                continue;
            }
            if (line.isBlank()) {
                spaces++;
                continue;
            }
            break;
        }

        return spaces;
    }


    private Set<String> getModifiers(DetailAST ast) {
        if (ast.findFirstToken(MODIFIERS) == null) return Set.of();
        DetailAST modifiers = ast.findFirstToken(MODIFIERS).getFirstChild();

        Set<String> modifiersSet = Stream.iterate(modifiers, Objects::nonNull, DetailAST::getNextSibling)
                .map(modifier -> modifier.getType() == ANNOTATION
                        ? "@" + FullIdent.createFullIdent(modifier.getFirstChild().getNextSibling()).getText()
                        : modifier.getText()
                )
                .collect(toSet());

        if (ast.getType() == VARIABLE_DEF) {
            modifiersSet.add("field");
        }
        if (ast.getType() == CTOR_DEF) {
            modifiersSet.add("constructor");
        }

        if (isGetter(ast)) {
            modifiersSet.add("getter");
        } else if (isSetter(ast)) {
            modifiersSet.add("setter");
        } else if (ast.getType() == METHOD_DEF) {
            modifiersSet.add("method");
        }

        if (List.of(CLASS_DEF, INTERFACE_DEF, RECORD_DEF, ANNOTATION_DEF).contains(ast.getType())) {
            modifiersSet.add("class");
        }

        return modifiersSet;
    }


    private boolean isGetter(DetailAST ast) {
        if (ast.getType() != METHOD_DEF) return false;
        String name = ast.findFirstToken(IDENT).getText();

        if (name.length() < 4) return false;
        if (!name.startsWith("get") && !name.startsWith("is")) return false;
        if (!Character.isUpperCase(name.charAt(2)) && !Character.isUpperCase(name.charAt(3))) return false;

        DetailAST params = ast.findFirstToken(TokenTypes.PARAMETERS);
        if (params == null || params.getChildCount() > 0) return false;

        DetailAST returnType = ast.findFirstToken(TokenTypes.TYPE);
        DetailAST typeIdent = returnType.getFirstChild();
        return typeIdent.getType() != TokenTypes.LITERAL_VOID;
    }

    private boolean isSetter(DetailAST ast) {
        if (ast.getType() != METHOD_DEF) return false;
        String name = ast.findFirstToken(IDENT).getText();

        if (name.length() < 4) return false;
        if (!name.startsWith("set")) return false;
        if (!Character.isUpperCase(name.charAt(3))) return false;

        DetailAST params = ast.findFirstToken(TokenTypes.PARAMETERS);
        return params != null && params.getChildCount() == 1;
    }


    private record Boundary(List<ExpectedModifiers> modifiers) {
        boolean matches(Set<String> actualModifiers) {
            if (modifiers.isEmpty()) return true;
            return modifiers.stream().anyMatch(expectedModifiers -> expectedModifiers.matches(actualModifiers));
        }
    }

    private record ExpectedModifiers(Set<String> modifiers) {
        boolean matches(Set<String> actualModifiers) {
            return actualModifiers.containsAll(modifiers);
        }
    }
}
