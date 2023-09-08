package com.jcxx.saas.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcxx.saas.modules.sys.entity.DepartmentEntity;
import com.jcxx.saas.modules.sys.entity.SysUserEntity;
import com.jcxx.saas.modules.sys.vo.TreeVo;

import java.util.List;

public interface DepartmentService extends IService<DepartmentEntity> {
    List<TreeVo> queryTree();

    List<SysUserEntity> queryUserList(Long deptId, String status, String nameAndPhone);
}