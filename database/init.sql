-- 制造业质量追溯 API 服务（quality-trace）目标关系模型（PostgreSQL 15）
-- 当前演示运行时使用内存仓储；此 DDL 作为接入 PostgreSQL/MyBatis-Plus 的建表脚本与数据字典。

CREATE TABLE IF NOT EXISTS work_order (
  id BIGSERIAL PRIMARY KEY,
  order_no TEXT NOT NULL UNIQUE,
  product_code TEXT NOT NULL,
  product_name TEXT,
  planned_qty INTEGER,
  line_code TEXT,
  start_at TIMESTAMP,
  status TEXT NOT NULL CHECK (status IN ('PLANNED','RUNNING','PAUSED','FINISHED','CANCELLED'))
);

CREATE TABLE IF NOT EXISTS product_batch (
  id BIGSERIAL PRIMARY KEY,
  batch_no TEXT NOT NULL UNIQUE,
  work_order_id BIGINT NOT NULL REFERENCES work_order(id),
  quantity INTEGER,
  material_lot_no TEXT,
  produced_at TIMESTAMP,
  batch_status TEXT
);
CREATE INDEX IF NOT EXISTS idx_batch_work_order ON product_batch(work_order_id);

CREATE TABLE IF NOT EXISTS quality_inspection (
  id BIGSERIAL PRIMARY KEY,
  batch_id BIGINT NOT NULL REFERENCES product_batch(id),
  inspector_id TEXT,
  inspection_type TEXT NOT NULL CHECK (inspection_type IN ('FIRST','PATROL','FINAL')),
  standard_version TEXT,
  result_status TEXT NOT NULL CHECK (result_status IN ('PASS','FAIL','CONDITIONAL_PASS','RECHECK')),
  -- 该标准版本要求的检验项数量。旧数据升级后该列为 NULL，收尾时按“检验项缺失/未完成”处理，不能当合格。
  required_item_count INTEGER,
  inspected_at TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_inspection_batch_type
  ON quality_inspection(batch_id, inspection_type, inspected_at DESC);

CREATE TABLE IF NOT EXISTS inspection_item_result (
  id BIGSERIAL PRIMARY KEY,
  inspection_id BIGINT NOT NULL REFERENCES quality_inspection(id),
  item_code TEXT NOT NULL,
  item_name TEXT,
  measured_value TEXT,
  limit_min DOUBLE PRECISION,
  limit_max DOUBLE PRECISION,
  item_status TEXT NOT NULL CHECK (item_status IN ('OK','NG','PENDING'))
);
CREATE INDEX IF NOT EXISTS idx_item_inspection ON inspection_item_result(inspection_id);

CREATE TABLE IF NOT EXISTS defect_record (
  id BIGSERIAL PRIMARY KEY,
  batch_id BIGINT NOT NULL REFERENCES product_batch(id),
  defect_type TEXT NOT NULL,
  defect_qty INTEGER,
  severity TEXT NOT NULL CHECK (severity IN ('MINOR','MAJOR','CRITICAL')),
  root_cause TEXT,
  disposition_status TEXT NOT NULL CHECK (disposition_status IN ('OPEN','IN_PROGRESS','CLOSED'))
);
CREATE INDEX IF NOT EXISTS idx_defect_batch ON defect_record(batch_id);

-- 完工收尾请求：同工单并发去重 + 数据版本失效 + 写入失败恢复
CREATE TABLE IF NOT EXISTS finalization_request (
  id BIGSERIAL PRIMARY KEY,
  request_id TEXT NOT NULL UNIQUE,          -- 幂等键
  work_order_id BIGINT NOT NULL REFERENCES work_order(id),
  order_no TEXT NOT NULL,
  operator_id TEXT,
  status TEXT NOT NULL CHECK (status IN ('IN_PROGRESS','FINISHED','PAUSED','SUPERSEDED','FAILED')),
  data_version TEXT NOT NULL,               -- 提交时末检/检验项/不良内容哈希
  total_batch_count INTEGER NOT NULL DEFAULT 0,
  completed_batch_count INTEGER NOT NULL DEFAULT 0,  -- 逐批写入检查点
  blocker_count INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_finalization_work_order ON finalization_request(work_order_id, id DESC);

-- 逐批收尾结论（检查点单元：(request_id, batch_id) 唯一，恢复时已存在批次直接跳过）
CREATE TABLE IF NOT EXISTS batch_finalization (
  id BIGSERIAL PRIMARY KEY,
  request_id TEXT NOT NULL REFERENCES finalization_request(request_id),
  batch_id BIGINT NOT NULL REFERENCES product_batch(id),
  batch_no TEXT NOT NULL,
  batch_index INTEGER NOT NULL,
  conclusion TEXT NOT NULL CHECK (conclusion IN
    ('PASS','FINAL_FAIL','FINAL_RECHECK','FINAL_MISSING','ITEMS_INCOMPLETE','CRITICAL_OPEN')),
  blocker_count INTEGER NOT NULL DEFAULT 0,
  open_critical_defects INTEGER NOT NULL DEFAULT 0,
  open_minor_defects INTEGER NOT NULL DEFAULT 0,
  final_inspection_status TEXT,
  recorded_item_count INTEGER NOT NULL DEFAULT 0,
  required_item_count INTEGER NOT NULL DEFAULT 0,
  explanation TEXT NOT NULL,                -- 面向车间主管的逐批中文说明
  created_at TIMESTAMP,
  UNIQUE (request_id, batch_id)
);

-- 审计日志：idempotency_key 唯一，重试/恢复不重复写审计
CREATE TABLE IF NOT EXISTS audit_log (
  id BIGSERIAL PRIMARY KEY,
  actor TEXT,
  action TEXT NOT NULL,
  target_type TEXT,
  target_id TEXT,
  detail TEXT,
  idempotency_key TEXT UNIQUE,
  created_at TIMESTAMP
);

-- ---- 种子数据：覆盖 合格 / 末检FAIL / RECHECK / 旧数据缺检验项 / 未关闭严重不良 ----
INSERT INTO work_order (id, order_no, product_code, product_name, planned_qty, line_code, start_at, status) VALUES
  (1,'WO-20260920-01','P-1001','减速电机',200,'L1','2026-09-20 08:00:00','RUNNING'),
  (2,'WO-20260920-02','P-1002','传动轴',120,'L2','2026-09-21 08:00:00','PAUSED'),
  (3,'WO-20260921-03','P-1003','法兰盘',60,'L1','2026-09-21 09:00:00','PAUSED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO product_batch (id, batch_no, work_order_id, quantity, material_lot_no, produced_at, batch_status) VALUES
  (1,'B-0920-01-A',1,100,'MAT-L-77','2026-09-20 16:00:00','PRODUCED'),
  (2,'B-0920-01-B',1,100,'MAT-L-78','2026-09-20 20:00:00','PRODUCED'),
  (3,'B-0921-02-A',2,120,'MAT-L-79','2026-09-21 15:00:00','PRODUCED'),
  (4,'B-0921-03-A',3,60,'MAT-L-80','2026-09-21 14:00:00','PRODUCED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO quality_inspection
  (id, batch_id, inspector_id, inspection_type, standard_version, result_status, required_item_count, inspected_at) VALUES
  (1,1,'u-qc-01','FINAL','STD-2026-A','PASS',3,'2026-09-20 17:00:00'),
  (2,2,'u-qc-01','FINAL','STD-2026-A','FAIL',3,'2026-09-20 21:00:00'),
  (3,3,'u-qc-02','FINAL','STD-2026-B','RECHECK',2,'2026-09-21 16:00:00'),
  -- 旧数据：末检标 PASS 但 required_item_count 为 NULL，且无检验项行 -> 收尾按未完成，不能当合格
  (4,4,'u-qc-02','FINAL','STD-2025-OLD','PASS',NULL,'2026-09-21 14:30:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO inspection_item_result
  (id, inspection_id, item_code, item_name, measured_value, limit_min, limit_max, item_status) VALUES
  (1,1,'DIM-A','总长','99.98',99.90,100.10,'OK'),
  (2,1,'DIM-B','外径','25.01',24.95,25.05,'OK'),
  (3,1,'SURF','粗糙度','Ra1.6',NULL,NULL,'OK'),
  (4,2,'DIM-A','总长','100.40',99.90,100.10,'NG'),
  (5,2,'DIM-B','外径','25.00',24.95,25.05,'OK'),
  (6,2,'SURF','粗糙度','Ra1.5',NULL,NULL,'OK'),
  (7,3,'DIA','孔径','12.02',11.98,12.05,'OK'),
  (8,3,'RUNOUT','跳动','0.09',0.0,0.08,'NG')
ON CONFLICT (id) DO NOTHING;

INSERT INTO defect_record
  (id, batch_id, defect_type, defect_qty, severity, root_cause, disposition_status) VALUES
  (1,1,'划伤',2,'MAJOR','周转碰撞','CLOSED'),
  (2,1,'毛刺',1,'MINOR','刀具磨损','IN_PROGRESS'),
  (3,2,'尺寸超差',6,'CRITICAL','夹具偏移','OPEN')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('work_order','id'), 100);
SELECT setval(pg_get_serial_sequence('product_batch','id'), 100);
SELECT setval(pg_get_serial_sequence('quality_inspection','id'), 100);
SELECT setval(pg_get_serial_sequence('inspection_item_result','id'), 100);
SELECT setval(pg_get_serial_sequence('defect_record','id'), 100);
