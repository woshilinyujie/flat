package com.wl.wlflatproject.support.sdk.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FindDeviceEnableBean {
	@JSONField(name = "Enable")
	private boolean enable;

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
}
