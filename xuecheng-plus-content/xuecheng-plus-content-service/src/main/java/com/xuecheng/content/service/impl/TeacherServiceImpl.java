package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.TeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    /**
     * @description 查询课程教师信息列表接口
     * @param courseId 课程Id
     * @return
     */

    @Override
    public List<CourseTeacher> getCourseTeacher(Long courseId) {
        // 增加条件查询教师资源列表
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        return courseTeacherMapper.selectList(queryWrapper);
    }

    /**
     * @description 查询教师信息接口
     * @param teacher 传入的教师信息
     * @return
     */

    @Override
    @Transactional
    public CourseTeacher addCourseTeacher(CourseTeacher teacher) {
        // 查询教师是否已经添加过
        if(getTeacherInfo(teacher) != null) XueChengPlusException.cast("该教师资料已添加过");

        // 增加教师资源到数据库
        int insert = courseTeacherMapper.insert(teacher);
        if(insert <= 0 ) XueChengPlusException.cast("增加教师资料失败，请检查参数");

        // 查询该教师的信息
        return getTeacherInfo(teacher);
    }


    /**
     * @description 修改教师信息接口
     * @param teacher 传入教师信息
     * @return
     */
    @Override
    public CourseTeacher editCourseTeacherInfo(CourseTeacher teacher) {
        // 查询教师是否存在
        if(getTeacherInfo(teacher) == null) XueChengPlusException.cast("该教师资料不存在");

        // 修改内容
        int update = courseTeacherMapper.updateById(teacher);
        if(update <= 0) XueChengPlusException.cast("教师资料修改失败");

        // 返回查询结果
        return getTeacherInfo(teacher);
    }

    /**
     * @description 删除教师信息接口
     * @param courseId 课程Id
     * @param id 教师Id
     */
    @Override
    public void deleteCourseTeacherInfo(Long courseId, Long id) {
        // 查询教师是否存在
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getId, id);
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        if(courseTeacherMapper.selectOne(queryWrapper) == null) XueChengPlusException.cast("找不到该教师信息");
        int delete = courseTeacherMapper.delete(queryWrapper);
        if(delete <= 0) XueChengPlusException.cast("删除教师信息失败");
    }

    // 查询教师信息
    private CourseTeacher getTeacherInfo(CourseTeacher teacher){
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getTeacherName, teacher.getTeacherName());
        queryWrapper.eq(CourseTeacher::getPosition, teacher.getPosition());
        queryWrapper.eq(CourseTeacher::getCourseId, teacher.getCourseId());
        return courseTeacherMapper.selectOne(queryWrapper);
    }

}