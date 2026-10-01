package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constants.DefectSeverity;
import com.generated.qualityTrace.constants.DispositionStatus;
import com.generated.qualityTrace.constants.ItemResultStatus;
import com.generated.qualityTrace.constants.WorkOrderStatus;
import com.generated.qualityTrace.models.AuditLog;
import com.generated.qualityTrace.models.BatchFinalization;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.models.FinalizationRequest;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.models.WorkOrder;
import com.generated.qualityTrace.repositories.AuditLogRepository;
import com.generated.qualityTrace.repositories.BatchFinalizationRepository;
import com.generated.qualityTrace.repositories.DefectRecordRepository;
import com.generated.qualityTrace.repositories.FinalizationRequestRepository;
import com.generated.qualityTrace.repositories.InspectionItemResultRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.repositories.QualityInspectionRepository;
import com.generated.qualityTrace.repositories.WorkOrderRepository;
import com.generated.qualityTrace.types.FinalizationResultView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 完工收尾验收测试（纯手工装配，避免数据源自动配置）：
 * 1. 有问题留在暂停并逐批说明；全通过才完工
 * 2. 末检/不良变化后旧结论失效，重新提交按最新记录重算
 * 3. 并发重复提交只产生一份结果，重复请求取回首次结论
 * 4. 写入失败从未完成批次恢复，重试不再写审计
 * 5. 旧数据升级：缺检验项的批次按未完成，不能当合格
 */
class WorkOrderFinalizationServiceTest {

  private WorkOrderRepository workOrderRepository;
  private ProductBatchRepository batchRepository;
  private QualityInspectionRepository inspectionRepository;
  private InspectionItemResultRepository itemRepository;
  private DefectRecordRepository defectRepository;
  private FinalizationRequestRepository requestRepository;
  private BatchFinalizationRepository batchFinalizationRepository;
  private AuditLogRepository auditLogRepository;
  private AuditLogService auditLogService;
  private BatchDataVersionService dataVersionService;
  private FinalizationInvalidationService invalidationService;
  private FinalizationFailureGate failureGate;
  private WorkOrderFinalizationService finalizationService;

  @BeforeEach
  void setUp() {
    workOrderRepository = new WorkOrderRepository();
    batchRepository = new ProductBatchRepository();
    inspectionRepository = new QualityInspectionRepository();
    itemRepository = new InspectionItemResultRepository();
    defectRepository = new DefectRecordRepository();
    requestRepository = new FinalizationRequestRepository();
    batchFinalizationRepository = new BatchFinalizationRepository();
    auditLogRepository = new AuditLogRepository();
    auditLogService = new AuditLogService(auditLogRepository);
    dataVersionService = new BatchDataVersionService(
        batchRepository, inspectionRepository, itemRepository, defectRepository);
    invalidationService = new FinalizationInvalidationService(
        batchRepository, requestRepository, workOrderRepository,
        dataVersionService, auditLogService);
    failureGate = new FinalizationFailureGate(new com.generated.qualityTrace.config.FinalizationProperties());
    finalizationService = new WorkOrderFinalizationService(
        workOrderRepository, requestRepository, batchFinalizationRepository,
        dataVersionService, new BatchFinalizationEvaluator(), auditLogService, failureGate);
  }

  private long auditCount() {
    return auditLogRepository.findAll().size();
  }

  private long auditCount(String actionPrefix) {
    return auditLogRepository.findAll().stream()
        .filter(a -> a.getAction().startsWith(actionPrefix))
        .count();
  }

