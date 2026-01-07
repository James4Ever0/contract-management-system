package com.htguanl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.htguanl.entity.ContractAiExtract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 合同AI提取结果Mapper接口
 */
@Mapper
public interface ContractAiExtractMapper extends BaseMapper<ContractAiExtract> {

    /**
     * 根据合同ID查询AI提取结果
     */
    ContractAiExtract selectByContractId(@Param("contractId") Long contractId);

    /**
     * 根据附件ID查询AI提取结果
     */
    ContractAiExtract selectByAttachmentId(@Param("attachmentId") Long attachmentId);
}
