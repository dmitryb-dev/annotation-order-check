package io.github.dmitrybdev.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import java.util.List;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

public class AnnotationOrderCheck extends AbstractModifiersCheck {

    private Order typeTemplate = new Order(List.of());
    private Order fieldTemplate = new Order(List.of());
    private Order methodTemplate = new Order(List.of());


    public void setTypeTemplate(String typeTemplate) { this.typeTemplate = parseTemplate(typeTemplate); }
    public void setFieldTemplate(String fieldTemplate) { this.fieldTemplate = parseTemplate(fieldTemplate); }
    public void setMethodTemplate(String methodTemplate) { this.methodTemplate = parseTemplate(methodTemplate); }


    @Override
    public void visitToken(DetailAST ast) {
        List<Modifier> modifiers = getModifiers(ast);
        if (modifiers == null) return;

        ModifierOrder lastFoundModifierExpectedOrder = new ModifierOrder("", false, -1, -1);
        Modifier lastFoundModifier = new Modifier("", false, -1, -1);
        for (Modifier modifier : modifiers) {
            ModifierOrder expectedOrder = getTemplate(ast).getOrder(modifier);
            if (expectedOrder == null) continue;

            if (expectedOrder.order() < lastFoundModifierExpectedOrder.order()) {
                log(modifier.lineNo(), modifier.colNo(),
                        "{0} must be placed before {1}", modifier.toString(), lastFoundModifier.toString());
            }
            if (!lastFoundModifierExpectedOrder.matches(modifier, true)
                    && expectedOrder.groupOrder() == lastFoundModifierExpectedOrder.groupOrder()
                    && modifier.lineNo() != lastFoundModifier.lineNo()) {
                log(modifier.lineNo(), modifier.colNo(),
                        "{0} must be placed on the same line with {1}", modifier.toString(), lastFoundModifier.toString());
            }
            if (expectedOrder.groupOrder() > lastFoundModifierExpectedOrder.groupOrder()
                    && modifier.lineNo() <= lastFoundModifier.lineNo()) {
                log(modifier.lineNo(), modifier.colNo(),
                        "{0} must be placed on the new line after {1}", modifier.toString(), lastFoundModifier.toString());
            }

            lastFoundModifier = modifier;
            lastFoundModifierExpectedOrder = expectedOrder;
        }
    }

    private Order getTemplate(DetailAST ast) {
        return switch (ast.getType()) {
            case CLASS_DEF, INTERFACE_DEF, ANNOTATION_DEF, RECORD_DEF, ENUM_DEF -> typeTemplate;
            case VARIABLE_DEF, ANNOTATION_FIELD_DEF, PARAMETER_DEF -> fieldTemplate;
            case CTOR_DEF, METHOD_DEF -> methodTemplate;
            default -> new Order(List.of());
        };
    }
}
