package com.wl.wlflatproject.support.config;

public interface JsonListener {
	String getSendMsg();

	boolean onParse(String json);
}
