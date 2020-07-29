package com.pinyougou.shop.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;

@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String fileServerUrl;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file) {
        try {
            // 1. 获取文件全名和文件拓展名
            String filename = file.getOriginalFilename();
            String exdentname = filename.substring(filename.lastIndexOf(".")+1);
            // 2. 创建一个fastDFS客户端
            util.FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            // 3. 执行上传操作
            String url = fileServerUrl+fastDFSClient.uploadFile(file.getBytes(), exdentname);
            return new Result(true, url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上传失败!");
        }

    }

}
