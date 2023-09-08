package com.jcxx.saas.modules.sys.utils;

import com.jcxx.saas.modules.sys.vo.TreeVo;

import java.util.Comparator;
import java.util.List;

public class TreeUtil {


    /**
     * 对组织树进行排序
     *
     * @param trees 部门列表
     */
    public static void recSort(List<TreeVo> trees) {
        if (trees == null) return;
        trees.sort(Comparator.comparingInt(TreeVo::getSort));
        for (TreeVo tree : trees) {
            recSort(tree.getChildren());
        }
    }
}
