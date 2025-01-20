package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里oss配置类，用来生成AliOssUtil对象，并交由IOC容器管理
 */
@Slf4j
@Configuration
public class OssConfiguration {

    @Bean
    public AliOssUtil aliOssUtil(AliOssProperties properties) {
        AliOssUtil aliOssUtil = new AliOssUtil(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret(),
                properties.getBucketName());
        return aliOssUtil;
    }

}
