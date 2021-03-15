package s2.spring.automate

import s2.dsl.automate.model.WithS2State
import s2.dsl.automate.S2Command
import s2.dsl.automate.S2InitCommand
import s2.dsl.automate.S2State

interface S2Aggregate<STATE: S2State, ID, ENTITY: WithS2State<STATE>> {

	suspend fun <EVENT> createWithEvent(command: S2InitCommand, to: STATE, buildEvent: suspend ENTITY.() -> EVENT, buildEntity: suspend () -> ENTITY): EVENT
	suspend fun <EVENT> createWithEvent(command: S2InitCommand, to: STATE, build: suspend () -> Pair<ENTITY, EVENT>): EVENT

	suspend fun <T> doTransition(command: S2Command<ID>, to: STATE, save: Boolean = true, exec: suspend ENTITY.() -> Pair<ENTITY, T>): T
	suspend fun <T> doTransition(id: ID, command: S2Command<ID>, to: STATE, save: Boolean = true, exec: suspend ENTITY.() -> Pair<ENTITY, T>): T

}