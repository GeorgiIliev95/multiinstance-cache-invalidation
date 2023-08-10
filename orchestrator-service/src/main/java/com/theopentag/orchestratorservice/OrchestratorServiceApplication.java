package com.theopentag.orchestratorservice;

import jakarta.annotation.PostConstruct;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@SpringBootApplication
@EnableScheduling
public class OrchestratorServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrchestratorServiceApplication.class, args);
	}

    @Configuration
    public static class OkHttpClientConfig {

        @Bean
        public OkHttpClient httpClient() {
            return new OkHttpClient.Builder().build();
        }

    }
    @Configuration
    public static class RedisConfig {

        @Value("${redis.ip}")
        private String redisIp;

        @Value("${redis.port}")
        private int redisPort;

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisIp, redisPort);
            return new LettuceConnectionFactory(config);
        }

        @Bean
        public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
            RedisTemplate<String, String> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            template.setDefaultSerializer(new StringRedisSerializer());
            return template;
        }

        @Bean
        public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
            return new StringRedisTemplate(connectionFactory);
        }
    }

    @Component
    public static class RedisIpScheduler {
        private final StringRedisTemplate redisTemplate;
        private final OkHttpClient httpClient;

        public RedisIpScheduler(final StringRedisTemplate redisTemplate, final OkHttpClient httpClient) {
            this.redisTemplate = redisTemplate;
            this.httpClient = httpClient;
        }

        @Scheduled(cron = "0 0/1 * * * *")
        public void task() {

            Set<Object> serviceIps = redisTemplate.opsForHash().keys("service-a-ips");

            System.out.println("service-a IPs [%s]".formatted(serviceIps));

            serviceIps
                    .forEach(ip -> {
                        System.out.println("Invalidate cache for %s".formatted(ip));

                        Request request = new Request.Builder()
                                .url("http://%s:%s/%s".formatted(ip, "8080", "cache-management"))
                                .delete()
                                .build();

                        Call call = httpClient.newCall(request);

                        try {
                            Response response = call.execute();
                            System.out.println("Response from %s: { %s %s}".formatted(ip, response.code(),
                                    response.body().string()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

}
