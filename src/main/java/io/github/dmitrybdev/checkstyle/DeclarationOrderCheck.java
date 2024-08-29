//package io.github.dmitrybdev.checkstyle;
//
//import com.puppycrawl.tools.checkstyle.api.DetailAST;
//
//import java.util.List;
//
//import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
//
//public class DeclarationOrderCheck extends AbstractModifiersCheck {
//
//    private Order template = new Order(List.of());
//
//
//    public void setTemplate(String template) { this.template = parseTemplate(template); }
//
//
//    @Override
//    public int[] getAcceptableTokens() {
//        return new int[] { COMPILATION_UNIT, CLASS_DEF, INTERFACE_DEF, ENUM_DEF, RECORD_DEF, ANNOTATION_DEF };
//    }
//
//    @Override
//    public void visitToken(DetailAST ast) {
//        List<Modifier> modifiers = getModifiers(ast);
//        if (modifiers == null) return;
//
//        ModifierOrder lastFoundModifierExpectedOrder = new ModifierOrder("", false, -1, -1);
//        Modifier lastFoundModifier = new Modifier("", false, -1, -1);
//        for (Modifier modifier : modifiers) {
//            ModifierOrder expectedOrder = getTemplate(ast).getOrder(modifier);
//            if (expectedOrder == null) continue;
//
//            if (expectedOrder.order() < lastFoundModifierExpectedOrder.order()) {
//                log(modifier.lineNo(), modifier.colNo(),
//                        "{0} must be placed before {1}", modifier.toString(), lastFoundModifier.toString());
//            }
//            if (!lastFoundModifierExpectedOrder.matches(modifier, true)
//                    && expectedOrder.groupOrder() == lastFoundModifierExpectedOrder.groupOrder()
//                    && modifier.lineNo() != lastFoundModifier.lineNo()) {
//                log(modifier.lineNo(), modifier.colNo(),
//                        "{0} must be placed on the same line with {1}", modifier.toString(), lastFoundModifier.toString());
//            }
//            if (expectedOrder.groupOrder() > lastFoundModifierExpectedOrder.groupOrder()
//                    && modifier.lineNo() <= lastFoundModifier.lineNo()) {
//                log(modifier.lineNo(), modifier.colNo(),
//                        "{0} must be placed on the new line after {1}", modifier.toString(), lastFoundModifier.toString());
//            }
//
//            lastFoundModifier = modifier;
//            lastFoundModifierExpectedOrder = expectedOrder;
//        }
//    }
//}
