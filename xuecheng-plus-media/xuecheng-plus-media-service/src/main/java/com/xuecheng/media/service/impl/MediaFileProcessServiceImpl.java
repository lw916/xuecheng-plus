package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import groovy.util.logging.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    // 查MeidiaProcess列表
    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
    }

    // 开启一个任务 乐观锁
    @Override
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result > 0;
    }

    // 更新处理结果
    @Override
    @Transactional
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {

        // 查询要更新的任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if(mediaProcess == null){
            return ;
        }else{
            // 如果任务施行失败
            if(status.equals("3")){
                // 更新MediaProcess表的状态
                mediaProcess.setStatus("3");
                mediaProcess.setFailCount(mediaProcess.getFailCount() + 1); // 失败次数加1
                mediaProcess.setErrormsg(errorMsg);
                mediaProcessMapper.updateById(mediaProcess);
                // 高效的更新方法
                // mediaProcessMapper.update(mediaProcess,)
                return;
            }else{
                // 如果任务执行成功
                // 文件表记录
                MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
                // 更新media_file表的URL
                mediaFiles.setFilePath(url);
                mediaFilesMapper.updateById(mediaFiles);
                // 更新MediaProcess表的状态
                mediaProcess.setStatus("2");
                mediaProcess.setFinishDate(LocalDateTime.now());
                mediaProcess.setUrl(url);
                // 将MediaProcess表的数据放到History表
                MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
                BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
                mediaProcessHistoryMapper.insert(mediaProcessHistory);
                // 删表内容
                mediaProcessMapper.deleteById(mediaProcess);
            }
        }

    }


}
