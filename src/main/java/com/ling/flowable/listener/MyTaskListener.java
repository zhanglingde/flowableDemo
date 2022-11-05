package com.ling.flowable.listener;

import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;

public class MyTaskListener implements TaskListener {
    /**
     * 当任务被创建的时候，这个方法会被触发
     * @param delegateTask
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        // 可以在这里设置任务的处理人
        delegateTask.setAssignee("zhangling");
    }
}