package com.alibaba.p3c.pmd.lang.java.rule.ex;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRuleEx;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;

/**
 * @author Polaris
 */
public class AvoidAccessModifiersInInterfaceRule extends AbstractAliRuleEx {

    private static final String MESSAGE_KEY_PREFIX = "java.ex.AvoidAccessModifiersInInterfaceRule.rule.msg";

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        if (!node.isInterface()) {
            return null;
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTMethodDeclaration node, Object data) {
        System.out.println(node.getName()+": "+node.getModifiers());
        if (node.getModifiers() > 0) {
            if (node.isPublic() || node.isProtected() || node.isPrivate()) {
                addViolationWithMessage(data, node, MESSAGE_KEY_PREFIX);
            }
        }
        return null;
    }

}
