package io.github.qifan777.knowledge.ai.document;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

@RequestMapping("document")
@RestController
@RequiredArgsConstructor
public class DocumentController {
    private final VectorStore vectorStore;
    @Value("${upload.path}")
    private String uploadPath;

    /**
     * 嵌入文件
     *
     * @param file 待嵌入的文件
     * @return 是否成功
     */
    @SneakyThrows
    @PostMapping("embedding")
    public Boolean embedding(@RequestParam MultipartFile file) {
        String originalFilename = file.getOriginalFilename() == null ? "document.txt" : file.getOriginalFilename();
        String extension = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".txt";
        String storedFilename = UUID.randomUUID() + extension;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        File destFile = new File(uploadDir, storedFilename);
        file.transferTo(destFile);
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(new FileSystemResource(destFile));
        List<Document> splitDocuments = new TokenTextSplitter()
                .apply(tikaDocumentReader.read());
        for (int i = 0; i < splitDocuments.size(); i++) {
            Document document = splitDocuments.get(i);
            document.getMetadata().put("source", originalFilename);
            document.getMetadata().put("sourceUrl", "/uploads/" + storedFilename);
            document.getMetadata().put("chunkIndex", String.valueOf(i + 1));
            document.getMetadata().put("sourceType", "upload");
        }
        vectorStore.add(splitDocuments);
        return true;
    }
}
