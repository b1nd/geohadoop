package ru.b1nd.datanode.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.b1nd.filesystem.controllers.FileSystemController;
import ru.b1nd.filesystem.services.FileSystemService;

@Configuration
public class FileSystemConfiguration {

    @Bean
    public FileSystemService fileSystemService(RestTemplate restTemplate) {
        return new FileSystemService(restTemplate);
    }

    @Bean
    public FileSystemController fileSystemController(FileSystemService fileSystemService) {
        return new FileSystemController(fileSystemService);
    }
}
