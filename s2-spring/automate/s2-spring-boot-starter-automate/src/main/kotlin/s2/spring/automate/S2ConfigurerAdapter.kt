package s2.spring.automate

import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import s2.automate.core.AutomateExecutor
import s2.automate.core.AutomateExecutorCore
import s2.automate.core.TransitionStateGuard
import s2.automate.core.appevent.publisher.AutomateAppEventPublisher
import s2.automate.core.context.AutomateContext
import s2.automate.core.guard.GuardExecutorImpl
import s2.automate.core.persist.AutotmatePersister
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2State
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2State
import s2.spring.automate.persister.SpringEventPublisher

abstract class S2ConfigurerAdapter<STATE, ID, ENTITY, out AGGREGATE> : InitializingBean where
STATE : S2State,
ENTITY : WithS2State<STATE>,
ENTITY : WithS2Id<ID>,
AGGREGATE :  S2SpringAggregate<STATE, ID, ENTITY> {

	@Autowired
	private lateinit var eventPublisher: SpringEventPublisher;

	open fun aggregate(): AutomateExecutor<STATE, ID, ENTITY> {
		val automateContext = automateContext()
		val publisher = automateAppEventPublisher(eventPublisher)
		val guardExecutor = guardExecutor(publisher)
		val persister = aggregateRepository()
		return AutomateExecutorCore(automateContext, guardExecutor, persister, publisher)
	}

//	@Bean
//	open fun aggregateBuilder(): AGGREGATE {
//		val automateExecutor = aggregate()
//		val agg = s2SpringAggregate()
//		agg.withContext(automateExecutor, eventPublisher)
//		return agg
//	}

	protected open fun automateContext() = AutomateContext(automate(), guards())

	protected open fun guardExecutor(automateAppEventPublisher: AutomateAppEventPublisher<STATE, ID, ENTITY>): GuardExecutorImpl<STATE, ID, ENTITY> {
		return GuardExecutorImpl(
			guards = guards(),
			publisher = automateAppEventPublisher
		)
	}

	protected open fun automateAppEventPublisher(eventPublisher: SpringEventPublisher): AutomateAppEventPublisher<STATE, ID, ENTITY> {
		return AutomateAppEventPublisher(eventPublisher)
	}

	protected open fun guards() =
		listOf(TransitionStateGuard<STATE, ID, ENTITY>())

	override fun afterPropertiesSet() {
		val automateExecutor = aggregate()
		val agg = s2SpringAggregate()
		agg.withContext(automateExecutor, eventPublisher)
	}

	abstract fun aggregateRepository(): AutotmatePersister<STATE, ID, ENTITY>

	abstract fun automate(): S2Automate
	abstract fun s2SpringAggregate(): AGGREGATE

}