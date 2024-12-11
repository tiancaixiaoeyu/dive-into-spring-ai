package io.github.qifan777.knowledge.demo;
import cn.hutool.core.io.resource.InputStreamResource;
import cn.hutool.core.io.resource.Resource;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequestMapping("demo/document")
@RestController
@AllArgsConstructor
public class DocumentDemoController {
private  final DashScopeEmbeddingModel embeddingModel;
@PostMapping("embedding")
    public float[] embedding(@RequestParam String text){
    return  embeddingModel.embed(text);

}
@SneakyThrows
@PostMapping("etl/reader/multipart")
public String readForMultiPart(@RequestParam MultipartFile file)  {
    Resource resource=new  InputStreamResource(file.getInputStream());
    TikaDocumentReader tikaDocumentReader =new TikaDocumentReader(String.valueOf(resource));
    return  tikaDocumentReader.get().get(0).getContent();

}
@PostMapping("etl/reader/local-file")
public String readFromLocalFile(@RequestParam String path) {
    org.springframework.core.io.Resource springResource = new FileSystemResource(path);
    return new TikaDocumentReader(springResource)
            .read()
            .get(0)
            .getContent();
}
//     @PostMapping("etl/reader/local-file")
//     public  String readFromLocalFile(@RequestParam String path){
//         Resource resource = (Resource) new FileSystemResource(path);
//         return  new   TikaDocumentReader(String.valueOf(resource))
//                 .read()
//                 .get(0)
//                 .getContent();
//     }
}


