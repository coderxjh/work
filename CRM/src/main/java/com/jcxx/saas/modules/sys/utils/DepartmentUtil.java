package com.jcxx.saas.modules.sys.utils;


import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiDepartmentListRequest;
import com.dingtalk.api.request.OapiUserListidRequest;
import com.dingtalk.api.request.OapiV2UserGetRequest;
import com.dingtalk.api.response.OapiDepartmentListResponse;
import com.dingtalk.api.response.OapiUserListidResponse;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.jcxx.saas.common.exception.SaaSException;
import com.jcxx.saas.constant.DingDingUrlConstant;
import com.jcxx.saas.modules.sys.entity.DepartmentEntity;
import com.jcxx.saas.modules.sys.entity.SysUserEntity;
import com.taobao.api.ApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DepartmentUtil {

    private static final HashMap<Long, Integer> depts = new HashMap<>();

    static {
        //公司
//        depts.put(1L, 0);
        //总经办
        depts.put(865806900L, 1);
        //市场部
        depts.put(893067887L, 0);
        //软件销售部
        depts.put(893402186L, 0);
        //大客户销售部
        depts.put(893400207L, 1);
        //综合管理部
        depts.put(865900908L, 1);
        //其他部门
        depts.put(12345678L, 2);
    }

    public static List<DepartmentEntity> getDeptList(String accessToken) {
        //部门列表
        List<DepartmentEntity> departmentEntities;
        //获取部门列表
        try {
            List<OapiDepartmentListResponse.Department> departments = getDepartments(accessToken);
            //通过部门id查询用户列表
            departmentEntities = departments.stream()
                    //只需要特定的几个部门,比如：总经办、市场部
                    .filter(ding -> depts.containsKey(ding.getId()))
                    .map(ddept -> {
                        DepartmentEntity departmentEntity = new DepartmentEntity();
                        departmentEntity.setDeptName(ddept.getName());
                        departmentEntity.setParentDeptId(ddept.getParentid());
                        departmentEntity.setDeptId(ddept.getId());
                        departmentEntity.setSort(depts.get(ddept.getId()));
                        return departmentEntity;
                    }).collect(Collectors.toList());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return departmentEntities;
    }

    public static List<SysUserEntity> getUserList(String accessToken, List<DepartmentEntity> deptList) {
        //用户id列表
        List<String> userIds = new ArrayList<>();
        //用户列表
        List<SysUserEntity> sysUserEntities = new ArrayList<>();
        try {
            //通过部门id获取用户id列表
            for (DepartmentEntity departmentEntity : deptList) {
                //不需要市场部下面的一级用户
                if (!departmentEntity.getDeptId().equals(893067887L)) {
                    //获取部门下的用户id
                    userIds.addAll(getUseridListByDept(accessToken, departmentEntity.getDeptId()));
                }
            }
            //通过用户id查询用户详情信息
            for (String userId : userIds) {
                OapiV2UserGetResponse.UserGetResponse result = getSysUserEntity(accessToken, userId);
                SysUserEntity sysUserEntity = new SysUserEntity();
                //当用户没有角色时，接口不会返回此字段，list会为空
                if (result.getRoleList() != null) {
                    List<String> roleNames = result.getRoleList()
                            .stream()
                            .map(OapiV2UserGetResponse.UserRole::getName)
                            .collect(Collectors.toList());
                    //目前只需要每个部门的部门经理,部门经理在钉钉中的角色为“主管”
                    if (roleNames.contains("主管")) {
                        sysUserEntity.setUsername(result.getUserid());
                        sysUserEntity.setMobile(result.getMobile());
                        sysUserEntity.setName(result.getName());
                        sysUserEntity.setPosition(result.getTitle());
                        sysUserEntity.setDeptId(result.getDeptIdList().get(0));
                        //目前所需用户中，只有许总有两个部门
                        if (result.getUserid().equals("manager5148")) {
                            sysUserEntity.setDeptId(865806900L);
                        }
                        //张俊宇的部门改为其他部门
                        if (result.getUserid().equals("08476067381184139")) {
                            sysUserEntity.setDeptId(12345678L);
                        }
                    }
                }
                //用户没有角色列表，代表他是普通用户，单独获取闫彩峰人员数据
                if (result.getUserid().equals("022700444355774889")) {
                    sysUserEntity.setUsername(result.getUserid());
                    sysUserEntity.setMobile(result.getMobile());
                    sysUserEntity.setName(result.getName());
                    sysUserEntity.setDeptId(12345678L);
                }
                sysUserEntities.add(sysUserEntity);
            }
        } catch (ApiException e) {
            throw new SaaSException("网络错误:" + e.getErrMsg());
        }
        return sysUserEntities;
    }

    /**
     * 通过userid获取用户详情信息
     *
     * @param accessToken 访问凭证
     * @param userId      用户id
     * @return 用户详情信息
     * @throws ApiException
     */
    private static OapiV2UserGetResponse.UserGetResponse getSysUserEntity(String accessToken, String userId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(DingDingUrlConstant.USER_GET_URL);
        OapiV2UserGetRequest req = new OapiV2UserGetRequest();
        req.setUserid(userId);
        req.setLanguage("zh_CN");
        OapiV2UserGetResponse rsp = client.execute(req, accessToken);
        if (0 != rsp.getErrcode()) {
            throw new RuntimeException(rsp.getErrmsg());
        }
        return rsp.getResult();
    }

    /**
     * 获取部门下面所有用户id
     *
     * @param accessToken 访问凭证
     * @param deptId      部门id
     * @return 用户id列表
     * @throws ApiException
     */
    private static List<String> getUseridListByDept(String accessToken, Long deptId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(DingDingUrlConstant.DEPARTMENT_USERID_LIST);
        OapiUserListidRequest req = new OapiUserListidRequest();
        req.setDeptId(deptId);
        OapiUserListidResponse rsp = client.execute(req, accessToken);
        if (0 != rsp.getErrcode()) {
            throw new RuntimeException(rsp.getErrmsg());
        }
        return rsp.getResult().getUseridList();
    }

    /**
     * 获取部门列表
     *
     * @param accessToken 访问凭证
     * @return 部门列表
     * @throws ApiException
     */
    private static List<OapiDepartmentListResponse.Department> getDepartments(String accessToken) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(DingDingUrlConstant.DEPARTMENT_LIST);
        OapiDepartmentListRequest request = new OapiDepartmentListRequest();
        request.setHttpMethod("GET");
        OapiDepartmentListResponse response = client.execute(request, accessToken);
        if (0 != response.getErrcode()) {
            throw new RuntimeException(response.getErrmsg());
        }
        return response.getDepartment();
    }
}
