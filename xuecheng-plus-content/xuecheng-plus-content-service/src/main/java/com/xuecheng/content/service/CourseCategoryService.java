package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 *
 * @author Wayne
 * @description 调用mapper查询课程分类接口
 * @date 2023/7/1
 */
public interface CourseCategoryService {

    /**
     *
     * @param id
     * @return 返回的课程分类列表
     */
    // 课程列表树结构查询
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);

}
