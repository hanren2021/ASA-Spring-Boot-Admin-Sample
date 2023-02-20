# Setup Spring Boot Admin in Consumption Tier

This article shows you how to Setup Spring Boot Admin with Azure Spring Apps Consumption Tier.

[Spring Boot Admin](https://github.com/codecentric/spring-boot-admin) is an open-source web application that provides a user interface to manage and monitor Spring Boot applications. It allows administrators to view important metrics such as CPU and memory usage, making it a valuable tool for monitoring the health and performance of Spring Boot applications.

## Prerequisites

- An already provisioned Azure Spring Apps Consumption Tier service instance. For more information, see [Create Azure Spring Apps Consumption Plan](https://github.com/Azure/Azure-Spring-Apps-Consumption-Plan/blob/main/articles/create-asa-standard-gen2.md).
- Install the [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) version 2.28.0 or higher.

## Deploy Spring Boot Admin Server in Azure Spring Apps
In this example, we will first create a sample Spring Boot Admin server application, then deploy it to Azure Spring Apps instance.

### Step 1: Create an App called "sbaserver" in the Azure Spring Apps Consumption Tier service instance

- Use the following command to specify the app name on Azure Spring Apps as "sbaserver".
  ```
  az spring app create `
      --resource-group <name-of-resource-group> `
      --service <service-instance-name> `
      --name sbaserver `
      --cpu 500m `
      --memory 1Gi `
      --instance-count 1 `
      --assign-endpoint true
  ```
  After the app being successfully create, you should be able get the app URL like this: 
  > https://sbaserver.xxx.xxx.azurecontainerapps.io/
  
- Build the Spring Boot Admin server project
   - Navigate to https://start.spring.io. This service pulls in all the dependencies you need for an application and does most of the setup for you.
   - Choose Maven, Spring Boot, Java version you want to use. 
   - Click Dependencies and select **Spring Web** and **codecentric's Spring Boot Admin (Server)**.
   - Click Generate and download the resulting ZIP file, which is an archive of a web application that is configured with your choices.

   ![image](https://user-images.githubusercontent.com/90367028/220031888-0fc31438-001b-4ad9-94dc-44bee901d701.png)
   
   - Make sure the following dependency can be found in the pom.xml file
     ```
     <dependency>
			  <groupId>de.codecentric</groupId>
			  <artifactId>spring-boot-admin-starter-server</artifactId>
		 </dependency>
     ```
   - If you need to secure your Spring Boot Admin with a login page, you also need to add the following dependency in your pom.xml file
     ```
     <dependency>
			  <groupId>de.codecentric</groupId>
			  <artifactId>spring-boot-admin-server-ui</artifactId>
			  <version>${spring-boot-admin.version}</version>
		 </dependency>
    
		 <dependency>
			  <groupId>org.springframework.boot</groupId>
			  <artifactId>spring-boot-starter-security</artifactId>
		 </dependency>
     ```

   - The top level of application class should look like the following.
     ```
     package com.sbaexample.sbaserver;

     import org.springframework.boot.SpringApplication;
     import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
     import org.springframework.boot.autoconfigure.SpringBootApplication;
     import org.springframework.context.annotation.Bean;
     import org.springframework.context.annotation.Configuration;
     import org.springframework.http.HttpMethod;
     import org.springframework.security.config.Customizer;
     import org.springframework.security.config.annotation.web.builders.HttpSecurity;
     import org.springframework.security.web.SecurityFilterChain;
     import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
     import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
     import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

     import de.codecentric.boot.admin.server.config.AdminServerProperties;
     import de.codecentric.boot.admin.server.config.EnableAdminServer;

     @SpringBootApplication
     @Configuration
     @EnableAutoConfiguration
     @EnableAdminServer
     public class SbaserverApplication {
     
     	public static void main(String[] args) {
     		SpringApplication.run(SbaserverApplication.class, args);
     	}

     	@Configuration(proxyBeanMethods = false)
     	public static class SecuritySecureConfig {

     		private final AdminServerProperties adminServer;

     		public SecuritySecureConfig(AdminServerProperties adminServer) {
			this.adminServer = adminServer;
		}

		@Bean
		protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
			SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
			successHandler.setTargetUrlParameter("redirectTo");
			successHandler.setDefaultTargetUrl(this.adminServer.path("/"));

			http.authorizeHttpRequests((authorizeRequests) -> authorizeRequests
					.requestMatchers(new AntPathRequestMatcher(this.adminServer.path("/assets/**"))).permitAll()
					.requestMatchers(new AntPathRequestMatcher(this.adminServer.path("/login"))).permitAll()
					.anyRequest().authenticated())

					.formLogin((formLogin) -> formLogin.loginPage(this.adminServer.path("/login"))
							.successHandler(successHandler))
					.logout((logout) -> logout.logoutUrl(this.adminServer.path("/logout")))
					.httpBasic(Customizer.withDefaults())
					.csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
							.ignoringRequestMatchers(
									new AntPathRequestMatcher(this.adminServer.path("/instances"),
											HttpMethod.POST.toString()),
									new AntPathRequestMatcher(this.adminServer.path("/instances/*"),
											HttpMethod.DELETE.toString()),
									new AntPathRequestMatcher(this.adminServer.path("/actuator/**"))));

			return http.build();
     		}
     	}
     }
     ```
     
   - Create application.yml file under ./resources/ folder with the following contents
   
     **Example application.yml for your app running in Azure Spring Apps consumption tier:**
       - Fill in your sbaserver App URL as the "public-url" value, which should like this: https://sbaserver.xxx.xxx.azurecontainerapps.io/
       - Fill in yuor Spring boot Admin server access user name and password
     ```
     spring:
       boot:
         admin:
           ui:
             public-url: "<your sbaserver App URL>"
       security:
         user:
           name: "<username>"
           password: "<password>"
     ```
     
     **Example application.yml for your app running in local machine for test purpose:**
     ```
     server:
       port: 8080
       
     spring:
       boot:
         admin:
           ui:
             public-url: "http://localhost:8080"
       security:
         user:
           name: "<username>"
           password: "<password>"
     ```

   - Run Maven command to build the project
    
      ```
      mvn clean package -DskipTests
      ```
      There should be a sbaserver-0.0.1-SNAPSHOT.jar file generated under the ./target/ folder

-  Use the following command to deploy your "sbaserver" Azure Spring App.
   ```
   az spring app deploy `
       --resource-group <name-of-resource-group> `
       --service <service-instance-name> `
       --name sbaserver `
       --artifact-path <file path to sbaserver-0.0.1-SNAPSHOT.jar> `
       --runtime-version Java_17 `
       --jvm-options '-Xms512m -Xmx800m'
   ```

- Test your sbaserver Azure Spring App

  Navigate to https://sbaserver.xxx.xxx.azurecontainerapps.io/, the Spring Boot Admin server should pop up a login page.
  
  ![image](https://user-images.githubusercontent.com/90367028/220039001-cfa0da33-cb2f-4bd8-a34c-1b20d6f85da0.png)

  Login with your username and password:
  
  ![image](https://user-images.githubusercontent.com/90367028/220040263-957a27bb-0748-42ca-9a50-76e9988d5214.png)


### Step 2: Setup the client App to allow it being montiored by your Spring Boot Admin server

- Use the following command to specify the app name on Azure Spring Apps as "sbaclient".
  ```
  az spring app create `
      --resource-group <name-of-resource-group> `
      --service <service-instance-name> `
      --name sbaclient `
      --cpu 500m `
      --memory 1Gi `
      --instance-count 1 `
      --assign-endpoint true
  ```
  After the app being successfully create, you should be able get the app URL like this: 
  > https://sbaclient.xxx.xxx.azurecontainerapps.io/


