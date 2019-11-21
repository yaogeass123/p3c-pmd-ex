package com.alibaba.p3c.pmd.lang.java.rule.ex;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRuleEx;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import java.util.List;
import java.util.Map;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTArgumentList;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceBodyDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimarySuffix;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import org.jaxen.JaxenException;

/**
 * @author Polaris
 */
public class TransactionInvalidRule extends AbstractAliRuleEx {

    private static final String MESSAGE_KEY_PREFIX = "java.ex.TransactionInvalidRule.rule.msg";
    private static final String ANNOTATION_NAME = "Transactional";
    private static final String FUNCTION_XPATH = "//Statement/StatementExpression/PrimaryExpression"
            + "[PrimaryPrefix/Name[@Image='%s'] and PrimarySuffix/Arguments]";
    private static final String THIS_FUNCTION_XPATH =
            "//Statement/StatementExpression/PrimaryExpression"
                    + "[PrimaryPrefix and PrimarySuffix[@Image='%s'] and PrimarySuffix/Arguments]";
    private static final String LOCAL_VAR_XPATH =
            "//BlockStatement//LocalVariableDeclaration" + "[VariableDeclarator[@Name='%s']]";
    private static final String ARGS_VAR_XPATH = "//MethodDeclaration//MethodDeclarator//"
            + "FormalParameters//FormalParameter[VariableDeclaratorId[@Image='%s']]";
    private static final String GLOBAL_VAR_XPATH = "//ClassOrInterfaceBodyDeclaration//"
            + "FieldDeclaration[VariableDeclarator[@Name='%s']]";

    @Override
    public Object visit(ASTClassOrInterfaceBodyDeclaration node, Object data) {
        ASTAnnotation annotation = node.getFirstChildOfType(ASTAnnotation.class);
        if (annotation != null && ANNOTATION_NAME.equals(annotation.getAnnotationName())) {
            //考虑多态
            ASTMethodDeclaration methodDeclaration = node
                    .getFirstChildOfType(ASTMethodDeclaration.class);
            if (methodDeclaration != null) {
                String methodName = methodDeclaration.getMethodName();
                List<String> args = Lists.newArrayList();
                if (methodDeclaration.getMethodDeclarator() != null
                        && methodDeclaration.getMethodDeclarator().getParameterCount() != 0) {
                    List<ASTFormalParameter> list = methodDeclaration
                            .findDescendantsOfType(ASTFormalParameter.class);
                    if (!list.isEmpty()) {
                        list.forEach(param -> {
                            if (param.getTypeDefinition() != null) {
                                args.add(param.getTypeDefinition().getType().getName());
                            } else {
                                args.add(param.getTypeNode().getTypeImage());
                            }
                        });
                    }
                }
                String thisXpath = String.format(THIS_FUNCTION_XPATH, methodName);
                String functionXpath = String.format(FUNCTION_XPATH, methodName);
                List<ASTPrimaryExpression> expressions = Lists.newArrayList();
                try {
                    List<Node> targetNodes = node.findChildNodesWithXPath(thisXpath);
                    targetNodes.forEach(x -> expressions.add((ASTPrimaryExpression) x));
                    targetNodes = node.findChildNodesWithXPath(functionXpath);
                    targetNodes.forEach(x -> expressions.add((ASTPrimaryExpression) x));
                    for (ASTPrimaryExpression expression : expressions) {
                        if (check(expression, args)) {
                            addViolationWithMessage(data, expression, MESSAGE_KEY_PREFIX);
                        }
                    }
                } catch (JaxenException e) {
                    e.printStackTrace();
                }
            }

        }
        return super.visit(node, data);
    }

    /**
     * @param expression 疑似调用该函数的表达式
     * @param args 函数声明的参数类型列表
     * @return 是否调用目标函数
     * @throws JaxenException XPATH异常
     */
    private boolean check(ASTPrimaryExpression expression, List<String> args)
            throws JaxenException {
        ASTMethodDeclaration parent = expression.getFirstParentOfType(ASTMethodDeclaration.class);
        Node suffix = expression.jjtGetChild(expression.jjtGetNumChildren() - 1);
        if (suffix instanceof ASTPrimarySuffix) {
            ASTPrimarySuffix arguments = (ASTPrimarySuffix) suffix;
            if (arguments.getArgumentCount() == 0 && args.isEmpty()) {
                return true;
            }
            if (arguments.getArgumentCount() != 0 && !args.isEmpty()
                    && arguments.getArgumentCount() == args.size()) {
                ASTArgumentList list = (ASTArgumentList) (arguments.jjtGetChild(0)).jjtGetChild(0);
                return paramCheck(expression, args, list);
            }
        }
        return false;
    }

