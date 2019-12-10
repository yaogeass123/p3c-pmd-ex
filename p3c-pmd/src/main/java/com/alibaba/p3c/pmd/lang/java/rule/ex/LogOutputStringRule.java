package com.alibaba.p3c.pmd.lang.java.rule.ex;

import static com.alibaba.p3c.pmd.lang.java.rule.utils.LogBuild.addImageWords;
import static com.alibaba.p3c.pmd.lang.java.rule.utils.LogBuild.buildLogCall;
import static com.alibaba.p3c.pmd.lang.java.rule.utils.PrimaryNodeSearch.findPrimaryName;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractLogRuleEx;
import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAdditiveExpression;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTStatementExpression;
import org.apache.commons.lang3.StringUtils;
import org.jaxen.JaxenException;

/**
 * @author polaris
 */
public class LogOutputStringRule extends AbstractLogRuleEx {

    private static final String MESSAGE_KEY_PREFIX = "java.ex.LogOutputStringRule.rule.msg";

    private static final String STRING_CLASS = "java.lang.String";

    private static final String TARGET_XPATH_FORM = "//Statement/StatementExpression/"
            + "PrimaryExpression[PrimaryPrefix/Name[%s] and PrimarySuffix/Arguments/ArgumentList"
            + "/Expression/AdditiveExpression/PrimaryExpression/PrimaryPrefix]";

    @Override
    public Object visit(ASTStatementExpression node, Object data) {
        String call = findPrimaryName(node.jjtGetChild(0));
        if (StringUtils.isNotEmpty(call)) {
            call = addImageWords(call);
            List<String> target = buildLogCall(loggerName);
            if (target.contains(call)) {
                check(node, data, call);
            }
        }
        return super.visit(node, data);
    }

    private void check(ASTStatementExpression node, Object data, String call) {
        //更改为xpath搜索
        String xpath = String.format(TARGET_XPATH_FORM, call);
        try {
            List<Node> targets = node.findChildNodesWithXPath(xpath);
            for (Node target : targets) {
                if (!node.equals(target.getFirstParentOfType(ASTStatementExpression.class))) {
                    continue;
                }
                ASTAdditiveExpression add = (ASTAdditiveExpression) target.jjtGetChild(1)
                        .jjtGetChild(0).jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
                List<ASTPrimaryExpression> t = add.findChildrenOfType(ASTPrimaryExpression.class);
                for (ASTPrimaryExpression expression : t) {
                    ASTPrimaryPrefix prefix = (ASTPrimaryPrefix) expression.jjtGetChild(0);
                    if (prefix.jjtGetChild(0) instanceof ASTName) {
                        ASTName name = (ASTName) prefix.jjtGetChild(0);
                        if (STRING_CLASS.equals(name.getType().getName())) {
                            addViolationWithMessage(data, node, MESSAGE_KEY_PREFIX);
                            return;
                        }
                    }
                    if (prefix.jjtGetChild(0) instanceof ASTLiteral) {
                        ASTLiteral literal = (ASTLiteral) prefix.jjtGetChild(0);
                        if (STRING_CLASS.equals(literal.getType().getName())) {
                            addViolationWithMessage(data, node, MESSAGE_KEY_PREFIX);
                            return;
                        }
                    }
                }

            }
        } catch (JaxenException ignore) {
        }
    }

}
