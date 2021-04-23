package com.flow.saga.entity;

/**
 * 事务类型
 */

public enum SagaTransactionTypeEnum {
    ROLLBACK(1, "回滚"),
    RE_EXECUTE(2, "重新执行"),
    CONFIG_BY_EXCEPTION(3, "回滚或者重新执行(通过子事务异常配置)");

    private int type;
    private String desc;

    SagaTransactionTypeEnum(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
