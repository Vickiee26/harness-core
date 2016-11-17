package software.wings.service.intfc;

import ru.vyarus.guice.validator.group.annotation.ValidationGroups;
import software.wings.beans.ExecutionArgs;
import software.wings.beans.Pipeline;
import software.wings.beans.PipelineExecution;
import software.wings.beans.WorkflowExecution;
import software.wings.dl.PageRequest;
import software.wings.dl.PageResponse;
import software.wings.utils.validation.Create;
import software.wings.utils.validation.Update;

import javax.validation.Valid;

/**
 * Created by anubhaw on 10/26/16.
 */
public interface PipelineService {
  /**
   * List pipeline executions page response.
   *
   * @param pageRequest the page request
   * @return the page response
   */
  PageResponse<PipelineExecution> listPipelineExecutions(PageRequest<PipelineExecution> pageRequest);

  /**
   * Execute workflow execution.
   *
   * @param appId         the app id
   * @param pipelineId    the pipeline id
   * @param executionArgs the execution args
   * @return the workflow execution
   */
  WorkflowExecution execute(String appId, String pipelineId, ExecutionArgs executionArgs);

  /**
   * Refresh pipeline execution.
   *
   * @param appId               the app id
   * @param workflowExecutionId the workflow execution id
   */
  void refreshPipelineExecution(String appId, String workflowExecutionId);

  /**
   * Refresh pipeline execution.
   *
   * @param appId               the app id
   * @param workflowExecutionId the workflow execution id
   */
  void refreshPipelineExecutionAsync(String appId, String workflowExecutionId);

  /**
   * List pipelines page response.
   *
   * @param pageRequest the page request
   * @return the page response
   */
  PageResponse<Pipeline> listPipelines(PageRequest<Pipeline> pageRequest);

  /**
   * Read pipeline pipeline.
   *
   * @param appId      the app id
   * @param pipelineId the pipeline id
   * @return the pipeline
   */
  Pipeline readPipeline(String appId, String pipelineId);

  /**
   * Create pipeline pipeline.
   *
   * @param pipeline the pipeline
   * @return the pipeline
   */
  @ValidationGroups(Create.class) Pipeline createPipeline(@Valid Pipeline pipeline);

  /**
   * Update pipeline pipeline.
   *
   * @param pipeline the pipeline
   * @return the pipeline
   */
  @ValidationGroups(Update.class) Pipeline updatePipeline(@Valid Pipeline pipeline);

  /**
   * Delete pipeline boolean.
   *
   * @param appId      the app id
   * @param pipelineId the pipeline id
   * @return the boolean
   */
  boolean deletePipeline(String appId, String pipelineId);
}
