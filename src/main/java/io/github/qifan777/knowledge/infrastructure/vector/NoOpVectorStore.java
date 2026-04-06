package io.github.qifan777.knowledge.infrastructure.vector;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NoOpVectorStore implements VectorStore {
  @Override
  public void add(List<Document> documents) {
    // no-op
  }

  @Override
  public Optional<Boolean> delete(List<String> ids) {
    return Optional.of(Boolean.FALSE);
  }

  @Override
  public List<Document> similaritySearch(SearchRequest request) {
    return Collections.emptyList();
  }
}
