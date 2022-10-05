package com.example.server;

import java.util.Collections;
import java.util.Random;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.observation.aop.ObservedAspect;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.TracingAwareMeterObservationHandler;
import jakarta.servlet.DispatcherType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.observation.HttpRequestsObservationFilter;

@SpringBootApplication
@Import(ExemplarsConfiguration.class)
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

	// tag::filter[]
	// You must set this manually until this is registered in Boot
	@Bean
	FilterRegistrationBean observationWebFilter(ObservationRegistry observationRegistry) {
		FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new HttpRequestsObservationFilter(observationRegistry));
		filterRegistrationBean.setDispatcherTypes(DispatcherType.ASYNC, DispatcherType.ERROR, DispatcherType.FORWARD,
				DispatcherType.INCLUDE, DispatcherType.REQUEST);
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		// We provide a list of URLs that we want to create observations for
		filterRegistrationBean.setUrlPatterns(Collections.singletonList("/foo"));
		return filterRegistrationBean;
	}
	// end::filter[]

	// tag::aspect[]
	// To have the @Observed support we need to register this aspect
	@Bean
	ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
		return new ObservedAspect(observationRegistry);
	}
	// end::aspect[]

}

// tag::controller[]
@RestController
class MyController {

	private static final Logger log = LoggerFactory.getLogger(MyController.class);
	private final MyService myService;

	MyController(MyService myService) {
		this.myService = myService;
	}

	@GetMapping("/foo")
	String myMapping() {
		log.info("Got a request");
		return myService.foo();
	}
}
// end::controller[]

// tag::service[]
@Service
class MyService {

	private final Random random = new Random();

	// Example of using an annotation to observe methods
	@Observed(name = "foo.metric",
			contextualName = "my-contextual-name",
			lowCardinalityKeyValues = {"low.cardinality.key", "low cardinality value"})
	String foo() {
		try {
			Thread.sleep(random.nextLong(200L)); // simulates latency
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return "foo";
	}
}
// end::service[]

// tag::handler[]
// Example of plugging in a custom handler that in this case will print a statement before and after all observations take place
@Component
class MyHandler implements ObservationHandler<Observation.Context> {

	private static final Logger log = LoggerFactory.getLogger(MyHandler.class);

	@Override
	public void onStart(Observation.Context context) {
		log.info("Before running the observation for context [{}]", context.getName());
	}

	@Override
	public void onStop(Observation.Context context) {
		log.info("After running the observation for context [{}]", context.getName());
	}

	@Override
	public boolean supportsContext(Observation.Context context) {
		return true;
	}
}
// end::handler[]