    /**
     * 参数校验
     *
     * @param expression 表达式
     * @param args 参数类型
     * @param list 参数列表
     * @return boolea
     * @throws JaxenException XPATH异常
     */
    private boolean paramCheck(ASTPrimaryExpression expression, List<String> args,
            ASTArgumentList list) throws JaxenException {
        for (int i = 0; i < args.size(); i++) {
            //exp:第i个参数
            ASTExpression exp = (ASTExpression) list.jjtGetChild(i);
            Node varNode = exp.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
            if (varNode instanceof ASTName) {
                ASTName node = (ASTName) varNode;
                if (node.getTypeDefinition() != null) {
                    //基本类型
                    if (node.getTypeDefinition().getType().getName().equals(args.get(i))) {
                        continue;
                    } else {
                        return false;
                    }
                }
                //自定义的类型变量
                String varName = node.getImage();
                int result = localDeclarationCheck(expression, args.get(i), exp, varName);
                if (result == 1) {
                    continue;
                }
                if (result == 0) {
                    return false;
                }
                result = argumentsCheck(expression, args.get(i), exp, varName);
                if (result == 1) {
                    continue;
                }
                if (result == 0) {
                    return false;
                }
                result = globalCheck(expression, args.get(i), exp, varName);
                if (result == 1) {
                    continue;
                }
                if (result == 0) {
                    return false;
                }
                return false;
            }
            if (varNode instanceof ASTLiteral) {
                //魔法值
                String type = ((ASTLiteral) varNode).getType().getTypeName();
                if (!type.equals(args.get(i))) {
                    return false;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * @param expression 函数调用表达式 save(info,i,1)
     * @param targetType 目标类型
     * @param exp 参数的表达式
     * @param varName 名称
     * @return true: 二者类型相同 false：类型不同或者存在本地变量
     */

    private int localDeclarationCheck(ASTPrimaryExpression expression, String targetType,
            ASTExpression exp, String varName) throws JaxenException {
        String path = String.format(LOCAL_VAR_XPATH, varName);
        List<Node> varDeclaration = exp.findChildNodesWithXPath(path);
        if (varDeclaration != null && varDeclaration.size() != 0) {
            Map<Integer, ASTLocalVariableDeclaration> blockMap = Maps.newHashMap();
            for (Node declaration : varDeclaration) {
                ASTLocalVariableDeclaration localVariableDeclaration = (ASTLocalVariableDeclaration) declaration;
                List<ASTBlock> expBlocks = expression.getParentsOfType(ASTBlock.class);
                ASTBlock localBlock = localVariableDeclaration.getFirstParentOfType(ASTBlock.class);
                int count = 1;
                for (ASTBlock expBlock : expBlocks) {
                    if (localBlock.equals(expBlock)) {
                        blockMap.put(count, localVariableDeclaration);
                        break;
                    }
                    count++;
                }
            }
            if (!blockMap.isEmpty()) {
                int key = blockMap.keySet().stream().sorted().findFirst().get();
                return targetType.equals(blockMap.get(key).getTypeNode().getTypeImage()) ? 1 : 0;
            }
        }
        return -1;
    }

    private int argumentsCheck(ASTPrimaryExpression expression, String targetType,
            ASTExpression exp, String varName) throws JaxenException {
        String path = String.format(ARGS_VAR_XPATH, varName);
        List<Node> varDeclaration = exp.findChildNodesWithXPath(path);
        if (varDeclaration != null && varDeclaration.size() != 0) {
            for (Node declaration : varDeclaration) {
                ASTFormalParameter formalParameter = (ASTFormalParameter) declaration;
                ASTMethodDeclaration fParent = formalParameter
                        .getFirstParentOfType(ASTMethodDeclaration.class);
                ASTMethodDeclaration eParent = expression
                        .getFirstParentOfAnyType(ASTMethodDeclaration.class);
                if (fParent != null && fParent.equals(eParent)) {
                    String type = formalParameter.getTypeNode().getTypeImage();
                    return targetType.equals(type) ? 1 : 0;
                }
            }
        }
        return -1;
    }

    private int globalCheck(ASTPrimaryExpression expression, String targetType, ASTExpression exp,
            String varName) throws JaxenException {
        String path = String.format(GLOBAL_VAR_XPATH, varName);
        List<Node> varDeclaration = exp.findChildNodesWithXPath(path);
        if (varDeclaration != null && varDeclaration.size() != 0) {
            for (Node node : varDeclaration) {
                ASTFieldDeclaration fieldDeclaration = (ASTFieldDeclaration) node;
                String type = fieldDeclaration.getFirstChildOfType(ASTType.class).getTypeImage();
                return targetType.equals(type) ? 1 : 0;
            }

        }
        return -1;
    }
}

