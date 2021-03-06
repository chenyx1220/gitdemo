package com.ruipengkj.demo;

import javax.comm.CommPortIdentifier;

/**
 * 端口下拉列表对象
 */
public class ComboBoxItem {
	
	private String name;
	private CommPortIdentifier port;
	private String testNameCD;
	
	/**
	 * 构造方法
	 * @param name
	 * @param port
	 */
	public ComboBoxItem(String name, CommPortIdentifier port) {
		this.name = name;
		this.port = port;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public CommPortIdentifier getPort() {
		return port;
	}
	public void setPort(CommPortIdentifier port) {
		this.port = port;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	

}
