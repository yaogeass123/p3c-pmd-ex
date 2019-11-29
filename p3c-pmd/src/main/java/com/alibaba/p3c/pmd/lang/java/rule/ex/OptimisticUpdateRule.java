package com.alibaba.p3c.pmd.lang.java.rule.ex;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRuleEx;
import com.alibaba.p3c.pmd.lang.java.util.namelist.NameListConfigEx;
import java.util.List;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import org.jaxen.JaxenException;

/**
 * @author Polaris
 */
public class OptimisticUpdateRule extends AbstractAliRuleEx {

    private static final String MESSAGE_KEY_PREFIX = "java.ex.OptimisticUpdateRule.rule.msg";
    private static final List<String> TARGET_FUNCTION_NAME = NameListConfigEx.NAME_LIST_SERVICE
            .getNameList("OptimisticUpdateRule", "TARGET_FUNCTION_NAME_LIST");
    private static final String MANAGER_SUFFIX = "Manager";
    private static final String MAPPER = "Mapper";
    private static final String TARGET_XPATH_FORM = "//Statement/StatementExpression/"
            + "PrimaryExpression[PrimaryPrefix/Name[ends-with(@Image,'%s')] and PrimarySuffix/Arguments]";
    private static final String THIS_FUNCTION_XPATH = "//Statement/StatementExpression/PrimaryExpression"
            + "[PrimaryPrefix[@ThisModifier='true'] and PrimarySuffix[@Image='%s'] and PrimarySuffix/Arguments]";

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        List<ASTClassOrInterfaceDeclaration> klasses = node
                .findDescendantsOfType(ASTClassOrInterfaceDeclaration.class);
        for (ASTClassOrInterfaceDeclaration klass : klasses) {
            if (isManager(klass)) {
                findAndCheck(node, data);
            }
        }
        return super.visit(node, data);
    }

    private void findAndCheck(ASTCompilationUnit node, Object data) {
        for (String functionName : TARGET_FUNCTION_NAME) {
            try {
                findAndCheckWithName(node, data, functionName);
                findAndCheckThisWithName(node, data, functionName);
            } catch (JaxenException ignore) {
            }

        }
    }

    private void findAndCheckThisWithName(ASTCompilationUnit node, Object data, String functionName)
            throws JaxenException {
        String path = String.format(THIS_FUNCTION_XPATH, functionName);
        List<Node> nodes = node.findChildNodesWithXPath(path);
        for (Node targetNode : nodes) {
            ASTPrimaryExpression expression = (ASTPrimaryExpression) targetNode;
            GenericToken thisModifier = expression.jjtGetFirstToken();
            GenericToken name = thisModifier.getNext().getNext();
            String variableName = name.getImage();
            if (variableName.endsWith(MAPPER)) {
                GenericToken functionToken = name.getNext().getNext();
                if (functionName.equals(functionToken.getImage())){
                    addViolationWithMessage(data, targetNode, MESSAGE_KEY_PREFIX);
                }
            }
        }
    }

    private void findAndCheckWithName(ASTCompilationUnit node, Object data, String functionName)
            throws JaxenException {
        String path = String.format(TARGET_XPATH_FORM, "." + functionName);
        List<Node> nodes = node.findChildNodesWithXPath(path);
        for (Node targetNode : nodes) {
            ASTPrimaryPrefix call = targetNode.getFirstChildOfType(ASTPrimaryPrefix.class);
            ASTName nameNode = call.getFirstChildOfType(ASTName.class);
            String name = nameNode.getImage();
            String variableName = name.substring(0, name.lastIndexOf("." + functionName));
            if (variableName.endsWith(MAPPER)) {
                addViolationWithMessage(data, targetNode, MESSAGE_KEY_PREFIX);
            }
        }
    }

    private boolean isManager(ASTClassOrInterfaceDeclaration node) {
        if (node != null) {
            String name = node.getImage();
            return name != null && name.contains(MANAGER_SUFFIX);
        }
        return false;
    }

}
