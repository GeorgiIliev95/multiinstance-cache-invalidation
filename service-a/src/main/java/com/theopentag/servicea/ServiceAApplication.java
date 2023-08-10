package com.theopentag.servicea;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class ServiceAApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceAApplication.class, args);
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
    public static class RedisIpPublisher {
        private final StringRedisTemplate redisTemplate;

        public RedisIpPublisher(final StringRedisTemplate redisTemplate) {this.redisTemplate = redisTemplate;}

        @PostConstruct
        public void init() {
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                String ipAddress = localHost.getHostAddress();

                redisTemplate.opsForHash().put("service-a-ips", ipAddress, ipAddress);

                System.out.println("Add IP Address %s to redis".formatted(ipAddress));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        @PreDestroy
        public void destroy() {    
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                String ipAddress = localHost.getHostAddress();

                redisTemplate.opsForHash().delete("service-a-ips", ipAddress);

                System.out.println("Removed IP %s from redis".formatted(ipAddress));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    @RestController
    public static class CacheManagementController {

        @DeleteMapping("/cache-management")
        public ResponseEntity<String> invalidateCache() {

            try {
                InetAddress localHost = InetAddress.getLocalHost();
                String ipAddress = localHost.getHostAddress();

                System.out.println("Invalidate cache - instance %s".formatted(ipAddress));
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().body("error");
            }

            return ResponseEntity.ok("ok");
        } 
    }
}
