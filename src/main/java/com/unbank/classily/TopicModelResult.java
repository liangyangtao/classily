package com.unbank.classily;

import java.util.List;
import java.util.Map;

public class TopicModelResult {

	private Map<String,Integer> word2Index;
	private List<Tmodel> model;
	public Map<String, Integer> getWord2Index() {
		return word2Index;
	}
	public void setWord2Index(Map<String, Integer> word2Index) {
		this.word2Index = word2Index;
	}
	public List<Tmodel> getModel() {
		return model;
	}
	public void setModel(List<Tmodel> model) {
		this.model = model;
	}
	
}
