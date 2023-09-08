package com.jcxx.saas.modules.sys.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcxx.saas.common.utils.GsonUtil;
import com.jcxx.saas.modules.sys.entity.DepartmentEntity;
import com.jcxx.saas.modules.sys.entity.SysUserEntity;
import com.jcxx.saas.modules.sys.service.AppTokenService;
import com.jcxx.saas.modules.sys.service.DepartmentService;
import com.jcxx.saas.modules.sys.service.SysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Component
@EnableScheduling//开启定时任务
public class FetchUserScheduler {

    private static DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:MM:ss");

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private AppTokenService appTokenService;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private DepartmentService departmentService;


    /**
     * 每24小时获取钉钉的人员数据，与数据库进行比对
     */
    @Scheduled(cron = "0 0 0 * * ?") //
//    @Scheduled(cron = "*/5 * * * * ?")//每隔5秒执行一次，以空格分隔
    @Transactional
    public void cron() {
        String accessToken = appTokenService.getAccessToken();
        //获取钉钉部门列表
        List<DepartmentEntity> dingDeptList = DepartmentUtil.getDeptList(accessToken);
        //获取钉钉用户列表
        List<SysUserEntity> dingUserList = DepartmentUtil.getUserList(accessToken, dingDeptList);
        //获取数据库用户列表，只获取状态为1的用户
        List<SysUserEntity> dataUserList = sysUserService.queryAll();
        List<SysUserEntity> dingremove = new LinkedList<>(dingUserList);
        List<SysUserEntity> dataremove = new LinkedList<>(dataUserList);
        for (SysUserEntity dinguser : dingUserList) {
            for (SysUserEntity datauser : dataUserList) {
                //这里username在数据库中存储的是钉钉中的userid
                if (dinguser.getUsername().equals(datauser.getUsername())) {
                    if (!(dinguser.getName().equals(datauser.getName()))
                            || !(dinguser.getMobile().equals(datauser.getMobile()))
                            || !(dinguser.getDeptId().equals(datauser.getDeptId()))
                            || !(dinguser.getPosition().equals(datauser.getPosition()))) {
                        sysUserService.update(dinguser, new QueryWrapper<SysUserEntity>()
                                .eq("username", dinguser.getUsername()));
                    }
                    //一旦两个list中有相同的userid，就同时移除此数据
                    dingremove.remove(dinguser);
                    dataremove.remove(datauser);
                }
            }
        }
        //for循环结束之后，dingremove有数据代表钉钉有新成员加入
        if (!dingremove.isEmpty()) {
            //临时记录状态为0的用户
            List<SysUserEntity> tmp = new LinkedList<>();
            dingremove.forEach(u -> {
                //之前查询所有用户是查状态为1的
                //判断新增的成员是不是之前被禁用的
                SysUserEntity user = sysUserService.getOne(new QueryWrapper<SysUserEntity>()
                        .eq("username", u.getUsername()));
                //如果有相同的用户名则吧状态改为1，无需新增
                if (user != null && u.getStatus() == 0) {
                    user.setStatus(1);
                    tmp.add(user);
                }
                u.setStatus(1);
                //插入的用户默认为最高级
                u.setParentId(0L);
            });
            if (!tmp.isEmpty()) {
                dingremove.removeAll(tmp);
            }
            sysUserService.saveBatch(dingremove);
        }
        //dataremove有数据代表钉钉中有成员离开
        if (!dataremove.isEmpty()) {
            dataremove.forEach(u -> {
                u.setStatus(0);
            });
            //将钉钉中不存在的用户但数据库中存在的用户移出
            sysUserService.updateBatchById(dataremove);
        }
    }

//            @Scheduled(cron = "*/5 * * * * ?")//每隔5秒执行一次，以空格分隔
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void doUpdateDept() {
        String accessToken = appTokenService.getAccessToken();
        //获取钉钉部门列表
        List<DepartmentEntity> dingDeptList = DepartmentUtil.getDeptList(accessToken);
        //获取数据库部门列表
        List<DepartmentEntity> dataDeptList = departmentService.list();
        //用来判断钉钉上有没有新增部门
        List<DepartmentEntity> dingremove = new ArrayList<>(dingDeptList);
        //用来判断数据上有没有新增部门
//        List<DepartmentEntity> dataremove = new ArrayList<>(dataDeptList);
        for (DepartmentEntity dingEntity : dingDeptList) {
            for (DepartmentEntity dataEntity : dataDeptList) {
                if (dingEntity.getDeptId().equals(dataEntity.getDeptId())) {
                    if (!dingEntity.getDeptName().equals(dataEntity.getDeptName())
                            || !dingEntity.getParentDeptId().equals(dataEntity.getParentDeptId())) {
                        departmentService.update(dingEntity, new QueryWrapper<DepartmentEntity>()
                                .eq("dept_id", dataEntity.getDeptId()));
                    }
                    dingremove.remove(dingEntity);
//                    dataremove.remove(dataEntity);
                }
            }
        }
        //不为空，钉钉上创建了新部门
        if (!dingremove.isEmpty()) {
            logger.info("钉钉上更新了新的部门：{}", GsonUtil.bean2Json(dingremove));
            departmentService.saveBatch(dingremove);
        }
        //不为空，数据库上创建了新部门
//        if (!dataremove.isEmpty()) {
//            departmentService.removeByIds(dataDeptList.stream().map(DepartmentEntity::getId).collect(Collectors.toList()));
//        }
    }
}