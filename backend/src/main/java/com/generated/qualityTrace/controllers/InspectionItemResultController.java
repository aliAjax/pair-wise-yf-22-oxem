package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.services.InspectionItemResultService;

@RestController
@RequestMapping("/api/inspection-item-result")
public class InspectionItemResultController {

  private final InspectionItemResultService service;

  public InspectionItemResultController(InspectionItemResultService service) {
    this.service = service;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list();
  }

  /** 录入/补录检验项：末检检验项一变化，旧收尾结论失效。 */
  @PostMapping
  public Map<String, Object> record(@RequestBody InspectionItemResult item) {
    return service.record(item);
  }
}
