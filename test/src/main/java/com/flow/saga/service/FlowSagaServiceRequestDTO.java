package com.flow.saga.service;

import com.flow.saga.annotation.BizSerialNo;
import com.flow.saga.annotation.ShardRoutingKey;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FlowSagaServiceRequestDTO {

    /**
     * 程序中根据注解获取业务流水号，存入事务日志
     */
    @BizSerialNo
    private String bizSerialNo = "";

    /**
     * 如果分库分表需设置，分库分表健
     */
    @ShardRoutingKey
    private Long userId = 0L;
}
