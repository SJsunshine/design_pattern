package strategy.service.impl;

import strategy.service.QuackBehavior;

/**安静**/
public class MuteQuack implements QuackBehavior {

	@Override
	public String quack() {
		return "Silence";
	}
}
