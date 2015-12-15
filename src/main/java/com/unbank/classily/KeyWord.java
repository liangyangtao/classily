package com.unbank.classily;

/**
 * Created by Administrator on 2015/11/24.
 */
public class KeyWord implements Comparable<KeyWord> {

	private Integer name;
	private double value;

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public Integer getName() {
		return name;
	}

	public void setName(Integer name) {
		this.name = name;
	}

	public KeyWord(Integer name, double value) {
		this.name = name;
		this.value = value;
	}

	public int compareTo(KeyWord word) {
		if (this.getValue() > word.getValue()) {
			return 1;
		} else if (this.getValue() < word.getValue()) {
			return -1;
		} else {
			return 0;
		}
	}
}
