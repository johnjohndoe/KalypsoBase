/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.warehouse;

public class Data {
	private int stock;
	private int minimum;
	private int maximum;

	public int getStock() {
		return stock;
	}
	public void setStock(int stock) {
		this.stock = stock;
	}

	public int getMinimum() {
		return minimum;
	}
	public void setMinimum(int minimum) {
		this.minimum = minimum;
	}

	public int getMaximum() {
		return maximum;
	}
	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}

}
