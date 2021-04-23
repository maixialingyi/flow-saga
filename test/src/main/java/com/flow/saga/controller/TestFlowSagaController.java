package com.flow.saga.controller;

import com.flow.saga.service.FlowSagaServiceRequestDTO;
import com.flow.saga.service.TestExMainFlowSagaServiceImpl;
import com.flow.saga.service.TestNormalMainFlowSagaServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "saga功能测试")
@RestController
public class TestFlowSagaController {

    @Autowired
    private TestNormalMainFlowSagaServiceImpl testNormalMainFlowSagaServiceImpl;

    @Autowired
    private TestExMainFlowSagaServiceImpl testExMainFlowSagaServiceImpl;

    @ApiOperation(value = "正常执行")
    @GetMapping("/testNormal")
    public Object testNormal(){
        FlowSagaServiceRequestDTO requestDTO = new FlowSagaServiceRequestDTO();
        requestDTO.setBizSerialNo(String.valueOf(System.currentTimeMillis()));
        requestDTO.setUserId(1l);
        return testNormalMainFlowSagaServiceImpl.testMainFlowNomal(requestDTO);
    }

    @ApiOperation(value = "异常测试")
    @GetMapping("/testEx")
    public Object testEx(){
        FlowSagaServiceRequestDTO requestDTO = new FlowSagaServiceRequestDTO();
        return testExMainFlowSagaServiceImpl.testTransactionMainFlowEx(requestDTO);
    }

    @ApiOperation(value = "恢复")
    @GetMapping("/testRevocer")
    public Object testRevocer(){
        FlowSagaServiceRequestDTO requestDTO = new FlowSagaServiceRequestDTO();
        //return testNormalFlowSagaServiceImpl.testTransaction_main_1(requestDTO);
        return null;
    }
}
