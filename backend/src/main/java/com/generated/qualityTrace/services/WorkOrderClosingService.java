package com.generated.qualityTrace.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.generated.qualityTrace.constants.ClosingVerdict;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.FinalConclusion;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.constants.WorkOrderStatus;
import com.generated.qualityTrace.constructors.WorkOrderClosingDtoFactory;
import com.generated.qualityTrace.exceptions.ClosingException;
import com.generated.qualityTrace.middlewares.AuditLogMiddleware;
import com.generated.qualityTrace.models.WorkOrderClosing;
import com.generated.qualityTrace.repositories.DefectRecordRepository;
import com.generated.qualityTrace.repositories.InspectionItemResultRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.repositories.QualityInspectionRepository;
import com.generated.qualityTrace.repositories.WorkOrderClosingRepository;
import com.generated.qualityTrace.repositories.WorkOrderRepository;
import com.generated.qualityTrace.validators.WorkOrderClosingValidator;

/**
 * 工单完工收尾服务。
 *
 * 提交工单号后汇总各批次末检结论与未关闭不良：有问题则工单暂停并逐批说明，
 * 全部合格才放行完工。末检或不良一变，旧结论即失效，重新提交按最新记录重算。
 * 同一工单的并发提交合并为一次计算，重复请求取回首次结论。
 * 写入失败时保留 PENDING 进度，重试从未完成批次恢复，审计不重复写入。
 */
@Service
public class WorkOrderClosingService {

    private final WorkOrderClosingRepository closingRepo;
    private final WorkOrderRepository workOrderRepo;
    private final ProductBatchRepository batchRepo;
    private final QualityInspectionRepository inspectionRepo;
    private final InspectionItemResultRepository itemRepo;
    private final DefectRecordRepository defectRepo;
    private final AuditLogMiddleware auditLog;
    private final WorkOrderClosingValidator validator;

