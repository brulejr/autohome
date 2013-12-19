/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Jon Brule
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.jrb.autohome.coordinator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jrb.autohome.commons.service.broker.BrokerProperties;
import org.jrb.autohome.commons.service.broker.BrokerService;
import org.jrb.autohome.commons.service.broker.BrokerServiceImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;

/**
 * ZeroMQ Broker configuration for the Home Automation Master node.
 * 
 * @author <a href="mailto:brulejr@gmail.com">Jon Brule</a>
 */
@Configuration
@EnableConfigurationProperties(BrokerProperties.class)
public class BrokerConfig {

	@Bean
	public BrokerService brokerService(
			final BrokerProperties brokerProperties, 
			final EventBus messageBus,
			final ObjectMapper messageMapper) {
		final ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		final BrokerService service = new BrokerServiceImpl(
				brokerProperties, executorService, messageBus, messageMapper);
		return service;
	}

	@Bean
	public EventBus messageBus() {
		final EventBus messageBus = new EventBus();
		return messageBus;
	}

	@Bean
	public ObjectMapper messageMapper() {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper;
	}
}
