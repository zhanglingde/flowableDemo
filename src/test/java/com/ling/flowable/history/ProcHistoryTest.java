
package com.ling.flowable.history;


import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 流程历史测试
 */
@SpringBootTest
public class ProcHistoryTest {

    private static final Logger logger = LoggerFactory.getLogger(ProcHistoryTest.class);

    @Autowired
    HistoryService historyService;

    // 查询所有的流程信息
    @Test
    void test01(){
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().list();
        for (HistoricProcessInstance hpi : list) {
            Date endTime = hpi.getEndTime();
            if (endTime != null) {
                //说明这个流程已经执行完毕
                logger.info("执行完毕：name:{},startTime:{},endTime:{}", hpi.getName(), hpi.getStartTime(), hpi.getEndTime());
            }else{
                //说明这个流程尚未执行
                logger.info("未执行完毕：name:{},startTime:{},endTime:{}", hpi.getName(), hpi.getStartTime(), hpi.getEndTime());
            }
        }
    }

    /**
     * 查询未执行完毕的流程信息
     */
    @Test
    void test02() {
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery()
                .unfinished()//END_TIME_ IS NULL
                .list();
        for (HistoricProcessInstance hpi : list) {
            logger.info("name:{},startTime:{},endTime:{}", hpi.getName(), hpi.getStartTime(), hpi.getEndTime());
        }
    }

    /**
     * 查询已经完成的历史流程信息，单纯的查询流程本身的信息
     * 流程的历史表中保存的流程信息，不仅有已经完成的流程信息，也包括正在执行的流程信息
     *
     * SELECT RES.* , DEF.KEY_ as PROC_DEF_KEY_, DEF.NAME_ as PROC_DEF_NAME_, DEF.VERSION_ as PROC_DEF_VERSION_, DEF.DEPLOYMENT_ID_ as DEPLOYMENT_ID_ from ACT_HI_PROCINST RES left outer join ACT_RE_PROCDEF DEF on RES.PROC_DEF_ID_ = DEF.ID_ WHERE RES.END_TIME_ is not NULL order by RES.ID_ asc
     *
     */
    @Test
    void test03() {
        List<HistoricProcessInstance> list = historyService
                //这个表示查询流程的历史信息（单纯的只是查询流程的信息）
                .createHistoricProcessInstanceQuery()
                //查询已经完成的流程信息
                .finished()//END_TIME_ is not NULL
                .list();
        for (HistoricProcessInstance hpi : list) {
            logger.info("name:{},startTime:{},endTime:{}", hpi.getName(), hpi.getStartTime(), hpi.getEndTime());
        }
    }

}
