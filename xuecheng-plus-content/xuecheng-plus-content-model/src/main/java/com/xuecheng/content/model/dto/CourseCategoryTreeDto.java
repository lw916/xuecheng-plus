package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;

import java.util.List;

/**
 *
 * @author Wayne
 * @description 课程分类树形节点DTO
 * @date 2023/7/1
 */

@Data
// PO类少了个category的下级属性，继承他
// java.io.Serializable 网络传输数据序列化
// 先需求分析，再搞定模型，在定义控制器（接口），
public class CourseCategoryTreeDto extends CourseCategory implements java.io.Serializable{
    List<CourseCategoryTreeDto> childrenTreeNodes; // 下级属性是属性他本身 为接口使用

}
