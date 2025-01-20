package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import com.sky.utils.FileUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Api(tags = "通用相关接口")
@RequestMapping("/admin/common")
@RestController
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private FileUtils fileUtils;

    /**
     * 文件上传 --使用阿里云oss来做文件存储
     *
     * @param file
     * @return
     */
    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + suffix;
        String url;
        try {
            url = fileUtils.upload(file);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("文件上传失败！！！{}", e.getMessage());
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
        return Result.success(url);
    }

}
