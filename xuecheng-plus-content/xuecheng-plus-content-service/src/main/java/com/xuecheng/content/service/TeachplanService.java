package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
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
    public List<TeachPlanDto> findTeachplanTree(Long courseId);

    /**
     * @description 添加或修改章节
     * @param saveTeachplanDto 传过来的一些参数
     */
    public void saveOrUpdateTeachPlan(SaveTeachplanDto saveTeachplanDto);


    /**
     * @description 删除章节
     * @param id 删除章节的章节id
     */
    public void deleteTeachPlan(Long id);

    /**
     * @description 章节上移动
     * @param id 章节ID
     * @param direction 向上true或向下false
     */
    public void move(Long id, Boolean direction);


}
