package com.alibaba.p3c.pmd.lang.java.rule.utils;

import com.alibaba.p3c.pmd.lang.java.util.namelist.NameListConfigEx;
import com.beust.jcommander.internal.Lists;
import java.util.List;
import java.util.Set;

/**
 * @author polaris
 */
public class LogBuild {

    private static final String PATTER = "@Image='%s.%s'";

    private static final String TARGET_XPATH_FORM = "//Statement/StatementExpression/"
            + "PrimaryExpression[PrimaryPrefix/Name[%s] and PrimarySuffix/Arguments";

    private static final String ERROR_SUFFIX ="/ArgumentList/Expression/PrimaryExpression/"
            + "PrimaryPrefix/Name[@Image='%s']]";

    private static final List<String> TARGET_FUNCTION_NAME = NameListConfigEx.NAME_LIST_SERVICE
            .getNameList("LogOutputStringRule", "TARGET_FUNCTION_NAME_LIST");

    public static final String INFO = "info";

    public static final String ERROR = "error";


    public static String buildLogXpath(Set<String> loggerName, String method) {
        StringBuilder value = new StringBuilder();
        boolean firstTime = true;
        for (String logName : loggerName) {
            if (firstTime) {
                value.append(String.format(PATTER, logName, method));
                firstTime = false;
                continue;
            }
            value.append(" or ");
            value.append(String.format(PATTER, logName, method));
        }
        return String.format(TARGET_XPATH_FORM, value.toString());
    }

    public static String buildExceptionSuffix(String varName){
        return String.format(ERROR_SUFFIX, varName);
    }

    public static List<String> buildLogCall(Set<String> loggerName) {
        List<String> names = Lists.newArrayList();
        for (String logName : loggerName) {
            for (String method : TARGET_FUNCTION_NAME) {
                names.add(String.format(PATTER, logName, method));
            }
        }
        return names;
    }

    public static String addImageWords(String str){
        return "@Image='" + str + "'";
    }
}
