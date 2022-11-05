
package com.ling.flowable;


import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


/**
 * 接收任务测试
 */
@SpringBootTest
public class ReceiveTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveTaskTest.class);

    @Autowired
    RuntimeService runtimeService;

    /**
     * 通过 key 启动流程
     */
    @Test
    void test01() {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("ReceivableTaskKey");
        logger.info("id:{},name:{}", pi.getId(), pi.getName());
    }

    /**
     * 触发使接收任务通过
     */
    @Test
    void test02() {
        // activityId 表示当前实例执行到哪个节点上
        List<Execution> list = runtimeService.createExecutionQuery().activityId("sid-4AF01ECA-6224-4079-8F74-82553216C0E1").list();
        for (Execution execution : list) {
            // 触发一个 ReceiveTask 继续向下执行，但是这里需要的参数是一个
            runtimeService.trigger(execution.getId());
        }
    }


}
