package s2.automate.core.appevent.listener

import s2.automate.core.appevent.*
import s2.dsl.automate.S2State
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2State

open class AutomateListenerAdapter<STATE, ID, ENTITY> : AutomateListener<STATE, ID, ENTITY>
where STATE : S2State, ENTITY : WithS2State<STATE>, ENTITY : WithS2Id<ID>
{

	override fun automateStateEntered(event: AutomateStateEntered) {}

	override fun automateStateExited(event: AutomateStateExited) {}

	override fun automateTransitionNotAccepted(event: AutomateTransitionNotAccepted) {}

	override fun automateInitTransitionStarted(event: AutomateInitTransitionStarted) {}

	override fun automateInitTransitionEnded(event: AutomateInitTransitionEnded<STATE, ID, ENTITY>) {}

	override fun automateTransitionStarted(event: AutomateTransitionStarted) {}

	override fun automateTransitionEnded(event: AutomateTransitionEnded<STATE, ID, ENTITY>) {}

	override fun automateTransitionError(event: AutomateTransitionError) {}

	override fun automateSessionStarted(event: AutomateSessionStarted) {}

	override fun automateSessionStopped(event: AutomateSessionStopped) {}

	override fun automateSessionError(event: AutomateSessionError) {}

}