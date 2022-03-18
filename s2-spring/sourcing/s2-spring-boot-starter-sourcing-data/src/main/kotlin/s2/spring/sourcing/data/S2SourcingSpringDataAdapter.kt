package s2.spring.sourcing.data

import org.springframework.context.annotation.Bean
import org.springframework.data.repository.core.support.ReactiveRepositoryFactorySupport
import s2.dsl.automate.Evt
import s2.dsl.automate.S2State
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2State
import s2.sourcing.dsl.snap.SnapRepository
import s2.sourcing.dsl.view.View
import s2.spring.automate.sourcing.S2AutomateDeciderSpring
import s2.spring.automate.sourcing.S2AutomateDeciderSpringAdapter
import s2.spring.sourcing.data.event.EventPersisterData
import s2.spring.sourcing.data.event.SpringDataEventRepository

abstract class S2SourcingSpringDataAdapter<ENTITY, STATE, EVENT, ID, EXECUTER>(
	executor: EXECUTER,
	view: View<EVENT, ENTITY>,
	snapRepository: SnapRepository<ENTITY, ID>? = null
)
	: S2AutomateDeciderSpringAdapter<ENTITY, STATE, EVENT, ID, EXECUTER>(executor, view, snapRepository) where
STATE : S2State,
ENTITY : WithS2State<STATE>,
ENTITY : WithS2Id<ID>,
EVENT: Evt,
EVENT: WithS2Id<ID>,
EXECUTER : S2AutomateDeciderSpring<ENTITY, STATE, EVENT, ID> {

	@Bean
	open fun eventStore(
		eventRepository: SpringDataEventRepository<EVENT, ID>
	): EventPersisterData<EVENT, ID> {
		return EventPersisterData(eventRepository)
	}


	@Bean
	open fun springDataEventRepository(repositoryFactorySupport: ReactiveRepositoryFactorySupport): SpringDataEventRepository<EVENT, ID> {
		return repositoryFactorySupport.getRepository(SpringDataEventRepository::class.java) as SpringDataEventRepository<EVENT, ID>
	}

}