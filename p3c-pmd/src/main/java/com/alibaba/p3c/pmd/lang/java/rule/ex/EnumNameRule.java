package com.alibaba.p3c.pmd.lang.java.rule.ex;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRuleEx;
import net.sourceforge.pmd.lang.java.ast.ASTEnumConstant;
import net.sourceforge.pmd.lang.java.ast.ASTEnumDeclaration;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Polaris
 */
public class EnumNameRule extends AbstractAliRuleEx {

    private static final String ENUM_SUFFIX = "En";

    private static final String MESSAGE_KEY_PREFIX = "java.ex.EnumNameRule.rule.msg";

    private static final String MESSAGE_KEY_PREFIX_VAR = "java.ex.EnumNameRuleVar.rule.msg";

    @Override
    public Object visit(ASTEnumDeclaration node, Object data) {
        if (!node.getImage().endsWith(ENUM_SUFFIX)) {
            addViolationWithMessage(data, node, MESSAGE_KEY_PREFIX);
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTEnumConstant node, Object data) {
        String name = node.getImage();
        if (StringUtils.isNotEmpty(name) && !name.equals(name.toUpperCase())) {
            addViolationWithMessage(data, node, MESSAGE_KEY_PREFIX_VAR);
        }
        return super.visit(node, data);
    }

}
