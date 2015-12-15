package com.unbank.classily;

import java.util.Map;

public class Tmodel {

	private String name;
	private double pz;
	private Map<Integer,Double> pwz;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getPz() {
		return pz;
	}
	public void setPz(double pz) {
		this.pz = pz;
	}
	public Map<Integer, Double> getPwz() {
		return pwz;
	}
	public void setPwz(Map<Integer, Double> pwz) {
		this.pwz = pwz;
	}
}
