package com.generated.qualityTrace.controllers;

import com.generated.qualityTrace.models.BatchFinalization;
import com.generated.qualityTrace.models.FinalizationRequest;
import com.generated.qualityTrace.services.WorkOrderFinalizationService;
import com.generated.qualityTrace.types.FinalizationResultView;
import com.generated.qualityTrace.types.FinalizationSubmitPayload;
import com.generated.qualityTrace.validators.FinalizationSubmitValidator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 完工收尾接口：
 * - POST /api/work-orders/finalizations        提交工单号，汇总末检结论与未关闭不良
 * - GET  /api/work-orders/finalizations/{id}   取回某次收尾结论（重复请求取回首次结论）
 */
@RestController
@RequestMapping("/api/work-orders/finalizations")
public class WorkOrderFinalizationController {

  private final WorkOrderFinalizationService service;

  public WorkOrderFinalizationController(WorkOrderFinalizationService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  public FinalizationResultView submit(@RequestBody FinalizationSubmitPayload payload) {
    FinalizationSubmitValidator.validate(payload);
    return service.submit(payload.orderNo(), payload.operatorId(), payload.requestId());
  }

  @GetMapping("/{requestId}")
  public Map<String, Object> get(@PathVariable String requestId) {
    FinalizationRequest request = service.findRequest(requestId).orElse(null);
    if (request == null) {
      return Map.of("found", false, "requestId", requestId);
    }
    List<BatchFinalization> batches = service.batchResults(requestId);
    return Map.of(
        "found", true,
        "request", com.generated.qualityTrace.constructors.FinalizationDtoFactory.requestView(request),
        "batches", batches.stream()
            .map(com.generated.qualityTrace.constructors.FinalizationDtoFactory::batchView)
            .toList());
  }
}
