package com.ctrip.sysdev.apptools.daogen.dao.enums;

public enum FlagsEnum {
	COMMIT(1),
	TEST(2);

	private int intVal;

	FlagsEnum(int intVal) {
		this.intVal = intVal;
	}

	public int getIntVal() {
		return intVal;
	}
}
