package io.harness.iterator;

import static io.harness.data.structure.EmptyPredicate.isNotEmpty;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface PersistentCronIterable extends PersistentIrregularIterable {
  int INVENTORY_MINIMUM = 2;

  CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));

  default boolean expandNextIterations(boolean skipMissing, long throttled, String cronExpression, List<Long> times) {
    final ZonedDateTime now = ZonedDateTime.now();

    // Take this item here, before we cleanup the list and potentially make it empty. We would like to align the items
    // based on the last previous one, instead of now, if they depend on each other.
    ZonedDateTime time =
        isNotEmpty(times) ? Instant.ofEpochMilli(times.get(times.size() - 1)).atZone(ZoneOffset.UTC) : now;

    final long epochMilli = now.toInstant().toEpochMilli();

    if (skipMissing) {
      int end = Collections.binarySearch(times, epochMilli);
      if (end < 0) {
        end = -end - 1;
      } else {
        while (end < times.size() && times.get(end) == epochMilli) {
          end++;
        }
      }
      times.subList(0, end).clear();
    }

    if (times.size() > INVENTORY_MINIMUM) {
      return false;
    }

    final Cron cron = parser.parse(cronExpression);
    ExecutionTime executionTime = ExecutionTime.forCron(cron);

    if (times.isEmpty()) {
      times.add(ZonedDateTime.now().toInstant().toEpochMilli());
    }

    while (times.size() < 10) {
      final Optional<ZonedDateTime> nextTime = executionTime.nextExecution(time);
      if (!nextTime.isPresent()) {
        break;
      }

      time = nextTime.get();
      if (skipMissing && time.isBefore(now)) {
        continue;
      }

      times.add(time.toInstant().toEpochMilli());
    }

    return true;
  }
}
