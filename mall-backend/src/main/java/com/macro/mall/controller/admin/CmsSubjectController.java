package com.macro.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macro.mall.common.CommonPage;
import com.macro.mall.common.CommonResult;
import com.macro.mall.dto.SubjectParam;
import com.macro.mall.entity.Subject;
import com.macro.mall.mapper.LinkMapper;
import com.macro.mall.mapper.SubjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/subject")
@RequiredArgsConstructor
public class CmsSubjectController {
    private final SubjectMapper subjectMapper;
    private final LinkMapper linkMapper;

    @GetMapping("/list")
    public CommonResult<CommonPage<Subject>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return CommonResult.success(CommonPage.of(subjectMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Subject>()
                        .like(keyword != null && !keyword.isBlank(), Subject::getName, keyword)
                        .orderByAsc(Subject::getSort))));
    }

    @GetMapping("/{id}")
    public CommonResult<SubjectParam> detail(@PathVariable Long id) {
        Subject subject = subjectMapper.selectById(id);
        if (subject == null) {
            return CommonResult.failed("专题不存在");
        }
        SubjectParam param = new SubjectParam();
        BeanUtils.copyProperties(subject, param);
        param.setProductIds(linkMapper.selectProductIdsBySubject(id));
        return CommonResult.success(param);
    }

    @PostMapping("/create")
    @Transactional
    public CommonResult<Long> create(@RequestBody SubjectParam param) {
        Subject subject = new Subject();
        BeanUtils.copyProperties(param, subject);
        subject.setId(null);
        subject.setCreateTime(LocalDateTime.now());
        if (subject.getShowStatus() == null) {
            subject.setShowStatus(1);
        }
        subjectMapper.insert(subject);
        saveProducts(subject.getId(), param.getProductIds());
        return CommonResult.success(subject.getId());
    }

    @PostMapping("/update/{id}")
    @Transactional
    public CommonResult<Void> update(@PathVariable Long id, @RequestBody SubjectParam param) {
        Subject subject = new Subject();
        BeanUtils.copyProperties(param, subject);
        subject.setId(id);
        subjectMapper.updateById(subject);
        linkMapper.deleteSubjectProducts(id);
        saveProducts(id, param.getProductIds());
        return CommonResult.success();
    }

    private void saveProducts(Long subjectId, List<Long> productIds) {
        if (productIds != null) {
            for (Long productId : productIds) {
                linkMapper.insertSubjectProduct(subjectId, productId);
            }
        }
    }

    @PostMapping("/delete")
    @Transactional
    public CommonResult<Void> delete(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            linkMapper.deleteSubjectProducts(id);
            subjectMapper.deleteById(id);
        }
        return CommonResult.success();
    }
}
