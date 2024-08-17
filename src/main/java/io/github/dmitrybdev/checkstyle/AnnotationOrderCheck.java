package io.github.dmitrybdev.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import java.util.HashMap;
import java.util.Map;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

public class AnnotationOrderCheck extends AbstractCheck {

    private Map<String, ExpectedOrder> typeTemplate = Map.of();
    private Map<String, ExpectedOrder> fieldTemplate = Map.of();
    private Map<String, ExpectedOrder> methodTemplate = Map.of();


    public void setTypeTemplate(String typeTemplate) {
        this.typeTemplate = parseTemplate(typeTemplate);
    }

    public void setFieldTemplate(String fieldTemplate) {
        this.fieldTemplate = parseTemplate(fieldTemplate);
    }

    public void setMethodTemplate(String methodTemplate) {
        this.methodTemplate = parseTemplate(methodTemplate);
    }

    private Map<String, ExpectedOrder> parseTemplate(String template) {
        HashMap<String, ExpectedOrder> parsedTemplate = new HashMap<>();

        int lineNo = 0, order = 0;
        for (String line : template.split("\n")) {
            for (String annotation : line.split("\\s+")) {
                annotation = annotation.trim();
                if (annotation.isBlank()) continue;

                parsedTemplate.put(annotation, new ExpectedOrder(lineNo, order));
                order++;
            }
            lineNo++;
        }

        return parsedTemplate;
    }


    @Override
    public int[] getDefaultTokens() {
        return new int[] { CLASS_DEF, VARIABLE_DEF, CTOR_DEF, METHOD_DEF };
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] {};
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST modifiers = ast.findFirstToken(MODIFIERS).getFirstChild();

        String lastFoundModifierText = "";
        int lastFoundModifierLineNo = -1;
        ExpectedOrder lastFoundModifierExpectedOrder = new ExpectedOrder(-1, -1);

        for (DetailAST modifier = modifiers; modifier != null; modifier = modifier.getNextSibling()) {
            String modifierText = modifier.getType() == ANNOTATION
                    ? "@" + modifier.getFirstChild().getNextSibling().getText()
                    : modifier.getText();

            ExpectedOrder expectedOrder = getTemplate(ast).get(modifierText);
            if (expectedOrder == null) continue;

            if (expectedOrder.order() < lastFoundModifierExpectedOrder.order()) {
                log(
                        modifier.getLineNo(), modifier.getColumnNo(),
                        "{0} must be placed before {1}", modifierText, lastFoundModifierText
                );
            }
            if (expectedOrder.lineNo() == lastFoundModifierExpectedOrder.lineNo()
                    && modifier.getLineNo() != lastFoundModifierLineNo) {
                log(
                        modifier.getLineNo(), modifier.getColumnNo(),
                        "{0} must be placed on the same line with {1}", modifierText, lastFoundModifierText
                );
            }
            if (expectedOrder.lineNo() > lastFoundModifierExpectedOrder.lineNo()
                    && modifier.getLineNo() <= lastFoundModifierLineNo) {
                log(
                        modifier.getLineNo(), modifier.getColumnNo(),
                        "{0} must be placed on the new line after {1}", modifierText, lastFoundModifierText
                );
            }

            lastFoundModifierText = modifierText;
            lastFoundModifierLineNo = modifier.getLineNo();
            lastFoundModifierExpectedOrder = expectedOrder;
        }
    }

    private Map<String, ExpectedOrder> getTemplate(DetailAST ast) {
        return switch (ast.getType()) {
            case CLASS_DEF -> typeTemplate;
            case VARIABLE_DEF -> fieldTemplate;
            case CTOR_DEF, METHOD_DEF -> methodTemplate;
            default -> Map.of();
        };
    }


    private record ExpectedOrder(int lineNo, int order) {}
}
