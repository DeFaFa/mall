
CREATE DATABASE IF NOT EXISTS mall_vue DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE mall_vue;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS pms_brand;
DROP TABLE IF EXISTS pms_product_category;
DROP TABLE IF EXISTS pms_product_attribute_category;
DROP TABLE IF EXISTS pms_product_attribute;
DROP TABLE IF EXISTS pms_product;
DROP TABLE IF EXISTS pms_product_attribute_value;
DROP TABLE IF EXISTS pms_sku_stock;
DROP TABLE IF EXISTS pms_member_price;
DROP TABLE IF EXISTS pms_product_ladder;
DROP TABLE IF EXISTS pms_product_full_reduction;
DROP TABLE IF EXISTS cms_subject;
DROP TABLE IF EXISTS cms_prefrence_area;
DROP TABLE IF EXISTS cms_subject_product_relation;
DROP TABLE IF EXISTS cms_prefrence_area_product_relation;
DROP TABLE IF EXISTS oms_order;
DROP TABLE IF EXISTS oms_order_item;
DROP TABLE IF EXISTS oms_order_operate_history;
DROP TABLE IF EXISTS oms_order_setting;
DROP TABLE IF EXISTS oms_cart_item;
DROP TABLE IF EXISTS oms_order_return_apply;
DROP TABLE IF EXISTS oms_order_return_reason;
DROP TABLE IF EXISTS sms_coupon;
DROP TABLE IF EXISTS sms_coupon_history;
DROP TABLE IF EXISTS sms_coupon_product_relation;
DROP TABLE IF EXISTS sms_coupon_product_category_relation;
DROP TABLE IF EXISTS sms_flash_promotion;
DROP TABLE IF EXISTS sms_flash_promotion_session;
DROP TABLE IF EXISTS sms_flash_promotion_product_relation;
DROP TABLE IF EXISTS sms_home_advertise;
DROP TABLE IF EXISTS sms_home_brand;
DROP TABLE IF EXISTS sms_home_new_product;
DROP TABLE IF EXISTS sms_home_recommend_product;
DROP TABLE IF EXISTS ums_admin;
DROP TABLE IF EXISTS ums_role;
DROP TABLE IF EXISTS ums_menu;
DROP TABLE IF EXISTS ums_resource;
DROP TABLE IF EXISTS ums_admin_role_relation;
DROP TABLE IF EXISTS ums_role_menu_relation;
DROP TABLE IF EXISTS ums_role_resource_relation;
DROP TABLE IF EXISTS ums_member;
DROP TABLE IF EXISTS ums_member_level;
DROP TABLE IF EXISTS ums_member_receive_address;
DROP TABLE IF EXISTS ums_member_product_collection;
DROP TABLE IF EXISTS ums_member_brand_attention;
DROP TABLE IF EXISTS ums_member_browse_history;

