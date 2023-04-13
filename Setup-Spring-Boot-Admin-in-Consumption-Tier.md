# Setup Spring Boot Admin in Standard consumption plan

This article shows you how to Setup Spring Boot Admin with Azure Spring Apps Standard consumption plan.

[Spring Boot Admin](https://github.com/codecentric/spring-boot-admin) is an open-source web application that provides a user interface to manage and monitor Spring Boot applications. It allows administrators to view important metrics such as CPU and memory usage, making it a valuable tool for monitoring the health and performance of Spring Boot applications.

## Prerequisites

- An already provisioned Azure Spring Apps Standard consumption plan service instance. For more information, see [Create Azure Spring Apps Consumption Plan](https://github.com/Azure/Azure-Spring-Apps-Consumption-Plan/blob/main/articles/create-asa-standard-gen2.md).
- Install the [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) version 2.28.0 or higher.

## Deploy Spring Boot Admin Server in Azure Spring Apps
In this example, we will first create a sample Spring Boot Admin server application, then deploy it to Azure Spring Apps instance.

### Step 1: Create an App called "sbaserver" in the Azure Spring Apps Standard consumption plan service instance

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

   ![image](https://user-images.githubusercontent.com/90367028/230339889-9f3f8d2b-52db-4945-82b7-1f18e63c5e39.png)
   
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

   - The top level of application class should look like the following. We can pull in the Spring Boot Admin Server configuration via adding @EnableAdminServer to your configuration.
     ```
     @SpringBootApplication
     @Configuration
     @EnableAutoConfiguration
     @EnableAdminServer
     public class SbaserverApplication {
     
     	public static void main(String[] args) {
     		SpringApplication.run(SbaserverApplication.class, args);
     	}
     }
     ```
     If you want to secure the Spring Boot Admin Server, please refer to [Securing Spring Boot Admin Server](https://codecentric.github.io/spring-boot-admin/2.5.1/#_securing_spring_boot_admin_server). Sample code can be found in [SecuritySecureConfig.java](https://github.com/codecentric/spring-boot-admin/blob/master/spring-boot-admin-samples/spring-boot-admin-sample-servlet/src/main/java/de/codecentric/boot/admin/SecuritySecureConfig.java)
     
     
   - Create application.yml file under ./resources/ folder with the following contents
   
     **Example application.yml for your app running in Azure Spring Apps Standard Consumption Plan:**
 
     - Fill in your sbaserver App URL as the "public-url" value, which should like this: https://sbaserver.[env-name].[region].azurecontainerapps.io/
     - If you secured your Spring boot Admin server with user name and password, also fill in the access user name and password.
       
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
       --runtime-version Java_17
   ```

- Test your sbaserver Azure Spring App

  Navigate to https://sbaserver.[env-name].[region].azurecontainerapps.io/, if you secured your Spring Boot Admin server in previouse step, the Spring Boot Admin server should pop up a login page. Otherwise, you can directly access it.
  
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
  > https://sbaclient.[env-name].[region].azurecontainerapps.io/


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
   
     **Example application.properties for your app running in Azure Spring Apps Standard Consumption Plan:**
       - Fill in your Spring Boot Admin server App URL as the "spring.boot.admin.client.url" value, which should like this: https://sbaserver.[env-name].[region].azurecontainerapps.io/
       - Fill in your client App name as the "spring.boot.admin.client.instance.name" value. You can manuanlly set it as "sbaclient" or use environment variable: ${SPRING_APPLICATION_NAME}
       - Set management.endpoints.web.exposure.include=* to expose all the available actuator endpoints. 
       - Set spring.boot.admin.client.instance.prefer-ip=true to let Spring Boot Admin use the ip-address rather then the hostname in the monitor urls. 
       - If you secured your Spring Boot Admin server in Step 1, fill in spring.boot.admin.client.username and spring.boot.admin.client.password with the same username and password to allow the client access the admin server.
      
       ```
       spring.boot.admin.client.url=<your Spring Boot Admin server App URL>
       management.endpoints.web.exposure.include=*
       spring.boot.admin.client.instance.name=${SPRING_APPLICATION_NAME} 
       spring.boot.admin.client.instance.prefer-ip=true
       spring.boot.admin.client.username=<username>
       spring.boot.admin.client.password=<password>
       ```
     
     **Example application.properties for your app running in local machine for test purpose:**
     ```
     server.port=8081
     
     spring.boot.admin.client.url=http://localhost:8080
     management.endpoints.web.exposure.include=*
     spring.boot.admin.client.instance.name=sbaclient 
     spring.boot.admin.client.instance.management-base-url=http://localhost:8081
     spring.boot.admin.client.username=<username>
     spring.boot.admin.client.password=<password>
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
       --runtime-version Java_17
   ```

- Test your sbaserver Azure Spring App

  Navigate to https://sbaclient.[env-name].[region].azurecontainerapps.io/actuator/health, the client should report UP status.
  
  ![image](https://user-images.githubusercontent.com/90367028/220045410-26eac213-6d9e-484a-8576-ffbd8d9ae275.png)

  Navigate to https://sbaserver.[env-name].[region].azurecontainerapps.io/, and login with your username and password.
  
  You should be able to montior your client app with in the Spring Boot Admin server:
  ![image](https://user-images.githubusercontent.com/90367028/230575726-58fc3cef-5d3d-4be7-af0f-92f303d56b84.png)

  ![image](https://user-images.githubusercontent.com/90367028/230577416-bb4ac913-02b9-45f6-9388-5821813bbd64.png)

  ![image](https://user-images.githubusercontent.com/90367028/220046927-dfd663bb-3284-40d5-8f13-1c1da1e8570d.png)
  
  ![image](https://user-images.githubusercontent.com/90367028/230577174-b3c2c101-31be-45f9-a45e-43457d8316a2.png)

