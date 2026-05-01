package com.ezmeal.review.infrastructure.config;

import com.ezmeal.common.security.interceptor.KafkaSecurityInterceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.util.backoff.FixedBackOff;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Bean
    public JsonMessageConverter jsonMessageConverter() {
        return new ByteArrayJsonMessageConverter();
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        // 3초 대기 후 최대 2번 더 재시도
        return new DefaultErrorHandler(new FixedBackOff(3000L, 2));
    }

    // Bean 이름 충돌을 피하기 위해 customKafkaListenerContainerFactory 로 이름 변경
    // 하단의 팩토리를 최우선으로 사용하도록 @Primary 어노테이션 추가
    @Bean
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, Object> customKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            JsonMessageConverter jsonMessageConverter,
            DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        // JSON 변환 및 에러 핸들링
        factory.setRecordMessageConverter(jsonMessageConverter);
        factory.setCommonErrorHandler(errorHandler);

        // Common 모듈에 있던 보안 기능(헤더 가로채기)을 여기에 명시적으로 합침
        factory.setRecordInterceptor(new KafkaSecurityInterceptor<>());

        return factory;
    }
}
