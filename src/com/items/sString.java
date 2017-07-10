package com.items;

import java.io.Serializable;
import java.util.List;

public class sString implements Serializable {
	private static final long serialVersionUID = 3L;
	private List<String> strings;
	public sString(List<String> strings) {
		super();
		this.strings = strings;
	}
	public List<String> getStrings() {
		return strings;
	}
	public void setStrings(List<String> strings) {
		this.strings = strings;
	}
}
