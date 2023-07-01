package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author Wayne
 * @description 课程信息管理接口的实现
 * @date 2023/6/24
 */

@Service
@Slf4j
// 此注注解属于业务逻辑层，service或者manager层
// Service层叫服务层，被称为服务，可以理解就是对一个或多个DAO进行的再次封装，封装成一个服务，所以这里也就不会是一个原子操作了，需要事物控制。
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        // 拼装查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        // 根据名称模糊查询 like 在sql中拼接 course_base.name like '%xxx%'
        queryWrapper.like(StringUtils.isNotBlank(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());
        // 根据课程审核状态查询 eq course_base.audit_status = xxxx
        queryWrapper.eq(StringUtils.isNotBlank(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        // @TODO 按课程发布状态查询
        // 创建page 分页参数对象 自动补齐左边代码 CTRL + alt + v
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 开始进行分页查询
        Page<CourseBase> searchResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 数据列表
        List<CourseBase> records = searchResult.getRecords();
        // 总记录数
        long counts = searchResult.getTotal();
        // 需要数据：List<T> items, long counts, long page, long pageSize
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(records, counts, pageParams.getPageNo(), pageParams.getPageSize());
        return courseBasePageResult;
    }

    @Override
    @Transactional // 加事务控制
    // 实现创建课程的接口
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {

        // 参数合法性校验
        // @TODO 后期通过加参数的方式，实现参数合法性校验
        if (StringUtils.isBlank(addCourseDto.getName())) {
//            throw new RuntimeException("课程名称为空");
            XueChengPlusException.cast("课程名称为空");
        }

        if (StringUtils.isBlank(addCourseDto.getMt())) {
            XueChengPlusException.cast("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getSt())) {
            XueChengPlusException.cast("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getGrade())) {
            XueChengPlusException.cast("课程等级为空");
        }

        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
            XueChengPlusException.cast("教育模式为空");
        }

        if (StringUtils.isBlank(addCourseDto.getUsers())) {
            XueChengPlusException.cast("适应人群为空");
        }

        if (StringUtils.isBlank(addCourseDto.getCharge())) {
            XueChengPlusException.cast("收费规则为空");
        }

        // 向课程基本信息表写入数据 course_base
        CourseBase courseBase = new CourseBase();
        // 将传入的页面参数放到CourseBase对象里
        // 利用工具从原始对象中提取部分对象存进对象
        BeanUtils.copyProperties(addCourseDto, courseBase); // 自动匹配对象（属性名相同）， 原始对象空，新对象有值会覆盖！！
        courseBase.setCompanyId(companyId); // 敏感数据去覆盖前面写好的数据
        courseBase.setCreateDate(LocalDateTime.now());
        // 默认审核状态
        courseBase.setAuditStatus("202002");
        // 发布状态
        courseBase.setStatus("203001");
        // 插入数据库
        int insertToCourseBase = courseBaseMapper.insert(courseBase);
        if( insertToCourseBase <= 0 ) XueChengPlusException.cast("添加课程信息失败");

        // 像课程营销表写入数据 course_market
        CourseMarket courseMarket = new CourseMarket();
        // 将数据拷贝到营销信息模型中
        BeanUtils.copyProperties(addCourseDto, courseMarket);
        // 获取课程的主键ID
        Long id = courseBase.getId();
        courseMarket.setId(id);
        // 写入或更新课程营销信息到数据库
        int insertToCourseMarket = saveCourseMarketInfo(courseMarket);
        if( insertToCourseMarket <= 0 ) XueChengPlusException.cast("课程营销信息插入失败");
        //从数据库中查询课程信息
        CourseBaseInfoDto courseBaseInfoDto = getCourseBaseInfo(id);
        if(courseBaseInfoDto == null) XueChengPlusException.cast("查询课程信息失败");
        return courseBaseInfoDto;
    }

    // 保存营销信息， 逻辑：存在即更新，不存在就创建
    private int saveCourseMarketInfo(CourseMarket courseMarket){

        // 检验参数合法性
        String charge = courseMarket.getCharge();
        if(StringUtils.isBlank(charge)){
            XueChengPlusException.cast("收费规则为空");
        }

        // 如果选了收费，但是没有价格的话
        if(charge.equals("201001")){
            if(courseMarket.getPrice() == null || courseMarket.getPrice().floatValue() <= 0){
                XueChengPlusException.cast("课程价格不能为空且不能为负数");
//                throw new RuntimeException("课程价格不能为空且必须大于0");
            }
        }

        if(charge.equals("201000")){
            courseMarket.setOriginalPrice((float) 0);
            courseMarket.setPrice((float) 0);
        }

        // 从数据库中查营销信息，有更新无创建
        Long id = courseMarket.getId();
        CourseMarket courseMarket1 = courseMarketMapper.selectById(id);
        if(courseMarket1 == null){
            // 插入数据库
            return courseMarketMapper.insert(courseMarket);
        }else{
            // 数据库存在该课程需要更新
            BeanUtils.copyProperties(courseMarket, courseMarket1);
            courseMarket1.setId(courseMarket.getId());
            // 更新
            return courseMarketMapper.updateById(courseMarket1);
        }
    }

    // 获取课程详细信息， 包括两部分
    private CourseBaseInfoDto getCourseBaseInfo(long courseId){

        // 从课程信息表查询
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }

        // 从课程营销表查询
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        // 组装
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if(courseMarket!=null){
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }
        // 缺少分类类名，查数据库
        String bidCategoryId = courseBaseInfoDto.getMt();
        String smallCategoryId = courseBaseInfoDto.getSt();
        CourseCategory bigCategoryName = courseCategoryMapper.selectById(bidCategoryId);
        CourseCategory smallCategoryName = courseCategoryMapper.selectById(smallCategoryId);
        courseBaseInfoDto.setMtName(bigCategoryName.getName());
        courseBaseInfoDto.setStName(smallCategoryName.getName());
        return courseBaseInfoDto;
    }

}
