
package com.ling.flowable;


import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.idm.engine.impl.persistence.entity.GroupEntityImpl;
import org.flowable.idm.engine.impl.persistence.entity.UserEntityImpl;
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
public class UserTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(UserTaskTest.class);

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
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("SingleUserTaskKey");
        logger.info("id:{},name:{}", pi.getId(), pi.getName());
    }

    /**
     * 查询某一个用户需要处理的任务
     */
    @Test
    void test02() {
        List<Task> list = taskService.createTaskQuery().taskAssignee("zhangsan").list();
        for (Task task : list) {
            logger.info("name:{},assignee:{}", task.getName(), task.getAssignee());
            // 1. 查询到任务后，可以委派给其他人
            // taskService.setAssignee(task.getId(),"zhangsan");
            // 2. 自己处理
            taskService.complete(task.getId());
        }
    }


    /**
     * 启动流程的时候，通过变量设置委托人
     */
    @Test
    void test03() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("manager", "ling");
        //在流程启动的时候，通过变量去指定任务的处理人
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("SingleUserTaskKey", vars);
        logger.info("id:{},name:{}", pi.getId(), pi.getName());
    }

    /**
     * 启动一个流程，并设置流程的发起人
     */
    @Test
    void test06() {
        // 设置流程当前的处理人（一会流程启动的时候，会将这里设置的作为流程的发起人）
        Authentication.setAuthenticatedUserId("zhangsan");
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("SingleUserTaskKey");
        logger.info("id:{},name:{}", pi.getId(), pi.getName());
    }

    /**
     * 第二种流程发起人的设置方式
     */
    @Test
    void test07() {
        identityService.setAuthenticatedUserId("lisi");
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("SingleUserTaskKey");
        logger.info("id:{},name:{}", pi.getId(), pi.getName());
    }

//    ===================设置多个委托人

    /**
     * 根据候选人去查询任务
     *
     * 对应的 SQL 如下：
     *
     * SELECT RES.* from ACT_RU_TASK RES WHERE RES.ASSIGNEE_ is null and exists(select LINK.ID_ from ACT_RU_IDENTITYLINK LINK
     * where LINK.TYPE_ = 'candidate' and LINK.TASK_ID_ = RES.ID_ and ( LINK.USER_ID_ = ? ) ) order by RES.ID_ asc
     *
     * 从 SQL 中 ，可以看到，任务由哪些用户来处理，主要是由 ACT_RU_IDENTITYLINK 表来维护。
     *
     */
    @Test
    void test08() {
        List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("ling").list();
        for (Task task : tasks) {
            logger.info("name:{},CreateTime:{}", task.getName(), task.getCreateTime());
        }
    }

    /**
     * 根据流程的 ID 查询流程的参与者
     *
     * 对应的 SQL：
     *
     * select * from ACT_RU_IDENTITYLINK where PROC_INST_ID_ = ?
     */
    @Test
    void test09() {

        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId("e131d1d2-5d0d-11ed-bae3-0cdd24fda526").singleResult();
        //根据流程的 ID 去查询流程的参与者
        List<IdentityLink> links = runtimeService.getIdentityLinksForProcessInstance(pi.getId());
        for (IdentityLink link : links) {
            logger.info("流程参与人：{}",link.getUserId());
        }
    }

    /**
     * 认领任务
     *
     * 这个认领，对应的 SQL：
     *
     * update ACT_RU_TASK SET REV_ = ?, ASSIGNEE_ = ?, CLAIM_TIME_ = ? where ID_= ? and REV_ = ?
     */
    @Test
    void test10() {
        List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("ling").list();
        for (Task task : tasks) {
            // 认领任务
            // taskService.claim(task.getId(), "ling");
        }

        List<Task> completedTask = taskService.createTaskQuery().taskAssignee("ling").list();
        for (Task task : completedTask) {
            // 完成任务
            taskService.complete(task.getId());
        }
    }

    // ==================候选组

    /**
     * 根据候选人去查询任务（可能 zhangsan 就是候选人，也可能 zhangsan 属于某一个或者某多个用户组，那么此时就需要先查询到 zhangsan 所属的用户组，然后再根据用户组去查询对应的任务）
     *
     * 这个查询实际上分为两步：
     *
     * 1. 执行的 SQL 如下：SELECT RES.* from ACT_ID_GROUP RES WHERE exists(select 1 from ACT_ID_MEMBERSHIP M where M.GROUP_ID_ = RES.ID_ and M.USER_ID_ = ?) order by RES.ID_ asc
     * 第一步这个 SQL 可以查询出来这个 zhangsan 所属的用户组
     *
     * 2. 执行的 SQL 如下：
     * SELECT RES.* from ACT_RU_TASK RES WHERE RES.ASSIGNEE_ is null and
     * exists(select LINK.ID_ from ACT_RU_IDENTITYLINK LINK where LINK.TYPE_ = 'candidate' and LINK.TASK_ID_ = RES.ID_ and
     * ( LINK.USER_ID_ = ? or ( LINK.GROUP_ID_ IN ( ? ) ) ) ) order by RES.ID_ asc
     *
     * 这个 SQL，本质上是查询 zhangsan 或者 zhangsan 所属的用户组的任务
     *
     */
    @Test
    void test17() {
        Task task = taskService.createTaskQuery().taskCandidateUser("zhangsan").singleResult();
        logger.info("name:{},createTime:{}", task.getName(), task.getCreateTime());
    }

    /**
     * 也可以根据候选用户组去查询一个任务
     *
     * 对应的 SQL 如下：
     *
     * SELECT RES.* from ACT_RU_TASK RES WHERE RES.ASSIGNEE_ is null and exists(select LINK.ID_ from ACT_RU_IDENTITYLINK LINK where LINK.TYPE_ = 'candidate'
     * and LINK.TASK_ID_ = RES.ID_ and ( ( LINK.GROUP_ID_ IN ( ? ) ) ) ) order by RES.ID_ asc
     *
     * 这个查询一步到位，直接指定候选组即可
     *
     */
    @Test
    void test18() {
        Task task = taskService.createTaskQuery().taskCandidateGroup("manager").singleResult();
        logger.info("name:{},createTime:{}", task.getName(), task.getCreateTime());
        taskService.claim(task.getId(),"zhangsan");
        taskService.complete(task.getId());
    }




}
