package com.generated.qualityTrace.constants;

import java.util.Locale;

/**
 * 逐批收尾说明文案（车间主管需要看到每个批次为什么被留在暂停）。
 * 与 BatchFinalStatus 一一对应；新增判定原因时必须同步 formatters 与 README 清单。
 */
public final class FinalizationMessages {
  public static final String PASS = "批次 %s 末检合格，无未关闭严重不良，可以放行";
  public static final String FINAL_FAIL = "批次 %s 末检结论为不合格(FAIL)，不予放行";
  public static final String FINAL_RECHECK = "批次 %s 末检结论为复检(RECHECK)，需复检合格后再提交";
  public static final String FINAL_MISSING = "批次 %s 缺少末检记录，按未完成处理";
  public static final String ITEMS_INCOMPLETE =
      "批次 %s 的末检缺少检验项结果(已录 %d/%d 项要求项)，按未完成处理，不能当合格";
  public static final String CRITICAL_OPEN =
      "批次 %s 存在 %d 条未关闭严重不良(%s)，不予放行";
  public static final String NON_CRITICAL_OPEN_HINT =
      "另有 %d 条未关闭一般不良，不阻断完工但需继续跟进";

  public static String minorHint(int count) {
    return String.format(Locale.ROOT, NON_CRITICAL_OPEN_HINT, count);
  }

  public static String msg(BatchFinalStatus status, Object... args) {
    String tpl =
        switch (status) {
          case PASS -> PASS;
          case FINAL_FAIL -> FINAL_FAIL;
          case FINAL_RECHECK -> FINAL_RECHECK;
          case FINAL_MISSING -> FINAL_MISSING;
          case ITEMS_INCOMPLETE -> ITEMS_INCOMPLETE;
          case CRITICAL_OPEN -> CRITICAL_OPEN;
        };
    return String.format(Locale.ROOT, tpl, args);
  }

  private FinalizationMessages() {}
}
