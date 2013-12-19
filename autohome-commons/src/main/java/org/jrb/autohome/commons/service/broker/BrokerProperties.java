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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ZeroMQ Broker configuration properties for the Home Automation system.
 * 
 * @author <a href="mailto:brulejr@gmail.com">Jon Brule</a>
 */
@ConfigurationProperties(name = "service.broker", ignoreUnknownFields = false)
public class BrokerProperties {
	
	private final static String DEFAULT_MESSAGE_SEPARATOR = "|";

	private BrokerType brokerType;
	private String publisherAddress;
	private String subscriberAddress;
	private String subscriberTopicFilter;
	private String subscriberMessageSeparator = DEFAULT_MESSAGE_SEPARATOR;

	public BrokerType getBrokerType() {
		return brokerType;
	}

	public String getPublisherAddress() {
		return publisherAddress;
	}

	public String getSubscriberAddress() {
		return subscriberAddress;
	}

	public String getSubscriberMessageSeparator() {
		return subscriberMessageSeparator;
	}

	public String getSubscriberTopicFilter() {
		return subscriberTopicFilter;
	}

	public void setBrokerType(final BrokerType brokerType) {
		this.brokerType = brokerType;
	}

	public void setPublisherAddress(final String publisherAddress) {
		this.publisherAddress = publisherAddress;
	}

	public void setSubscriberAddress(final String subscriberAddress) {
		this.subscriberAddress = subscriberAddress;
	}

	public void setSubscriberMessageSeparator(final String subscriberMessageSeparator) {
		this.subscriberMessageSeparator = subscriberMessageSeparator;
	}

	public void setSubscriberTopicFilter(final String subscriberTopicFilter) {
		this.subscriberTopicFilter = subscriberTopicFilter;
	}

}
