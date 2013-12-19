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
package org.jrb.autohome.commons.service.broker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.jrb.autohome.commons.service.broker.message.Message;
import org.jrb.autohome.commons.service.broker.message.SimpleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.zeromq.ZMQ;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;

/**
 * Implementation of a service that brokers messages using ZeroMQ in a
 * bi-directional PUB/SUB configuration. In the outbound direction, messages
 * published by this service using any of the <code>publish()</code> methods may
 * be received by any number of subscribers. In contrast, inbound messages come
 * from any number of publishers to this service's single subscriber, which are
 * directly placed upon the initialized {@link EventBus}.
 * 
 * @author <a href="mailto:brulejr@gmail.com">Jon Brule</a>
 */
public class BrokerServiceImpl implements BrokerService {

	private static final Logger LOG = LoggerFactory.getLogger(BrokerService.class);

	private final BrokerProperties brokerProperties;
	private final ExecutorService executorService;
	private final EventBus messageBus;
	private final ObjectMapper messageMapper;
	private final Map<String, Class<?>> messageClasses;

	private ZMQ.Context context;
	private ZMQ.Socket publisher;
	private ZMQ.Socket subscriber;

	private volatile boolean running = false;

	public BrokerServiceImpl(
			final BrokerProperties brokerProperties,
			final ExecutorService executorService,
			final EventBus messageBus) {
		this.brokerProperties = brokerProperties;
		this.executorService = executorService;
		this.messageBus = messageBus;
		messageMapper = defaultObjectMapper();
		messageClasses = defaultMessageClasses();
	}

	public BrokerServiceImpl(
			final BrokerProperties brokerProperties,
			final ExecutorService executorService,
			final EventBus messageBus,
			final ObjectMapper messageMapper) {
		this.brokerProperties = brokerProperties;
		this.executorService = executorService;
		this.messageBus = messageBus;
		this.messageMapper = messageMapper;
		messageClasses = defaultMessageClasses();
	}

	public BrokerServiceImpl(
			final BrokerProperties brokerProperties,
			final ExecutorService executorService,
			final EventBus messageBus,
			final ObjectMapper messageMapper,
			final Map<String, Class<?>> messageClasses) {
		this.brokerProperties = brokerProperties;
		this.executorService = executorService;
		this.messageBus = messageBus;
		this.messageMapper = messageMapper;
		this.messageClasses = messageClasses;
	}

	protected Map<String, Class<?>> defaultMessageClasses() {
		final Map<String, Class<?>> messageClasses = new HashMap<>();
		messageClasses.put(Message.class.getSimpleName(), SimpleMessage.class);
		return messageClasses;
	}

	protected ObjectMapper defaultObjectMapper() {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper;
	}

	@Override
	public int getPhase() {
		return 0;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public <T> void publish(final String topic, final T raw) throws BrokerServiceException {
		final String separator = brokerProperties.getSubscriberMessageSeparator();
		MessageUtils.publish(topic, separator, raw, messageMapper, publisher);
	}

	@Override
	public void run() {
		try {
			while (running) {
				final String message = subscriber.recvStr();
				final String separator = brokerProperties.getSubscriberMessageSeparator();
				final String filter = brokerProperties.getSubscriberTopicFilter();
				if (StringUtils.hasText(filter) && message.contains(separator)) {
					final int pos = message.indexOf(separator);
					final String type = message.substring(0, pos);
					final String json = message.substring(pos + 1);
					messageBus.post(messageMapper.readValue(json, messageClasses.get(type)));
				} else {
					messageBus.post(message);
				}
			}
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			running = false;
		}
	}

	@Override
	public void start() {
		final BrokerType brokerType = brokerProperties.getBrokerType();
		LOG.info("Starting BrokerService in {} mode...", brokerType);

		context = ZMQ.context(1);

		publisher = context.socket(ZMQ.PUB);
		subscriber = context.socket(ZMQ.SUB);

		switch (brokerType) {
		case MASTER:
			publisher.bind(brokerProperties.getPublisherAddress());
			subscriber.bind(brokerProperties.getSubscriberAddress());
			break;
		case COORDINATOR:
			publisher.connect(brokerProperties.getPublisherAddress());
			subscriber.connect(brokerProperties.getSubscriberAddress());
			break;
		default:
			throw new IllegalStateException("Unknown broker type - " + brokerType);
		}

		final String filter = brokerProperties.getSubscriberTopicFilter();
		LOG.info("Setting subscription filter to [{}]", filter);
		subscriber.subscribe(filter.getBytes());

		running = true;

		executorService.execute(this);
	}

	@Override
	public void stop() {
		LOG.info("Stopping BrokerService...");
		running = false;
		executorService.shutdown();
	}

	@Override
	public void stop(final Runnable callback) {
		stop();
		callback.run();
	}

}