  @Test
  @DisplayName("1. 末检不合格或严重不良未关闭 -> 留在 PAUSED 并逐批说明")
  void blockingBatchesKeepOrderPausedWithPerBatchReasons() {
    // 工单1：批次1 末检PASS(严重不良已关闭)、批次2 末检FAIL且有未关闭CRITICAL
    FinalizationResultView view =
        finalizationService.submit("WO-20260920-01", "supervisor-1", "req-wo1-first");

    assertEquals("PAUSED", view.decision());
    assertFalse(view.stale());
    assertEquals(1, view.blockerCount(), "阻断批次为 1 个（批次2，末检FAIL且严重不良未关闭）");
    assertEquals(WorkOrderStatus.PAUSED.name(),
        workOrderRepository.findByOrderNo("WO-20260920-01").orElseThrow().getStatus().name());

    var byNo = new java.util.HashMap<String, java.util.Map<String, Object>>();
    view.batches().forEach(b -> byNo.put((String) b.get("batchNo"), b));
    assertEquals("PASS", byNo.get("B-0920-01-A").get("conclusion"));
    assertEquals("FINAL_FAIL", byNo.get("B-0920-01-B").get("conclusion"));
    assertEquals(1, byNo.get("B-0920-01-B").get("openCriticalDefects"));
    String explanation = (String) byNo.get("B-0920-01-B").get("explanation");
    assertTrue(explanation.contains("B-0920-01-B"));
    assertTrue(explanation.contains("不合格"));
    assertTrue(explanation.contains("严重不良"), "末检问题与严重不良并存时逐批说明要都写出");
  }

  @Test
  @DisplayName("1b. 全部满足条件才 FINISHED")
  void allClearFinishesOrder() {
    // 关闭批次2的严重不良，并把批次2末检改为合格的新记录
    defectRepository.findEntities().stream()
        .filter(d -> d.getBatchId() == 2L)
        .findFirst()
        .ifPresent(d -> {
          d.setDispositionStatus(DispositionStatus.CLOSED);
          defectRepository.save(d);
        });
    QualityInspection newFinal = new QualityInspection();
    newFinal.setBatchId(2L);
    newFinal.setInspectorId("u-qc-01");
    newFinal.setInspectionType(com.generated.qualityTrace.constants.InspectionType.FINAL);
    newFinal.setStandardVersion("STD-2026-A");
    newFinal.setResultStatus(com.generated.qualityTrace.constants.InspectionResultStatus.PASS);
    newFinal.setInspectedAt("2026-09-22T09:00:00");
    newFinal.setRequiredItemCount(3);
    inspectionRepository.save(newFinal);
    itemRepository.save(new InspectionItemResult(null, newFinal.getId(), "DIM-A", "总长",
        "100.00", 99.90, 100.10, ItemResultStatus.OK));
    itemRepository.save(new InspectionItemResult(null, newFinal.getId(), "DIM-B", "外径",
        "25.00", 24.95, 25.05, ItemResultStatus.OK));
    itemRepository.save(new InspectionItemResult(null, newFinal.getId(), "SURF", "粗糙度",
        "Ra1.4", null, null, ItemResultStatus.OK));

    FinalizationResultView view =
        finalizationService.submit("WO-20260920-01", "supervisor-1", "req-wo1-clear");
    assertEquals("FINISHED", view.decision());
    assertEquals(0, view.blockerCount());
    view.batches().forEach(b -> assertEquals("PASS", b.get("conclusion"),
        () -> "batch " + b.get("batchNo") + " => " + b.get("conclusion")));
    assertEquals(WorkOrderStatus.FINISHED,
        workOrderRepository.findByOrderNo("WO-20260920-01").orElseThrow().getStatus());
  }

