# 制造业质量追溯 API 服务

面向小型制造工厂的批次质量追溯后端服务，覆盖工单、批次、检验项、不良记录和追溯查询，并提供**车间主管完工收尾（finalization）**：关工单前逐批核对末检结论与未关闭严重不良，有问题一律留在暂停，杜绝“只看工单状态就放行”。

## 快速启动

```bash
cp .env.example .env && docker compose up -d
```

健康检查：<http://localhost:21114/health>

## 完工收尾怎么用

```bash
# 车间主管提交工单号（requestId 建议由调用方生成，用于幂等/恢复）
curl -X POST http://localhost:21114/api/work-orders/finalizations \
  -H 'Content-Type: application/json' \
  -d '{"orderNo":"WO-20260920-01","operatorId":"line-sup-01","requestId":"fin-20261001-01"}'
```

返回整体结论 `FINISHED`/`PAUSED`/`SUPERSEDED` 与**逐批结论 + 中文说明**。业务规则：

1. **逐批核对，不再只看工单状态**：汇总每个批次的最新末检结论和未关闭不良。
   - 末检 `FAIL`/`RECHECK`（含让步接收）、缺末检、**末检检验项缺失**，或存在未关闭 `MAJOR/CRITICAL` 不良 → 该批次阻断；
   - 只要有一个批次阻断，工单保持 `PAUSED`，响应逐批说明原因；全部通过才置 `FINISHED`；
   - 未关闭 `MINOR` 不良不阻断，但在逐批说明中提示跟进。
2. **数据版本失效**：每份结论带 `dataVersion`，由“各批次末检（结论/时间/要求项数）+ 检验项 + 不良记录”内容算出。末检一重录、检验项一补录、不良一登记/处置/关闭，旧结论立即置 `SUPERSEDED`（曾放行的工单退回 `PAUSED`）；重新提交按最新记录重算。
3. **并发去重**：同一工单同时提交，服务按工单加锁，只生成一份结果；重复请求（含相同 `requestId`）取回首次结论，`duplicate=true`。
4. **写入失败可恢复**：逐批写检查点。中途写失败时请求置 `FAILED` 并保留已完成批次；用相同 `requestId` 重新提交，从未完成批次继续（`resumed=true`），审计按幂等键去重，**重试不再写重复审计**。
5. **旧数据升级**：升级前已存在的末检若没有要求项数或检验项行，即使 `result_status=PASS` 也判为 `ITEMS_INCOMPLETE`（按未完成处理，不能当合格）；补录检验项后版本变化，重新提交即可放行。

### 相关接口

| 方法/路径 | 说明 |
|---|---|
| `POST /api/work-orders/finalizations` | 提交工单号，汇总末检结论与未关闭不良，给出收尾结论 |
| `GET  /api/work-orders/finalizations/{requestId}` | 取回某次收尾结论与逐批说明 |
| `POST /api/quality-inspection/final` | 提交/重录批次末检（会作废旧收尾结论） |
| `POST /api/inspection-item-result` | 录入/补录检验项（旧数据补录走这里） |
| `POST /api/defect-record` | 登记不良（严重不良未关闭即阻断） |
| `PATCH /api/defect-record/{id}/disposition` | 处置/关闭不良 |
| `GET  /api/trace/{batchNo}` | 批次全链路追溯树（末检/检验项/不良 + dataVersion） |
| `GET  /api/audit-logs` | 审计日志（含幂等键，可核对重试未重复落审计） |

写入失败演练（默认关闭）：`.env` 中设置 `FINALIZATION_FAIL_AT_BATCH_INDEX=1`，首次收尾在第 2 个批次写入前失败一次，同 `requestId` 重试即从检查点恢复。

## 本地开发方式

