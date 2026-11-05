package com.macro.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.macro.mall.auth.TokenService;
import com.macro.mall.common.BizException;
import com.macro.mall.dto.MemberInfo;
import com.macro.mall.dto.RegisterParam;
import com.macro.mall.entity.*;
import com.macro.mall.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberMapper memberMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final MemberReceiveAddressMapper addressMapper;
    private final MemberProductCollectionMapper collectionMapper;
    private final MemberBrandAttentionMapper attentionMapper;
    private final MemberBrowseHistoryMapper historyMapper;
    private final CouponHistoryMapper couponHistoryMapper;
    private final ProductMapper productMapper;
    private final BrandMapper brandMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public Long register(RegisterParam param) {
        if (param.getUsername() == null || param.getUsername().isBlank()) {
            throw new BizException("账号不能为空");
        }
        if (param.getPassword() == null || param.getPassword().length() < 6) {
            throw new BizException("密码至少 6 位");
        }
        long exists = memberMapper.selectCount(new LambdaQueryWrapper<Member>()
                .eq(Member::getUsername, param.getUsername()));
        if (exists > 0) {
            throw new BizException("账号已存在");
        }
        Member member = new Member();
        member.setUsername(param.getUsername());
        member.setPhone(param.getPhone() == null || param.getPhone().isBlank() ? param.getUsername() : param.getPhone());
        member.setNickname(param.getNickname() == null || param.getNickname().isBlank()
                ? "用户" + param.getUsername() : param.getNickname());
        member.setPassword(passwordEncoder.encode(param.getPassword()));
        MemberLevel defaultLevel = memberLevelMapper.selectOne(new LambdaQueryWrapper<MemberLevel>()
                .eq(MemberLevel::getDefaultStatus, 1).last("limit 1"));
        member.setMemberLevelId(defaultLevel == null ? 1L : defaultLevel.getId());
        member.setStatus(1);
        member.setGender(0);
        member.setIntegration(0);
        member.setGrowth(0);
        member.setSource(1);
        member.setCreateTime(LocalDateTime.now());
        memberMapper.insert(member);
        return member.getId();
    }

    public Map<String, Object> login(String username, String password) {
        Member member = memberMapper.selectOne(new LambdaQueryWrapper<Member>()
                .and(w -> w.eq(Member::getUsername, username).or().eq(Member::getPhone, username))
                .last("limit 1"));
        if (member == null || !passwordEncoder.matches(password, member.getPassword())) {
            throw new BizException("账号或密码错误");
        }
        if (member.getStatus() == null || member.getStatus() != 1) {
            throw new BizException("账号已被禁用");
        }
        String token = tokenService.createMemberToken(member.getId());
        return Map.of("token", token, "member", member);
    }

    public void logout(String token) {
        tokenService.removeMemberToken(token);
    }

    public MemberInfo info(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException("会员不存在");
        }
        MemberInfo info = new MemberInfo();
        info.setMember(member);
        info.setLevel(member.getMemberLevelId() == null ? null : memberLevelMapper.selectById(member.getMemberLevelId()));
        info.setAddressList(addressMapper.selectList(new LambdaQueryWrapper<MemberReceiveAddress>()
                .eq(MemberReceiveAddress::getMemberId, memberId).orderByDesc(MemberReceiveAddress::getDefaultStatus)));
        info.setCollectionCount(collectionMapper.selectCount(new LambdaQueryWrapper<MemberProductCollection>()
                .eq(MemberProductCollection::getMemberId, memberId)));
        info.setAttentionCount(attentionMapper.selectCount(new LambdaQueryWrapper<MemberBrandAttention>()
                .eq(MemberBrandAttention::getMemberId, memberId)));
        info.setHistoryCount(historyMapper.selectCount(new LambdaQueryWrapper<MemberBrowseHistory>()
                .eq(MemberBrowseHistory::getMemberId, memberId)));
        info.setCouponCount(couponHistoryMapper.selectCount(new LambdaQueryWrapper<CouponHistory>()
                .eq(CouponHistory::getMemberId, memberId)));
        info.setUnusedCouponCount(couponHistoryMapper.selectCount(new LambdaQueryWrapper<CouponHistory>()
                .eq(CouponHistory::getMemberId, memberId).eq(CouponHistory::getUseStatus, 0)));
        return info;
    }

    public void updateProfile(Long memberId, Member param) {
        Member update = new Member();
        update.setId(memberId);
        update.setNickname(param.getNickname());
        update.setPhone(param.getPhone());
        update.setIcon(param.getIcon());
        update.setGender(param.getGender());
        update.setBirthday(param.getBirthday());
        update.setCity(param.getCity());
        memberMapper.updateById(update);
    }

    // ---------------- 收货地址 ----------------
    public List<MemberReceiveAddress> addresses(Long memberId) {
        return addressMapper.selectList(new LambdaQueryWrapper<MemberReceiveAddress>()
                .eq(MemberReceiveAddress::getMemberId, memberId)
                .orderByDesc(MemberReceiveAddress::getDefaultStatus)
                .orderByDesc(MemberReceiveAddress::getId));
    }

    public void addAddress(Long memberId, MemberReceiveAddress address) {
        validateAddress(address);
        address.setId(null);
        address.setMemberId(memberId);
        if (address.getDefaultStatus() != null && address.getDefaultStatus() == 1) {
            clearDefault(memberId);
        } else {
            address.setDefaultStatus(0);
        }
        addressMapper.insert(address);
    }

    public void updateAddress(Long memberId, MemberReceiveAddress address) {
        MemberReceiveAddress existing = addressMapper.selectById(address.getId());
        if (existing == null || !existing.getMemberId().equals(memberId)) {
            throw new BizException("收货地址不存在");
        }
        validateAddress(address);
        address.setMemberId(memberId);
        if (address.getDefaultStatus() != null && address.getDefaultStatus() == 1) {
            clearDefault(memberId);
        }
        addressMapper.updateById(address);
    }

    public void deleteAddress(Long memberId, Long id) {
        addressMapper.delete(new LambdaQueryWrapper<MemberReceiveAddress>()
                .eq(MemberReceiveAddress::getId, id).eq(MemberReceiveAddress::getMemberId, memberId));
    }

    private void clearDefault(Long memberId) {
        addressMapper.update(null, new LambdaUpdateWrapper<MemberReceiveAddress>()
                .eq(MemberReceiveAddress::getMemberId, memberId)
                .eq(MemberReceiveAddress::getDefaultStatus, 1)
                .set(MemberReceiveAddress::getDefaultStatus, 0));
    }

    private void validateAddress(MemberReceiveAddress address) {
        if (address.getName() == null || address.getName().isBlank()
                || address.getPhone() == null || address.getPhone().isBlank()
                || address.getDetailAddress() == null || address.getDetailAddress().isBlank()) {
            throw new BizException("请完整填写收货信息");
        }
    }

    // ---------------- 收藏 ----------------
    public List<MemberProductCollection> collections(Long memberId) {
        return collectionMapper.selectList(new LambdaQueryWrapper<MemberProductCollection>()
                .eq(MemberProductCollection::getMemberId, memberId).orderByDesc(MemberProductCollection::getId));
    }

    public void addCollection(Long memberId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleteStatus() == 1) {
            throw new BizException("商品不存在");
        }
        long exists = collectionMapper.selectCount(new LambdaQueryWrapper<MemberProductCollection>()
                .eq(MemberProductCollection::getMemberId, memberId)
                .eq(MemberProductCollection::getProductId, productId));
        if (exists > 0) {
            throw new BizException("已经收藏过该商品");
        }
        MemberProductCollection collection = new MemberProductCollection();
        collection.setMemberId(memberId);
        collection.setProductId(productId);
        collection.setProductName(product.getName());
        collection.setProductPic(product.getPic());
        collection.setProductPrice(product.getPrice());
        collection.setProductSubTitle(product.getSubTitle());
        collection.setCreateTime(LocalDateTime.now());
        collectionMapper.insert(collection);
    }

    public void deleteCollection(Long memberId, Long productId) {
        collectionMapper.delete(new LambdaQueryWrapper<MemberProductCollection>()
                .eq(MemberProductCollection::getMemberId, memberId)
                .eq(MemberProductCollection::getProductId, productId));
    }

    // ---------------- 品牌关注 ----------------
    public List<MemberBrandAttention> attentions(Long memberId) {
        return attentionMapper.selectList(new LambdaQueryWrapper<MemberBrandAttention>()
                .eq(MemberBrandAttention::getMemberId, memberId).orderByDesc(MemberBrandAttention::getId));
    }

    public void addAttention(Long memberId, Long brandId) {
        Brand brand = brandMapper.selectById(brandId);
        if (brand == null) {
            throw new BizException("品牌不存在");
        }
        long exists = attentionMapper.selectCount(new LambdaQueryWrapper<MemberBrandAttention>()
                .eq(MemberBrandAttention::getMemberId, memberId)
                .eq(MemberBrandAttention::getBrandId, brandId));
        if (exists > 0) {
            throw new BizException("已经关注过该品牌");
        }
        MemberBrandAttention attention = new MemberBrandAttention();
        attention.setMemberId(memberId);
        attention.setBrandId(brandId);
        attention.setBrandName(brand.getName());
        attention.setCreateTime(LocalDateTime.now());
        attentionMapper.insert(attention);
    }

    public void deleteAttention(Long memberId, Long brandId) {
        attentionMapper.delete(new LambdaQueryWrapper<MemberBrandAttention>()
                .eq(MemberBrandAttention::getMemberId, memberId)
                .eq(MemberBrandAttention::getBrandId, brandId));
    }

    // ---------------- 浏览足迹 ----------------
    public List<MemberBrowseHistory> histories(Long memberId) {
        return historyMapper.selectList(new LambdaQueryWrapper<MemberBrowseHistory>()
                .eq(MemberBrowseHistory::getMemberId, memberId)
                .orderByDesc(MemberBrowseHistory::getId).last("limit 100"));
    }

    public void recordHistory(Long memberId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleteStatus() == 1) {
            return;
        }
        historyMapper.delete(new LambdaQueryWrapper<MemberBrowseHistory>()
                .eq(MemberBrowseHistory::getMemberId, memberId)
                .eq(MemberBrowseHistory::getProductId, productId));
        MemberBrowseHistory history = new MemberBrowseHistory();
        history.setMemberId(memberId);
        history.setProductId(productId);
        history.setProductName(product.getName());
        history.setProductPic(product.getPic());
        history.setProductPrice(product.getPrice());
        history.setCreateTime(LocalDateTime.now());
        historyMapper.insert(history);
    }

    public void clearHistories(Long memberId) {
        historyMapper.delete(new LambdaQueryWrapper<MemberBrowseHistory>()
                .eq(MemberBrowseHistory::getMemberId, memberId));
    }
}