  @Test
  @DisplayName("2. 末检/不良一变旧结论失效，重新提交按最新记录重算")
  void sourceChangeInvalidatesConclusionAndRecompute() {
    // 先把工单1收尾 -> PAUSED
    FinalizationResultView first =
        finalizationService.submit("WO-20260920-01", "s1", "req-invalidate");
    assertEquals("PAUSED", first.decision());

    // 关闭严重不良 + 批次2末检重检合格（带齐检验项）
    defectRepository.findEntities().stream()
        .filter(d -> d.getBatchId() == 2L).findFirst()
        .ifPresent(d -> { d.setDispositionStatus(DispositionStatus.CLOSED);
          defectRepository.save(d); });
    QualityInspection recheck = new QualityInspection();
    recheck.setBatchId(2L);
    recheck.setInspectionType(com.generated.qualityTrace.constants.InspectionType.FINAL);
    recheck.setResultStatus(com.generated.qualityTrace.constants.InspectionResultStatus.PASS);
    recheck.setInspectedAt("2026-09-23T10:00:00");
    recheck.setRequiredItemCount(1);
    inspectionRepository.save(recheck);
    itemRepository.save(new InspectionItemResult(null, recheck.getId(), "DIM-A", "总长",
        "100.01", 99.90, 100.10, ItemResultStatus.OK));

    // 失效联动：旧请求置 SUPERSEDED（通过真实写服务触发一次）
    invalidationService.invalidateByBatch(2L);
    FinalizationRequest stale = requestRepository.findByRequestId("req-invalidate").orElseThrow();
    assertEquals(com.generated.qualityTrace.constants.FinalizationStatus.SUPERSEDED,
        stale.getStatus());

    // 重新提交 -> 新的一份结果，按最新数据：FINISHED
    FinalizationResultView second =
        finalizationService.submit("WO-20260920-01", "s1", "req-recompute");
    assertEquals("FINISHED", second.decision());
    assertNotEquals(first.dataVersion(), second.dataVersion());
  }

  @Test
  @DisplayName("3. 并发重复提交只产生一份结果，重复请求取回首次结论 (duplicate=true)")
  void concurrentDuplicateSubmissionsReturnFirstConclusion() throws Exception {
    int n = 8;
    ExecutorService pool = Executors.newFixedThreadPool(n);
    Callable<FinalizationResultView> task =
        () -> finalizationService.submit("WO-20260920-02", "sup-x", null);
    List<Future<FinalizationResultView>> futures = new java.util.ArrayList<>();
    for (int i = 0; i < n; i++) {
      futures.add(pool.submit(task));
    }
    List<FinalizationResultView> views = new java.util.ArrayList<>();
    for (Future<FinalizationResultView> f : futures) {
      views.add(f.get(10, TimeUnit.SECONDS));
    }
    pool.shutdown();

    long distinct = views.stream().map(v -> v.requestId() == null ? null : v.requestId())
        .distinct().count();
    // 可能第一份之后其余全部命中首次结论：结论内容必须一致
    assertEquals(views.get(0).decision(), views.get(n - 1).decision());
    assertEquals("PAUSED", views.get(0).decision());
    long firstCount = views.stream().filter(v -> !v.duplicate()).count();
    long dupCount = views.stream().filter(FinalizationResultView::duplicate).count();
    assertEquals(1, firstCount, "只有一份首次结果");
    assertEquals(n - 1, dupCount, "其余全部取回首次结论");
    assertEquals(1, requestRepository.findByWorkOrderId(2L).stream()
        .filter(r -> r.getStatus() != com.generated.qualityTrace.constants.FinalizationStatus.SUPERSEDED)
        .count(), "同工单只生成一份请求");
    assertTrue(distinct >= 1);
  }

