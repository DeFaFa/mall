package com.macro.mall.mapper;

import com.macro.mall.entity.Resource;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 关联表操作：商品子表、角色关系、内容关联、优惠券范围关系。
 */
public interface LinkMapper {

    // ---------- RBAC ----------
    @Select("SELECT role_id FROM ums_admin_role_relation WHERE admin_id = #{adminId}")
    List<Long> selectRoleIdsByAdmin(@Param("adminId") Long adminId);

    @Select("SELECT r.* FROM ums_resource r " +
            "JOIN ums_role_resource_relation rr ON rr.resource_id = r.id " +
            "JOIN ums_admin_role_relation ar ON ar.role_id = rr.role_id " +
            "WHERE ar.admin_id = #{adminId} ORDER BY r.id")
    List<Resource> selectResourcesByAdmin(@Param("adminId") Long adminId);

    @Select("SELECT menu_id FROM ums_role_menu_relation WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRole(@Param("roleId") Long roleId);

    @Select("SELECT resource_id FROM ums_role_resource_relation WHERE role_id = #{roleId}")
    List<Long> selectResourceIdsByRole(@Param("roleId") Long roleId);

    @Select("SELECT * FROM ums_resource ORDER BY id")
    List<Resource> selectAllResources();

    @Delete("DELETE FROM ums_admin_role_relation WHERE admin_id = #{adminId}")
    int deleteAdminRoles(@Param("adminId") Long adminId);

    @Insert("INSERT INTO ums_admin_role_relation(admin_id, role_id) VALUES(#{adminId}, #{roleId})")
    int insertAdminRole(@Param("adminId") Long adminId, @Param("roleId") Long roleId);

    @Delete("DELETE FROM ums_role_menu_relation WHERE role_id = #{roleId}")
    int deleteRoleMenus(@Param("roleId") Long roleId);

    @Insert("INSERT INTO ums_role_menu_relation(role_id, menu_id) VALUES(#{roleId}, #{menuId})")
    int insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    @Delete("DELETE FROM ums_role_resource_relation WHERE role_id = #{roleId}")
    int deleteRoleResources(@Param("roleId") Long roleId);

    @Insert("INSERT INTO ums_role_resource_relation(role_id, resource_id) VALUES(#{roleId}, #{resourceId})")
    int insertRoleResource(@Param("roleId") Long roleId, @Param("resourceId") Long resourceId);

    @Delete("DELETE FROM ums_admin_role_relation WHERE role_id = #{roleId}")
    int deleteAdminRolesByRole(@Param("roleId") Long roleId);

    @Delete("DELETE FROM ums_role_menu_relation WHERE menu_id = #{menuId}")
    int deleteRoleMenusByMenu(@Param("menuId") Long menuId);

    @Delete("DELETE FROM ums_role_resource_relation WHERE resource_id = #{resourceId}")
    int deleteRoleResourcesByResource(@Param("resourceId") Long resourceId);

    // ---------- 商品子表 ----------
    @Delete("DELETE FROM pms_product_attribute_value WHERE product_id = #{productId}")
    int deleteAttributeValues(@Param("productId") Long productId);

    @Delete("DELETE FROM pms_sku_stock WHERE product_id = #{productId}")
    int deleteSkus(@Param("productId") Long productId);

    @Delete("DELETE FROM pms_member_price WHERE product_id = #{productId}")
    int deleteMemberPrices(@Param("productId") Long productId);

    @Delete("DELETE FROM pms_product_ladder WHERE product_id = #{productId}")
    int deleteLadders(@Param("productId") Long productId);

    @Delete("DELETE FROM pms_product_full_reduction WHERE product_id = #{productId}")
    int deleteFullReductions(@Param("productId") Long productId);

    // ---------- 内容关联 ----------
    @Delete("DELETE FROM cms_subject_product_relation WHERE subject_id = #{subjectId}")
    int deleteSubjectProducts(@Param("subjectId") Long subjectId);

    @Insert("INSERT INTO cms_subject_product_relation(subject_id, product_id) VALUES(#{subjectId}, #{productId})")
    int insertSubjectProduct(@Param("subjectId") Long subjectId, @Param("productId") Long productId);

    @Select("SELECT product_id FROM cms_subject_product_relation WHERE subject_id = #{subjectId}")
    List<Long> selectProductIdsBySubject(@Param("subjectId") Long subjectId);

    @Delete("DELETE FROM cms_prefrence_area_product_relation WHERE prefrence_area_id = #{areaId}")
    int deletePrefrenceProducts(@Param("areaId") Long areaId);

    @Insert("INSERT INTO cms_prefrence_area_product_relation(prefrence_area_id, product_id) VALUES(#{areaId}, #{productId})")
    int insertPrefrenceProduct(@Param("areaId") Long areaId, @Param("productId") Long productId);

    @Select("SELECT product_id FROM cms_prefrence_area_product_relation WHERE prefrence_area_id = #{areaId}")
    List<Long> selectProductIdsByPrefrenceArea(@Param("areaId") Long areaId);

    @Select("SELECT subject_id FROM cms_subject_product_relation WHERE product_id = #{productId}")
    List<Long> selectSubjectIdsByProduct(@Param("productId") Long productId);

    @Delete("DELETE FROM cms_subject_product_relation WHERE product_id = #{productId}")
    int deleteSubjectProductsByProduct(@Param("productId") Long productId);

    @Select("SELECT prefrence_area_id FROM cms_prefrence_area_product_relation WHERE product_id = #{productId}")
    List<Long> selectPrefrenceAreaIdsByProduct(@Param("productId") Long productId);

    @Delete("DELETE FROM cms_prefrence_area_product_relation WHERE product_id = #{productId}")
    int deletePrefrenceProductsByProduct(@Param("productId") Long productId);

    // ---------- 优惠券范围 ----------
    @Delete("DELETE FROM sms_coupon_product_relation WHERE coupon_id = #{couponId}")
    int deleteCouponProducts(@Param("couponId") Long couponId);

    @Insert("INSERT INTO sms_coupon_product_relation(coupon_id, product_id, product_name, product_sn) " +
            "VALUES(#{couponId}, #{productId}, #{productName}, #{productSn})")
    int insertCouponProduct(@Param("couponId") Long couponId, @Param("productId") Long productId,
                            @Param("productName") String productName, @Param("productSn") String productSn);

    @Delete("DELETE FROM sms_coupon_product_category_relation WHERE coupon_id = #{couponId}")
    int deleteCouponCategories(@Param("couponId") Long couponId);

    @Insert("INSERT INTO sms_coupon_product_category_relation(coupon_id, product_category_id, product_category_name, " +
            "parent_category_name, parent_category_id) VALUES(#{couponId}, #{categoryId}, #{categoryName}, " +
            "#{parentName}, #{parentId})")
    int insertCouponCategory(@Param("couponId") Long couponId, @Param("categoryId") Long categoryId,
                             @Param("categoryName") String categoryName, @Param("parentName") String parentName,
                             @Param("parentId") Long parentId);

    @Select("SELECT product_id FROM sms_coupon_product_relation WHERE coupon_id = #{couponId}")
    List<Long> selectProductIdsByCoupon(@Param("couponId") Long couponId);

    @Select("SELECT product_category_id FROM sms_coupon_product_category_relation WHERE coupon_id = #{couponId}")
    List<Long> selectCategoryIdsByCoupon(@Param("couponId") Long couponId);

    @Select("SELECT c.* FROM sms_coupon c WHERE c.use_type = 2 AND EXISTS (" +
            "SELECT 1 FROM sms_coupon_product_relation r WHERE r.coupon_id = c.id AND r.product_id = #{productId})")
    List<com.macro.mall.entity.Coupon> selectProductCoupons(@Param("productId") Long productId);

    // ---------- 首页推荐关系清理 ----------
    @Delete("DELETE FROM sms_home_brand WHERE brand_id = #{brandId}")
    int deleteHomeBrandByBrand(@Param("brandId") Long brandId);

    @Delete("DELETE FROM sms_home_new_product WHERE product_id = #{productId}")
    int deleteHomeNewByProduct(@Param("productId") Long productId);

    @Delete("DELETE FROM sms_home_recommend_product WHERE product_id = #{productId}")
    int deleteHomeRecommendByProduct(@Param("productId") Long productId);

    @Delete("DELETE FROM sms_flash_promotion_product_relation WHERE product_id = #{productId}")
    int deleteFlashProductByProduct(@Param("productId") Long productId);
}
