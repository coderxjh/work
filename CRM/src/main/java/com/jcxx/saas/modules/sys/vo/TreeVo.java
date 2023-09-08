package com.jcxx.saas.modules.sys.vo;

import lombok.Data;

import java.util.List;

@Data
public class TreeVo {

    private Long id;
    private Long parentId;
    private String name;
    private Integer sort;
    private List<TreeVo> children;
}
