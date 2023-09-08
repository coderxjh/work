package com.jcxx.saas.modules.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcxx.saas.common.exception.SaaSException;
import com.jcxx.saas.common.utils.PageUtils;
import com.jcxx.saas.common.utils.Result;
import com.jcxx.saas.modules.sys.entity.SysUserEntity;
import com.jcxx.saas.modules.sys.service.DepartmentService;
import com.jcxx.saas.modules.sys.service.SysUserService;
import com.jcxx.saas.modules.sys.vo.TreeVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/sys/dept")
public class DepartmentController extends AbstractController {

    @Resource
    private DepartmentService departmentService;

    @Resource
    private SysUserService sysUserService;

    /**
     * 查询部门组织树
     */
    @GetMapping("/tree")
    public Result selectTreeList() {
        List<TreeVo> deptTree = departmentService.queryTree();
        return Result.ok().put("data", deptTree);
    }

    /**
     * 修改账号的状态
     *
     * @param status 0:禁用，1:正常
     * @param userId 用户id
     */
    @PutMapping("/edit-count")
    public Result editCount(@RequestParam Integer status, @RequestParam Integer userId) {
        boolean update = sysUserService.update(new QueryWrapper<SysUserEntity>().eq("status", status).eq("user_id", userId));
        if (!update) {
            throw new SaaSException("关闭失败");
        }
        return Result.ok();
    }

    /**
     * 根据部门id查询所有用户信息
     *
     * @param deptId 部门id
     */
    @GetMapping("/user-info/page")
    public Result getUserListByDeptId(@RequestParam("page") Integer page,
                                 @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,
                                 @RequestParam("deptId") Long deptId,
                                 @RequestParam(value = "status", required = false) String status,
                                 @RequestParam(value = "nameAndPhone", required = false) String nameAndPhone) {
        List<SysUserEntity> users = departmentService.queryUserList(deptId, status, nameAndPhone);
        return Result.ok().put("data", new PageUtils(users, users.size(), limit, page));
    }
}