package com.wl.wlflatproject.support.sdk.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class GeneralEnableBean {
	@JSONField(name = "OverWrite")
	private boolean overWrite;

	public boolean isOverWrite() {
		return overWrite;
	}

	public void setOverWrite(boolean overWrite) {
		this.overWrite = overWrite;
	}
}
