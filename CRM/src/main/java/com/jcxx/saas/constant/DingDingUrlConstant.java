package com.jcxx.saas.constant;

/**
 * 钉钉开放接口网关常量
 */
public class DingDingUrlConstant {

    /**
     * 获取access_token url
     */
    public static final String GET_ACCESS_TOKEN_URL = "https://oapi.dingtalk.com/gettoken";
    /**
     * 通过免登授权码获取用户信息 url
     */
    public static final String GET_USER_INFO_URL = "https://oapi.dingtalk.com/topapi/v2/user/getuserinfo";

    /**
     * 发送群助手消息 url
     */
    public static final String SCENCEGROUP_MESSAGE_SEND_V2 = "https://oapi.dingtalk.com/topapi/im/chat/scencegroup/message/send_v2";


    /**
     * 获取部门列表url
     */
    public static final String DEPARTMENT_LIST = "https://oapi.dingtalk.com/department/list";


    /**
     * 获取部门用户详情列表url
     */
    public static final String DEPARTMENT_USER_LIST = "https://oapi.dingtalk.com/topapi/v2/user/list";


    /**
     * 获取部门用户id列表 url
     */
    public static final String DEPARTMENT_USERID_LIST = "https://oapi.dingtalk.com/topapi/user/listid";


    /**
     * 根据用户id获取用户详情 url
     */
    public static final String USER_GET_URL = "https://oapi.dingtalk.com/topapi/v2/user/get";


}
