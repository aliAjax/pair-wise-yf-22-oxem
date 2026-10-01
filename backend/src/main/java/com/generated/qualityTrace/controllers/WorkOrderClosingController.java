package com.generated.qualityTrace.controllers;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.constructors.WorkOrderClosingDtoFactory;
import com.generated.qualityTrace.models.WorkOrderClosing;
import com.generated.qualityTrace.services.WorkOrderClosingService;

/**
 * 工单完工收尾接口。
 *
 * 提交工单号后汇总各批次末检结论与未关闭不良：有问题则工单暂停并逐批说明，
 * 全部合格才放行完工。末检/不良变化后旧结论失效，重新提交按最新记录重算。
 */
@RestController
@RequestMapping("/api/work-order")
public class WorkOrderClosingController {

    private final WorkOrderClosingService service;

    public WorkOrderClosingController(WorkOrderClosingService service) {
        this.service = service;
    }

    /** 提交工单号收尾，返回逐批末检结论与未关闭不良。 */
    @PostMapping("/{orderNo}/closing")
    public ResponseEntity<Map<String, Object>> submit(@PathVariable String orderNo) {
        WorkOrderClosing closing = service.submit(orderNo);
        return ResponseEntity.ok(WorkOrderClosingDtoFactory.toResponse(closing));
    }

    /** 查询当前收尾结论。 */
    @GetMapping("/{orderNo}/closing")
    public ResponseEntity<Map<String, Object>> getCurrent(@PathVariable String orderNo) {
        return service.getCurrent(orderNo)
                .map(c -> ResponseEntity.ok(WorkOrderClosingDtoFactory.toResponse(c)))
                .orElseGet(() -> ResponseEntity.ok(WorkOrderClosingDtoFactory.notFound(orderNo)));
    }

    /** 使当前收尾结论失效（末检/不良变化后调用）。 */
    @PostMapping("/{orderNo}/closing/invalidate")
    public ResponseEntity<Map<String, Object>> invalidate(@PathVariable String orderNo) {
        service.invalidate(orderNo);
        return service.getCurrent(orderNo)
                .map(c -> ResponseEntity.ok(WorkOrderClosingDtoFactory.toResponse(c)))
                .orElseGet(() -> ResponseEntity.ok(WorkOrderClosingDtoFactory.notFound(orderNo)));
    }

    /** 恢复待完成的收尾（写入失败后从未完成批次恢复，审计不重写）。 */
    @PostMapping("/{orderNo}/closing/recover")
    public ResponseEntity<Map<String, Object>> recover(@PathVariable String orderNo) {
        WorkOrderClosing closing = service.recover(orderNo);
        return ResponseEntity.ok(WorkOrderClosingDtoFactory.toResponse(closing));
    }

    /** 关闭不良并使收尾结论失效（不良一变，旧结论失效）。 */
    @PostMapping("/{orderNo}/defects/{defectNo}/close")
    public ResponseEntity<Map<String, Object>> closeDefect(@PathVariable String orderNo,
                                                           @PathVariable String defectNo) {
        WorkOrderClosing closing = service.closeDefectAndInvalidate(orderNo, defectNo);
        if (closing == null) {
            return ResponseEntity.ok(WorkOrderClosingDtoFactory.notFound(orderNo));
        }
        return ResponseEntity.ok(WorkOrderClosingDtoFactory.toResponse(closing));
    }
}
