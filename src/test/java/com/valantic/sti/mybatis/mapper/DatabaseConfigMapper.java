package com.valantic.sti.mybatis.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface DatabaseConfigMapper {

    // status = 'ON' | 'OFF'
    @Update("SET GLOBAL general_log = #{status}")
    void setGeneralLog(@Param("status") String status);

    // filePath = /var/log/mysql/general.log
    @Update("SET GLOBAL general_log_file = #{filePath}")
    void setGeneralLogFile(@Param("filePath") String filePath);

    // status = 'ON' | 'OFF'
    @Update("SET GLOBAL slow_query_log = #{status}")
    void setSlowQueryLog(@Param("status") String status);

    // filePath = /var/log/mysql/slow-query.log
    @Update("SET GLOBAL slow_query_log_file = #{filePath}")
    void setSlowQueryLogFile(@Param("filePath") String filePath);

    @Update("SET GLOBAL long_query_time = #{longQueryTime}")
    void setLongQueryTime(@Param("longQueryTime") Double longQueryTime);
}
