package com.jcxx.saas.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcxx.saas.modules.sys.entity.AppTokenEntity;

public interface AppTokenService extends IService<AppTokenEntity> {


    /**
     * 获取钉钉应用的access_token
     * @return access_token
     */
    String getAccessToken();


}
