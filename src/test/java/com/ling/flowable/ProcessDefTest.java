package com.ling.flowable;


import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


/**
 * 流程定义测试
 */
@SpringBootTest
public class ProcessDefTest {

    @Autowired
    RepositoryService repositoryService;

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefTest.class);


    @Test
    void test01() {
        //查询所有定义的流程
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();
        for (ProcessDefinition pd : list) {
            logger.info("id:{},name:{},version:{},category:{}",pd.getId(),pd.getName(),pd.getVersion(),pd.getCategory());
        }
    }

    @Test
    void test02() {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                //设置查询流程定义的最新版本
                .latestVersion()
                .list();
        for (ProcessDefinition pd : list) {
            logger.info("id:{},name:{},version:{},category:{}",pd.getId(),pd.getName(),pd.getVersion(),pd.getCategory());
        }
    }

    @Test
    void test03() {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                //根据流程定义的 XML 文件中的 id 去查询一个流程，注意，XML 文件中的 id 对应 ACT_RE_PROCDEF 表中的 KEY_
                .processDefinitionKey("baoxiao")
                //按照版本号排序
                .orderByProcessDefinitionVersion()
                .desc()
                .list();
        for (ProcessDefinition pd : list) {
            logger.info("id:{},name:{},version:{},category:{}",pd.getId(),pd.getName(),pd.getVersion(),pd.getCategory());
        }
    }

    /**
     * 根据流程定义的 key 去查询（自定义 sql 查询）
     */
    @Test
    void test04() {
        List<ProcessDefinition> list = repositoryService.createNativeProcessDefinitionQuery()
                .sql("SELECT RES.* from ACT_RE_PROCDEF RES WHERE RES.KEY_ = #{key} order by RES.VERSION_ asc")
                .parameter("key","baoxiao")
                .list();
        for (ProcessDefinition pd : list) {
            logger.info("id:{},name:{},version:{},category:{}",pd.getId(),pd.getName(),pd.getVersion(),pd.getCategory());
        }
    }


    /**
     * 查询流程部署信息，本质上就是查询 ACT_RE_DEPLOYMENT 表
     *
     */
    @Test
    void test05() {
        List<Deployment> list = repositoryService.createDeploymentQuery().list();
        for (Deployment d : list) {
            logger.info("id:{},category:{},,name:{},key:{}",d.getId(),d.getCategory(),d.getName(),d.getKey());
        }
    }

    /**
     * 根据流程部署的分类，去查询流程部署信息
     */
    @Test
    void test06() {
        List<Deployment> list = repositoryService.createDeploymentQuery()
                //根据流程部署的分类去查询
                .deploymentCategory("流程分类")
                .list();
        for (Deployment d : list) {
            logger.info("id:{},category:{},,name:{},key:{}",d.getId(),d.getCategory(),d.getName(),d.getKey());
        }
    }

    /**
     * 根据流程部署的 ID 去查询流程定义
     */
    @Test
    void test07() {
        List<Deployment> list = repositoryService.createDeploymentQuery().list();
        for (Deployment d : list) {
            List<ProcessDefinition> list1 = repositoryService.createProcessDefinitionQuery().deploymentId(d.getId()).list();
            for (ProcessDefinition pd : list1) {
                logger.info("id:{},name:{},version:{},category:{}", pd.getId(), pd.getName(), pd.getVersion(), pd.getCategory());
            }
        }
    }

    /**
     * 自定义流程部署的查询 SQL
     */
    @Test
    void test08() {
        List<Deployment> list = repositoryService.createNativeDeploymentQuery()
                .sql("SELECT RES.* from ACT_RE_DEPLOYMENT RES WHERE RES.CATEGORY_ = #{category} order by RES.ID_ asc")
                .parameter("category","流程分类")
                .list();
        for (Deployment d : list) {
            logger.info("id:{},category:{},,name:{},key:{}",d.getId(),d.getCategory(),d.getName(),d.getKey());
        }
    }

    /**
     * 删除流程部署（三个相关的表都会被删除）
     */
    @Test
    void test09() {
        List<Deployment> list = repositoryService.createDeploymentQuery().list();
        for (Deployment deployment : list) {
            repositoryService.deleteDeployment(deployment.getId());
        }
    }

    /**
     * 查看一个已经定义好的流程，是否是一个挂起状态
     *
     * 挂起的流程定义，是无法开启一个流程实例的
     *
     */
    @Test
    void test10() {
        //查询所有的流程定义
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();
        for (ProcessDefinition pd : list) {
            //根据流程定义的 id 判断一个流程定义是否挂起
            boolean processDefinitionSuspended = repositoryService.isProcessDefinitionSuspended(pd.getId());
            if (processDefinitionSuspended) {
                logger.info("流程定义 {} 已经挂起", pd.getId());
            }else {
                logger.info("流程定义 {} 没有挂起", pd.getId());
            }
        }
    }

    /**
     * 挂起一个流程定义
     *
     * 挂起的流程定义，是无法启动一个流程实例的
     *
     * 执行的 SQL 如下：
     *
     *
     : ==>  Preparing: update ACT_RE_PROCDEF SET REV_ = ?, SUSPENSION_STATE_ = ? where ID_ = ? and REV_ = ?
     : ==> Parameters: 2(Integer), 2(Integer), leave:1:48375905-43e2-11ed-ba47-acde48001122(String), 1(Integer)
     : <==    Updates: 1
     所以，挂起一个流程定义，本质上，其实就是修改 ACT_RE_PROCDEF 表中，对应的记录的 SUSPENSION_STATE_ 字段的状态值为 2
     *
     */
    @Test
    void test11() {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();
        for (ProcessDefinition pd : list) {
            //根据流程定义的 id 挂起一个流程定义
            repositoryService.suspendProcessDefinitionById(pd.getId());
            logger.info("{} 流程定义已经挂起",pd.getId());
        }
    }

    /**
     * 激活一个已经挂起的流程定义
     *
     * : ==>  Preparing: update ACT_RE_PROCDEF SET REV_ = ?, SUSPENSION_STATE_ = ? where ID_ = ? and REV_ = ?
     * : ==> Parameters: 3(Integer), 1(Integer), leave:1:48375905-43e2-11ed-ba47-acde48001122(String), 2(Integer)
     * : <==    Updates: 1
     *
     * 激活一个流程定义，本质上，其实就是将 ACT_RE_PROCDEF 表中相应记录的 SUSPENSION_STATE_ 字段的值改为 1
     *
     */
    @Test
    void test12() {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();
        for (ProcessDefinition pd : list) {
            repositoryService.activateProcessDefinitionById(pd.getId());
            logger.info("{} 流程定义已经被激活", pd.getId());
        }
    }
}
