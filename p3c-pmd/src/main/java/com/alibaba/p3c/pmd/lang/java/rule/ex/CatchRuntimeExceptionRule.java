package com.alibaba.p3c.pmd.lang.java.rule.ex;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRuleEx;
import com.alibaba.p3c.pmd.lang.java.util.namelist.NameListConfigEx;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.pmd.lang.java.ast.ASTCatchStatement;
import net.sourceforge.pmd.lang.java.ast.ASTType;


/**
 * @author Polaris
 */
public class CatchRuntimeExceptionRule extends AbstractAliRuleEx {

    private static final String MESSAGE_KEY_PREFIX = "java.ex.CatchRuntimeExceptionRule.rule.msg";

    private static final String PATTERN = "java\\.lang\\.[A-Za-z]*Exception";

    private static final List<String> WHITE_LIST = NameListConfigEx.NAME_LIST_SERVICE
            .getNameList("CatchRuntimeExceptionRule", "WHITE_LIST");

    @Override
    public Object visit(ASTCatchStatement node, Object data) {

        for (ASTType type : node.getCaughtExceptionTypeNodes()) {
            Class<?> e = type.getType();
            if (e == null) {
                continue;
            }
            if(WHITE_LIST.contains(e.getTypeName())) {
                continue;
            }
            Matcher matcher = Pattern.compile(PATTERN).matcher(e.getTypeName());
            if (matcher.find()) {
                if (RuntimeException.class.isAssignableFrom(e)) {
                    addViolationWithMessage(data, type, MESSAGE_KEY_PREFIX);
                }
            }
        }
        return super.visit(node, data);
    }
}
