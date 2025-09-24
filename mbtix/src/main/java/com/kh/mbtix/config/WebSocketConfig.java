package com.kh.mbtix.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	/**
	 * 클라이언트가 메시지를 보낼 때(Inbound), 서버가 메시지를 처리하는 쓰레드 풀을 설정합니다. (서버의 처리 성능 향상)
	 */
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		int corePoolSize = Runtime.getRuntime().availableProcessors() * 20;
		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(corePoolSize * 2);
		executor.setQueueCapacity(100000); // 대기열 크기를 넉넉하게 설정
		executor.setThreadNamePrefix("ws-inbound-thread-");
		executor.initialize();
		registration.taskExecutor(executor);
	}

	/**
	 * 서버가 클라이언트에게 메시지를 보낼 때(Outbound), 메시지를 전송하는 쓰레드 풀을 설정합니다. (서버의 전송 성능 향상)
	 */
	@Override
	public void configureClientOutboundChannel(ChannelRegistration registration) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		int corePoolSize = Runtime.getRuntime().availableProcessors();
		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(corePoolSize * 2);
		executor.setQueueCapacity(100000);
		executor.setThreadNamePrefix("ws-outbound-thread-");
		executor.initialize();
		registration.taskExecutor(executor);
	}

	/**
	 * 메시지 브로커 관련 설정을 정의합니다. Heartbeat 설정도 여기에 포함됩니다.
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
		ts.setPoolSize(1);
		ts.setThreadNamePrefix("wss-heartbeat-thread-");
		ts.initialize();

		registry.enableSimpleBroker("/sub").setHeartbeatValue(new long[] { 10000, 10000 }).setTaskScheduler(ts);

		registry.setApplicationDestinationPrefixes("/pub");
	}

	/**
	 * STOMP 연결을 위한 엔드포인트를 설정합니다.
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
	}
}