  @Test
  @DisplayName("3b. 首请求写入失败时，并发重复请求会等待并取回恢复后的首次结论")
  void concurrentRequestAutoResumesFailedFirst() throws Exception {
    String orderNo = "WO-20260920-01";
    failureGate.armOnce(orderNo, 1);
    ExecutorService pool = Executors.newFixedThreadPool(2);
    java.util.concurrent.CountDownLatch ready = new java.util.concurrent.CountDownLatch(2);
    java.util.concurrent.CountDownLatch go = new java.util.concurrent.CountDownLatch(1);
    Callable<Object> task = () -> {
      ready.countDown();
      go.await(5, TimeUnit.SECONDS);
      try {
        return finalizationService.submit(orderNo, "s1", "req-concurrent-resume");
      } catch (FinalizationWriteException e) {
        return e;
      }
    };
    Future<Object> f1 = pool.submit(task);
    Future<Object> f2 = pool.submit(task);
    ready.await(5, TimeUnit.SECONDS);
    go.countDown();
    Object r1 = f1.get(10, TimeUnit.SECONDS);
    Object r2 = f2.get(10, TimeUnit.SECONDS);
    pool.shutdown();

    // 恰有一个请求承担首次写入并撞上注入失败，另一个串行进入后自动恢复，最终只有一份成功结论
    long failures = java.util.stream.Stream.of(r1, r2)
        .filter(FinalizationWriteException.class::isInstance).count();
    long views = java.util.stream.Stream.of(r1, r2)
        .filter(FinalizationResultView.class::isInstance).count();
    assertEquals(1, failures);
    assertEquals(1, views);
    FinalizationResultView view = (FinalizationResultView)
        java.util.stream.Stream.of(r1, r2).filter(FinalizationResultView.class::isInstance)
            .findFirst().orElseThrow();
    assertEquals("PAUSED", view.decision());
    assertEquals(2, batchFinalizationRepository
        .findByRequestId("req-concurrent-resume").size(), "恢复后两批检查点齐全");
    assertEquals(1, auditCount("finalization submit"), "并发恢复也不重复写提交审计");
  }

  @Test
  @DisplayName("4. 写入失败 -> FAILED+检查点；同 requestId 恢复，且不重复写审计")
  void writeFailureResumesFromCheckpointWithoutDuplicateAudit() {
    String orderNo = "WO-20260920-01"; // 两个批次，index=1 前失败可验证检查点恢复
    failureGate.reset(orderNo);
    failureGate.armOnce(orderNo, 1);

    assertThrows(FinalizationWriteException.class,
        () -> finalizationService.submit(orderNo, "s1", "req-resume"));

    FinalizationRequest failed =
        requestRepository.findByRequestId("req-resume").orElseThrow();
    assertEquals(com.generated.qualityTrace.constants.FinalizationStatus.FAILED,
        failed.getStatus());
    assertEquals(1, failed.getCompletedBatchCount(), "批次0 检查点已落");
    assertEquals(1, batchFinalizationRepository.findByRequestId("req-resume").size());
    assertTrue(batchFinalizationRepository.exists("req-resume", 1L), "批次0(B-...-A)已完成");
    assertFalse(batchFinalizationRepository.exists("req-resume", 2L), "批次1未完成");

    long submitAuditBefore = auditCount("finalization submit");
    long batchAuditBefore = auditLogRepository.findAll().stream()
        .filter(a -> a.getAction().startsWith("finalization batch")).count();

    // 故障为一次性，重试 -> resumed=true，从批次1继续
    FinalizationResultView resumed =
        finalizationService.submit(orderNo, "s1", "req-resume");
    assertTrue(resumed.resumed());
    assertEquals("PAUSED", resumed.decision());
    assertTrue(batchFinalizationRepository.exists("req-resume", 2L), "批次1恢复后完成");
    assertEquals(2, batchFinalizationRepository.findByRequestId("req-resume").size());

    // 审计不重复：submit 全程只有 1 条；批次审计从 1 条补到 2 条（批次0不重写）
    assertEquals(1, auditCount("finalization submit"), "提交审计不重复");
    long batchAuditAfter = auditLogRepository.findAll().stream()
        .filter(a -> a.getAction().startsWith("finalization batch")).count();
    assertEquals(batchAuditBefore + 1, batchAuditAfter, "只为新完成的批次补审计");
    assertEquals(submitAuditBefore, auditCount("finalization submit"));
  }

