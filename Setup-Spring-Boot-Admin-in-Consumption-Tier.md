# Setup Spring Boot Admin in Consumption Tier

This article shows you how to Setup Spring Boot Admin with Azure Spring Apps Consumption Tier.

[Spring Boot Admin](https://github.com/codecentric/spring-boot-admin) is an open-source web application that provides a user interface to manage and monitor Spring Boot applications. It allows administrators to view important metrics such as CPU and memory usage, making it a valuable tool for monitoring the health and performance of Spring Boot applications.

## Prerequisites

- An already provisioned Azure Spring Apps Consumption Tier service instance. For more information, see [Create Azure Spring Apps Consumption Plan](https://github.com/Azure/Azure-Spring-Apps-Consumption-Plan/blob/main/articles/create-asa-standard-gen2.md).
- Install the [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) version 2.28.0 or higher.

## Deploy Spring Boot Admin Server in Azure Spring Apps
In this example, we will first create a sample Spring Boot Admin server application, then deploy it to Azure Spring Apps instance.

### Step 1: Create an App called "sbaserver" in the Azure Spring Apps Consumption Tier service instance

- Use the following command to specify the app name on Azure Spring Apps as "demo1".
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
