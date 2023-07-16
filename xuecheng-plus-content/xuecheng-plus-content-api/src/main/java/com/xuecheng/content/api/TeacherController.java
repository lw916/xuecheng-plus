package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.TeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author Wayne
 * @description 教师资源管理接口
 * @date 2023/7/3
 */

@RestController
@Api(value = "教师资源管理接口", tags = "教师资源管理接口")
public class TeacherController {

    @Autowired
    TeacherService teacherService;

    @GetMapping("/courseTeacher/list/{courseId}")
    @ApiOperation("查询课程教师资源接口")
    @ApiImplicitParam(value="courseId", name = "课程Id", required = true, dataType = "Long", paramType = "path")
    public List<CourseTeacher> getCourseTeacher(@PathVariable Long courseId){
        return teacherService.getCourseTeacher(courseId);
    }

    @PostMapping("/courseTeacher")
    @ApiOperation("增加课程教师资源接口")
    @ApiImplicitParam(value="teacher", name = "课程教师信息", required = true, dataType = "courseTeacher", paramType = "path")
    public CourseTeacher addCourseTeacher(@RequestBody CourseTeacher courseTeacher){
        return teacherService.addCourseTeacher(courseTeacher);
    }

    @PutMapping("/courseTeacher")
    @ApiOperation("修改课程教师资源接口")
    @ApiImplicitParam(value="teacher", name = "课程教师信息", required = true, dataType = "courseTeacher", paramType = "path")
    public CourseTeacher editCourseTeacher(@RequestBody CourseTeacher courseTeacher){
        return teacherService.editCourseTeacherInfo(courseTeacher);
    }

    @DeleteMapping("/courseTeacher/course/{courseId}/{Id}")
    @ApiOperation("查询课程教师资源接口")
    @ApiImplicitParam(value="courseId", name = "课程Id", required = true, dataType = "Long", paramType = "path")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long Id){
        teacherService.deleteCourseTeacherInfo(courseId, Id);
    }



}