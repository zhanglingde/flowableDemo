package com.ling.flowable;

import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.DataObject;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 流程实例测试
 */
@SpringBootTest
public class DataObjectTest {

    //跟流程运行相关的操作，都在这个 Bean 中，在 Spring Boot 中，该 Bean 已经配置好，可以直接使用
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    TaskService taskService;
    @Autowired
    RepositoryService repositoryService;

    private static final Logger logger = LoggerFactory.getLogger(DataObjectTest.class);

    /**
     * 查询一个流程的所有 DataObject 对象
     */
    @Test
    void test10() {
        // 查询运行时的流程
        List<Execution> list = runtimeService.createExecutionQuery().list();
        for (Execution execution : list) {
            // 查询执行实例中的 DataObject
            Map<String, DataObject> dataObjects = runtimeService.getDataObjects(execution.getId());
            Set<String> keySet = dataObjects.keySet();
            for (String key : keySet) {
                DataObject data = dataObjects.get(key);
                logger.info("id:{},name:{},value:{},type:{}",data.getId(),data.getName(),data.getValue(),data.getType());
            }
        }
    }
    /**
     * 流程执行实例，在一个流程中，查询 DataObject 的数据
     *
     * : ==>  Preparing: select * from ACT_RU_VARIABLE WHERE EXECUTION_ID_ = ? AND TASK_ID_ is null AND NAME_ = ?
     * : ==> Parameters: 0e557214-43f6-11ed-a596-acde48001122(String), 流程作者网站(String)
     * : <==      Total: 1
     *
     * 查询某一个具体的 dataObject 对象
     *
     */
    @Test
    void test09() {
        DataObject data = runtimeService.getDataObject("a1bfca6b-5ccf-11ed-a64a-0cdd24fda526", "请假人");
        logger.info("id:{},name:{},value:{},type:{}",data.getId(),data.getName(),data.getValue(),data.getType());
    }
}
