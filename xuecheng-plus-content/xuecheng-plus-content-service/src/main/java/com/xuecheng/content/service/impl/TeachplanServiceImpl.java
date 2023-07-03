package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TeachplanServiceImpl implements  TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachPlanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    @Transactional
    public void saveOrUpdateTeachPlan(SaveTeachplanDto saveTeachplanDto) {

        // 课程Id
        Long id = saveTeachplanDto.getId();
        if(id == null){
            // 新增
            Teachplan teachPlan = new Teachplan();
            // orderby字段需要服务端自行计算;
            int count = getTeachPlanCount(saveTeachplanDto.getCourseId(), saveTeachplanDto.getParentid());
            teachPlan.setOrderby(count + 1);
            BeanUtils.copyProperties(saveTeachplanDto, teachPlan);
            teachplanMapper.insert(teachPlan);
        }
        else{
            // 修改
            Teachplan teachPlan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(saveTeachplanDto, teachPlan);
            teachplanMapper.updateById(teachPlan);
        }

    }

    /**
     * @description 删除章节的接口
     * @param id 删除章节的章节id
     */
    @Override
    @Transactional
    public void deleteTeachPlan(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        if(teachplan == null) XueChengPlusException.cast("未查询到该章节，请确认章节是否存在");
        // 判断该章节是否为大章节
        if(teachplan.getParentid() == 0){
            // 判断该大章节下是否有小章节
            // Select count(1) from teachPlan where course_id = xx and parent_id = 该章节的id
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getCourseId, teachplan.getCourseId());
            queryWrapper.eq(Teachplan::getParentid, id);
            int count = teachplanMapper.selectCount(queryWrapper);
            if(count == 0){
                int result = teachplanMapper.deleteById(id);
                if(result <= 0) XueChengPlusException.cast("删除本章失败");
            }else XueChengPlusException.cast("课程计划信息还有子级信息，无法操作", "120409");
        }else{
            LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TeachplanMedia::getTeachplanId, id);
            TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(queryWrapper);
            if(teachplanMedia != null){
                int deleteMediaInfo = teachplanMediaMapper.deleteById(teachplanMedia.getId());
                if(deleteMediaInfo <= 0) XueChengPlusException.cast("删除本章失败");
            }
            int result = teachplanMapper.deleteById(id);
            if(result <= 0) XueChengPlusException.cast("删除本章失败");
        }
    }

    /**
     * @description 用于章节上移的服务
     * @param id 章节id
     */
    @Override
    @Transactional
    public void move(Long id, Boolean direction) {
        Teachplan original = teachplanMapper.selectById(id);
        // 如果不能找到这个章节
        if(original == null) XueChengPlusException.cast("未找到该章节");
        int orderBy = original.getOrderby();
        int newOrderBy = 0;
        if(direction){
            newOrderBy = orderBy + 1;
        }else{
            newOrderBy = orderBy - 1;
        }
        Long courseId = original.getCourseId();
        Long parentId = original.getParentid();
        // 寻找该章节的排序上章节
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        queryWrapper.eq(Teachplan::getOrderby, newOrderBy);
        Teachplan replace = teachplanMapper.selectOne(queryWrapper);
        if(replace == null) XueChengPlusException.cast("该章节不可移动");

        // 章节对调
        original.setOrderby(newOrderBy);
        replace.setOrderby(orderBy);

        // 修改数据库
        int original_result = teachplanMapper.updateById(original);
        int replace_result = teachplanMapper.updateById(replace);

        if(original_result <= 0 || replace_result <= 0){
            XueChengPlusException.cast("上移失败,请检查参数");
        }

    }


    /**
     * @description 查询子节点下所有的节点个数
     * @param courseId 课程ID
     * @param parentId 父节点ID
     * @return
     */
    private int getTeachPlanCount(Long courseId, Long parentId){
        // 编写sql语句对象 找到该节点下的所有子节点个数，后来的直接叠加上去
        // Select count(1) from teachPlan where course_id = xx and parent_id = xx;
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId);
        queryWrapper.eq(Teachplan::getParentid,parentId);
        // 查表
        return teachplanMapper.selectCount(queryWrapper);
    }
}