-- ---------------- 商品 ----------------
CREATE TABLE pms_brand (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  first_letter VARCHAR(8) DEFAULT '',
  sort INT DEFAULT 0,
  factory_status TINYINT DEFAULT 0 COMMENT '厂家制造商状态',
  show_status TINYINT DEFAULT 1 COMMENT '显示状态',
  logo VARCHAR(255) DEFAULT '',
  big_pic VARCHAR(255) DEFAULT '',
  brand_story VARCHAR(1024) DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='品牌';

CREATE TABLE pms_product_category (
  id BIGINT NOT NULL AUTO_INCREMENT,
  parent_id BIGINT DEFAULT 0,
  name VARCHAR(64) NOT NULL,
  level INT DEFAULT 0,
  product_count INT DEFAULT 0,
  product_unit VARCHAR(64) DEFAULT '件',
  nav_status TINYINT DEFAULT 1 COMMENT '导航栏显示',
  show_status TINYINT DEFAULT 1,
  sort INT DEFAULT 0,
  icon VARCHAR(255) DEFAULT '',
  keywords VARCHAR(255) DEFAULT '',
  description VARCHAR(255) DEFAULT '',
  PRIMARY KEY (id),
  KEY idx_parent (parent_id)
) ENGINE=InnoDB COMMENT='商品分类';

CREATE TABLE pms_product_attribute_category (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  product_category_id BIGINT NOT NULL COMMENT '关联商品分类',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='属性分类';

CREATE TABLE pms_product_attribute (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_attribute_category_id BIGINT NOT NULL,
  name VARCHAR(64) NOT NULL,
  type TINYINT DEFAULT 0 COMMENT '0规格 1参数',
  input_type TINYINT DEFAULT 1,
  input_list VARCHAR(255) DEFAULT '',
  sort INT DEFAULT 0,
  filter_type TINYINT DEFAULT 0,
  search_type TINYINT DEFAULT 0,
  related_status TINYINT DEFAULT 0,
  hand_add_status TINYINT DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_ac (product_attribute_category_id)
) ENGINE=InnoDB COMMENT='商品属性';

CREATE TABLE pms_product (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_category_id BIGINT NOT NULL,
  brand_id BIGINT DEFAULT 0,
  name VARCHAR(200) NOT NULL,
  sub_title VARCHAR(255) DEFAULT '',
  description TEXT,
  product_sn VARCHAR(64) DEFAULT '',
  price DECIMAL(10,2) DEFAULT 0,
  original_price DECIMAL(10,2) DEFAULT 0,
  stock INT DEFAULT 0,
  low_stock INT DEFAULT 0,
  pic VARCHAR(255) DEFAULT '',
  album_pics VARCHAR(1000) DEFAULT '',
  unit VARCHAR(32) DEFAULT '件',
  weight DECIMAL(10,2) DEFAULT 0,
  sort INT DEFAULT 0,
  sale INT DEFAULT 0,
  publish_status TINYINT DEFAULT 1,
  new_status TINYINT DEFAULT 1,
  recommand_status TINYINT DEFAULT 0,
  verify_status TINYINT DEFAULT 1,
  delete_status TINYINT DEFAULT 0,
  keywords VARCHAR(255) DEFAULT '',
  detail_title VARCHAR(255) DEFAULT '',
  detail_desc TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_category (product_category_id),
  KEY idx_brand (brand_id)
) ENGINE=InnoDB COMMENT='商品';

CREATE TABLE pms_product_attribute_value (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  product_attribute_id BIGINT NOT NULL,
  value VARCHAR(255) DEFAULT '',
  PRIMARY KEY (id),
  KEY idx_product (product_id)
) ENGINE=InnoDB COMMENT='商品属性值';

CREATE TABLE pms_sku_stock (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  sku_code VARCHAR(64) NOT NULL,
  price DECIMAL(10,2) DEFAULT 0,
  stock INT DEFAULT 0,
  lock_stock INT DEFAULT 0,
  low_stock INT DEFAULT 0,
  pic VARCHAR(255) DEFAULT '',
  sale INT DEFAULT 0,
  sp_data VARCHAR(500) DEFAULT '{}' COMMENT '规格 JSON',
  PRIMARY KEY (id),
  KEY idx_product (product_id),
  UNIQUE KEY uk_sku_code (sku_code)
) ENGINE=InnoDB COMMENT='SKU 库存';

CREATE TABLE pms_member_price (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  member_level_id BIGINT NOT NULL,
  member_price DECIMAL(10,2) DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_product (product_id)
) ENGINE=InnoDB COMMENT='会员价';

CREATE TABLE pms_product_ladder (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  member_level_id BIGINT DEFAULT 0,
  count INT DEFAULT 0,
  price DECIMAL(10,2) DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_product (product_id)
) ENGINE=InnoDB COMMENT='阶梯价';

CREATE TABLE pms_product_full_reduction (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  full_price DECIMAL(10,2) DEFAULT 0,
  reduce_price DECIMAL(10,2) DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_product (product_id)
) ENGINE=InnoDB COMMENT='满减规则';

-- ---------------- 内容 ----------------
CREATE TABLE cms_subject (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  sub_title VARCHAR(255) DEFAULT '',
  category_id BIGINT DEFAULT 0,
  pic VARCHAR(255) DEFAULT '',
  main_title VARCHAR(255) DEFAULT '',
  description TEXT,
  show_status TINYINT DEFAULT 1,
  sort INT DEFAULT 0,
  views INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='专题';

CREATE TABLE cms_prefrence_area (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  pic VARCHAR(255) DEFAULT '',
  sort INT DEFAULT 0,
  sub_title VARCHAR(255) DEFAULT '',
  show_status TINYINT DEFAULT 1,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='优选专区';

CREATE TABLE cms_subject_product_relation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  subject_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  KEY idx_subject (subject_id)
) ENGINE=InnoDB;

CREATE TABLE cms_prefrence_area_product_relation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  prefrence_area_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ---------------- 订单 ----------------
CREATE TABLE oms_order (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_sn VARCHAR(64) NOT NULL,
  member_id BIGINT NOT NULL,
  member_username VARCHAR(64) DEFAULT '',
  coupon_id BIGINT DEFAULT 0,
  order_type TINYINT DEFAULT 0,
  total_amount DECIMAL(10,2) DEFAULT 0,
  pay_amount DECIMAL(10,2) DEFAULT 0,
  freight_amount DECIMAL(10,2) DEFAULT 0,
  promotion_amount DECIMAL(10,2) DEFAULT 0,
  coupon_amount DECIMAL(10,2) DEFAULT 0,
  integration_amount DECIMAL(10,2) DEFAULT 0,
  discount_amount DECIMAL(10,2) DEFAULT 0,
  pay_type TINYINT DEFAULT 0,
  source_type TINYINT DEFAULT 1,
  status TINYINT DEFAULT 0 COMMENT '0待付款1待发货2已发货3已完成4已关闭5无效',
  delivery_company VARCHAR(64) DEFAULT '',
  delivery_sn VARCHAR(64) DEFAULT '',
  receiver_name VARCHAR(64) DEFAULT '',
  receiver_phone VARCHAR(32) DEFAULT '',
  receiver_province VARCHAR(64) DEFAULT '',
  receiver_city VARCHAR(64) DEFAULT '',
  receiver_region VARCHAR(64) DEFAULT '',
  receiver_detail_address VARCHAR(255) DEFAULT '',
  note VARCHAR(255) DEFAULT '',
  confirm_status TINYINT DEFAULT 0,
  delete_status TINYINT DEFAULT 0,
  integration INT DEFAULT 0,
  growth INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  payment_time DATETIME DEFAULT NULL,
  delivery_time DATETIME DEFAULT NULL,
  receive_time DATETIME DEFAULT NULL,
  comment_time DATETIME DEFAULT NULL,
  modify_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_sn (order_sn),
  KEY idx_member (member_id),
  KEY idx_status (status)
) ENGINE=InnoDB COMMENT='订单';

CREATE TABLE oms_order_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  order_sn VARCHAR(64) DEFAULT '',
  product_id BIGINT NOT NULL,
  product_sku_id BIGINT DEFAULT 0,
  product_name VARCHAR(200) DEFAULT '',
  product_pic VARCHAR(255) DEFAULT '',
  product_price DECIMAL(10,2) DEFAULT 0,
  product_quantity INT DEFAULT 1,
  product_sku VARCHAR(255) DEFAULT '',
  product_attr VARCHAR(1000) DEFAULT '',
  real_amount DECIMAL(10,2) DEFAULT 0,
  promotion_amount DECIMAL(10,2) DEFAULT 0,
  coupon_amount DECIMAL(10,2) DEFAULT 0,
  integration_amount DECIMAL(10,2) DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_order (order_id)
) ENGINE=InnoDB COMMENT='订单商品';

CREATE TABLE oms_order_operate_history (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  operate_man VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  order_status TINYINT DEFAULT 0,
  note VARCHAR(255) DEFAULT '',
  PRIMARY KEY (id),
  KEY idx_order (order_id)
) ENGINE=InnoDB COMMENT='订单操作记录';

CREATE TABLE oms_order_setting (
  id BIGINT NOT NULL AUTO_INCREMENT,
  flash_order_overtime INT DEFAULT 1800 COMMENT '秒杀订单超时秒',
  normal_order_overtime INT DEFAULT 1800 COMMENT '普通订单超时秒',
  confirm_overtime INT DEFAULT 15 COMMENT '发货后自动收货天数',
  comment_overtime INT DEFAULT 7,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='订单设置';

CREATE TABLE oms_cart_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  quantity INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_member (member_id),
  KEY idx_psk (member_id, product_id, sku_id)
) ENGINE=InnoDB COMMENT='购物车';

CREATE TABLE oms_order_return_apply (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  order_sn VARCHAR(64) DEFAULT '',
  member_id BIGINT NOT NULL,
  return_name VARCHAR(64) DEFAULT '',
  return_phone VARCHAR(32) DEFAULT '',
  status TINYINT DEFAULT 0 COMMENT '0待处理1同意2拒绝3已完成',
  product_img VARCHAR(255) DEFAULT '',
  return_reason_id BIGINT DEFAULT 0,
  return_reason_name VARCHAR(100) DEFAULT '',
  description VARCHAR(500) DEFAULT '',
  handle_man VARCHAR(64) DEFAULT '',
  handle_time DATETIME DEFAULT NULL,
  handle_note VARCHAR(255) DEFAULT '',
  refund_amount DECIMAL(10,2) DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_order (order_id)
) ENGINE=InnoDB COMMENT='退货申请';

CREATE TABLE oms_order_return_reason (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  sort INT DEFAULT 0,
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='退货原因';

-- ---------------- 营销 ----------------
CREATE TABLE sms_coupon (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  type TINYINT DEFAULT 0 COMMENT '0满减1折扣',
  platform TINYINT DEFAULT 0,
  count INT DEFAULT 0,
  amount DECIMAL(10,2) DEFAULT 0,
  min_point DECIMAL(10,2) DEFAULT 0,
  per_limit INT DEFAULT 1,
  start_time DATETIME DEFAULT NULL,
  end_time DATETIME DEFAULT NULL,
  note VARCHAR(255) DEFAULT '',
  use_type TINYINT DEFAULT 0 COMMENT '0全场1指定分类2指定商品',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='优惠券';

CREATE TABLE sms_coupon_history (
  id BIGINT NOT NULL AUTO_INCREMENT,
  coupon_id BIGINT NOT NULL,
  member_id BIGINT NOT NULL,
  member_nickname VARCHAR(64) DEFAULT '',
  coupon_code VARCHAR(64) DEFAULT '',
  get_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  use_status TINYINT DEFAULT 0,
  use_time DATETIME DEFAULT NULL,
  order_id BIGINT DEFAULT 0,
  order_sn VARCHAR(64) DEFAULT '',
  PRIMARY KEY (id),
  KEY idx_member (member_id),
  KEY idx_coupon (coupon_id)
) ENGINE=InnoDB COMMENT='优惠券领取记录';

CREATE TABLE sms_coupon_product_relation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  coupon_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(200) DEFAULT '',
  product_sn VARCHAR(64) DEFAULT '',
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE sms_coupon_product_category_relation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  coupon_id BIGINT NOT NULL,
  product_category_id BIGINT NOT NULL,
  product_category_name VARCHAR(100) DEFAULT '',
  parent_category_name VARCHAR(100) DEFAULT '',
  parent_category_id BIGINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE sms_flash_promotion (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='秒杀活动';

CREATE TABLE sms_flash_promotion_session (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  start_time TIME DEFAULT NULL,
  end_time TIME DEFAULT NULL,
  status TINYINT DEFAULT 1,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='秒杀场次';

CREATE TABLE sms_flash_promotion_product_relation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  flash_promotion_id BIGINT NOT NULL,
  flash_promotion_session_id BIGINT DEFAULT 0,
  product_id BIGINT NOT NULL,
  flash_promotion_price DECIMAL(10,2) DEFAULT 0,
  flash_promotion_count INT DEFAULT 0,
  flash_promotion_limit INT DEFAULT 1,
  sort INT DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_product (product_id)
) ENGINE=InnoDB COMMENT='秒杀商品';

CREATE TABLE sms_home_advertise (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  type TINYINT DEFAULT 0 COMMENT '0轮播1推荐',
  pic VARCHAR(255) DEFAULT '',
  start_time DATETIME DEFAULT NULL,
  end_time DATETIME DEFAULT NULL,
  status TINYINT DEFAULT 1,
  click_count INT DEFAULT 0,
  order_count INT DEFAULT 0,
  url VARCHAR(255) DEFAULT '',
  sort INT DEFAULT 0,
  note VARCHAR(255) DEFAULT '',
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='首页广告位';

CREATE TABLE sms_home_brand (
  id BIGINT NOT NULL AUTO_INCREMENT,
  brand_id BIGINT NOT NULL,
  brand_name VARCHAR(64) DEFAULT '',
  recommend_status TINYINT DEFAULT 1,
  sort INT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='首页品牌推荐';

CREATE TABLE sms_home_new_product (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(200) DEFAULT '',
  recommend_status TINYINT DEFAULT 1,
  sort INT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='首页新品推荐';

CREATE TABLE sms_home_recommend_product (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(200) DEFAULT '',
  recommend_status TINYINT DEFAULT 1,
  sort INT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='首页人气推荐';

-- ---------------- RBAC ----------------
CREATE TABLE ums_admin (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(200) NOT NULL,
  icon VARCHAR(255) DEFAULT '',
  email VARCHAR(100) DEFAULT '',
  nick_name VARCHAR(64) DEFAULT '',
  note VARCHAR(255) DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  login_time DATETIME DEFAULT NULL,
  status TINYINT DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB COMMENT='管理员';

CREATE TABLE ums_role (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  description VARCHAR(255) DEFAULT '',
  admin_count INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  status TINYINT DEFAULT 1,
  sort INT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='角色';

CREATE TABLE ums_menu (
  id BIGINT NOT NULL AUTO_INCREMENT,
  parent_id BIGINT DEFAULT 0,
  title VARCHAR(64) NOT NULL,
  level INT DEFAULT 0,
  sort INT DEFAULT 0,
  name VARCHAR(64) DEFAULT '',
  icon VARCHAR(255) DEFAULT '',
  hidden TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='后台菜单';

CREATE TABLE ums_resource (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  url VARCHAR(255) NOT NULL,
  method VARCHAR(16) DEFAULT 'GET',
  description VARCHAR(255) DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='接口资源';

CREATE TABLE ums_admin_role_relation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  admin_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  KEY idx_admin (admin_id)
) ENGINE=InnoDB;

CREATE TABLE ums_role_menu_relation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  KEY idx_role (role_id)
) ENGINE=InnoDB;

CREATE TABLE ums_role_resource_relation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  resource_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  KEY idx_role (role_id)
) ENGINE=InnoDB;

-- ---------------- 会员 ----------------
CREATE TABLE ums_member_level (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  growth_point INT DEFAULT 0,
  default_status TINYINT DEFAULT 0,
  free_freight_point DECIMAL(10,2) DEFAULT 99,
  comment_growth_point INT DEFAULT 5,
  priviledge_free_freight TINYINT DEFAULT 1,
  priviledge_sign_in TINYINT DEFAULT 1,
  priviledge_comment TINYINT DEFAULT 1,
  priviledge_promotion TINYINT DEFAULT 0,
  priviledge_birthday TINYINT DEFAULT 0,
  product_discount DECIMAL(4,2) DEFAULT 10.00 COMMENT '折扣，10为无折扣',
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='会员等级';

CREATE TABLE ums_member (
  id BIGINT NOT NULL AUTO_INCREMENT,
  member_level_id BIGINT DEFAULT 1,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(200) NOT NULL,
  nickname VARCHAR(64) DEFAULT '',
  phone VARCHAR(32) DEFAULT '',
  status TINYINT DEFAULT 1,
  icon VARCHAR(255) DEFAULT '',
  gender TINYINT DEFAULT 0,
  birthday DATE DEFAULT NULL,
  city VARCHAR(64) DEFAULT '',
  integration INT DEFAULT 0,
  growth INT DEFAULT 0,
  source TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB COMMENT='会员';

CREATE TABLE ums_member_receive_address (
  id BIGINT NOT NULL AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  name VARCHAR(64) NOT NULL,
  phone VARCHAR(32) NOT NULL,
  default_status TINYINT DEFAULT 0,
  province VARCHAR(64) DEFAULT '',
  city VARCHAR(64) DEFAULT '',
  region VARCHAR(64) DEFAULT '',
  detail_address VARCHAR(255) DEFAULT '',
  PRIMARY KEY (id),
  KEY idx_member (member_id)
) ENGINE=InnoDB COMMENT='收货地址';

CREATE TABLE ums_member_product_collection (
  id BIGINT NOT NULL AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(200) DEFAULT '',
  product_pic VARCHAR(255) DEFAULT '',
  product_price DECIMAL(10,2) DEFAULT 0,
  product_sub_title VARCHAR(255) DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_member (member_id)
) ENGINE=InnoDB COMMENT='商品收藏';

CREATE TABLE ums_member_brand_attention (
  id BIGINT NOT NULL AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  brand_id BIGINT NOT NULL,
  brand_name VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_member (member_id)
) ENGINE=InnoDB COMMENT='品牌关注';

CREATE TABLE ums_member_browse_history (
  id BIGINT NOT NULL AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(200) DEFAULT '',
  product_pic VARCHAR(255) DEFAULT '',
  product_price DECIMAL(10,2) DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_member (member_id)
) ENGINE=InnoDB COMMENT='浏览足迹';

SET FOREIGN_KEY_CHECKS = 1;



-- 会员等级
INSERT INTO ums_member_level (id, name, growth_point, default_status, free_freight_point, product_discount) VALUES
(1, '普通会员', 0, 1, 99.00, 10.00),
(2, '黄金会员', 1000, 0, 199.00, 9.50),
(3, '钻石会员', 3000, 0, 299.00, 8.80);

-- 管理员（密码 123456）
INSERT INTO ums_admin (id, username, password, nick_name, email, note, status) VALUES
(1, 'admin', '$2a$10$txxcG2OfwwmYQzZVQBbIUePPjNbcKxy5xOLZndDln9/4RYUUr8VCy', '超级管理员', 'admin@mall.com', '拥有全部权限', 1),
(2, 'operator', '$2a$10$txxcG2OfwwmYQzZVQBbIUePPjNbcKxy5xOLZndDln9/4RYUUr8VCy', '运营专员', 'op@mall.com', '商品与营销运营', 1);

INSERT INTO ums_role (id, name, description, admin_count, status, sort) VALUES
(1, '超级管理员', '拥有所有菜单与接口资源', 1, 1, 0),
(2, '运营', '商品与营销模块', 1, 1, 10),
(3, '客服', '订单与售后模块', 0, 1, 20);

INSERT INTO ums_admin_role_relation (admin_id, role_id) VALUES (1, 1), (2, 2);

INSERT INTO ums_menu (id, parent_id, title, level, sort, name, icon, hidden) VALUES
(1, 0, '首页', 0, 0, 'dashboard', 'HomeFilled', 0),
(2, 0, '商品管理', 0, 10, 'pms', 'Goods', 0),
(3, 2, '商品列表', 1, 0, 'product', '', 0),
(4, 2, '商品分类', 1, 10, 'category', '', 0),
(5, 2, '品牌管理', 1, 20, 'brand', '', 0),
(6, 2, '商品属性', 1, 30, 'attribute', '', 0),
(7, 2, 'SKU 库存', 1, 40, 'sku', '', 0),
(8, 0, '订单管理', 0, 20, 'oms', 'List', 0),
(9, 8, '订单列表', 1, 0, 'order', '', 0),
(10, 8, '退货申请', 1, 10, 'returnApply', '', 0),
(11, 8, '退货原因', 1, 20, 'returnReason', '', 0),
(12, 8, '订单设置', 1, 30, 'orderSetting', '', 0),
(13, 0, '营销管理', 0, 30, 'sms', 'Present', 0),
(14, 13, '优惠券', 1, 0, 'coupon', '', 0),
(15, 13, '秒杀活动', 1, 10, 'flash', '', 0),
(16, 13, '首页推荐', 1, 20, 'home', '', 0),
(17, 0, '权限管理', 0, 40, 'ums', 'Lock', 0),
(18, 17, '管理员', 1, 0, 'admin', '', 0),
(19, 17, '角色', 1, 10, 'role', '', 0),
(20, 17, '菜单', 1, 20, 'menu', '', 0),
(21, 17, '资源', 1, 30, 'resource', '', 0),
(22, 0, '内容与会员', 0, 50, 'cms', 'User', 0),
(23, 22, '专题管理', 1, 0, 'subject', '', 0),
(24, 22, '优选专区', 1, 10, 'prefrenceArea', '', 0),
(25, 22, '会员等级', 1, 20, 'memberLevel', '', 0),
(26, 22, '会员列表', 1, 30, 'member', '', 0);

INSERT INTO ums_role_menu_relation (role_id, menu_id)
SELECT 1, id FROM ums_menu;
INSERT INTO ums_role_menu_relation (role_id, menu_id) VALUES
(2,1),(2,2),(2,3),(2,4),(2,5),(2,6),(2,7),(2,13),(2,14),(2,15),(2,16),(2,22),(2,23),(2,24),(2,25),
(3,1),(3,8),(3,9),(3,10),(3,11),(3,12),(3,22),(3,26);

INSERT INTO ums_resource (id, name, url, method, description) VALUES
(1,'商品列表','/admin/product/**','GET','商品查询'),
(2,'商品编辑','/admin/product/**','POST','商品创建/更新/上下架'),
(3,'商品删除','/admin/product/**','DELETE','逻辑删除'),
(4,'商品分类','/admin/productCategory/**','*','分类管理'),
(5,'品牌管理','/admin/brand/**','*','品牌管理'),
(6,'商品属性','/admin/productAttribute/**','*','属性管理'),
(7,'SKU库存','/admin/sku/**','*','SKU 管理'),
(8,'订单查询','/admin/order/**','GET','订单查询'),
(9,'订单操作','/admin/order/**','POST','发货/关闭/修改'),
(10,'退货管理','/admin/returnApply/**','*','退货审核'),
(11,'退货原因','/admin/returnReason/**','*','退货原因'),
(12,'订单设置','/admin/orderSetting/**','*','订单设置'),
(13,'优惠券','/admin/coupon/**','*','优惠券'),
(14,'秒杀','/admin/flashPromotion/**','*','秒杀'),
(15,'首页推荐','/admin/home/**','*','首页推荐位'),
(16,'管理员','/admin/admin/**','*','管理员'),
(17,'角色','/admin/role/**','*','角色'),
(18,'菜单','/admin/menu/**','*','菜单'),
(19,'资源','/admin/resource/**','*','接口资源'),
(20,'专题','/admin/subject/**','*','专题'),
(21,'优选专区','/admin/prefrenceArea/**','*','优选专区'),
(22,'会员等级','/admin/memberLevel/**','*','会员等级'),
(23,'会员列表','/admin/member/**','*','会员查询'),
(24,'文件上传','/admin/oss/**','POST','文件上传');

INSERT INTO ums_role_resource_relation (role_id, resource_id)
SELECT 1, id FROM ums_resource;
INSERT INTO ums_role_resource_relation (role_id, resource_id) VALUES
(2,1),(2,2),(2,3),(2,4),(2,5),(2,6),(2,7),(2,13),(2,14),(2,15),(2,16),(2,20),(2,21),(2,22),(2,24),
(3,8),(3,9),(3,10),(3,11),(3,12);

-- 品牌
INSERT INTO pms_brand (id, name, first_letter, sort, factory_status, show_status, logo, big_pic, brand_story) VALUES
(1,'华为','H',1,1,1,'/images/brand-huawei.svg','/images/banner-huawei.jpg','华为：构建万物互联的智能世界'),
(2,'Apple','A',2,1,1,'/images/brand-apple.svg','/images/product-iphone.jpg','Apple：Think different'),
(3,'小米','X',3,1,1,'/images/brand-xiaomi.svg','/images/product-xiaomi.jpg','小米：让每个人享受科技的乐趣'),
(4,'Nike','N',4,1,1,'/images/brand-nike.svg','/images/product-nike.jpg','Nike：Just do it'),
(5,'优衣库','U',5,1,1,'/images/brand-uniqlo.svg','/images/product-jacket.jpg','优衣库：LifeWear 服适人生');

-- 分类
INSERT INTO pms_product_category (id, parent_id, name, level, product_count, product_unit, nav_status, show_status, sort, icon, keywords, description) VALUES
(1,0,'手机数码',0,6,'件',1,1,0,'','手机 数码','手机数码分类'),
(2,0,'服装',0,2,'件',1,1,10,'','服装','服装分类'),
(3,1,'手机通讯',1,3,'件',1,1,0,'','手机','手机通讯'),
(4,1,'智能设备',1,1,'件',1,1,10,'','手表','智能设备'),
(5,2,'男装',1,1,'件',1,1,0,'','男装','男装'),
(6,2,'女装',1,1,'件',1,1,10,'','女装','女装');

-- 属性分类 / 属性
INSERT INTO pms_product_attribute_category (id, name, product_category_id) VALUES
(1,'手机通讯属性',3),(2,'服装属性',5),(3,'智能设备属性',4);

INSERT INTO pms_product_attribute (id, product_attribute_category_id, name, type, input_type, input_list, sort) VALUES
(1,1,'颜色',0,1,'雅川青,雅丹黑,白沙银',0),
(2,1,'存储容量',0,1,'256G,512G,1T',10),
(3,1,'屏幕尺寸',1,1,'6.7英寸',20),
(4,1,'电池容量',1,1,'5000mAh',30),
(5,1,'处理器',1,1,'麒麟9000S',40),
(6,2,'颜色',0,1,'黑色,米白',0),
(7,2,'尺码',0,1,'S,M,L,XL',10),
(8,2,'面料',1,1,'摇粒绒',20),
(9,3,'颜色',0,1,'曜石黑,云杉绿',0),
(10,3,'表盘尺寸',1,1,'46mm',20);

-- 商品
INSERT INTO pms_product (id, product_category_id, brand_id, name, sub_title, description, product_sn, price, original_price, stock, low_stock, pic, album_pics, unit, weight, sort, sale, publish_status, new_status, recommand_status, verify_status, delete_status, keywords, detail_title, detail_desc, create_time) VALUES
(1,3,1,'华为 Mate 60 Pro','卫星通话 旗舰影像','华为 Mate 60 Pro 12GB+256GB', 'HWMATE60P',6999.00,7999.00,100,10,'/images/product-huawei.jpg','/images/product-huawei.jpg','件',0.50,0,120,1,1,1,1,0,'华为 手机','华为 Mate 60 Pro','第二代昆仑玻璃，超可靠玄武架构', NOW()),
(2,3,2,'iPhone 15','灵动岛 全新影像系统','iPhone 15 128GB', 'APIP15',5999.00,6999.00,80,10,'/images/product-iphone.jpg','/images/product-iphone.jpg','件',0.45,10,210,1,1,1,1,0,'苹果 手机','iPhone 15','全新 4800 万像素主摄', NOW()),
(3,3,3,'小米 14','徕卡光学 旗舰性能','小米 14 12GB+256GB', 'XMI14',3999.00,4599.00,120,10,'/images/product-xiaomi.jpg','','件',0.40,20,330,1,0,1,1,0,'小米 手机','小米 14','骁龙 8 Gen 3，徕卡影像', NOW()),
(4,5,4,'Nike Air 运动鞋','轻量缓震 透气舒适','Nike Air 男子运动鞋', 'NIKAIR',599.00,799.00,200,20,'/images/product-nike.jpg','','双',0.8,30,560,1,1,1,1,0,'耐克 运动鞋','Nike Air','Air 缓震中底，轻盈透气', NOW()),
(5,6,5,'优衣库 摇粒绒外套','柔软保暖 秋冬必备','优衣库 女装 摇粒绒外套', 'UQLFLEECE',299.00,399.00,300,30,'/images/product-jacket.jpg','','件',1.00,40,870,1,1,0,1,0,'优衣库 外套','摇粒绒外套','轻盈保暖，多色可选', NOW()),
(6,4,1,'华为 WATCH GT 4','轻盈设计 长续航','华为智能手表 46mm', 'HWGT4',1299.00,1499.00,90,10,'/images/product-watch.jpg','','件',0.30,50,150,1,1,1,1,0,'华为 手表','华为 WATCH GT 4','两周长续航，专业运动监测', NOW());

INSERT INTO pms_sku_stock (id, product_id, sku_code, price, stock, lock_stock, low_stock, pic, sale, sp_data) VALUES
(1,1,'202601011001001',6999.00,50,0,10,'/images/product-huawei.jpg',70,'{"颜色":"雅川青","存储容量":"256G"}'),
(2,1,'202601011001002',7999.00,50,0,10,'/images/product-huawei.jpg',50,'{"颜色":"雅丹黑","存储容量":"512G"}'),
(3,2,'202601011002001',5999.00,80,0,10,'',150,'{"颜色":"蓝色","存储容量":"128G"}'),
(4,3,'202601011003001',3999.00,60,0,10,'',200,'{"颜色":"黑色","存储容量":"256G"}'),
(5,3,'202601011003002',4299.00,60,0,10,'',130,'{"颜色":"白色","存储容量":"512G"}'),
(6,4,'202601011004001',599.00,100,0,20,'',300,'{"颜色":"白色","尺码":"M"}'),
(7,4,'202601011004002',599.00,100,0,20,'',260,'{"颜色":"黑色","尺码":"L"}'),
(8,5,'202601011005001',299.00,150,0,30,'',500,'{"颜色":"米白","尺码":"M"}'),
(9,5,'202601011005002',299.00,150,0,30,'',370,'{"颜色":"黑色","尺码":"L"}'),
(10,6,'202601011006001',1299.00,90,0,10,'',150,'{"颜色":"曜石黑","表盘尺寸":"46mm"}');

INSERT INTO pms_product_attribute_value (product_id, product_attribute_id, value) VALUES
(1,3,'6.7英寸'),(1,4,'5000mAh'),(1,5,'麒麟9000S'),(1,1,'雅川青'),(1,2,'256G'),
(2,3,'6.1英寸'),(2,4,'3349mAh'),(2,5,'A16 仿生'),
(3,3,'6.36英寸'),(3,4,'4610mAh'),(3,5,'骁龙 8 Gen 3'),
(4,6,'白色'),(4,7,'M'),
(5,6,'米白'),(5,7,'M'),(5,8,'摇粒绒'),
(6,9,'曜石黑'),(6,10,'46mm');

INSERT INTO pms_member_price (product_id, member_level_id, member_price) VALUES
(1,2,6799.00),(1,3,6599.00),(2,2,5799.00),(3,2,3899.00),(4,3,549.00),(5,2,279.00);

INSERT INTO pms_product_ladder (product_id, member_level_id, count, price) VALUES
(4,0,3,549.00),(4,0,5,499.00),(5,0,3,269.00);

INSERT INTO pms_product_full_reduction (product_id, full_price, reduce_price) VALUES
(4,1000.00,100.00),(4,2000.00,250.00),(5,500.00,50.00);

-- 内容
INSERT INTO cms_subject (id, name, sub_title, category_id, pic, main_title, description, show_status, sort, views) VALUES
(1,'nova 系列','高颜值 爱自拍',3,'/images/banner-huawei.jpg','nova 系列','nova 系列新品上市',1,0,1200),
(2,'秋日穿搭','轻盈保暖法则',5,'/images/product-jacket.jpg','秋日穿搭','秋冬新品穿搭',1,10,800);

INSERT INTO cms_prefrence_area (id, name, pic, sort, sub_title, show_status) VALUES
(1,'数码好物','/images/banner-digital.jpg',0,'精选数码产品',1),
(2,'运动生活','/images/product-nike.jpg',10,'运动装备优选',1);

INSERT INTO cms_subject_product_relation (subject_id, product_id) VALUES (1,1),(1,2),(1,3),(2,4),(2,5);
INSERT INTO cms_prefrence_area_product_relation (prefrence_area_id, product_id) VALUES (1,1),(1,2),(1,3),(2,4),(2,5),(2,6);

-- 订单设置
INSERT INTO oms_order_setting (id, flash_order_overtime, normal_order_overtime, confirm_overtime, comment_overtime) VALUES (1, 1800, 1800, 15, 7);

-- 退货原因
INSERT INTO oms_order_return_reason (id, name, sort, status) VALUES
(1,'质量问题',0,1),(2,'尺码不合适',10,1),(3,'7天无理由退货',20,1),(4,'发错货',30,1);

-- 优惠券
INSERT INTO sms_coupon (id, name, type, platform, count, amount, min_point, per_limit, start_time, end_time, note, use_type) VALUES
(1,'新人满100减20',0,0,1000,20.00,100.00,1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), '全场通用', 0),
(2,'数码品类满500减50',0,0,500,50.00,500.00,1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), '手机数码专享', 1),
(3,'秋冬服饰 8 折',1,0,300,8.00,200.00,2, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), '服装分类可用', 1);

INSERT INTO sms_coupon_product_category_relation (coupon_id, product_category_id, product_category_name, parent_category_name, parent_category_id) VALUES
(2,3,'手机通讯','手机数码',1),(2,4,'智能设备','手机数码',1),(3,5,'男装','服装',2),(3,6,'女装','服装',2);

INSERT INTO sms_coupon_history (coupon_id, member_id, member_nickname, coupon_code, get_time, use_status) VALUES
(1,1,'测试用户','C1-2026-001',NOW(),0),(3,1,'测试用户','C3-2026-001',NOW(),0);

-- 秒杀
INSERT INTO sms_flash_promotion (id, title, start_date, end_date, status) VALUES
(1,'双十一数码秒杀',CURDATE(),DATE_ADD(CURDATE(), INTERVAL 7 DAY),1);
INSERT INTO sms_flash_promotion_session (id, name, start_time, end_time, status) VALUES
(1,'00:00-08:00', '00:00:00','08:00:00',1),
(2,'08:00-12:00', '08:00:00','12:00:00',1),
(3,'12:00-18:00', '12:00:00','18:00:00',1),
(4,'18:00-24:00', '18:00:00','23:59:59',1);
INSERT INTO sms_flash_promotion_product_relation (flash_promotion_id, flash_promotion_session_id, product_id, flash_promotion_price, flash_promotion_count, flash_promotion_limit, sort) VALUES
(1,2,1,6499.00,20,1,0),(1,3,3,3799.00,30,1,10),(1,4,4,499.00,50,2,20);

-- 首页运营
INSERT INTO sms_home_advertise (name, type, pic, start_time, end_time, status, url, sort, note) VALUES
('Mate 60 Pro 新品首发',0,'/images/banner-huawei.jpg',DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(NOW(),INTERVAL 30 DAY),1,'/product/1',0,'首页轮播'),
('双十一数码狂欢',0,'/images/banner-digital.jpg',DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(NOW(),INTERVAL 30 DAY),1,'/search',10,'首页轮播'),
('秋日穿搭指南',1,'/images/product-jacket.jpg',DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(NOW(),INTERVAL 30 DAY),1,'/product/5',20,'推荐位');

INSERT INTO sms_home_brand (brand_id, brand_name, recommend_status, sort) VALUES
(1,'华为',1,0),(2,'Apple',1,10),(3,'小米',1,20),(4,'Nike',1,30),(5,'优衣库',1,40);

INSERT INTO sms_home_new_product (product_id, product_name, recommend_status, sort) VALUES
(1,'华为 Mate 60 Pro',1,0),(2,'iPhone 15',1,10),(3,'小米 14',1,20),(6,'华为 WATCH GT 4',1,30);

INSERT INTO sms_home_recommend_product (product_id, product_name, recommend_status, sort) VALUES
(4,'Nike Air 运动鞋',1,0),(5,'优衣库 摇粒绒外套',1,10),(3,'小米 14',1,20),(2,'iPhone 15',1,30);

-- 测试会员（密码 123456）
INSERT INTO ums_member (id, member_level_id, username, password, nickname, phone, status, gender, city, integration, growth, source) VALUES
(1,2,'13800138000','$2a$10$txxcG2OfwwmYQzZVQBbIUePPjNbcKxy5xOLZndDln9/4RYUUr8VCy','测试用户','13800138000',1,0,'上海',500,1200,1);

INSERT INTO ums_member_receive_address (member_id, name, phone, default_status, province, city, region, detail_address) VALUES
(1,'测试用户','13800138000',1,'上海市','上海市','浦东新区','世纪大道 100 号 8 楼');

-- 一条待发货订单，便于后台演示
INSERT INTO oms_order (id, order_sn, member_id, member_username, order_type, total_amount, pay_amount, freight_amount, pay_type, source_type, status, receiver_name, receiver_phone, receiver_province, receiver_city, receiver_region, receiver_detail_address, note, confirm_status, delete_status, integration, growth, create_time, payment_time) VALUES
(1,'202611011200001234',1,'13800138000',0,5999.00,5999.00,0.00,1,1,1,'测试用户','13800138000','上海市','上海市','浦东新区','世纪大道 100 号 8 楼','尽快发货',0,0,0,0,DATE_SUB(NOW(), INTERVAL 1 DAY),DATE_SUB(NOW(), INTERVAL 1 DAY));
INSERT INTO oms_order_item (order_id, order_sn, product_id, product_sku_id, product_name, product_pic, product_price, product_quantity, product_sku, real_amount) VALUES
(1,'202611011200001234',2,3,'iPhone 15','/images/product-iphone.jpg',5999.00,1,'蓝色 / 128G',5999.00);
INSERT INTO oms_order_operate_history (order_id, operate_man, order_status, note, create_time) VALUES
(1,'13800138000',0,'用户下单',DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1,'system',1,'支付成功',DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO oms_cart_item (member_id, product_id, sku_id, quantity) VALUES (1,4,6,1),(1,5,8,2);
