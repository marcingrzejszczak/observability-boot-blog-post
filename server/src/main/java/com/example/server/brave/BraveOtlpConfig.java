package com.example.server.brave;

import brave.Tags;
import brave.handler.MutableSpan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.BytesEncoder;
import zipkin2.reporter.otel.brave.OtelEncoder;

@Configuration(proxyBeanMethods = false)
public class BraveOtlpConfig {

  @Bean
  BytesEncoder<MutableSpan> otlpMutableSpanBytesEncoder() {
    return new OtelEncoder(Tags.ERROR);
  }

}