  @Test
  @DisplayName("5. 旧数据升级：缺检验项的批次即使末检标 PASS 也按未完成，不能当合格")
  void legacyBatchesMissingItemsAreIncompleteNotPass() {
    // 工单3 批次4：末检 PASS 但没有检验项行
    FinalizationResultView view =
        finalizationService.submit("WO-20260921-03", "s1", "req-legacy");
    assertEquals("PAUSED", view.decision());
    var batch4 = view.batches().get(0);
    assertEquals("ITEMS_INCOMPLETE", batch4.get("conclusion"));
    assertEquals(true, batch4.get("blocking"));
    assertEquals(true, batch4.get("legacyMissingStandard"), "旧数据缺标准标记");
    assertTrue(((String) batch4.get("explanation")).contains("按未完成处理"));
    assertEquals(WorkOrderStatus.PAUSED,
        workOrderRepository.findByOrderNo("WO-20260921-03").orElseThrow().getStatus());
  }

  @Test
  @DisplayName("5b. 补录缺失检验项后版本变化，重新提交可放行")
  void backfillItemsThenRecomputePasses() {
    finalizationService.submit("WO-20260921-03", "s1", "req-legacy-2");

    // 旧数据迁移：把要求项数补为 1，并录入 1 个合格项
    QualityInspection fin = inspectionRepository.findLatestFinalByBatchId(4L).orElseThrow();
    fin.setRequiredItemCount(1);
    inspectionRepository.save(fin);
    itemRepository.save(new InspectionItemResult(null, fin.getId(), "THICK", "厚度",
        "8.00", 7.95, 8.05, ItemResultStatus.OK));
    invalidationService.invalidateByBatch(4L);

    FinalizationResultView again =
        finalizationService.submit("WO-20260921-03", "s1", "req-legacy-fixed");
    assertEquals("FINISHED", again.decision());
    assertEquals("PASS", again.batches().get(0).get("conclusion"));
  }

  @Test
  @DisplayName("2b. 只有 MINOR 未关闭不阻断，但给出提示")
  void openMinorDefectsDoNotBlock() {
    // 批次1 单独看：末检PASS，1 条 MAJOR 已关闭 + 1 条 MINOR 处理中
    BatchFinalizationEvaluator evaluator = new BatchFinalizationEvaluator();
    var batch = batchRepository.findById(1L).orElseThrow();
    var fin = inspectionRepository.findLatestFinalByBatchId(1L).orElseThrow();
    BatchFinalization bf = evaluator.evaluate(batch, fin,
        itemRepository.findByInspectionId(fin.getId()),
        defectRepository.findByBatchId(1L), 0, "r");
    assertEquals(com.generated.qualityTrace.constants.BatchFinalStatus.PASS, bf.getConclusion());
    assertEquals(1, bf.getOpenMinorDefects());
    assertTrue(bf.getExplanation().contains("一般不良"));
  }

  @Test
  @DisplayName("审计幂等键：重复写同一动作只落一条")
  void auditIdempotentOnRepeatedCall() {
    auditLogService.recordFinalizationSubmit("dup-req", "WO-X", "s1", "v1");
    auditLogService.recordFinalizationSubmit("dup-req", "WO-X", "s1", "v1");
    assertEquals(1, auditLogRepository.findAll().stream()
        .filter(a -> "fin-submit:dup-req".equals(a.getIdempotencyKey())).count());
  }

  @Test
  @DisplayName("缺陷严重等级判定：MAJOR/CRITICAL 才算严重")
  void severityClassification() {
    DefectRecord major = new DefectRecord(null, 1L, "x", 1, DefectSeverity.MAJOR, null, DispositionStatus.OPEN);
    DefectRecord minor = new DefectRecord(null, 1L, "x", 1, DefectSeverity.MINOR, null, DispositionStatus.OPEN);
    DefectRecord closedCritical = new DefectRecord(null, 1L, "x", 1,
        DefectSeverity.CRITICAL, null, DispositionStatus.CLOSED);
    assertTrue(major.isCritical());
    assertFalse(minor.isCritical());
    assertTrue(minor.isOpen());
    assertFalse(closedCritical.isOpen());
  }
}
