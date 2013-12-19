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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.jrb.autohome.commons.service.broker.BrokerProperties;
import org.jrb.autohome.commons.service.broker.BrokerService;
import org.jrb.autohome.commons.service.broker.BrokerServiceException;
import org.jrb.autohome.commons.service.broker.MessageUtils;
import org.jrb.autohome.commons.service.broker.message.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zeromq.ZMQ;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Integration test case that exercises the Coordinator's subscriber interface.
 * 
 * @author <a href="mailto:brulejr@gmail.com">Jon Brule</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringApplication.class, loader = SpringApplicationContextLoader.class)
public class CoordinatorMessagingTest {

	private static final Logger LOG = LoggerFactory.getLogger(CoordinatorMessagingTest.class);

	@Autowired
	private BrokerService brokerService;

	@Autowired
	private BrokerProperties brokerProperties;

	@Autowired
	private EventBus eventBus;

	@Autowired
	private ObjectMapper messageMapper;

	@Test
	public void test() {
		LOG.info("BEGIN: test()");
		assertNotNull(brokerService);
		try {

			// setup testing harness
			final int numMessages = 5;
			final AtomicInteger msgcnt = new AtomicInteger(0);
			final CountDownLatch latch = new CountDownLatch(numMessages);
			eventBus.register(new Object() {
				@Subscribe
				public void handleMessage(Message<?> message) {
					LOG.info("handleMessage() = [{}]", message);
					msgcnt.getAndIncrement();
					latch.countDown();
				}
			});

			// setup zeromq publisher
			final ZMQ.Context context = ZMQ.context(1);
			final ZMQ.Socket publisher = context.socket(ZMQ.PUB);
			publisher.bind(brokerProperties.getSubscriberAddress());
			Thread.sleep(1000);

			// send good messages
			final String topic = brokerProperties.getSubscriberTopicFilter();
			for (int i = 1; i <= numMessages - 1; i++) {
				final String msg = "Test #" + i;
				LOG.info("Sending message '{}' to topic [{}]", msg, topic);
				publish(publisher, topic, msg);
			}
			Thread.sleep(500);
			assertEquals(numMessages - 1, msgcnt.get());

			// send filtered messages
			for (int i = 1; i <= 3; i++) {
				final String msg = "Test #" + i;
				LOG.info("Sending filtered message '{}' to topic [{}]", msg, topic);
				publish(publisher, 123 + topic, msg);
			}
			Thread.sleep(500);
			assertEquals(numMessages - 1, msgcnt.get());

			// send final good message
			final String msg = "Test #" + numMessages;
			LOG.info("Sending message '{}' to topic [{}]", msg, topic);
			publish(publisher, topic, msg);

			// ensure all good messages were received
			latch.await();
			assertEquals(numMessages, msgcnt.get());

			// closed down zeromq
			publisher.close();
			context.term();

		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
			fail(t.getMessage());
		}
		LOG.info("END: test()");
	}

	private <T> void publish(final ZMQ.Socket publisher, final String topic, final T message) throws BrokerServiceException {
		final String separator = brokerProperties.getSubscriberMessageSeparator();
		MessageUtils.publish(topic, separator, message, messageMapper, publisher);		
	}
	
}
