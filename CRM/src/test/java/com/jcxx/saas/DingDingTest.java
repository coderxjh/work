package com.jcxx.saas;

import com.aliyun.dingtalkoauth2_1_0.Client;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.Common;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiUserListidRequest;
import com.dingtalk.api.request.OapiV2DepartmentListsubRequest;
import com.dingtalk.api.response.OapiUserListidResponse;
import com.dingtalk.api.response.OapiV2DepartmentListsubResponse;
import com.jcxx.saas.constant.AppConstant;
import com.jcxx.saas.modules.sys.entity.DepartmentEntity;
import com.jcxx.saas.modules.sys.service.AppTokenService;
import com.jcxx.saas.modules.sys.service.DepartmentService;
import com.jcxx.saas.modules.sys.utils.DepartmentUtil;
import com.taobao.api.ApiException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DingDingTest {

    @Resource
    private AppTokenService appTokenService;

    @Resource
    private DepartmentService departmentService;

    @Test
    public void testDepartment() {
        long start = System.currentTimeMillis();
        String accessToken = appTokenService.getAccessToken();
        //获取钉钉部门列表
        List<DepartmentEntity> dingDeptList = DepartmentUtil.getDeptList(accessToken);
        //获取数据库部门列表
        List<DepartmentEntity> dataDeptList = departmentService.list();
        if (dataDeptList == null || dataDeptList.isEmpty()) {
//            for (DepartmentEntity departmentEntity : dingDeptList) {
//                departmentService.save(departmentEntity);
//            }
            departmentService.saveBatch(dingDeptList);
            return;
        }
        for (DepartmentEntity dingEntity : dingDeptList) {
            for (DepartmentEntity dataEntity : dataDeptList) {
                if (dingEntity.getId().equals(dataEntity.getId())) {
                    if (!dingEntity.getDeptName().equals(dataEntity.getDeptName())
                            || !dingEntity.getParentDeptId().equals(dataEntity.getParentDeptId())) {
                        departmentService.update(dingEntity, new QueryWrapper<DepartmentEntity>()
                                .eq("dept_id", dataEntity.getId()));
                    }
                    dingDeptList.remove(dingEntity);
                    dataDeptList.remove(dataEntity);
                }
            }
        }
        if (!dingDeptList.isEmpty()) {
            departmentService.saveBatch(dingDeptList);
        }
        if (!dataDeptList.isEmpty()) {
            for (DepartmentEntity departmentEntity : dataDeptList) {
                departmentService.removeById(departmentEntity.getId());
            }
        }
        long end = System.currentTimeMillis();
        long time = (end - start) / 1000;
        System.out.println(time + "秒");
    }

    /**
     * 使用 Token 初始化账号Client
     *
     * @return Client
     * @throws Exception
     */
    public static Client createClient() throws Exception {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        return new Client(config);
    }

    @Test
    public void getAccessToken() throws Exception {
        Client client = Sample.createClient();
        GetAccessTokenRequest getAccessTokenRequest = new GetAccessTokenRequest().setAppKey(AppConstant.APP_KEY).setAppSecret(AppConstant.APP_SECRET);
        try {
            GetAccessTokenResponse response = client.getAccessToken(getAccessTokenRequest);
            System.out.println("response = " + response.getBody().getAccessToken());
        } catch (TeaException err) {
            if (!Common.empty(err.code) && !Common.empty(err.message)) {
                // err 中含有 code 和 message 属性，可帮助开发定位问题
                System.out.println("错误代码 = " + err.code);
                System.out.println("错误信息 = " + err.message);
            }

        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            if (!Common.empty(err.code) && !Common.empty(err.message)) {
                // err 中含有 code 和 message 属性，可帮助开发定位问题
                System.out.println("错误代码 = " + err.code);
                System.out.println("错误信息 = " + err.message);
            }

        }
    }

    /**
     * 递归获取所有部门列表
     *
     * @throws ApiException
     */
    @Test
    public void getDepartUserIdList() throws ApiException {
        List<DepartEntity> departs = null;
        try {
            departs = getDepart(1L);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        System.out.println(departs);
        for (DepartEntity depart : departs) {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/user/listid");
            OapiUserListidRequest req = new OapiUserListidRequest();
            req.setDeptId(depart.getId());
            OapiUserListidResponse rsp = client.execute(req, appTokenService.getAccessToken());
            OapiUserListidResponse.ListUserByDeptResponse result = rsp.getResult();
            System.out.println(result.getUseridList());
        }
    }


    public List<DepartEntity> getDepart(Long deptId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/department/listsub");
        OapiV2DepartmentListsubRequest req = new OapiV2DepartmentListsubRequest();
        req.setDeptId(deptId);
        req.setLanguage("zh_CN");
        OapiV2DepartmentListsubResponse rsp = client.execute(req, appTokenService.getAccessToken());
        List<OapiV2DepartmentListsubResponse.DeptBaseResponse> result = rsp.getResult();
        List<DepartEntity> entities = new ArrayList<>();
        if (result == null || result.isEmpty()) {
            return null;
        }
        for (OapiV2DepartmentListsubResponse.DeptBaseResponse response : result) {
            DepartEntity entity = new DepartEntity();
            entity.setName(response.getName());
            entity.setId(response.getDeptId());
            entity.setParentId(response.getParentId());
            entities.add(entity);
            List<DepartEntity> depart = getDepart(response.getDeptId());
            if (depart != null) {
                entities.addAll(depart);
            }
        }
        return entities;
    }

}