/*
 * Copyright 2017 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.server;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ObservationTests {

    private static final Logger log = LoggerFactory.getLogger(ObservationTests.class);

    @Test
    void sample() {
        // @formatter:off
        // tag::example[]
        // Create an ObservationRegistry
        ObservationRegistry registry = ObservationRegistry.create();
        // Register an ObservationHandler
        registry.observationConfig().observationHandler(new MyHandler());

        // Create an Observation and observe your code!
        Observation.createNotStarted("user.name", registry)
                .contextualName("getting-user-name")
                .lowCardinalityKeyValue("userType", "userType1") // let's assume that you can have 3 user types
                .highCardinalityKeyValue("userId", "1234") // let's assume that this is an arbitrary number
                .observe(() -> log.info("Hello")); // this is a shortcut for starting an observation, opening a scope, running user's code, closing the scope and stopping the observation
        // end::example[]
        // @formatter:on
    }

    static class MyHandler implements ObservationHandler<Observation.Context> {

        @Override
        public boolean supportsContext(Observation.Context context) {
            return true;
        }
    }

}
