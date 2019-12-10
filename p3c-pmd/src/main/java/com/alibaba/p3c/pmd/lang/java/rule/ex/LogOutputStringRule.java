package com.alibaba.p3c.pmd.lang.java.rule.ex;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractLogRuleEx;
import net.sourceforge.pmd.lang.java.ast.ASTStatementExpression;

/**
 * @author polaris
 */
public class LogOutputStringRule extends AbstractLogRuleEx {

    private static final String MESSAGE_KEY_PREFIX = "java.ex.LogOutputStringRule.rule.msg";

    @Override
    public Object visit(ASTStatementExpression node, Object data){

        return super.visit(node, data);
    }

}
