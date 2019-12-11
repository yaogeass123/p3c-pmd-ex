package com.alibaba.p3c.pmd.lang.java.rule;

import com.beust.jcommander.internal.Maps;
import com.beust.jcommander.internal.Sets;
import java.util.Map;
import java.util.Set;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import org.apache.commons.lang3.StringUtils;

/**
 * @author polaris
 */
public abstract class AbstractLogRuleEx extends AbstractAliRuleEx {

    private static final String LOGGER_PACKAGE = "org.slf4j.Logger";

    private static Map<String, String> importPackage = Maps.newHashMap();
    protected static Set<String> loggerName = Sets.newHashSet();

    /**
     * 获取全限定名与简写名对应关系
     * @param importDeclaration import
     * @param data data
     * @return data
     */
    @Override
    public Object visit(ASTImportDeclaration importDeclaration, Object data) {
        importPackage.put(importDeclaration.getImportedSimpleName(), importDeclaration.getImportedName());
        return super.visit(importDeclaration, data);
    }

    /**
     * 获取日志变量名
     * @param fieldDeclaration 声明语句
     * @param data data
     * @return data
     */
    @Override
    public Object visit(ASTFieldDeclaration fieldDeclaration, Object data) {
        ASTType type = fieldDeclaration.getFirstChildOfType(ASTType.class);
        if (StringUtils.equals(importPackage.get(type.getTypeImage()), LOGGER_PACKAGE)) {
            ASTVariableDeclarator declarator = fieldDeclaration.getFirstChildOfType(ASTVariableDeclarator.class);
            if (declarator != null) {
                ASTVariableDeclaratorId id = declarator.getFirstChildOfType(ASTVariableDeclaratorId.class);
                loggerName.add(id.getImage());
            }
        }
        return super.visit(fieldDeclaration, data);
    }
}
