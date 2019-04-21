package ru.b1nd.namenode.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.b1nd.filesystem.services.FileSystemService;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.b1nd.namenode.utils.Converter.getWH;

@Service
public class FileService {
    private final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Value("${upload.dir}")
    private String uploadDir;
    @Value("${output.dir}")
    private String outputDir;
    @Value("${tile.width}")
    private Integer tileWidth;
    @Value("${tile.height}")
    private Integer tileHeight;

    private final FileSystemService fileSystemService;

    @Autowired
    public FileService(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createUploadDirectory() {
        if (new File(uploadDir).mkdirs()) {
            logger.debug("Dir " + uploadDir + " init");
        }
        if (new File(outputDir).mkdirs()) {
            logger.debug("Dir " + outputDir + " init");
        }
    }

    public void saveUploadedFiles(List<MultipartFile> files) throws IOException {
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                byte[] bytes = file.getBytes();
                String newFilePath = uploadDir + file.getOriginalFilename();
                String newFileName = "" + file.getOriginalFilename();
                File newFile = new File(newFilePath);
                String ext = getFileExtension(newFileName);

                Files.write(newFile.toPath(), bytes);
                logger.info("File " + newFileName + " saved");

                new Thread(() -> {
                    if (ext.equals("tif") || ext.equals("tiff")) {
                        try {
                            splitTifFile(newFileName);
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                            return;
                        }

                        if (new File(newFilePath).delete()) {
                            logger.info("File " + newFileName + " was successfully slitted and deleted");
                        } else {
                            logger.info("Can't delete file " + newFileName);
                        }
                    }
                }).start();
            }
        }
    }

    private void splitTifFile(String fileName) throws IOException {
        String path = uploadDir + fileName;
        try (var imageInputStream = new FileImageInputStream(new File(path))) {
            var reader = ImageIO.getImageReaders(imageInputStream).next();
            reader.setInput(imageInputStream);
            var param = reader.getDefaultReadParam();

            for (int w = 0; w < reader.getWidth(0); w += tileWidth) {
                for (int h = 0; h < reader.getHeight(0); h += tileHeight) {
                    param.setSourceRegion(new Rectangle(w, h, tileWidth, tileHeight));

                    BufferedImage tile = reader.read(0, param);

                    String filesDir = uploadDir + getFileNameWithoutExtension(fileName) + "/";
                    if (new File(filesDir).mkdirs()) {
                        logger.debug("Dir " + filesDir + " created");
                    }
                    ImageIO.write(tile, "tif", new File(filesDir + getFilePartName(w, h)));
                }
            }
        }
    }

    public Resource getFileAsResource(String fileName) throws IOException {
        Optional<File> file;
        try (var files = Files.list(new File(outputDir).toPath())) {
            file = files.map(Path::toFile)
                    .filter(f -> getFileNameWithoutExtension(f.getName()).equalsIgnoreCase(getFileNameWithoutExtension(fileName)))
                    .findFirst();
        }
        if (file.isPresent()) {
            return new UrlResource(file.get().toURI());
        } else {
            throw new IllegalArgumentException("There is no file " + fileName + ". " +
                    "You should download a file from cluster firstly");
        }
    }

    public List<String> getOutputFileNames() throws IOException {
        try (var files = Files.list(new File(outputDir).toPath())) {
            return files.map(f -> f.toFile().getName()).collect(Collectors.toList());
        }
    }

    public void mergeTifFile(String fileName, int imageType) throws IOException {
        logger.info("Merging image " + fileName);
        var fileToWH = fileSystemService.getFilePartitions(fileName).stream()
                .map(f -> Pair.of(f, getWH(f.getName())))
                .sorted(Comparator.comparing((Pair<File, Pair<Integer, Integer>> p) -> p.getSecond().getSecond())
                        .thenComparing(p -> p.getSecond().getFirst()))
                .collect(Collectors.toList());

        var files  = fileToWH.stream().map(Pair::getFirst).collect(Collectors.toList());
        var matrix = fileToWH.stream().map(Pair::getSecond).collect(Collectors.groupingBy(Pair::getFirst));
        var cols   = matrix.size();
        var rows   = matrix.values().iterator().next().size();

        logger.info("Image partitioning " + rows + "x" + cols);

        BufferedImage[] images = new BufferedImage[files.size()];
        for (var i = 0; i < files.size(); ++i) {
            images[i] = ImageIO.read(files.get(i));
        }

        int totalWidth = 0;
        for (int i = 0; i < cols; ++i) {
            totalWidth += images[i].getWidth();
        }

        int totalHeight = 0;
        for (int i = 0; i < rows; ++i) {
            totalHeight += images[i * cols].getHeight();
        }

        logger.info("Total image size " + totalWidth + "x" + totalHeight);

        BufferedImage image = new BufferedImage(totalWidth, totalHeight, imageType);
        var raster = image.getRaster();

        int c = 0;
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                var imageData = images[c].getData();
                var pixels = imageData.getPixels(imageData.getMinX(), imageData.getMinY(), imageData.getWidth(), imageData.getHeight(), (double[]) null);
                raster.setPixels(j * tileWidth, i * tileHeight, imageData.getWidth(), imageData.getHeight(), pixels);
                ++c;
            }
        }

        var file = new File(outputDir + fileName + ".tif");
        ImageIO.write(image, "tif", file);

        logger.info("image " + file.getName() + " merged!");
    }

    private String getFilePartName(int w, int h) {
        return "w" + w + "h" + h + ".tif";
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1);
    }

    private String getFileNameWithoutExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
    }
}
