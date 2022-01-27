package s2.dsl.automate

import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
@JsName("S2Automate")
class S2Automate(
	val name: String,
	val init: S2InitTransition,
	val transitions: Array<S2Transition>,
	val subMachines: Array<S2SubMachine>
):Automate {
	override fun getAvailableTransitions(state: S2State): List<S2Transition> {
		return transitions.filter { isSameState(it.from, state) }
	}

	override fun isAvailableTransition(currentState: S2State, command: Msg): Boolean {
		return getAvailableTransitions(currentState).any { it.command.isInstance(command) }
	}

	override fun isAvailableInitTransition(command: S2InitCommand): Boolean {
		return init.command.isInstance(command)
	}

	override fun isFinalState(state: S2State): Boolean {
		return getAvailableTransitions(state).isEmpty()
	}

	override fun isSameState(from: S2State?, to: S2State): Boolean {
		return from?.position == to.position
	}
}
