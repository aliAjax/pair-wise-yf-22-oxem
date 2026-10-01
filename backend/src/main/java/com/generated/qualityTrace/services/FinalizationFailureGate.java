package com.generated.qualityTrace.services;

import com.generated.qualityTrace.config.FinalizationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 写入失败注入门：在逐批检查点写入前按配置抛出，模拟“写入失败”。
 * failOnce=true 时同一工单只失败一次，使“首次写入失败、重试从未完成批次恢复”可复现。
 */
@Component
public class FinalizationFailureGate {

  private final FinalizationProperties properties;
  private final ConcurrentHashMap<String, Boolean> triggered = new ConcurrentHashMap<>();

  // 测试/演练用一次性武装状态
  private final AtomicBoolean armed = new AtomicBoolean(false);
  private final AtomicInteger armedIndex = new AtomicInteger(-1);
  private volatile String armedOrder;

  public FinalizationFailureGate(FinalizationProperties properties) {
    this.properties = properties;
  }

  /** 测试/演练：在指定工单、指定批次序号写入前失败一次。 */
  public void armOnce(String orderNo, int batchIndex) {
    this.armedOrder = orderNo;
    this.armedIndex.set(batchIndex);
    this.armed.set(true);
  }

  public void reset(String orderNo) {
    if (orderNo == null || orderNo.equals(armedOrder)) {
      armed.set(false);
    }
    if (orderNo != null) {
      triggered.remove(orderNo);
    }
  }

  /** 在写入前调用；命中即抛 {@link FinalizationWriteException}。 */
  public void checkpointBeforeWrite(String orderNo, int batchIndex) {
    boolean armedHit = armed.get()
        && orderNo.equals(armedOrder)
        && batchIndex == armedIndex.get();
    if (armedHit) {
      armed.compareAndSet(true, false);
      throw new FinalizationWriteException(
          "injected(armed) write failure at batchIndex=" + batchIndex + ", orderNo=" + orderNo);
    }

    int configIndex = properties.getFailAtBatchIndex();
    if (configIndex < 0 || batchIndex != configIndex) {
      return;
    }
    if (properties.isFailOnce() && triggered.putIfAbsent(orderNo, Boolean.TRUE) != null) {
      return;
    }
    throw new FinalizationWriteException(
        "injected(config) write failure at batchIndex=" + batchIndex + ", orderNo=" + orderNo);
  }
}
