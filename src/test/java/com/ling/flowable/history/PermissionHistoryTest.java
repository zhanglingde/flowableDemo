
package com.ling.flowable.history;


import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


/**
 * 权限历史信息
 */
@SpringBootTest
public class PermissionHistoryTest {

    private static final Logger logger = LoggerFactory.getLogger(PermissionHistoryTest.class);

    @Autowired
    HistoryService historyService;

    /**
     * 查询某一个流程的处理人
     * <p>
     * select * from ACT_HI_IDENTITYLINK where PROC_INST_ID_ = ?
     */
    @Test
    void test15() {
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().finished().list();
        for (HistoricProcessInstance hpi : list) {
            List<HistoricIdentityLink> links = historyService.getHistoricIdentityLinksForProcessInstance(hpi.getId());
            for (HistoricIdentityLink link : links) {
                logger.info("userId:{}", link.getUserId());
            }
        }
    }

    /**
     * 查询某一个任务的处理人
     *
     * select * from ACT_HI_IDENTITYLINK where TASK_ID_ = ?
     *
     */
    @Test
    void test16() {
        String taskName = "组长审批";
        HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery().taskName(taskName).singleResult();
        List<HistoricIdentityLink> links = historyService.getHistoricIdentityLinksForTask(task.getId());
        for (HistoricIdentityLink link : links) {
            logger.info("{} 任务的执行人是 {}", task.getName(), link.getUserId());
        }
    }

}
