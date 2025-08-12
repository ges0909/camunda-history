package com.valantic.sti.mybatis.mapper;

import java.util.List;
import java.util.Map;

public interface HistoryMapper {
    List<Map<String, Object>> findLastHistoricTask();
}
