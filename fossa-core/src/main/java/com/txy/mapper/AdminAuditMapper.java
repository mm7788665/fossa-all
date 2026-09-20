package com.txy.mapper;

import com.txy.entity.AdminAuditLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 操作审计日志 */
public interface AdminAuditMapper {

    int insert(AdminAuditLog log);

    /** 条件查询 */
    List<AdminAuditLog> select(@Param("user") String user,
                               @Param("module") String module,
                               @Param("action") String action,
                               @Param("success") Integer success,
                               @Param("kw") String kw,
                               @Param("dateFrom") String dateFrom,
                               @Param("dateTo") String dateTo,
                               @Param("offset") int offset,
                               @Param("size") int size);

    int count(@Param("user") String user,
              @Param("module") String module,
              @Param("action") String action,
              @Param("success") Integer success,
              @Param("kw") String kw,
              @Param("dateFrom") String dateFrom,
              @Param("dateTo") String dateTo);

    /** 删除 N 天前的日志 */
    int deleteBefore(@Param("days") int days);
}
