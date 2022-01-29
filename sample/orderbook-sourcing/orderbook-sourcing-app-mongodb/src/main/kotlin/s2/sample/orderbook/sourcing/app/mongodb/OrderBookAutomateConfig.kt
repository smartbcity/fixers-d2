package s2.sample.orderbook.sourcing.app.mongodb

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import s2.sample.subautomate.domain.OrderBookState
import s2.sample.subautomate.domain.model.OrderBook
import s2.sample.subautomate.domain.model.OrderBookId
import s2.sample.subautomate.domain.orderBook.OrderBookEvent
import s2.sample.subautomate.domain.orderBookAutomate
import s2.spring.automate.sourcing.S2AutomateDeciderSpring
import s2.spring.sourcing.data.S2SourcingSpringDataAdapter

@Configuration
class OrderBookAutomateConfig : S2SourcingSpringDataAdapter<
		OrderBook, OrderBookState, OrderBookEvent, OrderBookId, OrderBookS2Aggregate>() {
	override fun automate() = orderBookAutomate

	@Autowired
	lateinit var endableLoopS2Aggregate: OrderBookS2Aggregate

	override fun executor(): OrderBookS2Aggregate = endableLoopS2Aggregate
}

@Service
class OrderBookS2Aggregate : S2AutomateDeciderSpring<OrderBook, OrderBookState, OrderBookEvent, OrderBookId>()
