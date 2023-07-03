package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 *
 * @author Wayne
 * @description 课程查询条件模型类
 * @date 2023/6/24
 */

@Data
@ToString
public class QueryCourseParamsDto {

    //审核状态
    private String auditStatus;
    //课程名称
    private String courseName;
    //发布状态
    private String publishStatus;


}
