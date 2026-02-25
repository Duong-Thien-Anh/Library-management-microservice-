package com.fptdemo.borrowservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke-test: verifies the Spring context starts without any external services.
 * We override the datasource to use an in-memory H2 DB and disable Eureka.
 */
@SpringBootTest
@TestPropertySource(properties = {
        // Use H2 in-memory DB so we don't need a real MySQL
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        // Disable Eureka registration so the test doesn't wait for a Eureka server
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        // Feign: disable load-balancer so BookClient bean is created without Eureka
        "spring.cloud.loadbalancer.enabled=false"
})
class BorrowserviceApplicationTests {

    @Test
    void contextLoads() {
        // If this method runs, the Spring context started successfully
    }

}
