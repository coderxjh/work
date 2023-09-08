package com.jcxx.saas.modules.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcxx.saas.modules.sys.dao.DepartmentDao;
import com.jcxx.saas.modules.sys.entity.DepartmentEntity;
import com.jcxx.saas.modules.sys.entity.SysUserEntity;
import com.jcxx.saas.modules.sys.service.DepartmentService;
import com.jcxx.saas.modules.sys.service.SysUserService;
import com.jcxx.saas.modules.sys.utils.TreeUtil;
import com.jcxx.saas.modules.sys.vo.DepartmentTreeVo;
import com.jcxx.saas.modules.sys.vo.TreeVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service("departmentService")
public class DepartmentServiceImpl extends ServiceImpl<DepartmentDao, DepartmentEntity> implements DepartmentService {

    @Resource
    private DepartmentDao departmentDao;

    @Resource
    private SysUserService sysUserService;

    @Override
    public List<TreeVo> queryTree() {
        //先查询所有部门列表
        List<DepartmentEntity> departments = list();
        //声明一个集合，用来保存结果
        List<TreeVo> deptTrees = new ArrayList<>();
//        //遍历查询出来的结果，确定顶级父部门
        for (DepartmentEntity d : departments) {
            //首先找到顶级父部门的id
            //如果相等为第一级，这里“1L”表示顶级父部门id
            if (d.getParentDeptId().equals(1L)) {
                //创建树结构对象
                TreeVo deptTree = new DepartmentTreeVo();
                //实例树对象
                deptTree.setId(d.getDeptId());
                deptTree.setName(d.getDeptName());//第一级名字(根)
                deptTree.setSort(d.getSort());
                //将顶级父部门的id穿进去
                deptTree.setChildren(getDepartmentList(d.getDeptId(), departments));
                //将最终结构封装到集合
                deptTrees.add(deptTree);
            }
        }
        TreeUtil.recSort(deptTrees);
        return deptTrees;
    }

    /**
     * @param pid         所传为最大父级的id，默认值为 1L
     * @param departments 所有部门信息集合
     * @return 顶级父部门下的部门组织树列表
     */
    public List<TreeVo> getDepartmentList(Long pid, List<DepartmentEntity> departments) {
        List<TreeVo> deptTrees = new ArrayList<>();
        for (DepartmentEntity d : departments) {
            //如果所有pid等于父级id
            if (pid.equals(d.getParentDeptId())) {
                DepartmentTreeVo deptTree = new DepartmentTreeVo();
                deptTree.setId(d.getDeptId());
                deptTree.setName(d.getDeptName());//首次第一级名字
                deptTree.setSort(d.getSort());
                deptTree.setChildren(getDepartmentList(d.getDeptId(), departments));//递归
                deptTrees.add(deptTree);
            }
        }
        return deptTrees;
    }

    @Override
    public List<SysUserEntity> queryUserList(Long deptId, String status, String nameAndPhone) {
        List<DepartmentEntity> departmentEntities = departmentDao.selectUserListByDeptId(deptId);
        return sysUserService.queryByDeptIds(
                departmentEntities.stream()
                        .map(DepartmentEntity::getDeptId)
                        .collect(Collectors.toList()),status,nameAndPhone);
    }
}