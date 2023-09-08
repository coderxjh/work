package com.jcxx.saas.modules.sys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcxx.saas.modules.sys.entity.DepartmentEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DepartmentDao extends BaseMapper<DepartmentEntity> {


    List<DepartmentEntity> selectUserListByDeptId(Long deptId);

}
