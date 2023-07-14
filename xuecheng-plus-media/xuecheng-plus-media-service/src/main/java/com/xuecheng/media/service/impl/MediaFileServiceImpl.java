package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.MimeType;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFileService currentProxy;

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    //存储普通文件
    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;

    //存储视频
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }


    /**
     * 将文件上传到minio
     *
     * @param localFilePath 文件本地路径
     * @param mimeType      媒体类型
     * @param bucket        桶
     * @param objectName    对象名
     * @return
     */
    public boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)//桶
                    .filename(localFilePath) //指定本地文件路径
                    .object(objectName)//对象名 放在子目录下
                    .contentType(mimeType)//设置媒体文件类型
                    .build();
            //上传文件
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("上传文件到minio成功,bucket:{},objectName:{}", bucket, objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错,bucket:{},objectName:{},错误信息:{}", bucket, objectName, e.getMessage());
        }
        return false;
    }

    @Override
    public MediaFiles getFileById(String mediaId) {
        return mediaFilesMapper.selectById(mediaId);
    }

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/") + "/";
        return folder;
    }

    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    // 上传文件
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {

        //文件名
        String filename = uploadFileParamsDto.getFilename();
        //先得到扩展名
        String extension = filename.substring(filename.lastIndexOf("."));

        //得到mimeType
        String mimeType = getMimeType(extension);

        //子目录
        String defaultFolderPath = getDefaultFolderPath();
        //文件的md5值
        String fileMd5 = getFileMd5(new File(localFilePath));
        String objectName = defaultFolderPath + fileMd5 + extension;
        //上传文件到minio
        boolean result = addMediaFilesToMinIO(localFilePath, mimeType, bucket_mediafiles, objectName);
        if (!result) {
            XueChengPlusException.cast("上传文件失败");
        }
        //入库文件信息
        // 不能直接使用当前类的方法，而要使用代理对象的方式Proxy才可以使用事务注解！！！
        // 要让对象变成接口去@Autowired变成代理对象
        // 注意看第三章4.4.5
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectName);
        if (mediaFiles == null) {
            XueChengPlusException.cast("文件上传后保存信息失败");
        }
        //准备返回的对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);

        return uploadFileResultDto;
    }


    /**
     * @param companyId           机构id
     * @param fileMd5             文件md5值
     * @param uploadFileParamsDto 上传文件的信息
     * @param bucket              桶
     * @param objectName          对象名称
     * @return com.xuecheng.media.model.po.MediaFiles
     * @description 将文件信息添加到文件表
     * @author Mr.M
     * @date 2022/10/12 21:22
     */
    @Transactional
    // 在此处加事务的原因是让该任务占用的数据库时间缩短
    // 上传文件的过程需要网络请求浪费时间，不在主任务加事务
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        //将文件信息保存到数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            //文件id
            mediaFiles.setId(fileMd5);
            //机构id
            mediaFiles.setCompanyId(companyId);
            //桶
            mediaFiles.setBucket(bucket);
            //file_path
            mediaFiles.setFilePath(objectName);
            //file_id
            mediaFiles.setFileId(fileMd5);
            //url
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            //上传时间
            mediaFiles.setCreateDate(LocalDateTime.now());
            //状态
            mediaFiles.setStatus("1");
            //审核状态
            mediaFiles.setAuditStatus("002003");
            //插入数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                log.debug("向数据库保存文件失败,bucket:{},objectName:{}", bucket, objectName);
                return null;
            }
            // 记录文件的待处理任务 在这个方法里因为有事务
            // tongguomineType判断视频格式，如avi
            //@Todo 后期加后缀判断
            // 向mediaProcess插入记录
            addWaitingTask(mediaFiles);
            return mediaFiles;
        }
        return mediaFiles;

    }

    @Override
    // 查数据库看文件是否存在
    public RestResponse<Boolean> checkFile(String fileMd5) {

        // 查询数据库 id对应是文件md5
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            // 如果数据库存在在查询minIo
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(mediaFiles.getBucket()) // 根据md5查数据库得放在哪个桶里
                    .object(mediaFiles.getFilePath())
                    .build();
            //查询远程服务获取到一个流对象
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if(inputStream != null){
                    return RestResponse.success(true); // 返回文件已存在
                }
            } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                     InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                     XmlParserException e) {
                throw new RuntimeException(e);
            }
        }
        // 文件不存在
        return RestResponse.success(false);
    }

    @Override
    // 检查文件分块是否存在
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 分块的存储路径是md5的前两位作为子目录，chunk存储分块文件
        String chunkFolderPath = getChunkFileFolderPath(fileMd5);
        // 如果数据库存在在查询minIo
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video) // 根据md5查数据库得放在哪个桶里
                .object(chunkFolderPath + chunkIndex) // 检查分块是否存在
                .build();
        //查询远程服务获取到一个流对象
        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            if(inputStream != null){
                return RestResponse.success(true); // 返回文件已存在
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.success(false);
    }

    @Override
    // 实现文件的上传
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        // 分块文件的存储路径获取
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;
        // 获取MimeType
        String mimeType = getMimeType(null);
        boolean isUploaded = false;
        try{
            isUploaded = addMediaFilesToMinIO(localChunkFilePath, mimeType, bucket_video, chunkFilePath);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(!isUploaded) {
            return RestResponse.validfail(false, "上传文件失败");
        } else {
            return RestResponse.success(true);
        }
    }

    @Override
    // 上传文件的合并
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        
        //获取分块文件路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);

        // 调用分块文件调用minio的sdk进行文件合并
        // 获取指定分块文件信息
        List<ComposeSource> sourceList;

        // 用Stream流映射
        sourceList = Stream.iterate(0, i -> ++i).limit(chunkTotal).map(i -> ComposeSource.builder().bucket(bucket_video).object(chunkFileFolderPath+i).build()).collect(Collectors.toList());

        // 获取合并后的文件名
        String fileName = uploadFileParamsDto.getFilename();
        // 扩展名
        String extension = fileName.substring((fileName.lastIndexOf(".")));
        // 合并后的objectName
        String objectName = getFilePathByMd5(fileMd5, extension);

        // 指定合并的信息和合并后的命名
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(objectName) // 合并后文件的文件名
                .sources(sourceList)
                .build();// 使用源文件;
        // 合并文件
        // 报错 size 1048576 must be greater than 5242880 minio默认分块文件大小为5m
        try{
            minioClient.composeObject(composeObjectArgs);
        }catch (Exception e){
            e.printStackTrace();
            log.error("合并文件出错， bucket：{}， Object：{}， 错误信息：{}",bucket_video, objectName, e.getMessage());
            return RestResponse.validfail(false, "合并文件异常");
        }
        System.out.println("合并成功");

        // 校验合并后的源文件是否一致，视频上传才成功
        // 下载文件到临时文件，并获取文件md5值
        File file = downloadFileFromMinIO(bucket_video, objectName);

        // 计算合并后文件的md5
        try(FileInputStream fileInputStream = new FileInputStream(file)){
            String mergeFileMd5 = DigestUtils.md5Hex(fileInputStream);
            if(!fileMd5.equals(mergeFileMd5)) {
                log.error("校验合并文件md5值不一致，原始文件：{}，合并文件：{}", fileMd5, mergeFileMd5);
                return RestResponse.validfail(false, "文件校验失败");
            }
            // 获取文件大小
            uploadFileParamsDto.setFileSize(file.length());
        }catch (Exception e){
            log.error("校验合并文件md5值不一致，原始文件：{}，错误信息：{}",fileMd5, e.getMessage());
            return RestResponse.validfail(false,"文件校验失败");
        }

        // 将文件信息入库
        // 非事务方法调用事务方法，要使用代理对象去调用才可控制事务
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
        if(mediaFiles == null) {
            return RestResponse.validfail(false,"文件入库失败");
        }

        // 清理分块文件
        clearChunkFiles(chunkFileFolderPath, chunkTotal);
        return RestResponse.success(true);
    }

    //根据扩展名获取mimeType
    private String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;

    }

    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket,String objectName){
        //临时文件
        File minioFile;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile=File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    // 清理分块文件
    /**
     * 清除分块文件
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal){

        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucket_video).objects(deleteObjects).build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(r->{
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清除分块文件失败,objectname:{}",deleteError.objectName(),e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清除分块文件失败,chunkFileFolderPath:{}",chunkFileFolderPath,e);
        }
    }



    // 获取分块文件的目录
    private String getChunkFileFolderPath(String fileMd5){
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    // 根据md5和后缀名获取文件路径和文件名
    private String getFilePathByMd5(String fileMd5, String fileExt){
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

    // 添加待处理任务
    private void addWaitingTask(MediaFiles mediaFiles){
        // 获取mimeType
        String fileName = mediaFiles.getFilename();
        // 获取文件扩展名
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        if(mimeType.equals("video/x-msvideo")){
            // 如果是avi文件则写入数据库
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            // 设置状态
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);
            mediaProcessMapper.insert(mediaProcess);
        }
    }


}
