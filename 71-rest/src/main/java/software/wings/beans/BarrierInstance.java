package software.wings.beans;

import io.harness.annotation.HarnessEntity;
import io.harness.iterator.PersistentRegularIterable;
import io.harness.mongo.index.CompoundMongoIndex;
import io.harness.mongo.index.FdIndex;
import io.harness.mongo.index.FdTtlIndex;
import io.harness.mongo.index.MongoIndex;
import io.harness.persistence.PersistentEntity;
import io.harness.persistence.UuidAware;

import software.wings.beans.BarrierInstancePipeline.BarrierInstancePipelineKeys;
import software.wings.beans.BarrierInstanceWorkflow.BarrierInstanceWorkflowKeys;
import software.wings.beans.entityinterface.ApplicationAccess;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.reinert.jjschema.SchemaIgnore;
import com.google.common.collect.ImmutableList;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.UtilityClass;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@FieldNameConstants(innerTypeName = "BarrierInstanceKeys")
@Entity(value = "barrierInstances", noClassnameStored = true)
@HarnessEntity(exportable = false)
public class BarrierInstance implements PersistentEntity, UuidAware, PersistentRegularIterable, ApplicationAccess {
  public static List<MongoIndex> mongoIndexes() {
    return ImmutableList.<MongoIndex>builder()
        .add(CompoundMongoIndex.builder()
                 .name("search2")
                 .unique(true)
                 .field(BarrierInstanceKeys.name)
                 .field(BarrierInstanceKeys.pipeline_executionId)
                 .field(BarrierInstanceKeys.pipeline_parallelIndex)
                 .build())
        .add(CompoundMongoIndex.builder()
                 .name("next")
                 .field(BarrierInstanceKeys.state)
                 .field(BarrierInstanceKeys.nextIteration)
                 .build())
        .build();
  }

  @Id private String uuid;
  @FdIndex @NotNull protected String appId;

  private String name;
  @FdIndex private String state;

  private Long nextIteration;

  @Override
  public Long obtainNextIteration(String fieldName) {
    return nextIteration;
  }

  @Override
  public void updateNextIteration(String fieldName, long nextIteration) {
    this.nextIteration = nextIteration;
  }

  private BarrierInstancePipeline pipeline;

  @FdIndex private String accountId;

  @JsonIgnore
  @SchemaIgnore
  @FdTtlIndex
  @Builder.Default
  private Date validUntil = Date.from(OffsetDateTime.now().plusMonths(1).toInstant());

  @UtilityClass
  public static final class BarrierInstanceKeys {
    public static final String pipeline_executionId = pipeline + "." + BarrierInstancePipelineKeys.executionId;
    public static final String pipeline_parallelIndex = pipeline + "." + BarrierInstancePipelineKeys.parallelIndex;
    public static final String pipeline_workflows_pipelineStageId =
        pipeline + "." + BarrierInstancePipelineKeys.workflows + "." + BarrierInstanceWorkflowKeys.pipelineStageId;
  }
}