- 需要 JDK 17、Maven 3.9+。
- 后端：进入 `backend` 后 `mvn spring-boot:run`，接口统一挂在 `/api`。
- 测试：`mvn test`（覆盖暂停逐批说明、版本失效重算、并发去重、失败恢复审计幂等、旧数据缺检验项等 10 个用例）。
- 当前演示运行时为内存仓储（种子数据即覆盖全部判定场景）；`database/init.sql` 给出 PostgreSQL 目标表结构。接入真实数据库时启用 `pom.xml` 中注释的 MyBatis-Plus/PostgreSQL 依赖，并移除 `application.properties` 里的自动装配排除项。

## 访问地址或 CLI 示例

后端健康检查：<http://localhost:21114/health>

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | - |
| 后端 | Spring Boot 3 + Java 17 + MyBatis-Plus（目标栈；演示仓储为内存实现） |
| 数据库 | PostgreSQL 15（`database/init.sql` 为目标模型） |
| 部署 | Docker Compose |

## 项目目录结构

```text
backend/src/main/java/com/generated/qualityTrace/
├── routes/               # 按实体分文件（含 WorkOrderRoutes 收尾路径常量）
├── controllers/          # 按实体分文件（含 WorkOrderFinalizationController、AuditLogController）
├── services/             # WorkOrderFinalizationService / BatchFinalizationEvaluator /
│                         # BatchDataVersionService / FinalizationInvalidationService /
│                         # FinalizationFailureGate / AuditLogService 及各实体服务
├── models/               # WorkOrder/ProductBatch/QualityInspection/InspectionItemResult/
│                         # DefectRecord/FinalizationRequest/BatchFinalization/AuditLog
├── repositories/         # 内存数据访问 + 检查点/幂等键 + 变更版本
├── middlewares/          # ErrorHandlerMiddleware（全局异常）等
├── constants/            # 枚举、错误码、日志模板、状态文案、逐批说明模板
├── constructors/         # 请求/响应 DTO 构造器（含 FinalizationDtoFactory）
├── validators/           # FinalizationSubmitValidator、QualityInspectionValidator 等
├── utils/                # Formatters、FinalizationVersionCalculator
├── types/                # payload/视图 record、BusinessException
└── config/               # AppConfig、FinalizationProperties
```

## 环境变量说明

- `COMPOSE_PROJECT_NAME`: Compose 项目名，默认 `quality-trace`
- `BACKEND_PORT`: 后端端口，默认 `21114`
- `DB_PORT`: 数据库宿主机端口
- `DB_USER/DB_PASSWORD/DB_NAME`: 本地数据库凭据
- `JWT_SECRET`: JWT 密钥
- `FINALIZATION_FAIL_AT_BATCH_INDEX`: 收尾写入失败演练，处理到该批次序号(0 起)失败一次，`-1` 关闭
- `FINALIZATION_FAIL_ONCE`: 是否只失败一次（默认 true，用于演示恢复）

## Docker 部署说明

- 根 Compose 文件不写 `version`，顶层 `name: quality-trace`。
- 容器名均使用 `${COMPOSE_PROJECT_NAME:-quality-trace}` 前缀。
- 数据库使用命名卷，避免绑定中文路径。
- 常见问题：端口占用时修改 `.env` 中端口后重启；需要重置数据时执行 `docker compose down -v`。

## 枚举/常量出现位置清单

### WorkOrderStatus（PLANNED / RUNNING / PAUSED / FINISHED / CANCELLED）

- 常量/类型：`constants/WorkOrderStatus.java`
- model：`models/WorkOrder.java`；repository：`repositories/WorkOrderRepository.java`
- 构造器/展示：`constructors/WorkOrderDtoFactory.java`、`constants/StatusLabels.java`、`utils/Formatters.java`
- service：`services/WorkOrderFinalizationService.java`（放行置 FINISHED、有问题留 PAUSED）、`services/FinalizationInvalidationService.java`（失效后退回 PAUSED）
- controller：`controllers/WorkOrderController.java`、`controllers/WorkOrderFinalizationController.java`
- 校验/错误：`validators/FinalizationSubmitValidator.java`、`constants/ErrorCodes.java`、`constants/ErrorMessages.java`
- 日志：`constants/LogTemplates.java`（FINALIZE_*）；数据库：`database/init.sql` CHECK 约束