- Build the Spring Boot Admin client project
   - Navigate to https://start.spring.io. This service pulls in all the dependencies you need for an application and does most of the setup for you.
   - Choose Maven, Spring Boot, Java version you want to use. 
   - Click Dependencies and select **Spring Web** and **codecentric's Spring Boot Admin (Client)**.
   - Click Generate and download the resulting ZIP file, which is an archive of a web application that is configured with your choices.

     ![image](https://user-images.githubusercontent.com/90367028/220042141-6f25bae0-a592-4499-899b-d3efaf5b8b80.png)
   
   - Make sure the following dependency can be found in the pom.xml file
     ```
     <dependency>
          <groupId>de.codecentric</groupId>
          <artifactId>spring-boot-admin-starter-client</artifactId>
     </dependency>
     ```
     
   - There is no need to make changes in your client application code to make it montiored by Spring Boot Admin server.
   
   - Create application.properties file under ./resources/ folder with the following contents
   
     **Example application.properties for your app running in Azure Spring Apps consumption tier:**
       - Fill in your sbaserver App URL as the "spring.boot.admin.client.url" value, which should like this: https://sbaserver.xxx.xxx.azurecontainerapps.io/
       - Fill in your sbaclient App URL as the "spring.boot.admin.client.instance.management-base-url" value, which should like this: https://sbaclient.xxx.xxx.azurecontainerapps.io:443
       - Fill in spring.boot.admin.client.username and spring.boot.admin.client.password
      
       ```
       spring.boot.admin.client.url=<your sbaserver App URL>
       management.endpoints.web.exposure.include=*
       spring.boot.admin.client.instance.name=sbaclient 
       spring.boot.admin.client.instance.management-base-url=<your sbaclient App URL>
       spring.boot.admin.client.username=admin
       spring.boot.admin.client.password=changeme
       ```
     
     **Example application.properties for your app running in local machine for test purpose:**
     ```
     server.port=8081
     spring.boot.admin.client.url=http://localhost:8080
     management.endpoints.web.exposure.include=*
     spring.boot.admin.client.instance.name=localsbaclient 
     spring.boot.admin.client.instance.management-base-url=http://localhost:8081
     spring.boot.admin.client.username=myadmin
     spring.boot.admin.client.password=changeme
     ```
     
   - Run Maven command to build the project
      ```
      mvn clean package -DskipTests
      ```
      There should be a sbaclient-0.0.1-SNAPSHOT.jar file generated under the ./target/ folder

-  Use the following command to deploy your "sbaclient" Azure Spring App.
   ```
   az spring app deploy `
       --resource-group <name-of-resource-group> `
       --service <service-instance-name> `
       --name sbaclient `
       --artifact-path <file path to sbaclient-0.0.1-SNAPSHOT.jar> `
       --runtime-version Java_17 `
       --jvm-options '-Xms512m -Xmx800m'
   ```

- Test your sbaserver Azure Spring App

  Navigate to https://sbaclient.xxx.xxx.azurecontainerapps.io/actuator/health, the client should report UP status.
  
  ![image](https://user-images.githubusercontent.com/90367028/220045410-26eac213-6d9e-484a-8576-ffbd8d9ae275.png)

  Navigate to https://sbaserver.xxx.xxx.azurecontainerapps.io/, and login with your username and password.
  
  You should be able to montior your client app with in the Spring Boot Admin server:
  ![image](https://user-images.githubusercontent.com/90367028/220046028-b5feb488-f5ed-473d-94e1-867b596a60ba.png)

  ![image](https://user-images.githubusercontent.com/90367028/220046265-0a878e94-717b-4ecf-a1dc-57ff9ade4b43.png)

  ![image](https://user-images.githubusercontent.com/90367028/220046927-dfd663bb-3284-40d5-8f13-1c1da1e8570d.png)
