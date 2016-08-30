package software.wings.sm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.wings.WingsBaseTest;
import software.wings.beans.Application;
import software.wings.beans.Environment;
import software.wings.beans.Environment.Builder;
import software.wings.beans.ErrorCodes;
import software.wings.common.UUIDGenerator;
import software.wings.dl.WingsPersistence;
import software.wings.exception.WingsException;

import javax.inject.Inject;

/**
 *
 */

/**
 * The type State machine execution event manager test.
 *
 * @author Rishi
 */
public class StateMachineExecutionEventManagerTest extends WingsBaseTest {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Inject private WingsPersistence wingsPersistence;
  @InjectMocks @Inject private StateMachineExecutionEventManager stateMachineExecutionEventManager;
  @Mock private StateMachineExecutor stateMachineExecutor;

  /**
   * Should throw invalid argument for null state execution instance.
   */
  @Test
  public void shouldThrowInvalidArgumentForNullStateExecutionInstance() {
    Application app =
        wingsPersistence.saveAndGet(Application.class, Application.Builder.anApplication().withName("App1").build());
    Environment env =
        wingsPersistence.saveAndGet(Environment.class, Builder.anEnvironment().withAppId(app.getUuid()).build());
    ExecutionEvent executionEvent = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                                        .withAppId(app.getUuid())
                                        .withExecutionEventType(ExecutionEventType.PAUSE)
                                        .withEnvId(env.getUuid())
                                        .withExecutionUuid(UUIDGenerator.getUuid())
                                        .build();
    try {
      executionEvent = stateMachineExecutionEventManager.registerExecutionEvent(executionEvent);
      failBecauseExceptionWasNotThrown(WingsException.class);
    } catch (WingsException exception) {
      assertThat(exception.getParams().values()).doesNotContainNull();
      assertThat(exception.getParams().values().iterator().next())
          .isInstanceOf(String.class)
          .asString()
          .startsWith("null stateExecutionInstanceId");
      assertThat(exception).hasMessage(ErrorCodes.INVALID_ARGUMENT.getCode());
    }
  }

  /**
   * Should throw invalid argument for invalid state execution instance.
   */
  @Test
  public void shouldThrowInvalidArgumentForInvalidStateExecutionInstance() {
    Application app =
        wingsPersistence.saveAndGet(Application.class, Application.Builder.anApplication().withName("App1").build());
    Environment env =
        wingsPersistence.saveAndGet(Environment.class, Builder.anEnvironment().withAppId(app.getUuid()).build());
    ExecutionEvent executionEvent = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                                        .withAppId(app.getUuid())
                                        .withExecutionEventType(ExecutionEventType.PAUSE)
                                        .withEnvId(env.getUuid())
                                        .withExecutionUuid(UUIDGenerator.getUuid())
                                        .withStateExecutionInstanceId(UUIDGenerator.getUuid())
                                        .build();
    try {
      executionEvent = stateMachineExecutionEventManager.registerExecutionEvent(executionEvent);
      failBecauseExceptionWasNotThrown(WingsException.class);
    } catch (WingsException exception) {
      assertThat(exception.getParams().values()).doesNotContainNull();
      assertThat(exception.getParams().values().iterator().next())
          .isInstanceOf(String.class)
          .asString()
          .startsWith("invalid stateExecutionInstanceId");
      assertThat(exception).hasMessage(ErrorCodes.INVALID_ARGUMENT.getCode());
    }
  }

  /**
   * Should throw state not for resume.
   */
  @Test
  public void shouldThrowStateNotForResume() {
    Application app =
        wingsPersistence.saveAndGet(Application.class, Application.Builder.anApplication().withName("App1").build());
    Environment env =
        wingsPersistence.saveAndGet(Environment.class, Builder.anEnvironment().withAppId(app.getUuid()).build());
    StateExecutionInstance stateExecutionInstance = StateExecutionInstance.Builder.aStateExecutionInstance()
                                                        .withAppId(app.getUuid())
                                                        .withStateName("state1")
                                                        .build();
    stateExecutionInstance = wingsPersistence.saveAndGet(StateExecutionInstance.class, stateExecutionInstance);
    ExecutionEvent executionEvent = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                                        .withAppId(app.getUuid())
                                        .withExecutionEventType(ExecutionEventType.RESUME)
                                        .withEnvId(env.getUuid())
                                        .withExecutionUuid(UUIDGenerator.getUuid())
                                        .withStateExecutionInstanceId(stateExecutionInstance.getUuid())
                                        .build();
    try {
      executionEvent = stateMachineExecutionEventManager.registerExecutionEvent(executionEvent);
      failBecauseExceptionWasNotThrown(WingsException.class);
    } catch (WingsException exception) {
      assertThat(exception).hasMessage(ErrorCodes.STATE_NOT_FOR_RESUME.getCode());
    }
  }

