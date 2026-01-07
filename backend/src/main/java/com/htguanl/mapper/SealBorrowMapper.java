package com.htguanl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htguanl.entity.SealBorrow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SealBorrowMapper extends BaseMapper<SealBorrow> {

    @Select("<script>" +
            "SELECT sb.*, s.seal_name, s.seal_type, " +
            "COALESCE(u.real_name, sb.borrower) as borrower_name, " +
            "c.contract_no, c.contract_name " +
            "FROM biz_seal_borrow sb " +
            "LEFT JOIN biz_seal s ON sb.seal_id = s.id " +
            "LEFT JOIN sys_user u ON sb.borrower_id = u.id " +
            "LEFT JOIN biz_contract c ON sb.contract_id = c.id " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "(sb.borrow_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR s.seal_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR sb.borrower LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND sb.borrow_status = #{status} " +
            "</if>" +
            "</where>" +
            "ORDER BY sb.create_time DESC" +
            "</script>")
    IPage<SealBorrow> selectBorrowPage(Page<SealBorrow> page, @Param("keyword") String keyword,
                                        @Param("status") String status);

    @Select("SELECT sb.*, s.seal_name, s.seal_type, " +
            "COALESCE(u.real_name, sb.borrower) as borrower_name, " +
            "c.contract_no, c.contract_name " +
            "FROM biz_seal_borrow sb " +
            "LEFT JOIN biz_seal s ON sb.seal_id = s.id " +
            "LEFT JOIN sys_user u ON sb.borrower_id = u.id " +
            "LEFT JOIN biz_contract c ON sb.contract_id = c.id " +
            "WHERE sb.id = #{id}")
    SealBorrow selectBorrowWithDetail(@Param("id") Long id);
}

