package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置
 */
@Component
@ConfigurationProperties(prefix = "sky.file")
@Data
public class FileUploadProperties {

    private String uploadPath;
}
