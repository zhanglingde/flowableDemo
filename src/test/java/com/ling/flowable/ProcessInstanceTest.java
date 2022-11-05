package com.ling.flowable;

import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 流程实例测试
 */
@SpringBootTest
public class ProcessInstanceTest {

    //跟流程运行相关的操作，都在这个 Bean 中，在 Spring Boot 中，该 Bean 已经配置好，可以直接使用
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    TaskService taskService;
    @Autowired
    RepositoryService repositoryService;

    private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceTest.class);

    /**
     * 启动流程
     *
     * ACT_RU_EXECUTION 表保存了所有流程执行实例的信息，包括
     */
    @Test
    void test01() {
        //设置流程的发起人
        Authentication.setAuthenticatedUserId("wangwu");
        //这个流程定义的 key 实际上就是流程 XML 文件中的流程 id
        String processDefinitionKey = "dataObject_key";
        //流程启动成功之后，可以获取到一个流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(processDefinitionKey);
        logger.info("definitionId:{},id:{},name:{}", pi.getProcessDefinitionId(), pi.getId(), pi.getName());
        //也可以通过流程定义的 id 去启动一个流程，但是需要注意，流程定义的 id 需要自己去查询，这个 id 并不是 XML 文件中定义的流程 ID
//        String processDefinitionId = "";
//        runtimeService.startProcessInstanceById(processDefinitionId);
    }

    /**
     * 查询 wangwu 需要执行的任务，并处理
     *
     * 查询 wangwu 需要处理的任务，对应的 SQL：
     *
     * : ==>  Preparing: SELECT RES.* from ACT_RU_TASK RES WHERE RES.ASSIGNEE_ = ? order by RES.ID_ asc
     * : ==> Parameters: wangwu(String)
     * : <==      Total: 1
     *
     * wangwu 去处理任务：
     *
     * 首先去 ACT_RU_TASK 表中添加一条记录，这个新的记录，就是主管审批。
     * 然后从 ACT_RU_TASK 表中删除掉之前的需要 wangwu 完成的记录。
     *
     * 上面这两个操作是在同一个事务中完成的。
     *
     */
    @Test
    void test03() {
        List<Task> list = taskService.createTaskQuery().taskAssignee("wangwu").list();
        for (Task task : list) {
            logger.info("id:{},assignee:{},name:{}",task.getId(),task.getAssignee(),task.getName());
            //王五完成自己的任务
            taskService.complete(task.getId());
        }
    }

    /**
     * 查询一个流程是否执行结束
     * 如果 ACT_RU_EXECUTION 表中，由于关于这个流程的记录，说明这个流程还在执行中
     * 如果 ACT_RU_EXECUTION 表中，没有关于这个流程的记录，说明这个流程已经执行结束了
     *
     * 注意，虽然我们是去 ACT_RU_EXECUTION 表中查询，且该表中同一个流程实例 ID 对应了多条记录，但是大家注意，这里查询到的其实还是一个流程实例。
     *
     */
    @Test
    void test04() {
        String processId = "daa62a1c-5c5d-11ed-9fae-0cdd24fda526";
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        if (pi == null) {
            logger.info("流程执行结束");
        }else{
            logger.info("流程正在执行中");
        }
    }

    /**
     * 查看运行活动节点，本质上其实就是查看 ACT_RU_EXECUTION 表
     */
    @Test
    void test05() {
        List<Execution> executions = runtimeService.createExecutionQuery().list();
        for (Execution execution : executions) {
            //查询某一个执行实例的活动节点
            List<String> activeActivityIds = runtimeService.getActiveActivityIds(execution.getId());
            for (String activeActivityId : activeActivityIds) {
                //这里拿到的其实就是 ACT_RU_EXECUTION 表中的 ACT_ID_ 字段
                logger.info("activeActivityId:{}", activeActivityId);
            }
        }
    }

    /**
     *
     * 删除一个正在执行的流程，注意这个只会删除正在执行的流程实例信息，并不会删除历史流程信息（历史信息被更新）。
     *
     * : ==>  Preparing: delete from ACT_RU_VARIABLE where EXECUTION_ID_ = ?
     * : ==> Parameters: 87df7272-3cad-11ed-9026-acde48001122(String)
     * : <==    Updates: 1
     * : ==>  Preparing: delete from ACT_RU_IDENTITYLINK where PROC_INST_ID_ = ?
     * : ==> Parameters: 87df7272-3cad-11ed-9026-acde48001122(String)
     * : <==    Updates: 2
     * : ==>  Preparing: delete from ACT_RU_TASK where ID_ = ? and REV_ = ?
     * : ==> Parameters: 87e4c9a9-3cad-11ed-9026-acde48001122(String), 1(Integer)
     * : <==    Updates: 1
     * : ==>  Preparing: delete from ACT_RU_TASK where EXECUTION_ID_ = ?
     * : ==> Parameters: 87df7272-3cad-11ed-9026-acde48001122(String)
     * : <==    Updates: 0
     * : ==>  Preparing: delete from ACT_RU_ACTINST where PROC_INST_ID_ = ?
     * : ==> Parameters: 87df7272-3cad-11ed-9026-acde48001122(String)
     * : <==    Updates: 3
     * : ==>  Preparing: delete from ACT_RU_ACTINST where PROC_INST_ID_ = ?
     * : ==> Parameters: 87df7272-3cad-11ed-9026-acde48001122(String)
     * : <==    Updates: 0
     * : ==>  Preparing: delete from ACT_RU_EXECUTION where ID_ = ? and REV_ = ?
     * : ==> Parameters: 87e035c5-3cad-11ed-9026-acde48001122(String), 2(Integer)
     * : <==    Updates: 1
     * : ==>  Preparing: delete from ACT_RU_EXECUTION where ID_ = ? and REV_ = ?
     * : ==> Parameters: 87df7272-3cad-11ed-9026-acde48001122(String), 2(Integer)
     * : <==    Updates: 1
     */
    @Test
    void test06() {
        String processInstanceId = "daa62a1c-5c5d-11ed-9fae-0cdd24fda526";
        String deleteReason = "想删除了";
        runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
    }

    /**
     * 挂起一个流程实例
     * <p>
     * 对于一个挂起的流程实例，我们是无法执行相应的 Task 的
     *
     * 流程实例的挂起，最终也会挂起流程定义
     *
     * 一个被挂起的流程实例：
     * 1。 流程实例本身被挂起
     * 2。 流程的 Task 被挂起。
     *
     * : ==>  Preparing: update ACT_RU_EXECUTION SET REV_ = ?, SUSPENSION_STATE_ = ? where ID_ = ? and REV_ = ?
     * : ==> Parameters: 2(Integer), 2(Integer), dd7000f0-43e5-11ed-bbdc-acde48001122(String), 1(Integer)
     * : <==    Updates: 1
     * : updating: Execution[ id 'dd709d33-43e5-11ed-bbdc-acde48001122' ] - activity 'sid-2F900F54-E047-40AC-A09C-71181386A6C1' - parent 'dd7000f0-43e5-11ed-bbdc-acde48001122
     * : ==>  Preparing: update ACT_RU_EXECUTION SET REV_ = ?, SUSPENSION_STATE_ = ? where ID_ = ? and REV_ = ?
     * : ==> Parameters: 2(Integer), 2(Integer), dd709d33-43e5-11ed-bbdc-acde48001122(String), 1(Integer)
     * : <==    Updates: 1
     * : updating: Task[id=dd746dc7-43e5-11ed-bbdc-acde48001122, name=提交请假申请]
     * : ==>  Preparing: update ACT_RU_TASK SET REV_ = ?, SUSPENSION_STATE_ = ? where ID_= ? and REV_ = ?
     * : ==> Parameters: 2(Integer), 2(Integer), dd746dc7-43e5-11ed-bbdc-acde48001122(String), 1(Integer)
     * : <==    Updates: 1
     * : updating: ProcessDefinitionEntity[leave:1:cc46d851-43e5-11ed-bdc3-acde48001122]
     * : ==>  Preparing: update ACT_RE_PROCDEF SET REV_ = ?, SUSPENSION_STATE_ = ? where ID_ = ? and REV_ = ?
     * : ==> Parameters: 2(Integer), 2(Integer), leave:1:cc46d851-43e5-11ed-bdc3-acde48001122(String), 1(Integer)
     * : <==    Updates: 1
     *
     * 流程实例的挂起，一共涉及到三张表，分别是 ACT_RU_EXECUTION（流程实例被挂起）、ACT_RU_TASK（流程的 Task 被挂起） 以及 ACT_RE_PROCDEF（流程定义被挂起）
     *
     */
    @Test
    void test07() {
        //查询所有的流程定义
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();
        for (ProcessDefinition pd : list) {
            //1. 流程定义的 ID
            //2. 是否挂起这个流程定义所对应的流程实例
            //3。这是挂起的时间，null 表示立即挂起，也可以给一个具体的时间，表示到期之后才会被挂起。
            repositoryService.suspendProcessDefinitionById(pd.getId(), true, null);
        }
    }

    /**
     * 激活一个挂起的流程实例
     *
     * 激活就是挂起的一个反向操作：
     *
     * 激活，也会涉及到三张表，分别是：ACT_RU_EXECUTION、ACT_RU_TASK 以及 ACT_RE_PROCDEF
     *
     * 挂起是将这三张表中的 SUSPENSION_STATE_ 字段，由 1 改为 2
     * 激活就是将这三张表中的 SUSPENSION_STATE_ 由 2 改为 1
     *
     *
     : ==>  Preparing: update ACT_RE_PROCDEF SET REV_ = ?, SUSPENSION_STATE_ = ? where ID_ = ? and REV_ = ?
     : ==> Parameters: 3(Integer), 1(Integer), leave:1:cc46d851-43e5-11ed-bdc3-acde48001122(String), 2(Integer)
     : <==    Updates: 1
     : updating: ProcessInstance[dd7000f0-43e5-11ed-bbdc-acde48001122]
     : ==>  Preparing: update ACT_RU_EXECUTION SET REV_ = ?, SUSPENSION_STATE_ = ? where ID_ = ? and REV_ = ?
     : ==> Parameters: 3(Integer), 1(Integer), dd7000f0-43e5-11ed-bbdc-acde48001122(String), 2(Integer)
     : <==    Updates: 1
     : updating: Execution[ id 'dd709d33-43e5-11ed-bbdc-acde48001122' ] - activity 'sid-2F900F54-E047-40AC-A09C-71181386A6C1' - parent 'dd7000f0-43e5-11ed-bbdc-acde48001122'
     : ==>  Preparing: update ACT_RU_EXECUTION SET REV_ = ?, SUSPENSION_STATE_ = ? where ID_ = ? and REV_ = ?
     : ==> Parameters: 3(Integer), 1(Integer), dd709d33-43e5-11ed-bbdc-acde48001122(String), 2(Integer)
     : <==    Updates: 1
     : updating: Task[id=dd746dc7-43e5-11ed-bbdc-acde48001122, name=提交请假申请]
     : ==>  Preparing: update ACT_RU_TASK SET REV_ = ?, SUSPENSION_STATE_ = ? where ID_= ? and REV_ = ?
     : ==> Parameters: 3(Integer), 1(Integer), dd746dc7-43e5-11ed-bbdc-acde48001122(String), 2(Integer)
     : <==    Updates: 1
     *
     */
    @Test
    void test08() {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();
        for (ProcessDefinition pd : list) {
            repositoryService.activateProcessDefinitionById(pd.getId(), true, null);
        }
    }
}
