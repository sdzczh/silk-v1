package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

/**
 * @author Zhang Jinwei
 * @date 2018年01月26日
 */
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanMemberAddress {
    private long id;
    private String unit;
    private String remark;
    private String address;

    public static ScanMemberAddress toScanMemberAddress(MemberAddress memberAddress) {
        return ScanMemberAddress.builder().id(memberAddress.getId())
                .address(memberAddress.getAddress())
                .remark(memberAddress.getRemark())
                .unit(memberAddress.getCoin().getUnit())
                .build();
    }
}
