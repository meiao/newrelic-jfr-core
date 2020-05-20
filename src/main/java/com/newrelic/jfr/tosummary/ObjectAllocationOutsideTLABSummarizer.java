package com.newrelic.jfr.tosummary;

import com.newrelic.jfr.Workarounds;
import jdk.jfr.consumer.RecordedEvent;

/**
 * This class handles all TLAB allocation JFR events, and delegates them to the actual aggregators,
 * which operate on a per-thread basis
 */
public final class ObjectAllocationOutsideTLABSummarizer
    extends AbstractThreadDispatchingSummarizer {

  public static final String EVENT_NAME = "jdk.ObjectAllocationOutsideTLAB";

  @Override
  public void accept(RecordedEvent ev) {
    var threadName = Workarounds.getThreadName(ev);
    threadName.ifPresent(
        thread -> {
          if (perThread.get(thread) == null) {
            perThread.put(
                thread,
                new PerThreadObjectAllocationOutsideTLABSummarizer(
                    thread, ev.getStartTime().toEpochMilli()));
          }
          perThread.get(thread).accept(ev);
        });
  }

  @Override
  public String getEventName() {
    return EVENT_NAME;
  }
}
