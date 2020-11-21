package io.harness.app.impl;

import static org.mockito.Mockito.mock;

import io.harness.app.CIManagerConfiguration;
import io.harness.app.CIManagerServiceModule;
import io.harness.app.SCMGrpcClientModule;
import io.harness.app.ScmConnectionConfig;
import io.harness.ci.beans.entities.LogServiceConfig;
import io.harness.ci.config.CIExecutionServiceConfig;
import io.harness.factory.ClosingFactory;
import io.harness.factory.ClosingFactoryModule;
import io.harness.govern.ProviderModule;
import io.harness.govern.ServersModule;
import io.harness.morphia.MorphiaRegistrar;
import io.harness.ngpipeline.inputset.repository.spring.InputSetRepository;
import io.harness.ngpipeline.pipeline.repository.spring.NgPipelineRepository;
import io.harness.queue.QueueController;
import io.harness.remote.client.ServiceHttpClientConfig;
import io.harness.rule.InjectorRuleMixin;
import io.harness.serializer.CiBeansRegistrars;
import io.harness.serializer.CiExecutionRegistrars;
import io.harness.serializer.ConnectorNextGenRegistrars;
import io.harness.serializer.KryoRegistrar;
import io.harness.serializer.OrchestrationBeansRegistrars;
import io.harness.serializer.PersistenceRegistrars;
import io.harness.serializer.YamlBeansModuleRegistrars;
import io.harness.spring.AliasRegistrar;
import io.harness.testlib.module.MongoRuleMixin;
import io.harness.testlib.module.TestMongoModule;
import io.harness.threading.CurrentThreadExecutor;
import io.harness.threading.ExecutorModule;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mongodb.morphia.converters.TypeConverter;

public class CIManagerRule implements MethodRule, InjectorRuleMixin, MongoRuleMixin {
  ClosingFactory closingFactory;

  public CIManagerRule(ClosingFactory closingFactory) {
    this.closingFactory = closingFactory;
  }

  @Override
  public List<Module> modules(List<Annotation> annotations) {
    ExecutorModule.getInstance().setExecutorService(new CurrentThreadExecutor());

    List<Module> modules = new ArrayList<>();

    modules.add(new ProviderModule() {
      @Provides
      @Singleton
      Set<Class<? extends KryoRegistrar>> registrars() {
        return ImmutableSet.<Class<? extends KryoRegistrar>>builder()
            .addAll(YamlBeansModuleRegistrars.kryoRegistrars)
            .addAll(CiBeansRegistrars.kryoRegistrars)
            .addAll(CiExecutionRegistrars.kryoRegistrars)
            .addAll(ConnectorNextGenRegistrars.kryoRegistrars)
            .build();
      }

      @Provides
      @Singleton
      Set<Class<? extends MorphiaRegistrar>> morphiaRegistrars() {
        return ImmutableSet.<Class<? extends MorphiaRegistrar>>builder()
            .addAll(CiExecutionRegistrars.morphiaRegistrars)
            .build();
      }

      @Provides
      @Singleton
      Set<Class<? extends AliasRegistrar>> aliasRegistrars() {
        return ImmutableSet.<Class<? extends AliasRegistrar>>builder()
            .addAll(CiExecutionRegistrars.aliasRegistrars)
            .build();
      }

      @Provides
      @Singleton
      Set<Class<? extends TypeConverter>> morphiaConverters() {
        return ImmutableSet.<Class<? extends TypeConverter>>builder()
            .addAll(PersistenceRegistrars.morphiaConverters)
            .addAll(OrchestrationBeansRegistrars.morphiaConverters)
            .build();
      }
    });

    CIManagerConfiguration configuration =
        CIManagerConfiguration.builder()
            .managerAuthority("localhost")
            .managerTarget("localhost:9880")
            .ciExecutionServiceConfig(CIExecutionServiceConfig.builder()
                                          .addonImageTag("v1.4-alpha")
                                          .defaultCPULimit(200)
                                          .defaultInternalImageConnector("account.harnessimage")
                                          .defaultMemoryLimit(200)
                                          .delegateServiceEndpointVariableValue("delegate-service:8080")
                                          .liteEngineImageTag("v1.4-alpha")
                                          .pvcDefaultStorageSize(25600)
                                          .build())
            .logServiceConfig(
                LogServiceConfig.builder().baseUrl("http://localhost-inc:8079").globalToken("global-token").build())
            .scmConnectionConfig(ScmConnectionConfig.builder().url("localhost:8181").build())
            .managerServiceSecret("IC04LYMBf1lDP5oeY4hupxd4HJhLmN6azUku3xEbeE3SUx5G3ZYzhbiwVtK4i7AmqyU9OZkwB4v8E9qM")
            .ngManagerClientConfig(ServiceHttpClientConfig.builder().baseUrl("http://localhost:7457/").build())
            .managerClientConfig(ServiceHttpClientConfig.builder().baseUrl("http://localhost:3457/").build())
            .ngManagerServiceSecret("IC04LYMBf1lDP5oeY4hupxd4HJhLmN6azUku3xEbeE3SUx5G3ZYzhbiwVtK4i7AmqyU9OZkwB4v8E9qM")
            .build();

    modules.add(new SCMGrpcClientModule(configuration.getScmConnectionConfig()));
    modules.add(new ClosingFactoryModule(closingFactory));
    modules.add(mongoTypeModule(annotations));
    modules.add(new AbstractModule() {
      @Override
      protected void configure() {
        bind(NgPipelineRepository.class).toInstance(mock(NgPipelineRepository.class));
        bind(InputSetRepository.class).toInstance(mock(InputSetRepository.class));
        bind(QueueController.class).toInstance(new QueueController() {
          @Override
          public boolean isPrimary() {
            return true;
          }

          @Override
          public boolean isNotPrimary() {
            return false;
          }
        });
      }
    });
    modules.add(TestMongoModule.getInstance());
    modules.add(new CIManagerPersistenceTestModule());
    modules.add(new CIManagerServiceModule(configuration));
    return modules;
  }

  @Override
  public void initialize(Injector injector, List<Module> modules) {
    for (Module module : modules) {
      if (module instanceof ServersModule) {
        for (Closeable server : ((ServersModule) module).servers(injector)) {
          closingFactory.addServer(server);
        }
      }
    }
  }

  @Override
  public Statement apply(Statement statement, FrameworkMethod frameworkMethod, Object target) {
    return applyInjector(statement, frameworkMethod, target);
  }
}
