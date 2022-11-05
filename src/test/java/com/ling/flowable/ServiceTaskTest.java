
package com.ling.flowable;


import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 用户任务测试
 */
@SpringBootTest
public class ServiceTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(ServiceTaskTest.class);

    @Autowired
    RuntimeService runtimeService;
    @Autowired
    TaskService taskService;
    @Autowired
    IdentityService identityService;


    /**
     * 通过 key 启动流程
     */
    @Test
    void test01() {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("ScriptTaskKey");
        logger.info("id:{},name:{}", pi.getId(), pi.getName());
    }

}
