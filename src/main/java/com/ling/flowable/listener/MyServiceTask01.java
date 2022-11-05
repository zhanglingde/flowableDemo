package com.ling.flowable.listener;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * 服务任务监听类，这个类也就是 ServiceTask 执行到这里的时候，会自动执行该类中的 execute 方法
 */
public class MyServiceTask01 implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("=============MyServiceTask01=============");
    }
}