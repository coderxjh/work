package com.jcxx.saas.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.jcxx.saas.common.exception.SaaSException;
import com.jcxx.saas.constant.AppConstant;
import com.jcxx.saas.modules.sys.dao.AppTokenDao;
import com.jcxx.saas.modules.sys.entity.AppTokenEntity;
import com.jcxx.saas.modules.sys.service.AppTokenService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static com.jcxx.saas.constant.DingDingUrlConstant.GET_ACCESS_TOKEN_URL;

@Service("appTokenService")
public class AppTokenServiceImpl extends ServiceImpl<AppTokenDao, AppTokenEntity> implements AppTokenService {


    @Override
    public String getAccessToken() {
        //1.首先查询数据库是否有access_token
        AppTokenEntity appTokenEntity = this.getOne(new QueryWrapper<AppTokenEntity>().eq("id", 1L));
        //2.没有access_token,则调用钉钉接口获取
        AppTokenEntity tokenEntity = new AppTokenEntity();
        if (appTokenEntity == null) {
            OapiGettokenResponse rsp = getOapiGettokenResponse();
            tokenEntity.setId(1L);
            tokenEntity.setAppToken(rsp.getAccessToken());
            //在当前日期上加上有效期的秒数
            LocalDateTime expiration = LocalDateTime.now().plusSeconds(rsp.getExpiresIn());
            tokenEntity.setExpireTime(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant()));
            boolean success = save(tokenEntity);
            if (!success) {
                throw new SaaSException("添加错误，请联系管理员！");
            }
            return tokenEntity.getAppToken();
        }
        //3.数据库有access_token，但是已经过期了
        Date expireTime = appTokenEntity.getExpireTime();
        if (expireTime.before(new Date())) {
            OapiGettokenResponse rsp = getOapiGettokenResponse();
            appTokenEntity.setAppToken(rsp.getAccessToken());
            //在当前日期上加上有效期的秒数
            LocalDateTime expiration = LocalDateTime.now().plusSeconds(rsp.getExpiresIn());
            appTokenEntity.setExpireTime(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant()));
            boolean success = updateById(appTokenEntity);
            if (!success) {
                throw new SaaSException("添加错误，请联系管理员！");
            }
            return appTokenEntity.getAppToken();
        }

        //4.数据库有access_token，也没有过期，直接返回
        return appTokenEntity.getAppToken();
    }

    /**
     * 钉钉接口，获取access_token
     *
     * @return
     */
    private OapiGettokenResponse getOapiGettokenResponse() {
        try {
            DingTalkClient client = new DefaultDingTalkClient(GET_ACCESS_TOKEN_URL);
            OapiGettokenRequest req = new OapiGettokenRequest();
            req.setAppkey(AppConstant.APP_KEY);
            req.setAppsecret(AppConstant.APP_SECRET);
            req.setHttpMethod("GET");
            return client.execute(req);
        } catch (Exception e) {
            throw new SaaSException("access_token获取错误，请联系管理员！"+e.getMessage());
        }
    }
}
