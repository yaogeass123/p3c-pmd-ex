/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.p3c.pmd.lang.java.rule.ex;

import net.sourceforge.pmd.testframework.SimpleAggregatorTst;

/**
 * Test for other java rules.
 *
 * @author keriezhang
 * @date 2017/06/18
 */
public class ExRulesTest extends SimpleAggregatorTst {

    private static final String RULESET = "java-ali-ex";

    @Override
    public void setUp() {
//        addRule(RULESET, "LogErrorRule");
//        addRule(RULESET, "TransactionInvalidRule");
//        addRule(RULESET, "OptimisticUpdateRule");
//        addRule(RULESET, "PojoVariableNameRule");
//        addRule(RULESET, "EnumNameRule");
//        addRule(RULESET, "LogOutputStringRule");
//        addRule(RULESET, "LogExceptionRule");
        addRule(RULESET, "CatchRuntimeExceptionRule");
    }


}
