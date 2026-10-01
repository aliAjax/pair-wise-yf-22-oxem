package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.services.WorkOrderService;

@RestController
@RequestMapping("/api/work-order")
public class WorkOrderController {

  private final WorkOrderService service;

  public WorkOrderController(WorkOrderService service) {
    this.service = service;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list();
  }
}
