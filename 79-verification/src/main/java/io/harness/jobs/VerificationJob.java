package io.harness.jobs;

import static io.harness.jobs.LogDataProcessorJob.LOG_DATA_PROCESSOR_CRON_GROUP;
import static io.harness.jobs.MetricDataAnalysisJob.METRIC_DATA_ANALYSIS_CRON_GROUP;
import static io.harness.jobs.MetricDataProcessorJob.METRIC_DATA_PROCESSOR_CRON_GROUP;
import static io.harness.jobs.sg247.collection.ServiceGuardDataCollectionJob.SERVICE_GUARD_DATA_COLLECTION_CRON;
import static software.wings.common.VerificationConstants.CRON_POLL_INTERVAL;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.harness.jobs.sg247.collection.ServiceGuardDataCollectionJob;
import io.harness.managerclient.VerificationManagerClient;
import io.harness.managerclient.VerificationManagerClientHelper;
import io.harness.scheduler.PersistentScheduler;
import io.harness.service.intfc.ContinuousVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import software.wings.beans.Account;
import software.wings.service.intfc.verification.CVConfigurationService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Verification job that handles scheduling jobs related to APM and Logs
 *
 * Created by Pranjal on 10/04/2018
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Slf4j
@Deprecated
public class VerificationJob implements Job {
  // Cron name to uniquely identify the cron
  public static final String VERIFICATION_CRON_NAME = "VERIFICATION_CRON_NAME";
  // Cron Group name
  public static final String VERIFICATION_CRON_GROUP = "VERIFICATION_CRON_GROUP";

  @Inject @Named("BackgroundJobScheduler") private PersistentScheduler jobScheduler;

  @Inject private VerificationManagerClient verificationManagerClient;

  @Inject private VerificationManagerClientHelper verificationManagerClientHelper;

  @Inject private CVConfigurationService cvConfigurationService;

  @Inject private ContinuousVerificationService continuousVerificationService;

  private List<Account> lastAvailableAccounts = new ArrayList<>();

  @Override
  public void execute(JobExecutionContext JobExecutionContext) {
    logger.warn("Deprecating Verification Job .. New Job is ServiceGuardMainJob");
  }

  public static void removeJob(PersistentScheduler jobScheduler) {
    jobScheduler.deleteJob(VERIFICATION_CRON_NAME, VERIFICATION_CRON_GROUP);
  }

