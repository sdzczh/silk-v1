package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.CommonStatus;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商家认证缴纳押金的币种和数量
 * @author zhang yingxin
 * @date 2018/5/5
 */
@Entity
@Data
@Table(name = "business_auth_deposit")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BusinessAuthDeposit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 押金币种
     */
    @OneToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;
    /**
     * 押金数额
     */
    @Column(columnDefinition = "decimal(18,8) comment '保证金数额'")
    private BigDecimal amount;

    private Date createTime;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;

    /**
     * 启用、禁用
     */
    @Enumerated(EnumType.ORDINAL)
    private CommonStatus status = CommonStatus.NORMAL;
}
