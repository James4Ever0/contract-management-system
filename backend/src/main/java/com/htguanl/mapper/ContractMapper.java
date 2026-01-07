package com.htguanl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htguanl.entity.Contract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ContractMapper extends BaseMapper<Contract> {

    @Select("SELECT c.*, cu.customer_name FROM biz_contract c " +
            "LEFT JOIN biz_customer cu ON c.customer_id = cu.id " +
            "WHERE c.id = #{id}")
    Contract selectContractWithCustomer(@Param("id") Long id);

    @Select("<script>" +
            "SELECT c.*, cu.customer_name, " +
            "CASE WHEN c.end_date IS NOT NULL THEN (c.end_date - CURRENT_DATE) ELSE NULL END as days_remaining " +
            "FROM biz_contract c " +
            "LEFT JOIN biz_customer cu ON c.customer_id = cu.id " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "(c.contract_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR c.contract_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR cu.customer_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND c.contract_status = #{status} " +
            "</if>" +
            "</where>" +
            "ORDER BY c.create_time DESC" +
            "</script>")
    IPage<Contract> selectContractPage(Page<Contract> page, @Param("keyword") String keyword, @Param("status") String status);

    @Select("SELECT c.*, cu.customer_name FROM biz_contract c " +
            "LEFT JOIN biz_customer cu ON c.customer_id = cu.id " +
            "WHERE c.end_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '30 days' " +
            "ORDER BY c.end_date ASC")
    List<Contract> selectExpiringContracts();

    @Select("SELECT c.*, cu.customer_name FROM biz_contract c " +
            "LEFT JOIN biz_customer cu ON c.customer_id = cu.id " +
            "ORDER BY c.create_time DESC LIMIT #{limit}")
    List<Contract> selectRecentContracts(@Param("limit") int limit);

    @Select("SELECT contract_type as type, COUNT(*) as count FROM biz_contract " +
            "WHERE contract_type IS NOT NULL GROUP BY contract_type")
    List<Map<String, Object>> selectContractTypeDistribution();

    @Select("SELECT COUNT(*) FROM biz_contract WHERE contract_status = #{status}")
    Long countByStatus(@Param("status") String status);
}

