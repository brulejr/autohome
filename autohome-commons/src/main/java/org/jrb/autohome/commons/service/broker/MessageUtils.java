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

import org.jrb.autohome.commons.service.broker.message.Message;
import org.jrb.autohome.commons.service.broker.message.SimpleMessage;
import org.springframework.util.StringUtils;
import org.zeromq.ZMQ;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * TODO - MessageUtils
 * 
 * @author <a href="mailto:brulejr@gmail.com">Jon Brule</a>
 */
public final class MessageUtils {

	public static <T> String generateJSON(final ObjectMapper messageMapper, final T raw) throws BrokerServiceException {
		try {
			return messageMapper.writeValueAsString(raw);
		} catch (final JsonProcessingException e) {
			throw new BrokerServiceException(e.getMessage(), e);
		}
	}

	public static <T> String packMessage(final ObjectMapper messageMapper, final String topic, final String separator, final T raw) {
		return topic + separator + generateJSON(messageMapper, raw instanceof Message ? raw : new SimpleMessage<T>(raw));
	}

	public static <T> void publish(
			final String topic,
			final String separator,
			final T raw,
			final ObjectMapper messageMapper,
			final ZMQ.Socket publisher) throws BrokerServiceException {
		if (StringUtils.hasText(topic)) {
			final String packed = MessageUtils.packMessage(messageMapper, topic, separator, raw);
			publisher.send(packed);
		} else {
			publisher.send(raw.toString());
		}
	}

	private MessageUtils() {
	}

}
