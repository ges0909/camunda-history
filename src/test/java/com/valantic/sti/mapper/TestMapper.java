package com.valantic.sti.mapper;

import java.util.List;
import java.util.Map;

public interface TestMapper {
    List<Map<String, Object>> findLastHistoricTask();
}
