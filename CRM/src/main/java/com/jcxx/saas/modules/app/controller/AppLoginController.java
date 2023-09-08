/**
 * Copyright (c) 2010 - 2019  All rights reserved.
 */

package com.jcxx.saas.modules.app.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jcxx.saas.common.utils.Result;
import com.jcxx.saas.common.validator.ValidatorUtils;
import com.jcxx.saas.modules.app.form.LoginForm;
import com.jcxx.saas.modules.app.service.UserService;
import com.jcxx.saas.modules.app.utils.JwtUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * APP登录授权
 */
@RestController
@RequestMapping("/app")
@Api("APP登录接口")
public class AppLoginController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 登录
     */
    @PostMapping("login")
    @ApiOperation("登录")
    public Result login(@RequestBody LoginForm form){
        //表单校验
        ValidatorUtils.validateEntity(form);

        //用户登录
        long userId = userService.login(form);

        //生成token
        String token = jwtUtils.generateToken(userId);

        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("expire", jwtUtils.getExpire());

        return Result.ok(map);
    }

}
