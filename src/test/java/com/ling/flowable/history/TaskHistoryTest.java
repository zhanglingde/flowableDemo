
package com.ling.flowable.history;


import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;


/**
 * 任务历史测试
 */
@SpringBootTest
public class TaskHistoryTest {

    private static final Logger logger = LoggerFactory.getLogger(TaskHistoryTest.class);

    @Autowired
    HistoryService historyService;

    /**
     * 查询历史任务
     * <p>
     * 查询 zhangsan 已经完成的任务
     */
    @Test
    void test01() {
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee("zhangsan")
                .finished().list();
        for (HistoricTaskInstance hti : list) {
            logger.info("name:{},createTime:{},endTime:{},assignee:{}", hti.getName(), hti.getCreateTime(), hti.getEndTime(), hti.getAssignee());
        }
    }

    /**
     * 查询所有已完成的任务
     */
    @Test
    void test08() {
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().finished().list();
        for (HistoricTaskInstance hti : list) {
            logger.info("name:{},createTime:{},endTime:{},assignee:{}", hti.getName(), hti.getCreateTime(), hti.getEndTime(), hti.getAssignee());
        }
    }

    /**
     * 查询某一个流程实例所有完成的任务
     */
    @Test
    void test09() {
        // 查询所有已经完成的流程实例
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().finished().list();
        for (HistoricProcessInstance hpi : list) {
            List<HistoricTaskInstance> taskInstances = historyService.createHistoricTaskInstanceQuery().processInstanceId(hpi.getId()).finished().list();
            for (HistoricTaskInstance hti : taskInstances) {
                logger.info("name:{},createTime:{},endTime:{},assignee:{}", hti.getName(), hti.getCreateTime(), hti.getEndTime(), hti.getAssignee());
            }
        }
    }

}
