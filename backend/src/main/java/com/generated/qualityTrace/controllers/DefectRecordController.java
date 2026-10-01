package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.constants.DispositionStatus;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.services.DefectRecordService;
import com.generated.qualityTrace.validators.DefectRecordValidator;

@RestController
@RequestMapping("/api/defect-record")
public class DefectRecordController {

  private final DefectRecordService service;

  public DefectRecordController(DefectRecordService service) {
    this.service = service;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list();
  }

  /** 登记不良：严重不良未关闭会阻断完工收尾，旧结论失效。 */
  @PostMapping
  public Map<String, Object> register(@RequestBody DefectRecord defect) {
    DefectRecordValidator.validate(defect);
    return service.register(defect);
  }

  /** 处置/关闭不良。 */
  @PatchMapping("/{id}/disposition")
  public Map<String, Object> dispose(@PathVariable Long id, @RequestBody Map<String, String> body) {
    return service.dispose(id, DispositionStatus.valueOf(body.get("dispositionStatus")));
  }
}
