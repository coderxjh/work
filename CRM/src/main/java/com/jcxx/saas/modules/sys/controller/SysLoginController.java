/**
 * Copyright (c) 2010 - 2019  All rights reserved.
 */

package com.jcxx.saas.modules.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.jcxx.saas.common.exception.SaaSException;
import com.jcxx.saas.modules.sys.service.UserManager;
import com.taobao.api.ApiException;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.jcxx.saas.common.utils.Result;
import com.jcxx.saas.modules.sys.entity.SysUserEntity;
import com.jcxx.saas.modules.sys.form.SysLoginForm;
import com.jcxx.saas.modules.sys.service.SysCaptchaService;
import com.jcxx.saas.modules.sys.service.SysUserService;
import com.jcxx.saas.modules.sys.service.SysUserTokenService;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 登录相关
 */
@RestController
public class SysLoginController extends AbstractController {
	@Autowired
	private SysUserService sysUserService;
	@Autowired
	private SysUserTokenService sysUserTokenService;
	@Autowired
	private SysCaptchaService sysCaptchaService;

	@Resource
	private UserManager userManager;

	/**
	 * 验证码
	 */
	@GetMapping("captcha.jpg")
	public void captcha(HttpServletResponse response, String uuid)throws IOException {
		response.setHeader("Cache-Control", "no-store, no-cache");
		response.setContentType("image/jpeg");

		//获取图片验证码
		BufferedImage image = sysCaptchaService.getCaptcha(uuid);

		ServletOutputStream out = response.getOutputStream();
		ImageIO.write(image, "jpg", out);
		IOUtils.closeQuietly(out);
	}

	/**
	 * 钉钉免登录授权接口
	 *
	 * @param authCode 免登录授权码
	 * @return
	 */
	@PostMapping("/sys/login-free")
	public Map<String, Object> login(@RequestParam String authCode) {
		// 1. 获取用户id
		String userId = null;
		try {
			userId = userManager.getUserId(authCode);
		} catch (ApiException e) {
			logger.error(e.toString());
			throw new SaaSException(e.getErrMsg());
		}
		//2.与数据库中的用户信息比较
		//这里拿username字段去存储userId
		SysUserEntity user = sysUserService.getOne(new QueryWrapper<SysUserEntity>()
				.eq("username", userId));
		//用户不存在
		if (user == null) {
			return Result.error("数据库不存在当前用户,请联系管理员创建！");
		}
		//账号锁定
		if (user.getStatus() == 0) {
			return Result.error("账号已被锁定,请联系管理员");
		}
		//生成token，并保存到数据库
		Result r = sysUserTokenService.createToken(user.getUserId());
		String token = (String) r.get("token");
		//获取该用户所在部门id
		Long deptId = sysUserService.queryDeptByToken(token);
		//获取该用户所拥有的角色id
		List<Long> roles = sysUserService.queryRolesByToken(token);
		Map<String, Object> map = new HashMap<>();
		map.put("dept_id", deptId);
		map.put("roles", roles);
		map.put("name", user.getName());
		map.put("user_id", user.getUsername());
		map.put("token", r.get("token"));
		map.put("expire", r.get("expire"));
		r.remove("token");
		r.remove("expire");
		return r.put("data", map);
	}

	/**
	 * 登录
	 */
//	@PostMapping("/sys/login")
//	public Map<String, Object> login(@RequestBody SysLoginForm form)throws IOException {
//
//		boolean captcha = sysCaptchaService.validate(form.getUuid(), form.getCaptcha());
//		if(!captcha){
//			return Result.error("验证码不正确");
//		}
//
//		//用户信息
//		SysUserEntity user = sysUserService.queryByUserName(form.getUsername());
//
//		//账号不存在、密码错误
//		if(user == null ) {
//			return Result.error("账号不存在");
//		}
//
//		if(user.getRegisterTime() != null){
//			//如果锁定时间在范围内则锁定
//			Calendar calendar = new GregorianCalendar();
//			calendar.setTime(user.getRegisterTime());
//			calendar.add(Calendar.MINUTE,5); //把日期往后增加五分钟 300秒
//
//			//如果过期时间在当前时间之前则视为锁定,如果不在当前时间内记录设为0
//			if(calendar.getTime().after(new Date())){
//				sysUserService.updateLoginNum(user.getUserId(),0);
//				return Result.error("该账户已锁定，请" + (300 - (new Date().getTime()-user.getRegisterTime().getTime())/1000) + "秒后再试！");
//			}else {
//				sysUserService.updateLoginNum(user.getUserId(),1);
//				user.setRegisterNum(null);
//				user.setRegisterTime(null);
//			}
//		}
//
//		if(!user.getPassword().equals(new Sha256Hash(form.getPassword(), user.getSalt()).toHex())){
//			//如果没有记录则设为0
//			if(user.getRegisterNum() == null) {
//				user.setRegisterNum(0);
//			}else {
//				user.setRegisterNum(user.getRegisterNum() + 1);
//				//如果为两次则锁定添加锁定时间
//				if (user.getRegisterNum().equals(3)) {
//					user.setRegisterTime(new Date());
//					sysUserService.updateById(user);
//					return Result.error("密码错误！账户已锁定，请300秒后再试！");
//				}
//			}
//			sysUserService.updateById(user);
//			return Result.error("密码错误！还有" + (3 - user.getRegisterNum()) + "次机会,否则会锁定300秒！");
//		}
//
//		//账号锁定
//		if(user.getStatus() == 0){
//			return Result.error("账号已被锁定,请联系管理员");
//		}
//
//		//生成token，并保存到数据库
//		Result r = sysUserTokenService.createToken(user.getUserId());
//
//		//登录成功则清除次数
//		sysUserService.updateLoginNum(user.getUserId(),1);
//
//		return r;
//	}


	/**
	 * 退出
	 */
	@PostMapping("/sys/logout")
	public Result logout() {
		sysUserTokenService.logout(getUserId());
		return Result.ok();
	}



}
