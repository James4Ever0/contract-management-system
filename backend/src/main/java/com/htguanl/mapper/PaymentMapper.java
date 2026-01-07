package com.htguanl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htguanl.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    @Select("<script>" +
            "SELECT p.*, c.contract_no, c.contract_name FROM biz_payment p " +
            "LEFT JOIN biz_contract c ON p.contract_id = c.id " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "(p.payment_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR c.contract_no LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND p.payment_status = #{status} " +
            "</if>" +
            "<if test='type != null and type != \"\"'>" +
            "AND p.payment_type = #{type} " +
            "</if>" +
            "</where>" +
            "ORDER BY p.create_time DESC" +
            "</script>")
    IPage<Payment> selectPaymentPage(Page<Payment> page, @Param("keyword") String keyword,
                                      @Param("status") String status, @Param("type") String type);

    @Select("SELECT COALESCE(SUM(payment_amount), 0) FROM biz_payment " +
            "WHERE payment_type = 'RECEIVE' AND payment_status = 'PAID'")
    BigDecimal selectTotalReceived();

    @Select("SELECT TO_CHAR(actual_payment_date, 'YYYY-MM') as month, " +
            "SUM(CASE WHEN payment_type = 'RECEIVE' THEN payment_amount ELSE 0 END) as received, " +
            "SUM(CASE WHEN payment_type = 'RETURN' THEN payment_amount ELSE 0 END) as returned " +
            "FROM biz_payment WHERE payment_status = 'PAID' " +
            "AND actual_payment_date >= CURRENT_DATE - INTERVAL '12 months' " +
            "GROUP BY TO_CHAR(actual_payment_date, 'YYYY-MM') " +
            "ORDER BY month")
    List<Map<String, Object>> selectPaymentTrend();

    @Select("SELECT p.*, c.contract_no, c.contract_name FROM biz_payment p " +
            "LEFT JOIN biz_contract c ON p.contract_id = c.id " +
            "WHERE p.contract_id = #{contractId} ORDER BY p.create_time DESC")
    List<Payment> selectByContractId(@Param("contractId") Long contractId);
}

