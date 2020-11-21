package io.harness.service.impl;

import static com.mongodb.DBCollection.ID_FIELD_NAME;

import io.harness.callback.MongoDatabase;
import io.harness.delegate.beans.DelegateAsyncTaskResponse.DelegateAsyncTaskResponseKeys;
import io.harness.delegate.beans.DelegateSyncTaskResponse.DelegateSyncTaskResponseKeys;
import io.harness.delegate.beans.DelegateTaskProgressResponse.DelegateTaskProgressResponseKeys;
import io.harness.service.intfc.DelegateCallbackService;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoDelegateCallbackService implements DelegateCallbackService {
  private static final String SYNC_TASK_COLLECTION_NAME_SUFFIX = "delegateSyncTaskResponses";
  private static final String ASYNC_TASK_COLLECTION_NAME_SUFFIX = "delegateAsyncTaskResponses";
  private static final String TASK_PROGRESS_COLLECTION_NAME_SUFFIX = "delegateTaskProgressResponses";

  private final MongoClient mongoClient;
  private final com.mongodb.client.MongoDatabase database;
  private final MongoCollection<Document> syncTaskResponseCollection;
  private final MongoCollection<Document> asyncTaskResponseCollection;
  private final MongoCollection<Document> taskProgressResponseCollection;

  MongoDelegateCallbackService(MongoDatabase mongoDatabase) {
    String connectionString = mongoDatabase.getConnection();
    MongoClientURI mongoClientURI = new MongoClientURI(connectionString);
    mongoClient = new MongoClient(mongoClientURI);
    database = mongoClient.getDatabase(Objects.requireNonNull(mongoClientURI.getDatabase()));

    String syncTaskResponseCollectionName = StringUtils.isBlank(mongoDatabase.getCollectionNamePrefix())
        ? SYNC_TASK_COLLECTION_NAME_SUFFIX
        : mongoDatabase.getCollectionNamePrefix() + "_" + SYNC_TASK_COLLECTION_NAME_SUFFIX;
    syncTaskResponseCollection = database.getCollection(syncTaskResponseCollectionName);

    String asyncTaskResponseCollectionName = StringUtils.isBlank(mongoDatabase.getCollectionNamePrefix())
        ? ASYNC_TASK_COLLECTION_NAME_SUFFIX
        : mongoDatabase.getCollectionNamePrefix() + "_" + ASYNC_TASK_COLLECTION_NAME_SUFFIX;
    asyncTaskResponseCollection = database.getCollection(asyncTaskResponseCollectionName);

    String taskProgressResponseCollectionName = StringUtils.isBlank(mongoDatabase.getCollectionNamePrefix())
        ? TASK_PROGRESS_COLLECTION_NAME_SUFFIX
        : mongoDatabase.getCollectionNamePrefix() + "_" + TASK_PROGRESS_COLLECTION_NAME_SUFFIX;
    taskProgressResponseCollection = database.getCollection(taskProgressResponseCollectionName);
  }

  @Override
  public void publishSyncTaskResponse(String delegateTaskId, byte[] responseData) {
    Document document = new Document();
    document.put(ID_FIELD_NAME, delegateTaskId);
    document.put(DelegateSyncTaskResponseKeys.responseData, responseData);
    syncTaskResponseCollection.insertOne(document);
  }

  private static final UpdateOptions upsert = new UpdateOptions().upsert(true);

  @Override
  public void publishAsyncTaskResponse(String delegateTaskId, byte[] responseData) {
    Bson filter = Filters.eq(ID_FIELD_NAME, delegateTaskId);

    Document document = new Document();
    document.put(ID_FIELD_NAME, delegateTaskId);
    document.put(DelegateAsyncTaskResponseKeys.responseData, responseData);
    document.put(DelegateAsyncTaskResponseKeys.processAfter, 0);

    Bson update = new Document("$set", document);

    asyncTaskResponseCollection.updateOne(filter, update, upsert);
  }

  @Override
  public void publishTaskProgressResponse(String delegateTaskId, byte[] responseData) {
    Bson filter = Filters.eq(ID_FIELD_NAME, delegateTaskId);

    Document document = new Document();
    document.put(ID_FIELD_NAME, delegateTaskId);
    document.put(DelegateTaskProgressResponseKeys.responseData, responseData);
    document.put(DelegateTaskProgressResponseKeys.processAfter, 0);

    Bson update = new Document("$set", document);

    taskProgressResponseCollection.updateOne(filter, update, upsert);
  }

  @Override
  public void destroy() {
    mongoClient.close();
  }
}