### InspectionResultStatus（PASS / FAIL / CONDITIONAL_PASS / RECHECK）

- 常量：`constants/InspectionResultStatus.java`
- model：`models/QualityInspection.java`；repository：`repositories/QualityInspectionRepository.java`
- 判定：`services/BatchFinalizationEvaluator.java`（PASS 才放行，FAIL→FINAL_FAIL，其余→FINAL_RECHECK）
- 构造器/展示：`constructors/QualityInspectionDtoFactory.java`、`constants/StatusLabels.java`
- service/controller：`services/QualityInspectionService.java`、`controllers/QualityInspectionController.java`
- 版本：`utils/FinalizationVersionCalculator.java`（结论进入 dataVersion）；数据库：`database/init.sql`

### DefectSeverity（MINOR / MAJOR / CRITICAL）

- 常量：`constants/DefectSeverity.java`
- model：`models/DefectRecord.java`（isCritical：MAJOR/CRITICAL）；repository：`repositories/DefectRecordRepository.java`
- 判定：`services/BatchFinalizationEvaluator.java`（严重未关闭→CRITICAL_OPEN，MINOR 仅提示）
- 展示/格式化：`constants/StatusLabels.java`、`utils/Formatters.java#riskLevel`
- service/controller/校验：`services/DefectRecordService.java`、`controllers/DefectRecordController.java`、`validators/DefectRecordValidator.java`
- 版本：`utils/FinalizationVersionCalculator.java`；数据库：`database/init.sql`

### InspectionType（FIRST / PATROL / FINAL）

`constants/InspectionType.java`、`models/QualityInspection.java`、`repositories/QualityInspectionRepository.java`（findLatestFinalByBatchId）、`services/QualityInspectionService.java`、`validators/QualityInspectionValidator.java`、`constructors/QualityInspectionDtoFactory.java`、`database/init.sql`。

### DispositionStatus（OPEN / IN_PROGRESS / CLOSED）

`constants/DispositionStatus.java`、`models/DefectRecord.java`（isOpen）、`services/BatchFinalizationEvaluator.java`、`services/DefectRecordService.java`、`controllers/DefectRecordController.java`、`database/init.sql`。

### ItemResultStatus（OK / NG / PENDING）

`constants/ItemResultStatus.java`、`models/InspectionItemResult.java`、`repositories/InspectionItemResultRepository.java`、`services/InspectionItemResultService.java`、`constructors/InspectionItemResultDtoFactory.java`、`database/init.sql`。

### BatchFinalStatus（PASS / FINAL_FAIL / FINAL_RECHECK / FINAL_MISSING / ITEMS_INCOMPLETE / CRITICAL_OPEN）

`constants/BatchFinalStatus.java`、`constants/FinalizationMessages.java`（逐批说明）、`constants/StatusLabels.java`、`models/BatchFinalization.java`、`services/BatchFinalizationEvaluator.java`、`constructors/FinalizationDtoFactory.java`、`utils/Formatters.java`、`database/init.sql`。

### FinalizationStatus（IN_PROGRESS / FINISHED / PAUSED / SUPERSEDED / FAILED）

`constants/FinalizationStatus.java`、`models/FinalizationRequest.java`、`repositories/FinalizationRequestRepository.java`、`services/WorkOrderFinalizationService.java`、`services/FinalizationInvalidationService.java`、`constructors/FinalizationDtoFactory.java`、`constants/StatusLabels.java`、`utils/Formatters.java`、`database/init.sql`。

## 为什么会牵一发动全身

实体字段、枚举、日志模板、错误消息、构造器、筛选器和展示组件被刻意拆散到多个目录；末检/不良的每次变更还要联动数据版本（`FinalizationVersionCalculator`）、结论失效（`FinalizationInvalidationService`）、审计幂等键和逐批说明模板。新增一个判定原因通常需要同步：枚举常量、说明模板、评估器、DTO 工厂、状态文案、格式化器、README 清单与数据库 CHECK 约束。

## License

MIT
