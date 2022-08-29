package com.huawei.bigdata.flink.examples.bean;



public class caseName {
    public String name;
    public String entry_time;
    public String name_manual_type;
    public String remark;
    public String update_time;

    public caseName() {
    }

    public caseName(String name, String entry_time, String name_manual_type, String remark, String update_time) {
        this.name = name;
        this.entry_time = entry_time;
        this.name_manual_type = name_manual_type;
        this.remark = remark;
        this.update_time = update_time;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getEntry_time() {
        return entry_time;
    }

    public void setEntry_time(String entry_time) {
        this.entry_time = entry_time;
    }

    public String getName_manual_type() {
        return name_manual_type;
    }

    public void setName_manual_type(String name_manual_type) {
        this.name_manual_type = name_manual_type;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", entry_time='" + entry_time + '\'' +
                ", name_manual_type='" + name_manual_type + '\'' +
                ", remark='" + remark + '\'' +
                ", update_time='" + update_time + '\'' +
                '}';
    }
}
