# 制造业质量追溯 API 服务

面向小型制造工厂的批次质量追溯后端服务，覆盖工单、批次、检验项、不良记录和追溯查询。

## 快速启动

```bash
cp .env.example .env && docker compose up -d
```

## 访问地址或 CLI 示例

后端健康检查：<http://localhost:21114/health>

### 工单完工收尾

车间主管关工单时不再只看工单状态。提交工单号后，系统汇总各批次的末检结论与未关闭不良：
有问题则工单暂停并逐批说明，全部合格才放行完工。末检或不良一变，旧的收尾结论即失效，
重新提交时按最新记录重算。

```bash
# 提交工单号收尾（汇总各批次末检结论与未关闭不良）
curl -X POST http://localhost:21114/api/work-order/WO-2026-0001/closing

# 查询当前收尾结论
curl http://localhost:21114/api/work-order/WO-2026-0001/closing

# 使当前收尾结论失效（末检/不良变化后调用）
curl -X POST http://localhost:21114/api/work-order/WO-2026-0001/closing/invalidate

# 恢复待完成的收尾（写入失败后从未完成批次恢复，审计不重复写）
curl -X POST http://localhost:21114/api/work-order/WO-2026-0003/closing/recover

# 关闭不良并使收尾结论失效（不良一变，旧结论失效）
curl -X POST http://localhost:21114/api/work-order/WO-2026-0002/defects/D-001/close
```

收尾结论 `verdict`：
- `QUALIFIED`：各批次末检合格且无未关闭不良，工单完工放行。
- `UNQUALIFIED`：存在末检不合格或未关闭不良，工单暂停，响应逐批列出原因。
- `INCOMPLETE`：存在末检缺失或末检缺检验项的批次（旧数据升级按未完成处理），工单暂停。

并发与恢复：
- 同一工单同时提交收尾只生成一份结果，重复请求取回首次结论。
- 写入失败时收尾以 `PENDING` 状态保留已完成批次；调用恢复接口从未完成批次继续，审计不重复写入。
- 置 `APP_CLOSING_FAIL_ON_FIRST_WRITE=true` 可演示首次写入中断与恢复。


## 本地开发方式


- 后端：进入 `backend` 后按技术栈运行开发命令，接口统一挂在 `/api`。


## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | - |
| 后端 | Spring Boot 3 + Java 17 + MyBatis-Plus |
| 数据库 | PostgreSQL 15 |
| 部署 | Docker Compose |

## 项目目录结构

```text

backend/src/routes, controllers, services, models, repositories, middlewares, constants, constructors, utils, types, config
```

## 环境变量说明

- `COMPOSE_PROJECT_NAME`: Compose 项目名，默认 `quality-trace`

- `BACKEND_PORT`: 后端端口，默认 `21114`
- `DB_PORT`: 数据库宿主机端口
- `DB_USER/DB_PASSWORD/DB_NAME`: 本地数据库凭据

## Docker 部署说明

- 根 Compose 文件不写 `version`，顶层 `name: quality-trace`。
- 容器名均使用 `${COMPOSE_PROJECT_NAME:-quality-trace}` 前缀。
- 数据库使用命名卷，避免绑定中文路径。
- 常见问题：端口占用时修改 `.env` 中端口后重启；需要重置数据时执行 `docker compose down -v`。

## 枚举/常量出现位置清单

- WorkOrderStatus: constants/WorkOrderStatus、types/WorkOrderStatus、constructors、logTemplates、errorMessages、筛选器、展示组件/控制器均有引用。
- InspectionResultStatus: constants/InspectionResultStatus、types/InspectionResultStatus、constructors、logTemplates、errorMessages、筛选器、展示组件/控制器均有引用。
- DefectSeverity: constants/DefectSeverity、types/DefectSeverity、constructors、logTemplates、errorMessages、筛选器、展示组件/控制器均有引用。
- ClosingVerdict（新增）: constants/ClosingVerdict、services/WorkOrderClosingService、models/WorkOrderClosing、constructors/WorkOrderClosingDtoFactory、utils/Formatters、controllers/WorkOrderClosingController、logTemplates、errorMessages 均有引用。
- FinalConclusion（新增）: constants/FinalConclusion、services/WorkOrderClosingService、constructors/WorkOrderClosingDtoFactory、utils/Formatters、errorMessages 均有引用。
- InspectionType（新增）: constants/InspectionType、services/WorkOrderClosingService（末检判定）、repositories/QualityInspectionRepository 均有引用。
- 收尾错误码/日志模板（新增）: constants/ErrorCodes、constants/ErrorMessages、constants/LogTemplates、services/WorkOrderClosingService、middlewares/AuditLogMiddleware、middlewares/ErrorHandlerMiddleware、validators/WorkOrderClosingValidator 均有引用。

## 为什么会牵一发动全身

实体字段、枚举、日志模板、错误消息、构造器、筛选器和展示组件被刻意拆散到多个目录；修改一个状态值通常需要同步类型、构造器、服务、控制器、store、页面、README 与数据库种子。

## License

MIT
