package com.macro.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macro.mall.common.CommonPage;
import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.*;
import com.macro.mall.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/flashPromotion")
@RequiredArgsConstructor
public class SmsFlashController {
    private final FlashPromotionMapper promotionMapper;
    private final FlashPromotionSessionMapper sessionMapper;
    private final FlashPromotionProductRelationMapper relationMapper;
    private final ProductMapper productMapper;

    @GetMapping("/list")
    public CommonResult<List<FlashPromotion>> list() {
        return CommonResult.success(promotionMapper.selectList(new LambdaQueryWrapper<FlashPromotion>()
                .orderByDesc(FlashPromotion::getId)));
    }

    @GetMapping("/{id}")
    public CommonResult<FlashPromotion> detail(@PathVariable Long id) {
        return CommonResult.success(promotionMapper.selectById(id));
    }

    @PostMapping("/create")
    public CommonResult<Void> create(@RequestBody FlashPromotion promotion) {
        if (promotion.getStatus() == null) {
            promotion.setStatus(1);
        }
        promotionMapper.insert(promotion);
        return CommonResult.success();
    }

    @PostMapping("/update")
    public CommonResult<Void> update(@RequestBody FlashPromotion promotion) {
        promotionMapper.updateById(promotion);
        return CommonResult.success();
    }

    @PostMapping("/delete/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        relationMapper.delete(new LambdaQueryWrapper<FlashPromotionProductRelation>()
                .eq(FlashPromotionProductRelation::getFlashPromotionId, id));
        promotionMapper.deleteById(id);
        return CommonResult.success();
    }

    // ---------------- 场次 ----------------
    @GetMapping("/session/list")
    public CommonResult<List<FlashPromotionSession>> sessionList() {
        return CommonResult.success(sessionMapper.selectList(new LambdaQueryWrapper<FlashPromotionSession>()
                .orderByAsc(FlashPromotionSession::getStartTime)));
    }

    @PostMapping("/session/create")
    public CommonResult<Void> createSession(@RequestBody FlashPromotionSession session) {
        if (session.getStatus() == null) {
            session.setStatus(1);
        }
        sessionMapper.insert(session);
        return CommonResult.success();
    }

    @PostMapping("/session/update")
    public CommonResult<Void> updateSession(@RequestBody FlashPromotionSession session) {
        sessionMapper.updateById(session);
        return CommonResult.success();
    }

    @PostMapping("/session/delete/{id}")
    public CommonResult<Void> deleteSession(@PathVariable Long id) {
        relationMapper.delete(new LambdaQueryWrapper<FlashPromotionProductRelation>()
                .eq(FlashPromotionProductRelation::getFlashPromotionSessionId, id));
        sessionMapper.deleteById(id);
        return CommonResult.success();
    }

    // ---------------- 秒杀商品 ----------------
    @GetMapping("/product/list")
    public CommonResult<CommonPage<FlashPromotionProductRelation>> productList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long flashPromotionId,
            @RequestParam(required = false) Long flashPromotionSessionId) {
        Page<FlashPromotionProductRelation> page = relationMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<FlashPromotionProductRelation>()
                        .eq(flashPromotionId != null, FlashPromotionProductRelation::getFlashPromotionId, flashPromotionId)
                        .eq(flashPromotionSessionId != null,
                                FlashPromotionProductRelation::getFlashPromotionSessionId, flashPromotionSessionId)
                        .orderByAsc(FlashPromotionProductRelation::getSort));
        Map<Long, String> names = page.getRecords().isEmpty() ? Map.of()
                : productMapper.selectBatchIds(page.getRecords().stream()
                        .map(FlashPromotionProductRelation::getProductId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Product::getId, Product::getName));
        page.getRecords().forEach(r -> r.setProductName(names.getOrDefault(r.getProductId(), "")));
        return CommonResult.success(CommonPage.of(page));
    }

    @PostMapping("/product/create")
    public CommonResult<Void> createProduct(@RequestBody FlashPromotionProductRelation relation) {
        relationMapper.insert(relation);
        return CommonResult.success();
    }

    @PostMapping("/product/update")
    public CommonResult<Void> updateProduct(@RequestBody FlashPromotionProductRelation relation) {
        relationMapper.updateById(relation);
        return CommonResult.success();
    }

    @PostMapping("/product/delete")
    public CommonResult<Void> deleteProduct(@RequestBody List<Long> ids) {
        relationMapper.deleteBatchIds(ids);
        return CommonResult.success();
    }
}
