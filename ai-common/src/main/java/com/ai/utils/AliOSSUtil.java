package com.ai.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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
     * @param file 文件
     * @param fileName 文件名
     * @return  返回oss该文件地址
     */
    public String UpLoad(MultipartFile file, String fileName){
        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        String fileUrl = String.format("https://{%s}.{%s}.{%s}",bucketName,endpoint,fileName);
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 创建PutObject请求。
            byte[] fileBytes = file.getBytes();
            ossClient.putObject(bucketName, fileName, new ByteArrayInputStream(fileBytes));
            log.info("文件上传到:{}", fileUrl);

            return fileUrl;
        } catch (OSSException oe) {
            log.error("OSSException: 请求到达 OSS，但被拒绝。ErrorCode={}, ErrorMessage={}", oe.getErrorCode(), oe.getErrorMessage());
            throw new RuntimeException("上传文件到 OSS 失败: " + oe.getErrorMessage(), oe);
        } catch (ClientException ce) {
            log.error("ClientException: 客户端请求 OSS 失败，可能是网络异常。ErrorMessage={}", ce.getMessage());
            throw new RuntimeException("上传文件到 OSS 失败: 网络异常", ce);
        } catch (IOException e) {
            log.error("IOException: 读取文件数据失败", e);
            throw new RuntimeException("读取文件数据失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

}
