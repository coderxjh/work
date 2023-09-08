package com.jcxx.saas.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 企业内部应用访问token
 */
@Data
@TableName("app_token")
public class AppTokenEntity {
    /**
     * id
     */
    @TableId("id")
    private Long id;

    /**
     * 钉钉企业内部应用accessToken
     */
    private String appToken;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 失效时间
     */
    private Date expireTime;

}