package com.alibaba.p3c.pmd.lang.java.rule.ex;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRuleEx;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTMemberValue;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTSingleMemberAnnotation;

/**
 * @author Polais
 * @date 2019/10/16
 */
public class CheckNameAnnotationRule extends AbstractAliRuleEx {

    private static final String ANNOTATION_NAME = "Value";

    private static final String PART = "\"(.)*:(.)*\"";

    private static final String PART_RIGHT = "\"\\$\\{(\\w|-)+:(\\w|-)+}\"";

    private static final String MESSAGE_KEY_PREFIX = "java.ex.CheckNameAnnotationRule.rule.msg";

    @Override
    public Object visit(ASTSingleMemberAnnotation annotation, Object data) {
        if (ANNOTATION_NAME.equals(annotation.getAnnotationName())) {
            ASTMemberValue memberValue = annotation.getMemberValue();

            if (Objects.isNull(memberValue)) {
                return super.visit(annotation, data);
            }
            ASTPrimaryExpression expression = (ASTPrimaryExpression) memberValue.jjtGetChild(0);
            if (Objects.isNull(expression)) {
                return super.visit(annotation, data);
            }
            ASTPrimaryPrefix prefix = (ASTPrimaryPrefix) expression.jjtGetChild(0);
            if (Objects.isNull(prefix)) {
                return super.visit(annotation, data);
            }
            ASTLiteral literal = (ASTLiteral) prefix.jjtGetChild(0);
            if (Objects.isNull(literal)) {
                return super.visit(annotation, data);
            }
            String value = literal.getImage();
            Matcher part = Pattern.compile(PART).matcher(value);
            if (part.find()) {
                Matcher right = Pattern.compile(PART_RIGHT).matcher(value);
                if (!right.find()) {
                    addViolationWithMessage(data, annotation, MESSAGE_KEY_PREFIX);
                }
            }
        }
        return super.visit(annotation, data);
    }
}
