package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 *
 * @author Wayne
 * @description 分页查询的分页参数
 * @date 2023/6/24
 */

@Data
@ToString
public class PageParams {

    //当前页码 mybatist的分页用了long
    @ApiModelProperty("分页页码")
    private Long pageNo = 1L;

    //每页记录数默认值
    @ApiModelProperty("分页记录数")
    private Long pageSize =10L;

    public PageParams(){


    }

    public PageParams(long pageNo,long pageSize){
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }


}
