package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 *
 * @author Wayne
 * @description 课程信息管理接口
 * @date 2023/6/24
 */

public interface CourseBaseInfoService {


    /**
     *
     * @param pageParams 分页查询参数
     * @param queryCourseParamsDto 查询条件参数
     * @return 返回的查询结果
     */
    // 课程分页查询
    // 它由API调用，所以返回的内容要给API接口使用
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);


}