    /** 同一工单的并发收尾合并为一次计算。 */
    private final ConcurrentHashMap<String, CompletableFuture<WorkOrderClosing>> inFlight = new ConcurrentHashMap<>();
    private final ExecutorService closingExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "closing-compute");
        t.setDaemon(true);
        return t;
    });

    /** 配置开启后，首次收尾写入在处理完首个批次后模拟失败，用于演示恢复。 */
    @Value("${app.closing.fail-on-first-write:false}")
    private boolean failOnFirstWrite;
    private final AtomicBoolean failureArmed = new AtomicBoolean(false);

    public WorkOrderClosingService(WorkOrderClosingRepository closingRepo,
                                   WorkOrderRepository workOrderRepo,
                                   ProductBatchRepository batchRepo,
                                   QualityInspectionRepository inspectionRepo,
                                   InspectionItemResultRepository itemRepo,
                                   DefectRecordRepository defectRepo,
                                   AuditLogMiddleware auditLog,
                                   WorkOrderClosingValidator validator) {
        this.closingRepo = closingRepo;
        this.workOrderRepo = workOrderRepo;
        this.batchRepo = batchRepo;
        this.inspectionRepo = inspectionRepo;
        this.itemRepo = itemRepo;
        this.defectRepo = defectRepo;
        this.auditLog = auditLog;
        this.validator = validator;
    }

    @PostConstruct
    public void init() {
        failureArmed.set(failOnFirstWrite);
    }

    /**
     * 提交工单号收尾。
     * 已存在有效且指纹一致的结论时直接返回（重复/并发请求取回首次结论）；
     * 否则合并并发请求后按最新记录重算。
     */
    public WorkOrderClosing submit(String workOrderNo) {
        String orderNo = validator.validateWorkOrderNo(workOrderNo);
        ensureWorkOrderExists(orderNo);

        String fp = fingerprint(orderNo);
        Optional<WorkOrderClosing> stored = closingRepo.findByWorkOrderNo(orderNo);
        if (stored.isPresent() && stored.get().valid && fp.equals(stored.get().fingerprint)) {
            return stored.get();
        }

        CompletableFuture<WorkOrderClosing> fut = inFlight.computeIfAbsent(orderNo, k -> {
            CompletableFuture<WorkOrderClosing> f = new CompletableFuture<>();
            closingExecutor.execute(() -> {
                try {
                    WorkOrderClosing c = computeAndStore(orderNo, fp);
                    f.complete(c);
                } catch (Throwable t) {
                    f.completeExceptionally(t);
                } finally {
                    inFlight.remove(orderNo);
                }
            });
            return f;
        });

        try {
            return fut.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new ClosingException(ErrorCodes.CLOSING_WRITE_FAILED, ErrorMessages.CLOSING_WRITE_FAILED);
        }
    }

    /** 查询当前收尾结论（无则空）。 */
    public Optional<WorkOrderClosing> getCurrent(String workOrderNo) {
        String orderNo = validator.validateWorkOrderNo(workOrderNo);
        return closingRepo.findByWorkOrderNo(orderNo);
    }

    /**
     * 恢复待完成的收尾。
     * 加载 PENDING 结论，仅重算尚未完成的批次并合并，审计不重复写入。
     */
    public WorkOrderClosing recover(String workOrderNo) {
        String orderNo = validator.validateWorkOrderNo(workOrderNo);
        ensureWorkOrderExists(orderNo);

        WorkOrderClosing pending = closingRepo.findByWorkOrderNo(orderNo)
                .orElseThrow(() -> new ClosingException(ErrorCodes.CLOSING_RECOVERY_NOT_FOUND,
                        ErrorMessages.CLOSING_RECOVERY_NOT_FOUND));
        if ("COMPLETE".equals(pending.status)) {
            throw new ClosingException(ErrorCodes.CLOSING_ALREADY_COMPLETE,
                    ErrorMessages.CLOSING_ALREADY_COMPLETE);
        }

        String fp = fingerprint(orderNo);
        List<Map<String, Object>> allBatches = batchRepo.findByWorkOrderNo(orderNo);
        Set<String> doneBatchNos = pending.batchSummaries.stream()
                .map(s -> (String) s.get("batchNo"))
                .collect(Collectors.toSet());

        List<Map<String, Object>> summaries = new ArrayList<>(pending.batchSummaries);
        int qualified = pending.qualifiedBatchCount;
        boolean incomplete = ClosingVerdict.INCOMPLETE.name().equals(pending.verdict);
        boolean unqualified = ClosingVerdict.UNQUALIFIED.name().equals(pending.verdict);

        for (Map<String, Object> batch : allBatches) {
            String batchNo = (String) batch.get("batchNo");
            if (doneBatchNos.contains(batchNo)) {
                continue;
            }
            Map<String, Object> summary = evaluateBatch(batch);
            summaries.add(summary);
            String conclusion = (String) summary.get("finalConclusion");
            if (FinalConclusion.INCOMPLETE.name().equals(conclusion)) {
                incomplete = true;
            } else if (!FinalConclusion.PASS.name().equals(conclusion)) {
                unqualified = true;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> defects = (List<Map<String, Object>>) summary.get("unclosedDefects");
            if (defects != null && !defects.isEmpty()) {
                unqualified = true;
            }
            if (Boolean.TRUE.equals(summary.get("qualified"))) {
                qualified++;
            }
        }

        String verdict = resolveVerdict(incomplete, unqualified);
        pending.verdict = verdict;
        pending.paused = !ClosingVerdict.QUALIFIED.name().equals(verdict);
        pending.batchCount = allBatches.size();
        pending.qualifiedBatchCount = qualified;
        pending.batchSummaries = summaries;
        pending.fingerprint = fp;
        pending.valid = true;
        pending.status = "COMPLETE";
        pending.updatedAt = now();
        // 审计不重写：pending.auditWritten 已为 true
        closingRepo.save(pending);

        applyWorkOrderStatus(orderNo, pending);
        auditLog.write(LogTemplates.CLOSING_RECOVER, "WorkOrderClosing", orderNo,
                "收尾恢复完成，结论 " + verdict);
        return pending;
    }

    /** 使当前收尾结论失效（末检/不良变化后调用）。 */
    public void invalidate(String workOrderNo) {
        String orderNo = validator.validateWorkOrderNo(workOrderNo);
        closingRepo.invalidateByWorkOrderNo(orderNo);
        auditLog.write(LogTemplates.CLOSING_INVALIDATE, "WorkOrderClosing", orderNo, "收尾结论已失效");
    }

    /** 关闭不良并使收尾结论失效（不良一变，旧结论失效）。 */
    public WorkOrderClosing closeDefectAndInvalidate(String workOrderNo, String defectNo) {
        String orderNo = validator.validateWorkOrderNo(workOrderNo);
        ensureWorkOrderExists(orderNo);
        Map<String, Object> defect = defectRepo.findByDefectNo(defectNo)
                .orElseThrow(() -> new ClosingException(ErrorCodes.CLOSING_BATCHES_NOT_FOUND,
                        "不良记录不存在：" + defectNo));
        String batchNo = (String) defect.get("batchNo");
        Map<String, Object> batch = batchRepo.findByBatchNo(batchNo)
                .orElseThrow(() -> new ClosingException(ErrorCodes.CLOSING_BATCHES_NOT_FOUND,
                        "批次不存在：" + batchNo));
        if (!orderNo.equals(batch.get("workOrderNo"))) {
            throw new ClosingException(ErrorCodes.CLOSING_BATCHES_NOT_FOUND,
                    "不良 " + defectNo + " 不属于工单 " + orderNo);
        }
        defectRepo.closeDefect(defectNo);
        closingRepo.invalidateByWorkOrderNo(orderNo);
        auditLog.write(LogTemplates.CLOSING_INVALIDATE, "WorkOrderClosing", orderNo,
                "不良 " + defectNo + " 已关闭，收尾结论失效");
        return closingRepo.findByWorkOrderNo(orderNo).orElse(null);
    }

    // ------------------------------------------------------------------
    // 内部实现
    // ------------------------------------------------------------------

    private WorkOrderClosing computeAndStore(String workOrderNo, String fp) {
        Optional<WorkOrderClosing> stored = closingRepo.findByWorkOrderNo(workOrderNo);
        if (stored.isPresent() && stored.get().valid && fp.equals(stored.get().fingerprint)) {
            return stored.get();
        }

        List<Map<String, Object>> batches = batchRepo.findByWorkOrderNo(workOrderNo);
        if (batches.isEmpty()) {
            throw new ClosingException(ErrorCodes.CLOSING_BATCHES_NOT_FOUND,
                    ErrorMessages.CLOSING_BATCHES_NOT_FOUND);
        }

        List<Map<String, Object>> summaries = new ArrayList<>();
        int qualified = 0;
        boolean incomplete = false;
        boolean unqualified = false;

        for (int i = 0; i < batches.size(); i++) {
            Map<String, Object> summary = evaluateBatch(batches.get(i));
            summaries.add(summary);
            String conclusion = (String) summary.get("finalConclusion");
            if (FinalConclusion.INCOMPLETE.name().equals(conclusion)) {
                incomplete = true;
            } else if (!FinalConclusion.PASS.name().equals(conclusion)) {
                unqualified = true;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> defects = (List<Map<String, Object>>) summary.get("unclosedDefects");
            if (defects != null && !defects.isEmpty()) {
                unqualified = true;
            }
            if (Boolean.TRUE.equals(summary.get("qualified"))) {
                qualified++;
            }

            // 模拟写入失败：首个批次处理完即中断，保留 PENDING 进度
            if (failureArmed.get() && i == 0) {
                String provisional = resolveVerdict(incomplete, unqualified);
                WorkOrderClosing partial = buildClosing(workOrderNo, fp, summaries, qualified,
                        provisional, "PENDING");
                partial.auditWritten = true;
                closingRepo.save(partial);
                auditLog.write(LogTemplates.CLOSING_AUDIT, "WorkOrderClosing", workOrderNo,
                        "收尾写入中断，已完成 1/" + batches.size() + " 个批次");
                failureArmed.set(false);
                throw new ClosingException(ErrorCodes.CLOSING_WRITE_FAILED,
                        ErrorMessages.CLOSING_WRITE_FAILED);
            }
        }

        String verdict = resolveVerdict(incomplete, unqualified);
        WorkOrderClosing closing = buildClosing(workOrderNo, fp, summaries, qualified, verdict, "COMPLETE");
        closingRepo.save(closing);

        // 审计只写一次（幂等），重试不重复写
        if (!closing.auditWritten) {
            auditLog.write(LogTemplates.CLOSING_AUDIT, "WorkOrderClosing", workOrderNo,
                    "收尾结论 " + verdict + "，合格批次 " + qualified + "/" + batches.size());
            closing.auditWritten = true;
            closingRepo.save(closing);
        }

        applyWorkOrderStatus(workOrderNo, closing);
        return closing;
    }

    /** 逐批评估末检结论与未关闭不良。 */
    private Map<String, Object> evaluateBatch(Map<String, Object> batch) {
        String batchNo = (String) batch.get("batchNo");
        List<String> reasons = new ArrayList<>();

        Optional<Map<String, Object>> inspOpt = inspectionRepo.findFinalByBatchNo(batchNo);
        String finalConclusion;
        if (!inspOpt.isPresent()) {
            finalConclusion = FinalConclusion.INCOMPLETE.name();
            reasons.add(String.format(ErrorMessages.CLOSING_BATCH_NO_FINAL, batchNo));
        } else {
            Map<String, Object> insp = inspOpt.get();
            String inspectionNo = (String) insp.get("inspectionNo");
            String resultStatus = (String) insp.get("resultStatus");
            List<Map<String, Object>> items = itemRepo.findByInspectionNo(inspectionNo);
            if (items.isEmpty()) {
                // 旧数据升级：末检缺检验项，按未完成处理，不能当合格
                finalConclusion = FinalConclusion.INCOMPLETE.name();
                reasons.add(String.format(ErrorMessages.CLOSING_BATCH_FINAL_NO_ITEMS, batchNo));
            } else {
                boolean anyFail = items.stream()
                        .anyMatch(it -> "FAIL".equals(it.get("itemStatus")));
                if ("FAIL".equals(resultStatus) || anyFail) {
                    finalConclusion = FinalConclusion.FAIL.name();
                    String failNames = items.stream()
                            .filter(it -> "FAIL".equals(it.get("itemStatus")))
                            .map(it -> (String) it.get("itemName"))
                            .collect(Collectors.joining(","));
                    reasons.add(String.format(ErrorMessages.CLOSING_BATCH_FINAL_FAIL, batchNo, failNames));
                } else if ("CONDITIONAL_PASS".equals(resultStatus) || "RECHECK".equals(resultStatus)) {
                    finalConclusion = FinalConclusion.RECHECK.name();
                    reasons.add(String.format(ErrorMessages.CLOSING_BATCH_FINAL_RECHECK, batchNo));
                } else {
                    finalConclusion = FinalConclusion.PASS.name();
                }
            }
        }

        List<Map<String, Object>> unclosedDefects = defectRepo.findUnclosedByBatchNo(batchNo);
        for (Map<String, Object> d : unclosedDefects) {
            reasons.add(String.format(ErrorMessages.CLOSING_BATCH_OPEN_DEFECT, batchNo,
                    d.get("defectNo"), d.get("severity"), d.get("defectType")));
        }

        boolean qualified = FinalConclusion.PASS.name().equals(finalConclusion) && unclosedDefects.isEmpty();
        return WorkOrderClosingDtoFactory.batchSummary(batchNo, finalConclusion, unclosedDefects,
                reasons, qualified);
    }

    private String resolveVerdict(boolean incomplete, boolean unqualified) {
        if (incomplete) {
            return ClosingVerdict.INCOMPLETE.name();
        }
        if (unqualified) {
            return ClosingVerdict.UNQUALIFIED.name();
        }
        return ClosingVerdict.QUALIFIED.name();
    }

    private WorkOrderClosing buildClosing(String workOrderNo, String fp, List<Map<String, Object>> summaries,
                                          int qualified, String verdict, String status) {
        WorkOrderClosing c = new WorkOrderClosing();
        c.id = "CL-" + workOrderNo;
        c.workOrderNo = workOrderNo;
        c.verdict = verdict;
        c.paused = !ClosingVerdict.QUALIFIED.name().equals(verdict);
        c.batchCount = summaries.size();
        c.qualifiedBatchCount = qualified;
        c.batchSummaries = summaries;
        c.fingerprint = fp;
        c.valid = true;
        c.auditWritten = false;
        c.status = status;
        c.createdAt = now();
        c.updatedAt = now();
        return c;
    }

    private void applyWorkOrderStatus(String workOrderNo, WorkOrderClosing closing) {
        if (closing.paused) {
            workOrderRepo.updateStatus(workOrderNo, WorkOrderStatus.PAUSED.name());
            auditLog.write(LogTemplates.CLOSING_PAUSE, "WorkOrder", workOrderNo,
                    "收尾结论 " + closing.verdict + "，工单暂停");
        } else {
            workOrderRepo.updateStatus(workOrderNo, WorkOrderStatus.FINISHED.name());
            auditLog.write(LogTemplates.CLOSING_FINISH, "WorkOrder", workOrderNo,
                    "收尾结论 " + closing.verdict + "，工单完工放行");
        }
    }

    private void ensureWorkOrderExists(String workOrderNo) {
        if (!workOrderRepo.existsByOrderNo(workOrderNo)) {
            throw new ClosingException(ErrorCodes.CLOSING_WORK_ORDER_NOT_FOUND,
                    ErrorMessages.CLOSING_WORK_ORDER_NOT_FOUND);
        }
    }

    /**
     * 计算工单最新记录指纹。末检结论、检验项、不良处置任一变化都会改变指纹，
     * 从而使旧结论失效、重新提交时按最新记录重算。
     */
    private String fingerprint(String workOrderNo) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> batch : batchRepo.findByWorkOrderNo(workOrderNo)) {
            String batchNo = (String) batch.get("batchNo");
            sb.append(batchNo).append('|');
            Optional<Map<String, Object>> insp = inspectionRepo.findFinalByBatchNo(batchNo);
            if (insp.isPresent()) {
                Map<String, Object> i = insp.get();
                sb.append(i.get("inspectionNo")).append(',').append(i.get("resultStatus")).append(';');
                for (Map<String, Object> item : itemRepo.findByInspectionNo((String) i.get("inspectionNo"))) {
                    sb.append(item.get("itemNo")).append(':').append(item.get("itemStatus")).append(',');
                }
            } else {
                sb.append("NONE;");
            }
            sb.append('|');
            for (Map<String, Object> d : defectRepo.findByBatchNo(batchNo)) {
                sb.append(d.get("defectNo")).append(':').append(d.get("dispositionStatus")).append(',');
            }
            sb.append('\n');
        }
        return sha256(sb.toString());
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            return Integer.toHexString(input.hashCode());
        }
    }

    private static String now() {
        return LocalDateTime.now().toString();
    }
}
