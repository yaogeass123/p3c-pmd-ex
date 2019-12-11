package com.alibaba.p3c.pmd.lang.java.rule.utils;

import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTArguments;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTPrimarySuffix;

/**
 * @author polaris
 */
public class PrimaryNodeSearch {

    public static ASTPrimaryPrefix findPrimaryPrefix(Node node) {
        if (!(node instanceof ASTPrimaryExpression)) {
            return null;
        }
        while (node.jjtGetNumChildren() > 0) {
            node = node.jjtGetChild(0);
            if (node instanceof ASTPrimaryPrefix) {
                return (ASTPrimaryPrefix) node;
            }
        }
        return null;
    }

    public static ASTArguments findArguments(Node node) {
        if (!(node instanceof ASTPrimaryExpression)) {
            return null;
        }
        List<ASTPrimarySuffix> lists = node.findChildrenOfType(ASTPrimarySuffix.class);
        for (int i = lists.size() - 1; i >= 0; i--) {
            if (lists.get(i).jjtGetChild(0) instanceof ASTArguments) {
                return (ASTArguments) lists.get(i).jjtGetChild(0);
            }
        }
        return null;
    }

    public static String findPrimaryName(Node node) {
        if (node instanceof ASTPrimaryExpression) {
            ASTPrimaryPrefix prefix = findPrimaryPrefix(node);
            //this与super时prefix没有子节点
            if (prefix != null && prefix.jjtGetNumChildren() > 0 && prefix
                    .jjtGetChild(0) instanceof ASTName) {
                return prefix.jjtGetChild(0).getImage();
            }
            return null;
        }
        if (node instanceof ASTPrimaryPrefix && node.jjtGetNumChildren() > 0) {
            if (node.jjtGetChild(0) instanceof ASTName) {
                return node.jjtGetChild(0).getImage();
            }
        }
        return null;
    }
}
