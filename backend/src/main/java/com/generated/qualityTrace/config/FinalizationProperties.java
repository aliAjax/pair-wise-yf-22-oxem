package com.generated.qualityTrace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 收尾韧性演练开关（也可作为生产混沌开关）。
 *
 * finalization.fail-at-batch-index：处理到该批次序号（0 起）时模拟一次写入失败，默认 -1 关闭。
 * finalization.fail-once：是否只失败一次（默认 true），使“首次写入失败、重试从未完成批次恢复”可复现。
 * 配置经 .env / application.properties / 本类多处读取，新增配置需同步 README。
 */
@Component
@ConfigurationProperties(prefix = "finalization")
public class FinalizationProperties {
  private int failAtBatchIndex = -1;
  private boolean failOnce = true;

  public int getFailAtBatchIndex() { return failAtBatchIndex; }
  public void setFailAtBatchIndex(int failAtBatchIndex) { this.failAtBatchIndex = failAtBatchIndex; }
  public boolean isFailOnce() { return failOnce; }
  public void setFailOnce(boolean failOnce) { this.failOnce = failOnce; }
}
