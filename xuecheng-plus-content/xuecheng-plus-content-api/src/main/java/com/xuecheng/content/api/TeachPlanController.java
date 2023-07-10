package com.xuecheng.content.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.media.model.dto.BindTeachplanMediaDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author Wayne
 * @description 课程计划相关的接口
 * @date 2023/7/2
 */

@Api(value = "课程计划相关的接口",tags = "课程计划编辑接口")
@RestController
public class TeachPlanController {

    @Autowired
    TeachplanService teachplanService;

    // @TODO 加课程机构验证
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value="courseId", name = "课程Id", required = true, dataType = "Long", paramType = "path")
    public List<TeachPlanDto> getTree(@PathVariable Long courseId){
        List<TeachPlanDto> tree = teachplanService.findTeachplanTree(courseId);
        if(tree == null) XueChengPlusException.cast("未查询到该课程或该课程的小章节");
        return tree;
    }

    @PostMapping("/teachplan")
    @ApiOperation("增加或修改章节接口")
    public void saveOrUpdateTeachPlan(@RequestBody SaveTeachplanDto saveTeachplanDto){
        teachplanService.saveOrUpdateTeachPlan(saveTeachplanDto);
    }

    @DeleteMapping("/teachplan/{planId}")
    @ApiOperation("删除章节接口")
    @ApiImplicitParam(value="planId", name = "章节Id", required = true, dataType = "Long", paramType = "path")
    public void deleteTeachPlan(@PathVariable Long planId){
        teachplanService.deleteTeachPlan(planId);
    }

    @PostMapping("/teachplan/moveup/{planId}")
    @ApiOperation("章节上移接口")
    @ApiImplicitParam(value="planId", name = "章节Id", required = true, dataType = "Long", paramType = "path")
    public void moveUp(@PathVariable Long planId){
        teachplanService.move(planId, false);
    }

    @PostMapping("/teachplan/movedown/{planId}")
    @ApiOperation("章节上移接口")
    @ApiImplicitParam(value="planId", name = "章节Id", required = true, dataType = "Long", paramType = "path")
    public void moveDown(@PathVariable Long planId){
        teachplanService.move(planId, true);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    // 章节添加视频，需要先删除原有章节附带的视频再添加新的视频
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    @ApiOperation(value = "删除课程计划和媒资信息绑定")
    @DeleteMapping("/teachplan/association/media/{teachPlanId}/{mediaId}")
    // 章节添加视频，需要先删除原有章节附带的视频再添加新的视频
    public void deleteAssociationMedia(@PathVariable Long teachPlanId, @PathVariable String mediaId){
        teachplanService.deleteAssociationMedia(teachPlanId, mediaId);
    }


}
