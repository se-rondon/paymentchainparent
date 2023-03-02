package adminserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

@SpringBootApplication
@EnableAutoConfiguration 	//Hace la configuración por defecto
@EnableAdminServer			//Inidica a Spring boot que es unservidor de administracion
@EnableEurekaClient			//Indica que es un cliente de Eureka
public class AdminserverApplication {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SpringApplication.run(AdminserverApplication.class, args);
	}

	 @Configuration
	    public static class SecurityPermitAllConfig extends WebSecurityConfigurerAdapter {
	        @Override
	        protected void configure(HttpSecurity http) throws Exception {
	            http.authorizeRequests().anyRequest().permitAll()  
	                .and().csrf().disable();
	        }
	    }
	
}
