package com.zhang.designpattern.stategy;

/**
 * @创建者 keepon
 * @创建时间 2019/4/22 0022 上午 11:21
 * @描述 ${TODO}
 * @版本 $$Rev$$
 * @更新者 $$Author$$
 * @更新时间 $$Date$$
 */
public class Strategy3 implements Strategy {
	@Override
	public double stategyInterface(double price) {
		//返回低级会员打折后的价格
		return price*0.9;
	}
}
