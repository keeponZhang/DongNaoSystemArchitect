package com.zhang.designpattern.stategy;

/**
 * @创建者 keepon
 * @创建时间 2019/4/22 0022 上午 11:26
 * @描述 ${TODO}
 * @版本 $$Rev$$
 * @更新者 $$Author$$
 * @更新时间 $$Date$$
 */
public class Client {
	public static  void main(String[] args){
		Strategy strategy = new Strategy1();
		strategy.stategyInterface(22);
	}
}