  /**
   * Should throw state not for retry.
   */
  @Test
  public void shouldThrowStateNotForRetry() {
    Application app =
        wingsPersistence.saveAndGet(Application.class, Application.Builder.anApplication().withName("App1").build());
    Environment env =
        wingsPersistence.saveAndGet(Environment.class, Builder.anEnvironment().withAppId(app.getUuid()).build());
    StateExecutionInstance stateExecutionInstance = StateExecutionInstance.Builder.aStateExecutionInstance()
                                                        .withAppId(app.getUuid())
                                                        .withStateName("state1")
                                                        .build();
    stateExecutionInstance = wingsPersistence.saveAndGet(StateExecutionInstance.class, stateExecutionInstance);
    ExecutionEvent executionEvent = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                                        .withAppId(app.getUuid())
                                        .withExecutionEventType(ExecutionEventType.RETRY)
                                        .withEnvId(env.getUuid())
                                        .withExecutionUuid(UUIDGenerator.getUuid())
                                        .withStateExecutionInstanceId(stateExecutionInstance.getUuid())
                                        .build();
    try {
      executionEvent = stateMachineExecutionEventManager.registerExecutionEvent(executionEvent);
      failBecauseExceptionWasNotThrown(WingsException.class);
    } catch (WingsException exception) {
      assertThat(exception).hasMessage(ErrorCodes.STATE_NOT_FOR_RETRY.getCode());
    }
  }

  /**
   * Should throw state not for pause.
   */
  @Test
  public void shouldThrowStateNotForPause() {
    Application app =
        wingsPersistence.saveAndGet(Application.class, Application.Builder.anApplication().withName("App1").build());
    Environment env =
        wingsPersistence.saveAndGet(Environment.class, Builder.anEnvironment().withAppId(app.getUuid()).build());
    StateExecutionInstance stateExecutionInstance = StateExecutionInstance.Builder.aStateExecutionInstance()
                                                        .withAppId(app.getUuid())
                                                        .withStateName("state1")
                                                        .withStatus(ExecutionStatus.SUCCESS)
                                                        .build();
    stateExecutionInstance = wingsPersistence.saveAndGet(StateExecutionInstance.class, stateExecutionInstance);
    ExecutionEvent executionEvent = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                                        .withAppId(app.getUuid())
                                        .withExecutionEventType(ExecutionEventType.PAUSE)
                                        .withEnvId(env.getUuid())
                                        .withExecutionUuid(UUIDGenerator.getUuid())
                                        .withStateExecutionInstanceId(stateExecutionInstance.getUuid())
                                        .build();
    try {
      executionEvent = stateMachineExecutionEventManager.registerExecutionEvent(executionEvent);
      failBecauseExceptionWasNotThrown(WingsException.class);
    } catch (WingsException exception) {
      assertThat(exception).hasMessage(ErrorCodes.STATE_NOT_FOR_PAUSE.getCode());
    }
  }

  /**
   * Should throw state not for abort.
   */
  @Test
  public void shouldThrowStateNotForAbort() {
    Application app =
        wingsPersistence.saveAndGet(Application.class, Application.Builder.anApplication().withName("App1").build());
    Environment env =
        wingsPersistence.saveAndGet(Environment.class, Builder.anEnvironment().withAppId(app.getUuid()).build());
    StateExecutionInstance stateExecutionInstance = StateExecutionInstance.Builder.aStateExecutionInstance()
                                                        .withAppId(app.getUuid())
                                                        .withStateName("state1")
                                                        .withStatus(ExecutionStatus.SUCCESS)
                                                        .build();
    stateExecutionInstance = wingsPersistence.saveAndGet(StateExecutionInstance.class, stateExecutionInstance);
    ExecutionEvent executionEvent = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                                        .withAppId(app.getUuid())
                                        .withExecutionEventType(ExecutionEventType.ABORT)
                                        .withEnvId(env.getUuid())
                                        .withExecutionUuid(UUIDGenerator.getUuid())
                                        .withStateExecutionInstanceId(stateExecutionInstance.getUuid())
                                        .build();
    try {
      executionEvent = stateMachineExecutionEventManager.registerExecutionEvent(executionEvent);
      failBecauseExceptionWasNotThrown(WingsException.class);
    } catch (WingsException exception) {
      assertThat(exception).hasMessage(ErrorCodes.STATE_NOT_FOR_ABORT.getCode());
    }
  }

