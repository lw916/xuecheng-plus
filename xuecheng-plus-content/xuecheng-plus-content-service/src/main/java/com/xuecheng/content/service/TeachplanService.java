package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.TeachPlanDto;

import java.util.List;

/**
 *
 * @author Wayne
 * @description  课程教学基本信息管理业务接口
 * @date 2023/7/2
 */

public interface TeachplanService {
    /**
     * @description 查询课程计划树形结构
     * @param courseId 课程Id
     * @return
     */
    public  List<TeachPlanDto> findTeachplanTree(Long courseId);
}