  public static void addJob(PersistentScheduler jobScheduler) {
    if (!jobScheduler.checkExists(VERIFICATION_CRON_NAME, VERIFICATION_CRON_GROUP)) {
      JobDetail job = JobBuilder.newJob(VerificationJob.class)
                          .withIdentity(VERIFICATION_CRON_NAME, VERIFICATION_CRON_GROUP)
                          .withDescription("Verification job ")
                          .build();
      Trigger trigger = TriggerBuilder.newTrigger()
                            .withIdentity(VERIFICATION_CRON_NAME, VERIFICATION_CRON_GROUP)
                            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                              .withIntervalInSeconds((int) (CRON_POLL_INTERVAL / 2))
                                              .repeatForever())
                            .build();
      jobScheduler.scheduleJob(job, trigger);
      logger.info("Added job with details : {}", job);
    }
  }

  public void triggerDataProcessorCron(List<Account> enabledAccounts) {
    logger.info("Triggering crons for " + enabledAccounts.size() + " enabled accounts");
    enabledAccounts.forEach(account -> {
      scheduleServiceGuardTimeSeriesCronJobs(account.getUuid());
      scheduleLogDataProcessorCronJob(account.getUuid());
    });
  }

  private void scheduleServiceGuardTimeSeriesCronJobs(String accountId) {
    if (!jobScheduler.checkExists(accountId, SERVICE_GUARD_DATA_COLLECTION_CRON)) {
      Date startDate = new Date(new Date().getTime() + TimeUnit.MINUTES.toMillis(1));
      JobDetail job = JobBuilder.newJob(ServiceGuardDataCollectionJob.class)
                          .withIdentity(accountId, SERVICE_GUARD_DATA_COLLECTION_CRON)
                          .usingJobData("timestamp", System.currentTimeMillis())
                          .usingJobData("accountId", accountId)
                          .build();

      Trigger trigger = TriggerBuilder.newTrigger()
                            .withIdentity(accountId, SERVICE_GUARD_DATA_COLLECTION_CRON)
                            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                              .withIntervalInSeconds(30)
                                              .withMisfireHandlingInstructionNowWithExistingCount()
                                              .repeatForever())
                            .startAt(startDate)
                            .build();

      jobScheduler.scheduleJob(job, trigger);
      logger.info("Scheduled APM data collection Cron Job for Account : {}, with details : {}", accountId, job);
    }

    if (!jobScheduler.checkExists(accountId, METRIC_DATA_ANALYSIS_CRON_GROUP)) {
      Date startDate = new Date(new Date().getTime() + TimeUnit.MINUTES.toMillis(1));
      JobDetail job = JobBuilder.newJob(MetricDataAnalysisJob.class)
                          .withIdentity(accountId, METRIC_DATA_ANALYSIS_CRON_GROUP)
                          .usingJobData("timestamp", System.currentTimeMillis())
                          .usingJobData("accountId", accountId)
                          .build();

      Trigger trigger = TriggerBuilder.newTrigger()
                            .withIdentity(accountId, METRIC_DATA_ANALYSIS_CRON_GROUP)
                            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                              .withIntervalInSeconds(30)
                                              .withMisfireHandlingInstructionNowWithExistingCount()
                                              .repeatForever())
                            .startAt(startDate)
                            .build();

      jobScheduler.scheduleJob(job, trigger);
      logger.info("Scheduled APM data collection Cron Job for Account : {}, with details : {}", accountId, job);
    }
  }

  private void scheduleLogDataProcessorCronJob(String accountId) {
    if (jobScheduler.checkExists(accountId, LOG_DATA_PROCESSOR_CRON_GROUP)) {
      return;
    }
    Date startDate = new Date(new Date().getTime() + TimeUnit.MINUTES.toMillis(1));
    JobDetail job = JobBuilder.newJob(LogDataProcessorJob.class)
                        .withIdentity(accountId, LOG_DATA_PROCESSOR_CRON_GROUP)
                        .usingJobData("timestamp", System.currentTimeMillis())
                        .usingJobData("accountId", accountId)
                        .build();

    Trigger trigger = TriggerBuilder.newTrigger()
                          .withIdentity(accountId, LOG_DATA_PROCESSOR_CRON_GROUP)
                          .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                            .withIntervalInSeconds((int) (CRON_POLL_INTERVAL / 10))
                                            .withMisfireHandlingInstructionNowWithExistingCount()
                                            .repeatForever())
                          .startAt(startDate)
                          .build();

    jobScheduler.scheduleJob(job, trigger);
    logger.info("Scheduled Log data collection Cron Job for Account : {}, with details : {}", accountId, job);
  }

  public static void deleteCrons(List<Account> disabledAccounts, PersistentScheduler jobScheduler) {
    logger.info("Deleting crons for " + disabledAccounts.size() + " accounts");
    disabledAccounts.forEach(account -> {
      if (jobScheduler.checkExists(account.getUuid(), METRIC_DATA_PROCESSOR_CRON_GROUP)) {
        jobScheduler.deleteJob(account.getUuid(), METRIC_DATA_PROCESSOR_CRON_GROUP);
        logger.info("Deleting old crons for account {} ", account.getUuid());
      }

      if (jobScheduler.checkExists(account.getUuid(), LOG_DATA_PROCESSOR_CRON_GROUP)) {
        jobScheduler.deleteJob(account.getUuid(), LOG_DATA_PROCESSOR_CRON_GROUP);
        logger.info("Deleting old crons for account {} ", account.getUuid());
      }

      if (jobScheduler.checkExists(account.getUuid(), METRIC_DATA_ANALYSIS_CRON_GROUP)) {
        jobScheduler.deleteJob(account.getUuid(), METRIC_DATA_ANALYSIS_CRON_GROUP);
        logger.info("Deleting old crons for account {} ", account.getUuid());
      }
    });
  }

  private void cleanUpAfterDeletionOfEntity() {
    cvConfigurationService.deleteStaleConfigs();
  }

  @VisibleForTesting
  public void setQuartzScheduler(PersistentScheduler jobScheduler) {
    this.jobScheduler = jobScheduler;
  }
}
