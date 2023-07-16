package com.xuecheng.content.service;


import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 *
 * @author Wayne
 * @description 教师信息的接口
 * @date 2023/7/3
 */


public interface TeacherService {

    // 获取某课程的教师列表

    /**
     * @description 获取课程教师资源
     * @param courseId 课程Id
     * @return
     */
    public List<CourseTeacher> getCourseTeacher(Long courseId);

    /**
     * @description 增加教师资源接口
     * @param teacher 传入的教师信息
     * @return
     */
    public CourseTeacher addCourseTeacher(CourseTeacher teacher);

    /**
     * @description 修改教师信息接口
     * @param teacher 传入教师信息
     * @return
     */
    public CourseTeacher editCourseTeacherInfo(CourseTeacher teacher);

    /**
     * @description 删除教师信息接口
     * @param courseId 课程Id
     * @param id 教师Id
     */
    public void deleteCourseTeacherInfo(Long courseId, Long id);


}