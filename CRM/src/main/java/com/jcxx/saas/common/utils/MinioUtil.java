package com.jcxx.saas.common.utils;

import com.jcxx.saas.common.exception.SaaSException;
import io.minio.MinioClient;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author wll
 * @create 2022-03-04-14:52
 * 文件服务器工具类
 */
//@Component
public class MinioUtil {
    //MinIO服务所在地址
    @Value("${minio.endpoint}")
    private String endpoint;

    //存储桶名称
    @Value("${minio.bucketName}")
    private String bucketName;

    //minio账号
    @Value("${minio.accessKey}")
    private String accessKey;

    //minio密码
    @Value("${minio.secretKey}")
    private String secretKey;


    private MinioClient minioClient;

    /**
     * 初始化 MinIO 客户端
     */
    @PostConstruct
    private void init() {
        try {
            minioClient = new MinioClient(endpoint, accessKey, secretKey);
            minioClient.ignoreCertCheck();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件上传
     *
     * @param file 要上传的文件
     * @return 文件路径
     */
    public String upload(MultipartFile file) {
        if (null != file) {
            try {
                UUID uuid = UUID.randomUUID();
                StringBuilder s = new StringBuilder();
                s.append(uuid.toString().replace("-", ""));
                // bucket 不存在，创建
                if (!minioClient.bucketExists(bucketName)) {
                    minioClient.makeBucket(bucketName);
                }
                //得到文件流
                InputStream input = file.getInputStream();

                //文件名
                String fileName = s.append("_").append(file.getOriginalFilename()).toString();
                //类型
                String contentType = file.getContentType();
                //把文件放置Minio桶(文件夹)
                // 通过年份月份分类
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/");
                Date date = new Date();
                String dateFolder = format.format(date);
                fileName = dateFolder + fileName;
                minioClient.putObject(bucketName, fileName, input, contentType);
                StringBuilder fileUrl = new StringBuilder(endpoint + "/" + bucketName + "/");
                String url = fileUrl.append(fileName).toString();
                return url;
            } catch (Exception e) {
                e.printStackTrace();
                throw new SaaSException("文件minio上传失败！");
            }
        }
        return null;
    }

    /**
     * 文件下载
     *
     * @param response
     * @param url 文件路径
     */
    public void download(HttpServletResponse response, String url) {
        // 从链接中得到文件名
        String minioFileName = url.split("/" + bucketName + "/")[1];
        String fileName = minioFileName.split("_")[1];
        InputStream inputStream;
        try {
            inputStream = minioClient.getObject(bucketName, minioFileName);
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            IOUtils.copy(inputStream, response.getOutputStream());
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new SaaSException("文件minio下载失败！");
        }

    }

    /**
     * 获取文件输入流
     * @param url
     * @return
     */
    public InputStream getInputStream(String url){
        String fileName = url.split("/" + bucketName + "/")[1];
        InputStream inputStream = null;
        try {
            inputStream = minioClient.getObject(bucketName, fileName);
        } catch (Exception e) {
                e.printStackTrace();
                throw new SaaSException("获取minio文件流失败！");
        }
        return inputStream;
    }

    /**
     * 获取字节数组
     *
     * @param url
     */
    public byte[] getBytes(String url) {
        // 从链接中得到文件名
        String fileName = url.split("/" + bucketName + "/")[1];
        InputStream inputStream;
        byte[] bytes = new byte[0];
        try {
            inputStream = minioClient.getObject(bucketName, fileName);
            bytes = toByteArray(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SaaSException("minio获取文件字节数组失败！");
        }
        return bytes;
    }

    public byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }
}

