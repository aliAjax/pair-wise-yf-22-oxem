package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.services.ProductBatchService;

@RestController
@RequestMapping("/api")
public class ProductBatchController {

  private final ProductBatchService service;

  public ProductBatchController(ProductBatchService service) {
    this.service = service;
  }

  @GetMapping("/product-batch")
  public List<Map<String, Object>> list() {
    return service.list();
  }

  /** 批次全链路追溯树。 */
  @GetMapping("/trace/{batchNo}")
  public Map<String, Object> trace(@PathVariable String batchNo) {
    return service.trace(batchNo);
  }
}
