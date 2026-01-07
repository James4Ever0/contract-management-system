package com.htguanl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htguanl.entity.ContractAttachment;
import com.htguanl.mapper.ContractAttachmentMapper;
import org.springframework.stereotype.Service;

/**
 * 合同附件服务类
 */
@Service
public class ContractAttachmentService extends ServiceImpl<ContractAttachmentMapper, ContractAttachment> 
        implements IService<ContractAttachment> {
    
}

