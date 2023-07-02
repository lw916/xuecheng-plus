package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author Wayne
 * @description 修改课程内容dto
 * @date 2023/7/2
 */

@Data
@ApiModel(value = "EditCourseDto", description = "修改课程的基本信息")
public class EditCourseDto extends AddCourseDto{

    @ApiModelProperty(value = "课程id", required = true)
    private Long id;

}
