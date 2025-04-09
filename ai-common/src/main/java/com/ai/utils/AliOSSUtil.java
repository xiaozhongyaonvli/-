package com.ai.utils;

import com.aliyun.oss.model.PutObjectRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

@Data
@Slf4j
@AllArgsConstructor
public class AliOSSUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 上传文件
     * @param file     文件
     * @param filePath 阿里云OSS文件地址 （OSS将/作为目录划分标志）
     * @return         返回oss该文件地址
     */
    public String UpLoad(MultipartFile file, String filePath) {
        //文件访问路径规则 https://BucketName.Endpoint/filePath （OSS将/作为目录划分标志）
        String fileUrl = String.format("https://%s.%s.%s", bucketName, endpoint, filePath);
        // 最大重试次数
        int maxRetries = 3;
        // 当前已重试次数
        int attempt = 0;
        // 记录最后一次尝试失败的报错
        Exception lastException = null;
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        while (attempt < maxRetries) {
            try {
                // 创建PutObject请求。
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filePath, file.getInputStream());
                ossClient.putObject(putObjectRequest);
                log.info("文件上传到:{}", fileUrl);
                ossClient.shutdown();
                return fileUrl;
            } catch (Exception e) {
                attempt++;
                lastException = e;
                log.warn("上传失败，尝试重试{}/{}次...", attempt, maxRetries, e);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        ossClient.shutdown();
        log.error("上传失败，重试{}次后仍然失败", maxRetries, lastException);
        throw new RuntimeException("上传到OSS失败: " + lastException.getMessage(), lastException);
    }
}
