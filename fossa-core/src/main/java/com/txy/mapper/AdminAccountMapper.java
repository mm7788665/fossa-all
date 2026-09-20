package com.txy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.txy.entity.AdminAccount;

import java.util.List;

@Mapper
public interface AdminAccountMapper {

    List<AdminAccount> selectAll();

    AdminAccount selectByUser(@Param("user") String user);

    /** 登录校验（自动过滤已删除账号） */
    AdminAccount selectByUserAndPass(@Param("user") String user, @Param("pass") String pass);

    int insert(AdminAccount account);

    int update(AdminAccount account);

    /** 逻辑删除 */
    int logicDelete(@Param("user") String user);
}
