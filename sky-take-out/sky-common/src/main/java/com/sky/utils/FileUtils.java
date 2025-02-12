package com.sky.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.UUID;

/**
 * 本地上传
 */
@Data
@Slf4j
@Component
public class FileUtils {

    @Autowired
    private AliOssUtil aliOssUtil;

    @Value("${sky.upload.isLocal}")
    private boolean isLocal;
    @Value("${sky.upload.path}")
    private String path;
    // 服务器地址
    @Value("${sky.hostAddress}")
    private String hostAddress;
    @Value("${server.port}")
    private String port;

    public static final String RESOURCE_PATH = "skyFile";

    public String upload(MultipartFile file) throws IOException {
        String res;
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID() + suffix;
        // 启用了本地上传
        if (isLocal) {
            // 文件见不存在创建文件夹
            File fold = new File(path);
            if (!fold.exists()) {
                fold.mkdirs();
            }
            String savePath = path + File.separator + File.separator + fileName;
            file.transferTo(new File(savePath));
            // 获取本地地址
            // InetAddress host = InetAddress.getLocalHost();
            // 静态资源地址
            res = "http://" + hostAddress + ":" + port + File.separator + RESOURCE_PATH + File.separator + fileName;
        } else {
            res = aliOssUtil.upload(file.getBytes(), fileName);
        }
        log.info("图片地址:{}", res);
        return res;
    }
}