  /**
   * Should throw abort all already.
   */
  @Test
  public void shouldThrowAbortAllAlready() {
    Application app =
        wingsPersistence.saveAndGet(Application.class, Application.Builder.anApplication().withName("App1").build());
    Environment env =
        wingsPersistence.saveAndGet(Environment.class, Builder.anEnvironment().withAppId(app.getUuid()).build());

    String executionUuid = UUIDGenerator.getUuid();
    ExecutionEvent executionEvent = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                                        .withAppId(app.getUuid())
                                        .withExecutionEventType(ExecutionEventType.ABORT_ALL)
                                        .withEnvId(env.getUuid())
                                        .withExecutionUuid(executionUuid)
                                        .build();
    executionEvent = stateMachineExecutionEventManager.registerExecutionEvent(executionEvent);

    executionEvent = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                         .withAppId(app.getUuid())
                         .withExecutionEventType(ExecutionEventType.PAUSE_ALL)
                         .withEnvId(env.getUuid())
                         .withExecutionUuid(executionUuid)
                         .build();
    try {
      executionEvent = stateMachineExecutionEventManager.registerExecutionEvent(executionEvent);
      failBecauseExceptionWasNotThrown(WingsException.class);
    } catch (WingsException exception) {
      assertThat(exception).hasMessage(ErrorCodes.ABORT_ALL_ALREADY.getCode());
    }
  }

  /**
   * Should throw pause all already.
   */
  @Test
  public void shouldThrowPauseAllAlready() {
    Application app =
        wingsPersistence.saveAndGet(Application.class, Application.Builder.anApplication().withName("App1").build());
    Environment env =
        wingsPersistence.saveAndGet(Environment.class, Builder.anEnvironment().withAppId(app.getUuid()).build());

    String executionUuid = UUIDGenerator.getUuid();
    ExecutionEvent executionEvent = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                                        .withAppId(app.getUuid())
                                        .withExecutionEventType(ExecutionEventType.PAUSE_ALL)
                                        .withEnvId(env.getUuid())
                                        .withExecutionUuid(executionUuid)
                                        .build();
    executionEvent = stateMachineExecutionEventManager.registerExecutionEvent(executionEvent);

    executionEvent = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                         .withAppId(app.getUuid())
                         .withExecutionEventType(ExecutionEventType.PAUSE_ALL)
                         .withEnvId(env.getUuid())
                         .withExecutionUuid(executionUuid)
                         .build();
    try {
      executionEvent = stateMachineExecutionEventManager.registerExecutionEvent(executionEvent);
      failBecauseExceptionWasNotThrown(WingsException.class);
    } catch (WingsException exception) {
      assertThat(exception).hasMessage(ErrorCodes.PAUSE_ALL_ALREADY.getCode());
    }
  }

  /**
   * Should pause all clear previous resume all.
   */
  @Test
  public void shouldPauseAllClearPreviousResumeAll() {
    Application app =
        wingsPersistence.saveAndGet(Application.class, Application.Builder.anApplication().withName("App1").build());
    Environment env =
        wingsPersistence.saveAndGet(Environment.class, Builder.anEnvironment().withAppId(app.getUuid()).build());

    String executionUuid = UUIDGenerator.getUuid();
    ExecutionEvent executionEvent = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                                        .withAppId(app.getUuid())
                                        .withExecutionEventType(ExecutionEventType.PAUSE_ALL)
                                        .withEnvId(env.getUuid())
                                        .withExecutionUuid(executionUuid)
                                        .build();
    executionEvent = stateMachineExecutionEventManager.registerExecutionEvent(executionEvent);

    ExecutionEvent resumeAll = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                                   .withAppId(app.getUuid())
                                   .withExecutionEventType(ExecutionEventType.RESUME_ALL)
                                   .withEnvId(env.getUuid())
                                   .withExecutionUuid(executionUuid)
                                   .build();
    resumeAll = stateMachineExecutionEventManager.registerExecutionEvent(resumeAll);

    executionEvent = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                         .withAppId(app.getUuid())
                         .withExecutionEventType(ExecutionEventType.PAUSE_ALL)
                         .withEnvId(env.getUuid())
                         .withExecutionUuid(executionUuid)
                         .build();
    executionEvent = stateMachineExecutionEventManager.registerExecutionEvent(executionEvent);
    assertThat(wingsPersistence.get(ExecutionEvent.class, resumeAll.getAppId(), resumeAll.getUuid())).isNull();
  }

