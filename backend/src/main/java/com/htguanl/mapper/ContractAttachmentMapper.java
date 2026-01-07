package com.htguanl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.htguanl.entity.ContractAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 合同附件Mapper接口
 */
@Mapper
public interface ContractAttachmentMapper extends BaseMapper<ContractAttachment> {

    /**
     * 根据合同ID查询附件列表
     */
    @Select("SELECT * FROM biz_contract_attachment WHERE contract_id = #{contractId} ORDER BY create_time DESC")
    List<ContractAttachment> selectByContractId(@Param("contractId") Long contractId);

    /**
     * 根据合同ID和文件类型查询附件
     */
    @Select("SELECT * FROM biz_contract_attachment WHERE contract_id = #{contractId} AND file_type = #{fileType}")
    List<ContractAttachment> selectByContractIdAndFileType(@Param("contractId") Long contractId, @Param("fileType") String fileType);

    /**
     * 根据AI提取状态查询附件
     */
    @Select("SELECT * FROM biz_contract_attachment WHERE ai_extract_status = #{status} ORDER BY create_time DESC")
    List<ContractAttachment> selectByAiExtractStatus(@Param("status") String status);
}
