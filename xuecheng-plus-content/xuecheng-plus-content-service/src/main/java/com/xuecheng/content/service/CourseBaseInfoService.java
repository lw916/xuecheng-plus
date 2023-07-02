package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
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
     * @description 带分页功能的请求课程列表接口
     * @param pageParams 分页查询参数
     * @param queryCourseParamsDto 查询条件参数
     * @return 返回的查询结果
     */
    // 课程分页查询
    // 它由API调用，所以返回的内容要给API接口使用
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * @description 创建课程接口
     * @param companyId 单点登录后获取机构id
     * @param addCourseDto 增加课程信息
     * @return 课程详细信息
     */
    // 新增课程
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);


    /**
     * @description 获取课程信息接口
     * @param courseId 课程id
     * @return 课程的所有信息
     */
    public CourseBaseInfoDto getCourseBaseById(Long courseId);

    /**
     * @description 修改课程基本信息接口
     * @param companyId 来自的机构id,来源是单点登录
     * @param editCourseDto 课程信息dto
     * @return
     */
    public CourseBaseInfoDto updateCourseBaseById(Long companyId, EditCourseDto editCourseDto);

}
