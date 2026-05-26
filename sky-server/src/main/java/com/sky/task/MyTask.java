package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 自定义定时任务
 */
@Component
@Slf4j
public class MyTask {

    /**
     * 定时任务1
     */
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void taskOne() {
        log.info("定时任务1开始执行,{}", new Date());
    }
}
