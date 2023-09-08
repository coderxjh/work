package com.jcxx.saas.modules.sys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcxx.saas.modules.sys.entity.AppTokenEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppTokenDao extends BaseMapper<AppTokenEntity> {
    int deleteByPrimaryKey(Integer id);

    int insert(AppTokenEntity record);

    int insertSelective(AppTokenEntity record);

    AppTokenEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AppTokenEntity record);

    int updateByPrimaryKey(AppTokenEntity record);
}