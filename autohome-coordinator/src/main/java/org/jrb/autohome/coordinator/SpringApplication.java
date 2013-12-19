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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot entry point for the Home Automation Coordinator node.
 * 
 * @author <a href="mailto:brulejr@gmail.com">Jon Brule</a>
 */
@EnableAutoConfiguration
@Import(ApplicationConfig.class)
public class SpringApplication {

	public static final String ENV_LOCAL = "LOCAL";

	private static final Logger LOG = LoggerFactory.getLogger(SpringApplication.class);

	/**
	 * Main entry method.
	 * 
	 * @param args
	 *            the command-line arguments
	 */
	public static void main(String[] args) {
		
		// determine application environment
		String appenv = System.getenv("APP_ENV");
		appenv = (appenv != null) ? appenv : ENV_LOCAL;

		// initialize application
		LOG.info("Starting application");
		new SpringApplicationBuilder(SpringApplication.class)
				.profiles(appenv)
				.showBanner(false)
				.run(args);
	}

}
