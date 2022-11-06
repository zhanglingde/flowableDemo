
package com.ling.flowable;


import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
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
 * 网关测试
 */
@SpringBootTest
public class GatewayTest {

    private static final Logger logger = LoggerFactory.getLogger(GatewayTest.class);

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
        Map<String, Object> vars = new HashMap<>();
        vars.put("days", 1);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("Gateway2Key", vars);
        logger.info("id:{},name:{}", pi.getId(), pi.getName());
    }

    @Test
    void test02() {
        // Task task = taskService.createTaskQuery().taskDefinitionKey("sid-930C13B5-E01F-4EC0-8368-EAC1C935E063").singleResult();
        // taskService.claim(task.getId(),"ling");
        // Task task1 = taskService.createTaskQuery().taskDefinitionKey("sid-0FA8902A-D463-42F5-9640-3CD6061A3CE9").singleResult();
        // taskService.claim(task1.getId(),"zhang");

        Task lingTask = taskService.createTaskQuery().taskAssignee("ling").singleResult();
        taskService.complete(lingTask.getId());
        Task zhangTask = taskService.createTaskQuery().taskAssignee("zhang").singleResult();
        taskService.complete(zhangTask.getId());
    }


}
