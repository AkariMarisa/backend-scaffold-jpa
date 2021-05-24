package com.codelodon.backendscaffold.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class SimpleFileServerConfig {
    final private Logger logger = LoggerFactory.getLogger(SimpleFileServerConfig.class);

    @Value("${simple_file_server.basedir}")
    private String baseDir;

    /**
     * 应用启动的时候，创建本地文件存放目录
     */
    @EventListener(ApplicationReadyEvent.class)
    public void createBaseDirectory() {
        logger.info("创建本地文件存放目录 {}", baseDir);

        Path path = Paths.get(baseDir);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            logger.error("创建基础目录失败", e);
        }

        logger.info("创建基础目录成功");
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
}
