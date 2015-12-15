package com.unbank.classily;

import java.util.Map;

public class SoftmaxModelResult {

	private double[][] W;
	private double[] b;
	private Map<Integer,String> index2Class;
	public double[][] getW() {
		return W;
	}
	public void setW(double[][] w) {
		W = w;
	}
	public double[] getB() {
		return b;
	}
	public void setB(double[] b) {
		this.b = b;
	}
	public Map<Integer, String> getIndex2Class() {
		return index2Class;
	}
	public void setIndex2Class(Map<Integer, String> index2Class) {
		this.index2Class = index2Class;
	}
	public SoftmaxModelResult(double[][] w, double[] b,
			Map<Integer, String> index2Class) {
		W = w;
		this.b = b;
		this.index2Class = index2Class;
	}
	public SoftmaxModelResult() {}
	
}
