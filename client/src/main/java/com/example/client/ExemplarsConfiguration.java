package com.example.client;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.TracingAwareMeterObservationHandler;
import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// You must set this manually until this is registered in Boot - this gives you Exemplars support
@Configuration(proxyBeanMethods = false)
class ExemplarsConfiguration {

	@Bean
	TracingAwareMeterObservationHandler<Observation.Context> tracingAwareMeterObservationHandler(
			MeterRegistry meterRegistry, Tracer tracer) {
		return new TracingAwareMeterObservationHandler<>(new DefaultMeterObservationHandler(meterRegistry), tracer);
	}

	@Bean
	@ConditionalOnMissingBean
	SpanContextSupplier spanContextSupplier(ObjectProvider<Tracer> tracerProvider) {
		return new LazyTracingSpanContextSupplier(tracerProvider);
	}

	/**
	 * Since the MeterRegistry can depend on the {@link Tracer} (Exemplars) and the
	 * {@link Tracer} can depend on the MeterRegistry (recording metrics), this
	 * {@link SpanContextSupplier} breaks the circle by lazily loading the {@link Tracer}.
	 */
	static class LazyTracingSpanContextSupplier implements SpanContextSupplier, SmartInitializingSingleton {

		private final ObjectProvider<Tracer> tracerProvider;

		private Tracer tracer;

		LazyTracingSpanContextSupplier(ObjectProvider<Tracer> tracerProvider) {
			this.tracerProvider = tracerProvider;
		}

		@Override
		public String getTraceId() {
			return this.tracer.currentSpan().context().traceId();
		}

		@Override
		public String getSpanId() {
			return this.tracer.currentSpan().context().spanId();
		}

		@Override
		public boolean isSampled() {
			return this.tracer != null && isSampled(this.tracer);
		}

		private boolean isSampled(Tracer tracer) {
			Span currentSpan = tracer.currentSpan();
			return currentSpan != null && currentSpan.context().sampled();
		}

		@Override
		public void afterSingletonsInstantiated() {
			this.tracer = this.tracerProvider.getIfAvailable();
		}

	}

}
