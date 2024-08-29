package io.github.dmitrybdev.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import java.util.List;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

public class BoundaryCheck extends AbstractModifiersCheck {

    private int minLength = 0;
    private Order after = new Order(List.of());
    private Order before = new Order(List.of());
    private int minNewLines = 0;
    private int commentsAsNewLines = Integer.MAX_VALUE;


    public void setMinLength(int minLength) { this.minLength = minLength; }
    public void setAfter(String after) { this.after = parseTemplate(after); }
    public void setBefore(String before) { this.before = parseTemplate(before); }
    public void setMinNewLines(int minNewLines) { this.minNewLines = minNewLines; }
    public void setCommentsAsNewLines(int commentsAsNewLines) { this.commentsAsNewLines = commentsAsNewLines; }


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
        int comments = 0;
        for (int i = 0; i < ast.getLineNo(); i++) {
            String line = getLine(ast.getLineNo() - i - 2).trim();

            if (line.startsWith("//") || line.startsWith("/*") || line.startsWith("*")) {
                comments++;
                continue;
            }
            if (line.isBlank()) {
                spaces++;
                continue;
            }
            break;
        }

        return Math.min(comments, commentsAsNewLines) + spaces;
    }
}
