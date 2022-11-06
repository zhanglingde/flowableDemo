
package com.ling.flowable.history;


import org.flowable.common.engine.api.history.HistoricData;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.ProcessInstanceHistoryLog;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


/**
 * 日志历史信息
 */
@SpringBootTest
public class LogHistoryTest {

    private static final Logger logger = LoggerFactory.getLogger(LogHistoryTest.class);

    @Autowired
    HistoryService historyService;

    @Test
    void test14() {
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().finished().list();
        for (HistoricProcessInstance pi : list) {
            ProcessInstanceHistoryLog pihl = historyService
                    //select RES.*, DEF.KEY_ as PROC_DEF_KEY_, DEF.NAME_ as PROC_DEF_NAME_, DEF.VERSION_ as PROC_DEF_VERSION_, DEF.DEPLOYMENT_ID_ as DEPLOYMENT_ID_ from ACT_HI_PROCINST RES left outer join ACT_RE_PROCDEF DEF on RES.PROC_DEF_ID_ = DEF.ID_ where PROC_INST_ID_ = ?
                    .createProcessInstanceHistoryLogQuery(pi.getId())
                    //增加查询历史任务信息
                    //SELECT RES.* from ACT_HI_TASKINST RES WHERE RES.PROC_INST_ID_ = ? order by RES.ID_ asc
                    .includeTasks()
                    //SELECT RES.* from ACT_HI_VARINST RES WHERE RES.PROC_INST_ID_ = ? order by RES.ID_ asc
                    .includeVariables()
                    //SELECT RES.* from ACT_HI_ACTINST RES WHERE RES.PROC_INST_ID_ = ? order by RES.ID_ asc
                    .includeActivities()
                    .singleResult();
            //打印历史流程信息
            logger.info("id:{},startTime:{},endTime:{},startUserId:{}",pihl.getId(),pihl.getStartTime(),pihl.getEndTime(),pihl.getStartUserId());
            List<HistoricData> historicData = pihl.getHistoricData();
            for (HistoricData data : historicData) {
                if (data instanceof HistoricTaskInstance) {
                    HistoricTaskInstance hti = (HistoricTaskInstance) data;
                    //说明这是历史任务信息
                    logger.info("name:{},createTime:{},endTime:{},assignee:{}", hti.getName(), hti.getCreateTime(), hti.getEndTime(), hti.getAssignee());
                } else if (data instanceof HistoricActivityInstance) {
                    HistoricActivityInstance hai = (HistoricActivityInstance) data;
                    logger.info("activityName:{},startTime:{},endTime:{},assignee:{},activityType:{}", hai.getActivityName(), hai.getStartTime(), hai.getEndTime(), hai.getAssignee(), hai.getActivityType());
                } else if (data instanceof HistoricVariableInstance) {
                    HistoricVariableInstance hvi = (HistoricVariableInstance) data;
                    logger.info("variableName:{},createTime:{},variableTypeName:{},value:{}", hvi.getVariableName(), hvi.getCreateTime(), hvi.getVariableTypeName(), hvi.getValue());
                }
            }
        }
    }

}
