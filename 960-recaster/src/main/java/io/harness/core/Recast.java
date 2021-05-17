package io.harness.core;

import io.harness.annotation.RecasterAlias;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.transformers.RecastTransformer;

import java.util.HashSet;
import java.util.Set;
import org.bson.Document;
import org.reflections.Reflections;

/**
 * The translation layer for conversion from PipelineService json string to sdk objects
 */
@OwnedBy(HarnessTeam.PIPELINE)
public class Recast {
  private final Recaster recaster;
  private final AliasRegistry aliasRegistry;

  public Recast() {
    this(new Recaster(), new HashSet<>());
  }

  public Recast(final Set<Class<?>> classesToMap) {
    this(new Recaster(), classesToMap);
  }

  public Recast(final Recaster recaster, final Set<Class<?>> classesToMap) {
    this.recaster = recaster;
    this.aliasRegistry = AliasRegistry.getInstance();

    for (final Class<?> c : classesToMap) {
      map(c);
    }
  }

  public synchronized Recast map(final Class<?>... entityClasses) {
    if (entityClasses != null && entityClasses.length > 0) {
      for (final Class<?> entityClass : entityClasses) {
        if (!recaster.isCasted(entityClass)) {
          recaster.addCastedClass(entityClass);
        }
      }
    }
    return this;
  }

  public synchronized void addTransformer(RecastTransformer recastTransformer) {
    if (recastTransformer == null) {
      return;
    }
    recaster.getTransformer().addCustomTransformer(recastTransformer);
  }

  public void registerAliases(Object... params) {
    Reflections reflections = new Reflections(params);
    Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(RecasterAlias.class);
    typesAnnotatedWith.forEach(aliasRegistry::register);
  }

  public <T> T fromDocument(final Document document, final Class<T> entityClazz) {
    return recaster.fromDocument(document, entityClazz);
  }

  public Document toDocument(final Object entity) {
    return recaster.toDocument(entity);
  }
}
