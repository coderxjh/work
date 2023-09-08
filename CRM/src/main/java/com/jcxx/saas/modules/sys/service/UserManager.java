package com.jcxx.saas.modules.sys.service;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiV2UserGetuserinfoRequest;
import com.dingtalk.api.response.OapiV2UserGetuserinfoResponse;
import com.jcxx.saas.common.exception.SaaSException;
import com.jcxx.saas.constant.DingDingUrlConstant;
import com.taobao.api.ApiException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UserManager {

    @Resource
    private AppTokenService appTokenService;

    public String getUserId(String authCode) throws ApiException {
        String accessToken = appTokenService.getAccessToken();
        DingTalkClient client = new DefaultDingTalkClient(DingDingUrlConstant.GET_USER_INFO_URL);
        OapiV2UserGetuserinfoRequest req = new OapiV2UserGetuserinfoRequest();
        req.setCode(authCode);
        OapiV2UserGetuserinfoResponse rsp = client.execute(req, accessToken);
        if (!rsp.isSuccess()) {
            throw new SaaSException(rsp.getErrmsg());
        }
        return rsp.getResult().getUserid();
    }
}
