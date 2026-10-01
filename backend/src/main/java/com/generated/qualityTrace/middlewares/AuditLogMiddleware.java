package com.generated.qualityTrace.middlewares;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 操作日志中间件。
 *
 * 收尾写操作（提交、暂停、完工、失效、恢复）均写审计。审计按
 * action + targetId 幂等：同一收尾结论的同一动作只写一次，
 * 重试不再重复写审计。
 */
@Component
public class AuditLogMiddleware {

    /** 幂等键（action|targetId） -> 审计记录。 */
    private final ConcurrentHashMap<String, Map<String, Object>> store = new ConcurrentHashMap<>();

    /**
     * 写审计日志。幂等：相同 action + targetId 已存在时直接返回已有记录。
     */
    public Map<String, Object> write(String action, String targetType, String targetId, String detail) {
        String key = action + "|" + targetId;
        Map<String, Object> existing = store.get(key);
        if (existing != null) {
            return existing;
        }
        Map<String, Object> entry = new java.util.LinkedHashMap<>();
        entry.put("action", action);
        entry.put("targetType", targetType);
        entry.put("targetId", targetId);
        entry.put("detail", detail);
        entry.put("createdAt", now());
        store.put(key, entry);
        return entry;
    }

    public List<Map<String, Object>> list() {
        return new ArrayList<>(store.values());
    }

    private static String now() {
        return java.time.LocalDateTime.now().toString();
    }
}
