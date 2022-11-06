
package com.ling.flowable.history;


import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


/**
 * 变量历史测试
 */
@SpringBootTest
public class VariableHistoryTest {

    private static final Logger logger = LoggerFactory.getLogger(VariableHistoryTest.class);

    @Autowired
    HistoryService historyService;

    /**
     * 查询已经完成的流程实例的所有历史变量
     *
     * SELECT RES.* from ACT_HI_VARINST RES WHERE RES.PROC_INST_ID_ = ? order by RES.ID_ asc
     *
     */
    @Test
    void test12() {
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().finished().list();
        for (HistoricProcessInstance hpi : list) {
            List<HistoricVariableInstance> variableInstances = historyService.createHistoricVariableInstanceQuery().processInstanceId(hpi.getId()).list();
            for (HistoricVariableInstance hvi : variableInstances) {
                logger.info("variableName:{},createTime:{},variableTypeName:{},value:{}",hvi.getVariableName(),hvi.getCreateTime(),hvi.getVariableTypeName(),hvi.getValue());
            }
        }
    }

    /**
     * 查询某一个流程的最终审批结果变量，即 state 的最终值
     *
     * SELECT RES.* from ACT_HI_VARINST RES WHERE RES.PROC_INST_ID_ = ? and RES.NAME_ = ? order by RES.ID_ asc
     *
     */
    @Test
    void test13() {
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().finished().list();
        for (HistoricProcessInstance hpi : list) {
            List<HistoricVariableInstance> variableInstances = historyService.createHistoricVariableInstanceQuery().processInstanceId(hpi.getId()).variableName("state").list();
            for (HistoricVariableInstance hvi : variableInstances) {
                logger.info("variableName:{},createTime:{},variableTypeName:{},value:{}",hvi.getVariableName(),hvi.getCreateTime(),hvi.getVariableTypeName(),hvi.getValue());
            }
        }
    }

}
