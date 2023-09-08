package com.jcxx.saas.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("department")
public class DepartmentEntity {
    /**
     *
     */
    @TableId
    private Long id;

    /**
     * 部门id
     */
    private Long deptId;

    /**
     * 部门名字
     */
    private String deptName;

    /**
     * 父级部门id
     */
    private Long parentDeptId;

    /**
     * 排序
     */
    private Integer sort;
}