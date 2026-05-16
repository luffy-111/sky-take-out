package com.sky.service.impl;

import com.sky.properties.FileUploadProperties;
import com.sky.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private FileUploadProperties fileUploadProperties;

    @Override
    public String upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        File dir = new File(fileUploadProperties.getUploadPath());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File target = new File(dir, fileName);
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }

        return "/files/" + fileName;
    }
}
