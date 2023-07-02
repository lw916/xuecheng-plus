package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 *
 * @author Wayne
 * @description 课程计划信息DTO
 * @date 2023/7/2
 */

@Data
@ToString
public class TeachPlanDto extends Teachplan {

    // 与媒体资源关联的列表
    private TeachplanMedia teachplanMedia;

    // 小章节列表
    List<TeachPlanDto> teachPlanTreeNodes;



}
