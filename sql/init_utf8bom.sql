-- ============================================================
-- 优选网购 初始化 SQL 脚本
-- ============================================================

USE mall;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone           VARCHAR(20)  NOT NULL UNIQUE COMMENT '手机号',
    password        VARCHAR(128) DEFAULT NULL COMMENT '密码（加密）',
    nickname        VARCHAR(50)  DEFAULT '用户XXXX' COMMENT '昵称',
    avatar          VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    gender          TINYINT      DEFAULT 0 COMMENT '0未知 1男 2女',
    last_login_time DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    total_sign_days INT          DEFAULT 0 COMMENT '累计签到天数',
    last_sign_time  DATETIME     DEFAULT NULL COMMENT '最后签到时间',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品表
CREATE TABLE IF NOT EXISTS t_product (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200) NOT NULL COMMENT '商品名称',
    description     TEXT COMMENT '商品描述',
    price           DECIMAL(10,2) NOT NULL COMMENT '单价',
    stock           INT          DEFAULT 0 COMMENT '库存',
    image           VARCHAR(500) DEFAULT NULL COMMENT '主图',
    images          TEXT COMMENT '图片列表（JSON数组）',
    category_id     BIGINT       DEFAULT NULL COMMENT '分类ID',
    status          TINYINT      DEFAULT 1 COMMENT '1上架 0下架',
    sales_count     INT          DEFAULT 0 COMMENT '销量',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    INDEX idx_category (category_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 商品评价表
CREATE TABLE IF NOT EXISTS t_product_review (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    product_id      BIGINT       NOT NULL COMMENT '商品ID',
    rating          TINYINT      NOT NULL COMMENT '评分 1-5',
    content         TEXT COMMENT '评价内容',
    images          VARCHAR(1000) DEFAULT NULL COMMENT '评价图片',
    like_count      INT          DEFAULT 0 COMMENT '点赞数',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    INDEX idx_product (product_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- 秒杀活动表
CREATE TABLE IF NOT EXISTS t_seckill_activity (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200) NOT NULL COMMENT '活动名称',
    product_id      BIGINT       NOT NULL COMMENT '商品ID',
    seckill_price   DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    total_stock     INT          NOT NULL COMMENT '总库存',
    remain_stock    INT          NOT NULL COMMENT '剩余库存',
    start_time      DATETIME     NOT NULL COMMENT '开始时间',
    end_time        DATETIME     NOT NULL COMMENT '结束时间',
    status          TINYINT      DEFAULT 1 COMMENT '1进行中 0已结束',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    INDEX idx_product (product_id),
    INDEX idx_status (status),
    INDEX idx_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动表';

-- 秒杀订单表
CREATE TABLE IF NOT EXISTS t_seckill_order (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no        VARCHAR(64)  NOT NULL UNIQUE COMMENT '订单号',
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    activity_id     BIGINT       NOT NULL COMMENT '秒杀活动ID',
    product_id      BIGINT       NOT NULL COMMENT '商品ID',
    seckill_price   DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    status          TINYINT      DEFAULT 1 COMMENT '1已下单 2已支付 0已取消',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    INDEX idx_user (user_id),
    INDEX idx_activity (activity_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';

-- 签到记录表
CREATE TABLE IF NOT EXISTS t_sign_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    sign_date       DATE         NOT NULL COMMENT '签到日期',
    reward_points   INT          DEFAULT 1 COMMENT '奖励积分',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    UNIQUE KEY uk_user_date (user_id, sign_date),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- ============================================================
-- 模拟商品数据
-- ============================================================
INSERT INTO t_product (name, description, price, stock, image, category_id, status, sales_count) VALUES
('小米14 Pro 5G手机', '骁龙8 Gen3处理器 | 徕卡光学镜头 | 2K AMOLED屏幕 | 120W快充', 4999.00, 200, 'https://img.mall.com/product/xiaomi14pro.jpg', 1, 1, 1280),
('Apple iPhone 15 Pro', 'A17 Pro芯片 | 钛金属设计 | 4800万像素 | 全系列灵动岛', 8999.00, 150, 'https://img.mall.com/product/iphone15pro.jpg', 1, 1, 2100),
('华为Mate60 Pro', '麒麟9000S芯片 | XMAGE影像 | 卫星通话 | 88W超级快充', 6999.00, 100, 'https://img.mall.com/product/mate60pro.jpg', 1, 1, 3500),
('ThinkPad X1 Carbon 2024', 'Intel i7-1365U | 32GB内存 | 2TB固态 | 14英寸2.8K屏', 12999.00, 50, 'https://img.mall.com/product/thinkpadx1.jpg', 2, 1, 320),
('戴尔 XPS 15 笔记本', 'Intel i9-13900H | RTX 4060显卡 | 64GB内存 | 15.6寸4K触控屏', 17999.00, 30, 'https://img.mall.com/product/xps15.jpg', 2, 1, 180),
('索尼 WH-1000XM5 降噪耳机', '行业顶级降噪 | 30小时续航 | LDAC高解析 | 多点连接', 2699.00, 300, 'https://img.mall.com/product/sonyxm5.jpg', 3, 1, 890),
('AirPods Pro 2代', '自适应降噪 | 空间音频 | USB-C充电 | 查找功能', 1899.00, 500, 'https://img.mall.com/product/airpodspro2.jpg', 3, 1, 2300),
('小米手环 8 Pro', '1.74寸AMOLED大屏 | 双通道监测 | 16天超长续航 | 150+运动模式', 399.00, 1000, 'https://img.mall.com/product/miband8pro.jpg', 3, 1, 4500),
('华为 WATCH GT 4', '46mm AMOLED屏 | 14天续航 | 100+运动模式 | 心脏健康研究', 1588.00, 400, 'https://img.mall.com/product/watchgt4.jpg', 3, 1, 1200),
('SK-II 护肤精华套装', '神仙水230ml+小灯泡50ml+大红瓶50g | 明星爆款组合', 1999.00, 600, 'https://img.mall.com/product/skiiset.jpg', 4, 1, 3800),
('兰蔻小黑瓶精华50ml', '第二代金钥匙精华 | 修护肌肤 | 改善细纹 | 焕亮肤色', 1680.00, 400, 'https://img.mall.com/product/lancome.jpg', 4, 1, 2900),
('戴森吹风机 HD15', '智能温控 | 4倍气流倍增 | 减少热损伤 | 快速干发', 3199.00, 200, 'https://img.mall.com/product/dysonhd15.jpg', 4, 1, 1600),
('五常大米 5kg', '正宗黑龙江五常稻花香 | 当年新米 | 生态种植 | 香糯可口', 89.00, 5000, 'https://img.mall.com/product/wuchangrice.jpg', 5, 1, 12000),
('蒙牛特仑苏有机纯牛奶 250ml*16', '有机认证 | 3.6g优质乳蛋白 | 丹麦菌种发酵', 68.00, 3000, 'https://img.mall.com/product/telunsu.jpg', 5, 1, 8000),
('飞天茅台53度 500ml', '酱香型白酒 | 正品保障 | 收藏投资佳品 | 限量供应', 2999.00, 50, 'https://img.mall.com/product/maotai.jpg', 5, 1, 500);

-- 秒杀活动数据
INSERT INTO t_seckill_activity (name, product_id, seckill_price, total_stock, remain_stock, start_time, end_time, status) VALUES
('小米14 Pro 限时秒杀', 1, 3999.00, 20, 20, '2026-04-15 10:00:00', '2026-04-20 22:00:00', 1),
('iPhone 15 Pro 限量秒杀', 2, 6999.00, 10, 10, '2026-04-15 10:00:00', '2026-04-20 22:00:00', 1),
('华为Mate60 Pro 秒杀专场', 3, 5499.00, 15, 15, '2026-04-15 10:00:00', '2026-04-20 22:00:00', 1),
('AirPods Pro 秒杀', 7, 1299.00, 50, 50, '2026-04-15 10:00:00', '2026-04-20 22:00:00', 1);
