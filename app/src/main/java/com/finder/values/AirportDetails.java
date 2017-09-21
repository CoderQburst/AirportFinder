package com.finder.values;

import java.util.ArrayList;

public class AirportDetails {
    private String name, address, detail, phoneNo;

    public void putData(ArrayList<String> data) {
        this.name = data.get(0);
        this.detail = data.get(1);
        this.address = data.get(2);
        this.phoneNo = data.get(3);
    }

    public String getName() {
        return this.name;
    }

    public String getDetail() {
        return this.detail;
    }

    public String getAddress() {
        return this.address;
    }

    public String getPhoneNo() {
        return this.phoneNo;
    }
}