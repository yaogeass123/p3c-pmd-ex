package com.alibaba.p3c.pmd.lang.java.rule.ex;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractPojoRuleEx;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;

/**
 * @author Polaris
 */
public class PojoVariableNameRule extends AbstractPojoRuleEx {

    private static final String MESSAGE_KEY_PREFIX = "java.ex.PojoVariableNameRule.rule.msg";

    private static final String PREFIX = "is";

    @Override
    public Object visit(ASTFieldDeclaration node, Object data){

        ASTVariableDeclaratorId id = node.getFirstDescendantOfType(ASTVariableDeclaratorId.class);
        if (id != null) {
            String name = id.getImage();
            if(name.startsWith(PREFIX)) {
                addViolationWithMessage(data, node, MESSAGE_KEY_PREFIX);
            }
        }
        return super.visit(node, data);
    }

}
