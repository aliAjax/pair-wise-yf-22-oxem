package com.generated.qualityTrace.repositories;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import com.generated.qualityTrace.models.WorkOrderClosing;

/**
 * 完工收尾结论存储。
 *
 * 以工单号为键保存唯一一份收尾结论：重复提交取回首次结论，
 * 末检/不良变化后旧结论失效并被新结论替换。
 */
@Repository
public class WorkOrderClosingRepository {

    private final ConcurrentHashMap<String, WorkOrderClosing> store = new ConcurrentHashMap<>();

    public Optional<WorkOrderClosing> findByWorkOrderNo(String workOrderNo) {
        return Optional.ofNullable(store.get(workOrderNo));
    }

    public WorkOrderClosing save(WorkOrderClosing closing) {
        store.put(closing.workOrderNo, closing);
        return closing;
    }

    public void invalidateByWorkOrderNo(String workOrderNo) {
        WorkOrderClosing existing = store.get(workOrderNo);
        if (existing != null) {
            existing.valid = false;
        }
    }

    public boolean existsByWorkOrderNo(String workOrderNo) {
        return store.containsKey(workOrderNo);
    }
}
