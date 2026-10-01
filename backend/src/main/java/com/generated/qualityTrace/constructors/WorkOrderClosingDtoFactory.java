package com.generated.qualityTrace.constructors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.generated.qualityTrace.models.WorkOrderClosing;
import com.generated.qualityTrace.utils.Formatters;

/**
 * 完工收尾响应 DTO 构造器。统一负责收尾结论的对外展示结构，
 * 页面/控制器不得直接散写默认结构。
 */
public final class WorkOrderClosingDtoFactory {

    private WorkOrderClosingDtoFactory() {}

    /** 构造逐批收尾结论。 */
    public static Map<String, Object> batchSummary(String batchNo, String finalConclusion,
                                                   List<Map<String, Object>> unclosedDefects,
                                                   List<String> reasons, boolean qualified) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("batchNo", batchNo);
        m.put("finalConclusion", finalConclusion);
        m.put("finalConclusionText", Formatters.finalConclusion(finalConclusion));
        m.put("unclosedDefects", unclosedDefects == null ? new ArrayList<>() : unclosedDefects);
        m.put("reasons", reasons == null ? new ArrayList<>() : reasons);
        m.put("qualified", qualified);
        return m;
    }

    /** 构造收尾结论响应。 */
    public static Map<String, Object> toResponse(WorkOrderClosing c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("closingId", c.id);
        m.put("workOrderNo", c.workOrderNo);
        m.put("verdict", c.verdict);
        m.put("verdictText", Formatters.closingVerdict(c.verdict));
        m.put("paused", c.paused);
        m.put("batchCount", c.batchCount);
        m.put("qualifiedBatchCount", c.qualifiedBatchCount);
        m.put("valid", c.valid);
        m.put("status", c.status);
        m.put("fingerprint", c.fingerprint);
        m.put("batches", c.batchSummaries);
        m.put("createdAt", c.createdAt);
        m.put("updatedAt", c.updatedAt);
        return m;
    }

    /** 构造“无收尾结论”响应。 */
    public static Map<String, Object> notFound(String workOrderNo) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("workOrderNo", workOrderNo);
        m.put("verdict", null);
        m.put("message", "暂无收尾结论，请先提交工单号收尾");
        return m;
    }
}
