package com.alibaba.p3c.pmd.lang.java.rule.ex;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRuleEx;
import com.beust.jcommander.internal.Maps;
import com.beust.jcommander.internal.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTCatchStatement;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import org.apache.commons.lang3.StringUtils;
import org.jaxen.JaxenException;


/**
 * @author Administrator
 */
public class LogErrorRule extends AbstractAliRuleEx {

    private static final String MESSAGE_KEY_PREFIX = "java.ex.LogErrorRule.rule.msg";
    private static final String LOGGER_PACKAGE = "org.slf4j.Logger";
    private static final String partter = "@Image='$$$$$.info'";
    private static final String targetChildXPath =
            "//Statement/StatementExpression/PrimaryExpression"
                    + "[PrimaryPrefix/Name[&&&&&] and PrimarySuffix/Arguments/ArgumentList/"
                    + "Expression/PrimaryExpression/PrimaryPrefix/Name[@Image='#####']]";

    private static Map<String, String> importPackage = Maps.newHashMap();
    private static Set<String> loggerName = Sets.newHashSet();

    @Override
    public Object visit(ASTImportDeclaration importDeclaration, Object data) {
        importPackage.put(importDeclaration.getImportedSimpleName(),
                importDeclaration.getImportedName());
        return super.visit(importDeclaration, data);
    }

    @Override
    public Object visit(ASTFieldDeclaration fieldDeclaration, Object data) {
        ASTType type = fieldDeclaration.getFirstChildOfType(ASTType.class);
        if (StringUtils.equals(importPackage.get(type.getTypeImage()), LOGGER_PACKAGE)) {
            ASTVariableDeclarator declarator = fieldDeclaration
                    .getFirstChildOfType(ASTVariableDeclarator.class);
            if (declarator != null) {
                ASTVariableDeclaratorId id = declarator
                        .getFirstChildOfType(ASTVariableDeclaratorId.class);
                loggerName.add(id.getImage());
            }
        }
        return super.visit(fieldDeclaration, data);
    }

    @Override
    public Object visit(ASTCatchStatement node, Object data) {
        if (loggerName.isEmpty()) {
            return super.visit(node, data);
        }
        String varName = node.getExceptionName();
        String replace = targetChildXPath.replace("#####", varName);
        for (String name : loggerName) {
            replace = replace.replace("&&&&&", partter.replace("$$$$$", name) + " or &&&&&");
        }
        replace = replace.replace(" or &&&&&", "");
        try {
            List<Node> targetNodes = node.findChildNodesWithXPath(replace);
            for (Node targetNode : targetNodes) {
                addViolationWithMessage(data, targetNode.jjtGetChild(0).jjtGetChild(0),
                        MESSAGE_KEY_PREFIX);
            }
        } catch (JaxenException e) {
            //写文件记录？
        }
        return super.visit(node, data);
    }
}
