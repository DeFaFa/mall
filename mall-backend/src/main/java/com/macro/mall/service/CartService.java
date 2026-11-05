package com.macro.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macro.mall.common.BizException;
import com.macro.mall.dto.CartAddParam;
import com.macro.mall.dto.CartItemView;
import com.macro.mall.entity.*;
import com.macro.mall.mapper.CartItemMapper;
import com.macro.mall.mapper.MemberMapper;
import com.macro.mall.mapper.ProductMapper;
import com.macro.mall.mapper.SkuStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final SkuStockMapper skuStockMapper;
    private final MemberMapper memberMapper;
    private final PriceService priceService;

    public List<CartItemView> list(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        List<CartItem> carts = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getMemberId, memberId).orderByDesc(CartItem::getId));
        List<CartItemView> result = new ArrayList<>();
        for (CartItem cart : carts) {
            Product product = productMapper.selectById(cart.getProductId());
            SkuStock sku = skuStockMapper.selectById(cart.getSkuId());
            if (product == null || product.getDeleteStatus() == 1 || sku == null) {
                continue;
            }
            result.add(toView(cart, product, sku, member));
        }
        return result;
    }

    public void add(Long memberId, CartAddParam param) {
        if (param.getProductId() == null || param.getSkuId() == null) {
            throw new BizException("请选择商品规格");
        }
        int quantity = param.getQuantity() == null || param.getQuantity() < 1 ? 1 : param.getQuantity();
        Product product = productMapper.selectById(param.getProductId());
        if (product == null || product.getDeleteStatus() == 1 || product.getPublishStatus() != 1) {
            throw new BizException("商品已下架");
        }
        SkuStock sku = skuStockMapper.selectById(param.getSkuId());
        if (sku == null || !sku.getProductId().equals(product.getId())) {
            throw new BizException("SKU 不存在");
        }
        CartItem existing = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getMemberId, memberId)
                .eq(CartItem::getProductId, param.getProductId())
                .eq(CartItem::getSkuId, param.getSkuId())
                .last("limit 1"));
        int target = quantity + (existing == null ? 0 : existing.getQuantity());
        if (target > sku.getStock()) {
            throw new BizException("库存不足");
        }
        if (existing == null) {
            CartItem cart = new CartItem();
            cart.setMemberId(memberId);
            cart.setProductId(param.getProductId());
            cart.setSkuId(param.getSkuId());
            cart.setQuantity(quantity);
            cartItemMapper.insert(cart);
        } else {
            existing.setQuantity(target);
            cartItemMapper.updateById(existing);
        }
    }

    public void updateQuantity(Long memberId, Long cartId, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new BizException("数量必须大于 0");
        }
        CartItem cart = ownedCart(memberId, cartId);
        SkuStock sku = skuStockMapper.selectById(cart.getSkuId());
        if (sku != null && quantity > sku.getStock()) {
            throw new BizException("库存不足");
        }
        cart.setQuantity(quantity);
        cartItemMapper.updateById(cart);
    }

    public void updateSku(Long memberId, Long cartId, Long skuId) {
        CartItem cart = ownedCart(memberId, cartId);
        SkuStock sku = skuStockMapper.selectById(skuId);
        if (sku == null || !sku.getProductId().equals(cart.getProductId())) {
            throw new BizException("规格不存在");
        }
        CartItem same = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getMemberId, memberId)
                .eq(CartItem::getProductId, cart.getProductId())
                .eq(CartItem::getSkuId, skuId)
                .ne(CartItem::getId, cartId)
                .last("limit 1"));
        if (same != null) {
            same.setQuantity(same.getQuantity() + cart.getQuantity());
            cartItemMapper.updateById(same);
            cartItemMapper.deleteById(cartId);
        } else {
            cart.setSkuId(skuId);
            cartItemMapper.updateById(cart);
        }
    }

    public void delete(Long memberId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getMemberId, memberId).in(CartItem::getId, ids));
    }

    public void clear(Long memberId) {
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>().eq(CartItem::getMemberId, memberId));
    }

    private CartItem ownedCart(Long memberId, Long cartId) {
        CartItem cart = cartItemMapper.selectById(cartId);
        if (cart == null || !cart.getMemberId().equals(memberId)) {
            throw new BizException("购物车记录不存在");
        }
        return cart;
    }

    private CartItemView toView(CartItem cart, Product product, SkuStock sku, Member member) {
        CartItemView view = new CartItemView();
        view.setId(cart.getId());
        view.setProductId(product.getId());
        view.setProductCategoryId(product.getProductCategoryId());
        view.setSkuId(sku.getId());
        view.setQuantity(cart.getQuantity());
        view.setProductName(product.getName());
        view.setSubTitle(product.getSubTitle());
        view.setPic(sku.getPic() == null || sku.getPic().isBlank() ? product.getPic() : sku.getPic());
        view.setSkuCode(sku.getSkuCode());
        view.setSpData(sku.getSpData());
        view.setStock(sku.getStock());
        view.setOriginalPrice(sku.getPrice());
        Long levelId = member == null ? null : member.getMemberLevelId();
        BigDecimal unitPrice = priceService.unitPrice(product, sku, cart.getQuantity(), levelId);
        view.setPrice(unitPrice);
        BigDecimal original = sku.getPrice() == null ? BigDecimal.ZERO : sku.getPrice();
        view.setPromotionAmount(original.subtract(unitPrice).multiply(BigDecimal.valueOf(cart.getQuantity())));
        return view;
    }
}
