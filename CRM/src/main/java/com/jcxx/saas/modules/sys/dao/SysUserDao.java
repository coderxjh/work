/**
 * Copyright (c) 2010 - 2019  All rights reserved.
 */

package com.jcxx.saas.modules.sys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcxx.saas.modules.app.entity.UserEntity;
import com.jcxx.saas.modules.sys.entity.SysUserEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统用户
 */
@Mapper
public interface SysUserDao extends BaseMapper<SysUserEntity> {
	
	/**
	 * 查询用户的所有权限
	 * @param userId  用户ID
	 */
	List<String> queryAllPerms(Long userId);
	
	/**
	 * 查询用户的所有菜单ID
	 */
	List<Long> queryAllMenuId(Long userId);
	
	/**
	 * 根据用户名，查询系统用户
	 */
	SysUserEntity queryByUserName(String username);

	/**
	 * 清除次数和登录时间
	 * @param userId
	 * state 1:将数据库时间和次数设为null 0：将次数设为null
	 */
    void updateLoginNum(Long userId,Integer state);

	/**
	 * 查询所有用户列表
	 * @param username
	 * @param createUserId
	 * @param page
	 * @param limit
	 * @return
	 */
    List<SysUserEntity> getUserList(String username, Long page, Long limit);

	List<SysUserEntity> selectByDeptIds(@Param(("ids")) List<Long> deptIds, @Param("status") Integer status, @Param("nameAndPhone") String nameAndPhone, @Param("isPhone") Boolean isPhone);


	Long queryDeptByToken(String token);

	List<Long> queryRolesByToken(String token);
}
