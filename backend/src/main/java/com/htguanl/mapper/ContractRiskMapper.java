package com.htguanl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htguanl.entity.ContractRisk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ContractRiskMapper extends BaseMapper<ContractRisk> {

    @Select("<script>" +
            "SELECT cr.*, c.contract_no, c.contract_name, cu.customer_name " +
            "FROM biz_contract_risk cr " +
            "LEFT JOIN biz_contract c ON cr.contract_id = c.id " +
            "LEFT JOIN biz_customer cu ON c.customer_id = cu.id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (c.contract_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR c.contract_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='level != null and level != \"\"'>" +
            "AND cr.risk_level = #{level} " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND cr.risk_status = #{status} " +
            "</if>" +
            "ORDER BY CASE cr.risk_level " +
            "WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 ELSE 3 END, " +
            "cr.create_time DESC" +
            "</script>")
    IPage<ContractRisk> selectRiskPage(Page<ContractRisk> page, @Param("keyword") String keyword,
                                        @Param("level") String level, @Param("status") String status);

    @Select("SELECT COUNT(*) FROM biz_contract_risk WHERE is_resolved = false")
    Long countUnresolved();
}

