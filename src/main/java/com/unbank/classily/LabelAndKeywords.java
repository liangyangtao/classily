package com.unbank.classily;

import java.util.Arrays;

/**
 * Created by Administrator on 2015/12/10.
 */
public class LabelAndKeywords {
    private String label;
    private String[] keywords;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public LabelAndKeywords(String label, String[] keywords) {
        this.label = label;
        this.keywords = keywords;
    }

    @Override
    public String toString() {
        return "LabelAndKeywords{" +
                "label='" + label + '\'' +
                ", keywords=" + Arrays.toString(keywords) +
                '}';
    }
}
