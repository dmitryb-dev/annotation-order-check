package io.github.dmitrybdev.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

public class DeclarationOrderCheck extends AbstractModifiersCheck {

    private Order template = new Order(List.of());


    public void setTemplate(String template) { this.template = parseTemplate(template); }


    @Override
    public int[] getAcceptableTokens() {
        return new int[] { COMPILATION_UNIT, CLASS_DEF, INTERFACE_DEF, ENUM_DEF, RECORD_DEF, ANNOTATION_DEF };
    }

    @Override
    public void visitToken(DetailAST ast) {
        var lastFoundDeclaration = new GroupOrder(List.of(), -1);
        for (DetailAST child : getChildren(ast)) {
            var modifiers = getModifiers(child);
            if (modifiers == null) continue;

            GroupOrder expectedOrder = template.getOrder(modifiers);
            if (expectedOrder == null) continue;

            if (expectedOrder.order() < lastFoundDeclaration.order()) {
                log(child.getLineNo(), child.getColumnNo(),
                        "{0} must be placed before {1}", expectedOrder.toString(), lastFoundDeclaration.toString());
            }

            lastFoundDeclaration = expectedOrder;
        }
    }

    private List<DetailAST> getChildren(DetailAST ast) {
        DetailAST childrenAst = ast.getType() == COMPILATION_UNIT
                ? ast.getFirstChild()
                : ast.findFirstToken(OBJBLOCK).getFirstChild();
        return Stream.iterate(childrenAst, Objects::nonNull, DetailAST::getNextSibling).toList();
    }
}
