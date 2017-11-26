package headfirst.duck.service.impl;

import headfirst.duck.service.QuackBehavior;

/**呱呱叫**/
public class Quack implements QuackBehavior {

	@Override
	public String quack() {
		return "Quack";
	}
}
