package com.example.coolweather.db;

import org.litepal.crud.DataSupport;

public class Province extends DataSupport {
    private  int id;//省的id
    private String ProvinceName;//省的名字
    private int ProvinceCode;//省的代号

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return ProvinceName;
    }

    public void setProvinceName(String provinceName) {
        ProvinceName = provinceName;
    }

    public int getProvinceCode() {
        return ProvinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        ProvinceCode = provinceCode;
    }
}
