package org.lin.fitnessapi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"org.lin"})
@EnableJpaRepositories(basePackages = {"org.lin"})
@EntityScan(basePackages = {"org.lin"})
public class FitnessApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(FitnessApiApplication.class, args);
    }

}