  /**
   * Should throw resume all already.
   */
  @Test
  public void shouldThrowResumeAllAlready() {
    Application app =
        wingsPersistence.saveAndGet(Application.class, Application.Builder.anApplication().withName("App1").build());
    Environment env =
        wingsPersistence.saveAndGet(Environment.class, Builder.anEnvironment().withAppId(app.getUuid()).build());

    String executionUuid = UUIDGenerator.getUuid();
    ExecutionEvent executionEvent = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                                        .withAppId(app.getUuid())
                                        .withExecutionEventType(ExecutionEventType.RESUME_ALL)
                                        .withEnvId(env.getUuid())
                                        .withExecutionUuid(executionUuid)
                                        .build();
    try {
      executionEvent = stateMachineExecutionEventManager.registerExecutionEvent(executionEvent);
      failBecauseExceptionWasNotThrown(WingsException.class);
    } catch (WingsException exception) {
      assertThat(exception).hasMessage(ErrorCodes.RESUME_ALL_ALREADY.getCode());
    }
  }

  /**
   * Should resume all clear prev pause all.
   */
  @Test
  public void shouldResumeAllClearPrevPauseAll() {
    Application app =
        wingsPersistence.saveAndGet(Application.class, Application.Builder.anApplication().withName("App1").build());
    Environment env =
        wingsPersistence.saveAndGet(Environment.class, Builder.anEnvironment().withAppId(app.getUuid()).build());

    String executionUuid = UUIDGenerator.getUuid();

    ExecutionEvent pauseAll = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                                  .withAppId(app.getUuid())
                                  .withExecutionEventType(ExecutionEventType.PAUSE_ALL)
                                  .withEnvId(env.getUuid())
                                  .withExecutionUuid(executionUuid)
                                  .build();
    pauseAll = stateMachineExecutionEventManager.registerExecutionEvent(pauseAll);

    ExecutionEvent executionEvent = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                                        .withAppId(app.getUuid())
                                        .withExecutionEventType(ExecutionEventType.RESUME_ALL)
                                        .withEnvId(env.getUuid())
                                        .withExecutionUuid(executionUuid)
                                        .build();
    executionEvent = stateMachineExecutionEventManager.registerExecutionEvent(executionEvent);

    assertThat(wingsPersistence.get(ExecutionEvent.class, pauseAll.getAppId(), pauseAll.getUuid())).isNull();
  }

  /**
   * Should throw resume all already 2.
   */
  @Test
  public void shouldThrowResumeAllAlready2() {
    Application app =
        wingsPersistence.saveAndGet(Application.class, Application.Builder.anApplication().withName("App1").build());
    Environment env =
        wingsPersistence.saveAndGet(Environment.class, Builder.anEnvironment().withAppId(app.getUuid()).build());

    String executionUuid = UUIDGenerator.getUuid();

    ExecutionEvent pauseAll = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                                  .withAppId(app.getUuid())
                                  .withExecutionEventType(ExecutionEventType.PAUSE_ALL)
                                  .withEnvId(env.getUuid())
                                  .withExecutionUuid(executionUuid)
                                  .build();
    pauseAll = stateMachineExecutionEventManager.registerExecutionEvent(pauseAll);

    ExecutionEvent executionEvent = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                                        .withAppId(app.getUuid())
                                        .withExecutionEventType(ExecutionEventType.RESUME_ALL)
                                        .withEnvId(env.getUuid())
                                        .withExecutionUuid(executionUuid)
                                        .build();
    executionEvent = stateMachineExecutionEventManager.registerExecutionEvent(executionEvent);

    executionEvent = ExecutionEvent.Builder.aWorkflowExecutionEvent()
                         .withAppId(app.getUuid())
                         .withExecutionEventType(ExecutionEventType.RESUME_ALL)
                         .withEnvId(env.getUuid())
                         .withExecutionUuid(executionUuid)
                         .build();
    try {
      executionEvent = stateMachineExecutionEventManager.registerExecutionEvent(executionEvent);
      failBecauseExceptionWasNotThrown(WingsException.class);
    } catch (WingsException exception) {
      assertThat(exception).hasMessage(ErrorCodes.RESUME_ALL_ALREADY.getCode());
    }
  }
}
