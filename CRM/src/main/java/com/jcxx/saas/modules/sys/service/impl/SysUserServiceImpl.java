/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package com.jcxx.saas.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcxx.saas.common.exception.SaaSException;
import com.jcxx.saas.common.utils.Constant;
import com.jcxx.saas.common.utils.PageUtils;
import com.jcxx.saas.common.utils.Query;
import com.jcxx.saas.common.utils.RegexUtils;
import com.jcxx.saas.modules.sys.dao.SysUserDao;
import com.jcxx.saas.modules.sys.entity.SysUserEntity;
import com.jcxx.saas.modules.sys.service.SysRoleService;
import com.jcxx.saas.modules.sys.service.SysUserRoleService;
import com.jcxx.saas.modules.sys.service.SysUserService;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 系统用户
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service("sysUserService")
public class SysUserServiceImpl extends ServiceImpl<SysUserDao, SysUserEntity> implements SysUserService {
	@Autowired
	private SysUserRoleService sysUserRoleService;
	@Autowired
	private SysRoleService sysRoleService;

	@Resource
	private SysUserDao sysUserDao;

	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		String username = (String) params.get("username");
		Long createUserId = (Long) params.get("createUserId");

		IPage<SysUserEntity> page = this.page(
				new Query<SysUserEntity>().getPage(params),
				new QueryWrapper<SysUserEntity>()
						.like(StringUtils.isNotBlank(username), "username", username)
						.eq(createUserId != null, "create_user_id", createUserId)
		);

		return new PageUtils(page);
	}

	@Override
	public List<String> queryAllPerms(Long userId) {
		return baseMapper.queryAllPerms(userId);
	}

	@Override
	public List<Long> queryAllMenuId(Long userId) {
		return baseMapper.queryAllMenuId(userId);
	}


	@Override
	@Transactional
	public void saveUser(SysUserEntity user) {
		user.setCrtTime(new Date());
		//sha256加密
		String salt = RandomStringUtils.randomAlphanumeric(20);
		user.setPassword(new Sha256Hash(user.getPassword(), salt).toHex());
		user.setSalt(salt);
		this.save(user);

		//检查角色是否越权
		checkRole(user);

		//保存用户与角色关系
		sysUserRoleService.saveOrUpdate(user.getUserId(), user.getRoleIdList());
	}

	@Override
	@Transactional
	public void update(SysUserEntity user) {
		if (StringUtils.isBlank(user.getPassword())) {
			user.setPassword(null);
		} else {
			user.setPassword(new Sha256Hash(user.getPassword(), user.getSalt()).toHex());
		}
		this.updateById(user);

		//检查角色是否越权
		checkRole(user);

		//保存用户与角色关系
		sysUserRoleService.saveOrUpdate(user.getUserId(), user.getRoleIdList());
	}

	@Override
	public void deleteBatch(Long[] userId) {
		this.removeByIds(Arrays.asList(userId));
	}

	@Override
	public boolean updatePassword(Long userId, String password, String newPassword) {
		SysUserEntity userEntity = new SysUserEntity();
		userEntity.setPassword(newPassword);
		return this.update(userEntity,
				new QueryWrapper<SysUserEntity>().eq("user_id", userId).eq("password", password));
	}

	@Override
	public Long queryDeptByToken(String token) {
		return baseMapper.queryDeptByToken(token);
	}

	@Override
	public List<Long> queryRolesByToken(String token) {
		return baseMapper.queryRolesByToken(token);
	}

	@Override
	public List<SysUserEntity> queryAll() {
		return this.list(new QueryWrapper<SysUserEntity>().eq("status", 1));
	}

	@Override
	public List<SysUserEntity> queryByDeptIds(List<Long> deptIds, String status, String nameAndPhone) {
		Integer s = null;
		if (status != null) {
			s = Integer.valueOf(status);
		}
		boolean isPhone = true;
		if (nameAndPhone != null) {
			//判断传入的是姓名还是手机号，只要有一个字符不是数字，都按姓名查找
			isPhone= RegexUtils.isIntegerInvalid(nameAndPhone);
		}
		return sysUserDao.selectByDeptIds(deptIds, s, nameAndPhone, isPhone);
	}

	/**
	 * 检查角色是否越权
	 */
	private void checkRole(SysUserEntity user) {
		if (user.getRoleIdList() == null || user.getRoleIdList().size() == 0) {
			return;
		}
		//如果不是超级管理员，则需要判断用户的角色是否自己创建
		if (user.getCrtUserId() == Constant.SUPER_ADMIN) {
			return;
		}

		//查询用户创建的角色列表
		List<Long> roleIdList = sysRoleService.queryRoleIdList(user.getCrtUserId());

		//判断是否越权
		if (!roleIdList.containsAll(user.getRoleIdList())) {
			throw new SaaSException("新增用户所选角色，不是本人创建");
		}
	}
}