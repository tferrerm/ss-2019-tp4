package ar.edu.itba.ss.tpe4;

import java.util.Arrays;
import java.util.Optional;

public enum Mode {
	
	OSCILLATOR (0),
	LENNARD_JONES_GAS (1);
	
	private int mode;
	
	Mode(final int mode) {
		this.mode = mode;
	}
	
	public int getMode() {
		return mode;
	}
	
	public static Optional<Mode> valueOf(final int value) {
		return Arrays.stream(values()).filter(m -> m.getMode() == value).findFirst();
	}

}
