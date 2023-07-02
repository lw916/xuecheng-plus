package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;

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
