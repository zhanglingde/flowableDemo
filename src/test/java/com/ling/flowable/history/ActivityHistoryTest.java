
package com.ling.flowable.history;


import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


/**
 * 活动历史测试
 */
@SpringBootTest
public class ActivityHistoryTest {

    private static final Logger logger = LoggerFactory.getLogger(ActivityHistoryTest.class);

    @Autowired
    HistoryService historyService;

    /**
     * 查询已经完成的流程的活动信息
     * <p>
     * 这个查询对应的表是 ACT_HI_ACTINST
     *
     * SELECT RES.* from ACT_HI_ACTINST RES WHERE RES.PROC_INST_ID_ = ? order by START_TIME_ asc
     */
    @Test
    void test01() {
        List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery().finished().list();
        for (HistoricProcessInstance pi : processInstances) {
            List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                    //按照流程实例 id 去查询
                    .processInstanceId(pi.getId())
                    //按照活动的开始时间排序
                    .orderByHistoricActivityInstanceStartTime()
                    .asc()
                    .list();
            for (HistoricActivityInstance hai : list) {
                logger.info("activityName:{},startTime:{},endTime:{},assignee:{},activityType:{}", hai.getActivityName(), hai.getStartTime(), hai.getEndTime(), hai.getAssignee(), hai.getActivityType());
            }
        }
    }

    /**
     * 查询所有已经完成的 startEvent 活动
     */
    @Test
    void test02() {
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                //按照类型去查找活动
                .activityType("startEvent")
                .finished().list();
        for (HistoricActivityInstance hai : list) {
            logger.info("activityName:{},startTime:{},endTime:{},assignee:{},activityType:{}", hai.getActivityName(), hai.getStartTime(), hai.getEndTime(), hai.getAssignee(), hai.getActivityType());
        }
    }

}
