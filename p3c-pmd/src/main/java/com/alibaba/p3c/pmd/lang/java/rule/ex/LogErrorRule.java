package com.alibaba.p3c.pmd.lang.java.rule.ex;

import static com.alibaba.p3c.pmd.lang.java.rule.utils.LogXpathBuild.buildExceptionSuffix;
import static com.alibaba.p3c.pmd.lang.java.rule.utils.LogXpathBuild.buildLogXpath;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractLogRuleEx;
import com.alibaba.p3c.pmd.lang.java.rule.utils.LogXpathBuild;
import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTCatchStatement;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import org.jaxen.JaxenException;


/**
 * @author Polaris
 */
public class LogErrorRule extends AbstractLogRuleEx {

    private static final String MESSAGE_KEY_PREFIX = "java.ex.LogErrorRule.rule.msg";

    @Override
    public Object visit(ASTCatchStatement node, Object data) {
        if (loggerName.isEmpty()) {
            return super.visit(node, data);
        }
        String varName = node.getExceptionName();
        String replace = buildLogXpath(loggerName, LogXpathBuild.INFO) + buildExceptionSuffix(varName);
        try {
            List<Node> targetNodes = node.findChildNodesWithXPath(replace);
            for (Node targetNode : targetNodes) {
                if (targetNode.getParentsOfType(ASTCatchStatement.class).contains(node)) {
                    addViolationWithMessage(data,
                            targetNode.getFirstChildOfType(ASTPrimaryPrefix.class),
                            MESSAGE_KEY_PREFIX);
                }
            }
        } catch (JaxenException ignore) {
        }
        return super.visit(node, data);
    }
}
