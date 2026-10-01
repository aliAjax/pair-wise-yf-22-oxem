package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.services.QualityInspectionService;
import com.generated.qualityTrace.validators.QualityInspectionValidator;

@RestController
@RequestMapping("/api/quality-inspection")
public class QualityInspectionController {

  private final QualityInspectionService service;

  public QualityInspectionController(QualityInspectionService service) {
    this.service = service;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list();
  }

  /** 提交末检：旧收尾结论随之失效。 */
  @PostMapping("/final")
  public Map<String, Object> submitFinal(@RequestBody QualityInspection inspection) {
    QualityInspectionValidator.validateFinal(inspection);
    return service.submitFinal(inspection);
  }
}
