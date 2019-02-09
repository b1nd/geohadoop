package ru.b1nd.filesystem.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class FileSystemService {
    private final Logger logger = LoggerFactory.getLogger(FileSystemService.class);

    @Value("${upload.dir}")
    private String uploadDir;

    private final RestTemplate restTemplate;

    @Autowired
    public FileSystemService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createUploadDirectory() {
        if (new File(uploadDir).mkdirs()) {
            logger.debug("Dir " + uploadDir + " init");
        }
    }

    public void requestAndSaveFile(String from, String file, int w, int h) {
        String fullName = file + "w" + w + "h" + h;
        ResponseEntity<Resource> entity = null;
        try {
            entity = restTemplate.getForEntity("http://" + from + "/file/" + file + "?" + "w=" + w + "&" + "h=" + h, Resource.class);
        } catch (RuntimeException e) {
            logger.info("Node " + from + " does not respond");
        }
        if (entity == null || entity.getBody() == null) {
            logger.info("File " + fullName + " was not downloaded");
            return;
        }
        logger.info("File " + file + "w" + w + "h" + h + " downloaded");
        String filePath = uploadDir + file;
        if (new File(filePath).mkdirs()) {
            logger.info("New file " + file + " in file system");
        }
        var resource = entity.getBody();
        try {
            var buffer = ByteBuffer.allocate((int) resource.contentLength());
            resource.readableChannel().read(buffer);
            Files.write(new File(filePath + "/" + "w" + w + "h" + h + ".tif").toPath(), buffer.array());
            logger.info("File " + fullName + " saved");
        } catch (IOException e) {
            logger.info("Cannot save file " + fullName, e);
        }
    }

    public List<String> getFileNames() throws IOException {
        return Files.list(new File(uploadDir).toPath())
                .filter(Files::isDirectory)
                .map(f -> f.toFile().getName())
                .collect(Collectors.toList());
    }

    public Resource getFileAsResource(String fileName, int w, int h) throws IOException {
        Optional<Path> dirPath = Files.list(new File(uploadDir).toPath())
                .filter(p -> p.toFile().getName().equals(fileName))
                .findFirst();
        if (dirPath.isEmpty()) {
            throw new FileNotFoundException("Cannot find file " + fileName);
        } else {
            Optional<Path> filePath = Files.list(dirPath.get())
                    .filter(p -> checkFilePartName(p.toFile().getName(), w, h))
                    .findFirst();
            if (filePath.isEmpty()) {
                throw new FileNotFoundException("Cannot find file " + fileName + " with w = " + w + " and h = " + h);
            } else {
                return getUrlResourceByPath(filePath.get());
            }
        }
    }

    public List<Resource> getFileAsResources(String fileName) throws IOException {
        Optional<Path> path = Files.list(new File(uploadDir).toPath())
                .filter(p -> p.toFile().getName().equals(fileName))
                .findFirst();
        if (path.isEmpty()) {
            throw new FileNotFoundException("Cannot find file " + fileName);
        } else {
            return Files.list(path.get())
                    .map(this::getUrlResourceByPath)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    private boolean checkFilePartName(String fileName, int w, int h) {
        Pattern pattern = Pattern.compile("^w(\\d+)h(\\d+).tif{1,2}$");
        Matcher match = pattern.matcher(fileName);

        return match.matches() &&
                w == Integer.parseInt(match.group(1)) &&
                h == Integer.parseInt(match.group(2));
    }

    private UrlResource getUrlResourceByPath(Path path) {
        try {
            return new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            logger.info("Cannot get resource url from file " + path.toFile().getName());
            return null;
        }
    }
}
