package io.github.dmitrybdev.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public class AbstractModifiersCheck extends AbstractCheck {

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] {
                CLASS_DEF, INTERFACE_DEF, ENUM_DEF, RECORD_DEF, ANNOTATION_DEF,
                VARIABLE_DEF, ANNOTATION_FIELD_DEF, PARAMETER_DEF, RECORD_COMPONENT_DEF, ENUM_CONSTANT_DEF,
                CTOR_DEF, METHOD_DEF
        };
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[0];
    }


    protected Order parseTemplate(String template) {
        int groupOrder = 0, modifierOrder = 0;
        List<GroupOrder> groupOrders = new ArrayList<>();
        for (String line : template.split("\\s{3,}|,")) {
            if (line.isBlank()) continue;

            List<ModifierOrder> modifierOrders = new ArrayList<>();
            for (String modifier : line.split("\\s+")) {
                if (modifier.isBlank()) continue;

                boolean hasArgs = modifier.contains("(") && modifier.contains(")");
                modifier = substringBefore(modifier, "(");
                modifierOrders.add(new ModifierOrder(modifier.trim(), hasArgs, groupOrder, modifierOrder++));
            }
            groupOrders.add(new GroupOrder(modifierOrders, groupOrder));
            groupOrder++;
        }

        return new Order(groupOrders);
    }


    @Override
    public void visitToken(DetailAST ast) {
        List<Modifier> modifiers = getModifiers(ast);
        if (modifiers == null) return;

        log(
                ast.getLineNo(), ast.getColumnNo(),
                modifiers.stream()
                        .map(modifier -> "%d:%d %s".formatted(modifier.lineNo(), modifier.colNo(), modifier.toString()))
                        .collect(joining(", "))
        );
    }


    protected @Nullable List<Modifier> getModifiers(DetailAST ast) {
        if (ast.getParent().getType() == SLIST) return null;

        return Stream.of(
                Stream.iterate(getModifiersAst(ast), Objects::nonNull, DetailAST::getNextSibling),
                Stream.ofNullable(ast.findFirstToken(TYPE)),
                Stream.of(ast),
                Stream.ofNullable(ast.findFirstToken(IDENT))
        )
                .flatMap(identity())
                .map(this::getModifier)
                .filter(Objects::nonNull)
                .toList();
    }

    private DetailAST getModifiersAst(DetailAST ast) {
        if (ast.findFirstToken(MODIFIERS) != null) return ast.findFirstToken(MODIFIERS).getFirstChild();
        if (ast.findFirstToken(ANNOTATIONS) != null) return ast.findFirstToken(ANNOTATIONS).getFirstChild();
        return null;
    }


    private @Nullable Modifier getModifier(@Nullable DetailAST ast) {
        if (ast == null) return null;
        if (ast.getType() == ANNOTATION) return getAnnotation(ast);
        if (ast.getType() == TYPE) return new Modifier("type", ast);
        if (ast.getType() == METHOD_DEF) return new Modifier("method", ast);
        if (ast.getType() == IDENT && isGetter(ast)) return new Modifier("getter", ast);
        if (ast.getType() == IDENT && isSetter(ast)) return new Modifier("setter", ast);
        if (ast.getType() == CTOR_DEF) return new Modifier("constructor", ast);
        if (ast.getType() == CLASS_DEF) return new Modifier("class", ast);
        if (ast.getType() == INTERFACE_DEF) return new Modifier("interface", ast);
        if (ast.getType() == ENUM_DEF) return new Modifier("enum", ast);
        if (ast.getType() == RECORD_DEF) return new Modifier("record", ast);
        if (ast.getType() == ANNOTATION_DEF) return new Modifier("@interface", ast);
        if (ast.getType() == VARIABLE_DEF) return new Modifier("field", ast);
        if (ast.getType() == PARAMETER_DEF) return new Modifier("param", ast);
        if (ast.getType() == RECORD_COMPONENT_DEF) return new Modifier("param", ast);
        if (ast.getParent().getType() == MODIFIERS) return new Modifier(ast.getText(), ast);
        return null;
    }

    private Modifier getAnnotation(DetailAST ast) {
        DetailAST args = ast.getFirstChild().getNextSibling().getNextSibling();
        return new Modifier(
                "@" + FullIdent.createFullIdent(ast.getFirstChild().getNextSibling()).getText(),
                args != null,
                ast.getLineNo(), ast.getColumnNo()
        );
    }


    private boolean isGetter(DetailAST ident) {
        if (ident.getParent().getType() != METHOD_DEF) return false;
        String name = ident.getText();

        if (name.length() < 4) return false;
        if (!name.startsWith("get") && !name.startsWith("is")) return false;
        if (!Character.isUpperCase(name.charAt(2)) && !Character.isUpperCase(name.charAt(3))) return false;

        DetailAST params = ident.getParent().findFirstToken(PARAMETERS);
        if (params == null || params.getChildCount() > 0) return false;

        DetailAST returnType = ident.getParent().findFirstToken(TYPE).getFirstChild();
        return returnType.getType() != LITERAL_VOID;
    }

    private boolean isSetter(DetailAST ident) {
        if (ident.getParent().getType() != METHOD_DEF) return false;
        String name = ident.getText();

        if (name.length() < 4) return false;
        if (!name.startsWith("set")) return false;
        if (!Character.isUpperCase(name.charAt(3))) return false;

        DetailAST params = ident.getParent().findFirstToken(PARAMETERS);
        return params != null && params.getChildCount() == 1;
    }


    public record Modifier(String text, boolean hasArgs, int lineNo, int colNo) {
        public Modifier(String text, DetailAST ast) {
            this(text, false, ast.getLineNo(), ast.getColumnNo());
        }

        @Override
        public String toString() {
            return text + (hasArgs ? "()" : "");
        }
    }


    public record Order(List<GroupOrder> groups) {
        public @Nullable GroupOrder getOrder(List<Modifier> modifiers) {
            return groups.stream()
                    .filter(group -> group.matches(modifiers))
                    .max(comparing(group -> group.modifiers().size()))
                    .orElse(null);
        }

        public @Nullable ModifierOrder getOrder(Modifier modifier) {
            if (!modifier.hasArgs()) return getOrder(modifier, true).orElse(null);
            return getOrder(modifier, false)
                    .or(() -> getOrder(modifier, true))
                    .orElse(null);
        }

        private Optional<ModifierOrder> getOrder(Modifier modifier, boolean ignoreArgs) {
            return groups.stream()
                    .flatMap(group -> group.getOrder(modifier, ignoreArgs).stream())
                    .findFirst();
        }

        public boolean matches(@Nullable List<Modifier> modifiers) {
            if (modifiers == null) return false;
            if (groups.isEmpty()) return true;
            return groups.stream().anyMatch(group -> group.matches(modifiers));
        }
    }

    public record GroupOrder(List<ModifierOrder> modifiers, int order) {
        public Optional<ModifierOrder> getOrder(Modifier modifier, boolean ignoreArgs) {
            return modifiers.stream()
                    .filter(modifierOrder -> modifierOrder.matches(modifier, ignoreArgs))
                    .findFirst();
        }

        public boolean matches(List<Modifier> modifiers) {
            return this.modifiers.stream()
                    .allMatch(modifierOrder -> modifierOrder.matches(modifiers));
        }

        @Override
        public String toString() {
            return modifiers.stream()
                    .map(ModifierOrder::toString)
                    .collect(joining(" "));
        }
    }

    public record ModifierOrder(String modifier, boolean hasArgs, int groupOrder, int order) {
        public boolean matches(Modifier modifier, boolean ignoreArgs) {
            if (ignoreArgs && !hasArgs) return this.modifier.equals(modifier.text());
            return this.modifier.equals(modifier.text()) && hasArgs == modifier.hasArgs();
        }

        public boolean matches(List<Modifier> modifiers) {
            return modifiers.stream().anyMatch(modifier -> matches(modifier, true));
        }

        @Override
        public String toString() {
            return modifier + (hasArgs ? "()" : "");
        }
    }
}
