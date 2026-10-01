package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constructors.WorkOrderDtoFactory;
import com.generated.qualityTrace.models.WorkOrder;
import com.generated.qualityTrace.repositories.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WorkOrderService {

  private final WorkOrderRepository repo;

  public WorkOrderService(WorkOrderRepository repo) {
    this.repo = repo;
  }

  public List<Map<String, Object>> list() {
    return repo.findEntities().stream().map(WorkOrderDtoFactory::toView).toList();
  }

  public WorkOrder getByOrderNo(String orderNo) {
    return repo.findByOrderNo(orderNo).orElse(null);
  }
}
