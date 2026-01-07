package com.htguanl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htguanl.entity.Seal;
import com.htguanl.entity.SealBorrow;
import com.htguanl.mapper.SealBorrowMapper;
import com.htguanl.mapper.SealMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SealService extends ServiceImpl<SealMapper, Seal> {

    @Autowired
    private SealBorrowMapper sealBorrowMapper;

    public IPage<Seal> getSealPage(Integer pageNum, Integer pageSize, String keyword, String status) {
        Page<Seal> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Seal> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(Seal::getSealName, keyword)
                .or().like(Seal::getSealType, keyword)
            );
        }
        // 由于数据库没有seal_status列，忽略状态过滤
        wrapper.orderByDesc(Seal::getCreateTime);

        return page(page, wrapper);
    }

    @Transactional
    public Seal createSeal(Seal seal) {
        // 数据库没有seal_code和seal_status列，只保存基本信息
        save(seal);
        return seal;
    }

    public IPage<SealBorrow> getBorrowPage(Integer pageNum, Integer pageSize, String keyword, String status) {
        Page<SealBorrow> page = new Page<>(pageNum, pageSize);
        return sealBorrowMapper.selectBorrowPage(page, keyword, status);
    }

    public SealBorrow getBorrowDetail(Long id) {
        return sealBorrowMapper.selectBorrowWithDetail(id);
    }

    public List<SealBorrow> getBorrowsBySeal(Long sealId) {
        LambdaQueryWrapper<SealBorrow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SealBorrow::getSealId, sealId)
               .orderByDesc(SealBorrow::getBorrowTime);
        return sealBorrowMapper.selectList(wrapper);
    }

    @Transactional
    public SealBorrow createBorrow(SealBorrow borrow) {
        // 检查印章是否存在
        Seal seal = getById(borrow.getSealId());
        if (seal == null) {
            throw new RuntimeException("印章不存在");
        }

        String borrowNo = generateBorrowNo();
        borrow.setBorrowNo(borrowNo);
        borrow.setBorrowStatus("BORROWED");
        borrow.setBorrowTime(LocalDateTime.now());
        sealBorrowMapper.insert(borrow);

        return sealBorrowMapper.selectBorrowWithDetail(borrow.getId());
    }

    @Transactional
    public SealBorrow returnSeal(Long borrowId) {
        SealBorrow borrow = sealBorrowMapper.selectById(borrowId);
        if (borrow == null) {
            throw new RuntimeException("借用记录不存在");
        }
        if ("RETURNED".equals(borrow.getBorrowStatus())) {
            throw new RuntimeException("印章已归还");
        }

        borrow.setBorrowStatus("RETURNED");
        borrow.setActualReturnTime(LocalDateTime.now());
        sealBorrowMapper.updateById(borrow);

        return sealBorrowMapper.selectBorrowWithDetail(borrowId);
    }

    public Long countBorrowing() {
        LambdaQueryWrapper<SealBorrow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SealBorrow::getBorrowStatus, "BORROWED");
        return sealBorrowMapper.selectCount(wrapper);
    }

    public Long countAvailableSeals() {
        // 由于数据库没有seal_status列，返回总数作为替代
        return count();
    }

    private String generateBorrowNo() {
        String prefix = "JY" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long todayCount = sealBorrowMapper.selectCount(new LambdaQueryWrapper<SealBorrow>()
                .likeRight(SealBorrow::getBorrowNo, prefix));
        return prefix + String.format("%04d", todayCount + 1);
    }
}

