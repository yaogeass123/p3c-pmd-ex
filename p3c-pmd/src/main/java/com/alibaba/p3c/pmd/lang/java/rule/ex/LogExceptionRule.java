package com.alibaba.p3c.pmd.lang.java.rule.ex;

import static com.alibaba.p3c.pmd.lang.java.rule.utils.LogBuild.buildLogXpath;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractLogRuleEx;
import com.alibaba.p3c.pmd.lang.java.rule.utils.LogBuild;
import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTArgumentList;
import net.sourceforge.pmd.lang.java.ast.ASTArguments;
import net.sourceforge.pmd.lang.java.ast.ASTCatchStatement;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import org.jaxen.JaxenException;

/**
 * @author Polaris
 */
public class LogExceptionRule extends AbstractLogRuleEx {

    private static final String MESSAGE_KEY_PREFIX = "java.ex.LogExceptionRule.rule.msg";

    @Override
    public Object visit(ASTCatchStatement node, Object data) {
        if (loggerName.isEmpty()) {
            return super.visit(node, data);
        }
        String varName = node.getExceptionName();
        String replace = buildLogXpath(loggerName, LogBuild.ERROR) + "]";
        try {
            List<Node> targetNodes = node.findChildNodesWithXPath(replace);
            for (Node targetNode : targetNodes) {
                if (targetNode.getParentsOfType(ASTCatchStatement.class).contains(node)) {
                    ASTArguments args = (ASTArguments) targetNode.jjtGetChild(1).jjtGetChild(0);
                    if(args.jjtGetNumChildren() == 0) {
                        addViolationWithMessage(data, targetNode, MESSAGE_KEY_PREFIX);
                        continue;
                    }
                    ASTArgumentList list = (ASTArgumentList) args.jjtGetChild(0);
                    Node exp = list.jjtGetChild(list.jjtGetNumChildren()-1);
                    if(!(exp instanceof ASTExpression)) {
                        continue;
                    }
                    if(!(exp.jjtGetChild(0) instanceof ASTPrimaryExpression)) {
                        continue;
                    }
                    if(!(exp.jjtGetChild(0).jjtGetChild(0) instanceof ASTPrimaryPrefix)) {
                        continue;
                    }
                    Node n = exp.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
                    if(!(n instanceof ASTName && varName.equals(n.getImage()))) {
                        addViolationWithMessage(data, targetNode, MESSAGE_KEY_PREFIX);
                    }
                }
            }
        } catch (JaxenException ignore) {
        }
        return super.visit(node, data);
    }

}
