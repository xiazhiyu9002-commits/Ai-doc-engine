/*
 Navicat Premium Dump SQL

 Source Server         : Postgre
 Source Server Type    : PostgreSQL
 Source Server Version : 170009 (170009)
 Source Host           : localhost:5432
 Source Catalog        : ai_doc_engine
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 170009 (170009)
 File Encoding         : 65001

 Date: 24/05/2026 18:13:17
*/


-- ----------------------------
-- Type structure for login_result
-- ----------------------------
DROP TYPE IF EXISTS "public"."login_result";
CREATE TYPE "public"."login_result" AS ENUM (
  'success',
  'failed'
);
ALTER TYPE "public"."login_result" OWNER TO "postgres";

-- ----------------------------
-- Type structure for login_type
-- ----------------------------
DROP TYPE IF EXISTS "public"."login_type";
CREATE TYPE "public"."login_type" AS ENUM (
  'PASSWORD',
  'SCHOOL_OAUTH',
  'TOKEN'
);
ALTER TYPE "public"."login_type" OWNER TO "postgres";

-- ----------------------------
-- Type structure for task_status
-- ----------------------------
DROP TYPE IF EXISTS "public"."task_status";
CREATE TYPE "public"."task_status" AS ENUM (
  'PENDING',
  'PROCESSING',
  'SUCCESS',
  'FAILED'
);
ALTER TYPE "public"."task_status" OWNER TO "postgres";

-- ----------------------------
-- Type structure for template_type
-- ----------------------------
DROP TYPE IF EXISTS "public"."template_type";
CREATE TYPE "public"."template_type" AS ENUM (
  'system',
  'custom'
);
ALTER TYPE "public"."template_type" OWNER TO "postgres";

-- ----------------------------
-- Type structure for user_role
-- ----------------------------
DROP TYPE IF EXISTS "public"."user_role";
CREATE TYPE "public"."user_role" AS ENUM (
  'USER',
  'ADMIN'
);
ALTER TYPE "public"."user_role" OWNER TO "postgres";

-- ----------------------------
-- Type structure for user_status
-- ----------------------------
DROP TYPE IF EXISTS "public"."user_status";
CREATE TYPE "public"."user_status" AS ENUM (
  'active',
  'disabled',
  'locked'
);
ALTER TYPE "public"."user_status" OWNER TO "postgres";

-- ----------------------------
-- Sequence structure for doc_export_task_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."doc_export_task_id_seq";
CREATE SEQUENCE "public"."doc_export_task_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for doc_template_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."doc_template_id_seq";
CREATE SEQUENCE "public"."doc_template_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for formula_ocr_record_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."formula_ocr_record_id_seq";
CREATE SEQUENCE "public"."formula_ocr_record_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_announcement_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_announcement_id_seq";
CREATE SEQUENCE "public"."sys_announcement_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_login_log_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_login_log_id_seq";
CREATE SEQUENCE "public"."sys_login_log_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_password_reset_token_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_password_reset_token_id_seq";
CREATE SEQUENCE "public"."sys_password_reset_token_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_user_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_user_id_seq";
CREATE SEQUENCE "public"."sys_user_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for user_feedback_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."user_feedback_id_seq";
CREATE SEQUENCE "public"."user_feedback_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for doc_export_task
-- ----------------------------
DROP TABLE IF EXISTS "public"."doc_export_task";
CREATE TABLE "public"."doc_export_task" (
  "id" int8 NOT NULL DEFAULT nextval('doc_export_task_id_seq'::regclass),
  "user_id" int8 NOT NULL,
  "template_id" int8,
  "task_status" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'PENDING'::character varying,
  "file_name" varchar(255) COLLATE "pg_catalog"."default",
  "file_size" int8,
  "char_count" int4,
  "error_message" varchar(500) COLLATE "pg_catalog"."default",
  "start_time" timestamptz(6),
  "end_time" timestamptz(6),
  "processing_time_ms" int4
)
;
COMMENT ON COLUMN "public"."doc_export_task"."id" IS '主键ID';
COMMENT ON COLUMN "public"."doc_export_task"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."doc_export_task"."template_id" IS '模板ID';
COMMENT ON COLUMN "public"."doc_export_task"."task_status" IS '任务状态：PENDING, PROCESSING, SUCCESS, FAILED';
COMMENT ON COLUMN "public"."doc_export_task"."file_name" IS '文件名';
COMMENT ON COLUMN "public"."doc_export_task"."file_size" IS '文件大小（字节）';
COMMENT ON COLUMN "public"."doc_export_task"."char_count" IS '字符数';
COMMENT ON COLUMN "public"."doc_export_task"."error_message" IS '错误信息';
COMMENT ON COLUMN "public"."doc_export_task"."start_time" IS '开始时间';
COMMENT ON COLUMN "public"."doc_export_task"."end_time" IS '结束时间';
COMMENT ON COLUMN "public"."doc_export_task"."processing_time_ms" IS '处理耗时（毫秒）';
COMMENT ON TABLE "public"."doc_export_task" IS '文档导出任务表';

-- ----------------------------
-- Records of doc_export_task
-- ----------------------------
INSERT INTO "public"."doc_export_task" VALUES (1, 10, 2, 'SUCCESS', '测试文档.docx', 10240, 500, NULL, '2026-05-23 20:00:29.282356+08', '2026-05-23 20:00:29.282356+08', 200);
INSERT INTO "public"."doc_export_task" VALUES (2, 10, 8, 'SUCCESS', '哈哈.docx', 14633, 93, NULL, '2026-05-23 20:56:58.47179+08', '2026-05-23 20:57:20.987944+08', 22516);

-- ----------------------------
-- Table structure for doc_template
-- ----------------------------
DROP TABLE IF EXISTS "public"."doc_template";
CREATE TABLE "public"."doc_template" (
  "id" int8 NOT NULL DEFAULT nextval('doc_template_id_seq'::regclass),
  "user_id" int8 NOT NULL,
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(500) COLLATE "pg_catalog"."default",
  "template_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'custom'::template_type,
  "config_json" jsonb NOT NULL,
  "is_default" bool NOT NULL DEFAULT false,
  "is_public" bool NOT NULL DEFAULT false,
  "use_count" int4 NOT NULL DEFAULT 0,
  "created_at" timestamptz(6) NOT NULL DEFAULT now(),
  "updated_at" timestamptz(6) NOT NULL DEFAULT now()
)
;
COMMENT ON COLUMN "public"."doc_template"."id" IS '主键ID';
COMMENT ON COLUMN "public"."doc_template"."user_id" IS '创建用户ID';
COMMENT ON COLUMN "public"."doc_template"."name" IS '模板名称';
COMMENT ON COLUMN "public"."doc_template"."description" IS '模板描述';
COMMENT ON COLUMN "public"."doc_template"."template_type" IS '模板类型';
COMMENT ON COLUMN "public"."doc_template"."config_json" IS '模板配置JSON';
COMMENT ON COLUMN "public"."doc_template"."is_default" IS '是否默认模板';
COMMENT ON COLUMN "public"."doc_template"."is_public" IS '是否公开';
COMMENT ON COLUMN "public"."doc_template"."use_count" IS '使用次数';
COMMENT ON COLUMN "public"."doc_template"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."doc_template"."updated_at" IS '更新时间';
COMMENT ON TABLE "public"."doc_template" IS '文档模板表';

-- ----------------------------
-- Records of doc_template
-- ----------------------------
INSERT INTO "public"."doc_template" VALUES (1, 10, '学术科研模板', '适用于毕业论文、期刊论文、开题报告、实验报告、文献综述', 'system', '{"fontSettings": {"fontSize": 12, "fontFamily": "宋体", "codeFontSize": 10, "headingFonts": {"h1": {"bold": true, "size": 22, "family": "黑体"}, "h2": {"bold": true, "size": 18, "family": "黑体"}, "h3": {"bold": true, "size": 16, "family": "黑体"}, "h4": {"bold": true, "size": 14, "family": "黑体"}, "h5": {"bold": true, "size": 12, "family": "黑体"}, "h6": {"bold": true, "size": 12, "family": "黑体"}}, "codeFontFamily": "Consolas"}, "pageSettings": {"margins": {"top": 2.5, "left": 2.8, "right": 2.5, "bottom": 2.5, "gutter": 0.5}, "pageSize": "A4", "orientation": "portrait"}, "paragraphSettings": {"alignment": "left", "lineSpacing": 1.5, "firstLineIndent": 2, "paragraphSpacing": {"after": 0, "before": 0}}, "headerFooterSettings": {"footer": "第 {page} 页，共 {total} 页", "header": "论文标题", "footerHeight": 1.5, "headerHeight": 1.5, "oddEvenDifferent": false, "firstPageDifferent": true}}', 'f', 't', 0, '2026-04-20 23:09:14+08', '2026-04-20 23:09:14+08');
INSERT INTO "public"."doc_template" VALUES (3, 10, '商业职场模板', '适用于商业计划书、可行性报告、项目方案、工作总结、述职报告', 'system', '{"fontSettings": {"fontSize": 11, "fontFamily": "微软雅黑", "codeFontSize": 10, "headingFonts": {"h1": {"bold": true, "size": 20, "family": "微软雅黑"}, "h2": {"bold": true, "size": 16, "family": "微软雅黑"}, "h3": {"bold": true, "size": 14, "family": "微软雅黑"}, "h4": {"bold": true, "size": 12, "family": "微软雅黑"}, "h5": {"bold": true, "size": 11, "family": "微软雅黑"}, "h6": {"bold": true, "size": 11, "family": "微软雅黑"}}, "codeFontFamily": "Consolas"}, "pageSettings": {"margins": {"top": 2.5, "left": 2.5, "right": 2.5, "bottom": 2.5, "gutter": 0}, "pageSize": "A4", "orientation": "portrait"}, "paragraphSettings": {"alignment": "left", "lineSpacing": 1.25, "firstLineIndent": 0, "paragraphSpacing": {"after": 6, "before": 6}}, "headerFooterSettings": {"footer": "第 {page} 页，共 {total} 页", "header": "文档名称 | 公司名称", "footerHeight": 1.2, "headerHeight": 1.2, "oddEvenDifferent": false, "firstPageDifferent": true}}', 'f', 't', 0, '2026-04-20 23:09:14+08', '2026-04-20 23:09:14+08');
INSERT INTO "public"."doc_template" VALUES (4, 10, '法律合同模板', '适用于合同、协议、备忘录、保密协议、授权委托书', 'system', '{"fontSettings": {"fontSize": 12, "fontFamily": "宋体", "codeFontSize": 10, "headingFonts": {"h1": {"bold": true, "size": 20, "family": "黑体"}, "h2": {"bold": true, "size": 16, "family": "黑体"}, "h3": {"bold": true, "size": 14, "family": "宋体"}, "h4": {"bold": true, "size": 12, "family": "宋体"}, "h5": {"bold": true, "size": 12, "family": "宋体"}, "h6": {"bold": false, "size": 12, "family": "宋体"}}, "codeFontFamily": "Consolas"}, "pageSettings": {"margins": {"top": 2.5, "left": 3.0, "right": 3.0, "bottom": 2.5, "gutter": 0.5}, "pageSize": "A4", "orientation": "portrait"}, "paragraphSettings": {"alignment": "left", "lineSpacing": 1.2, "firstLineIndent": 2, "paragraphSpacing": {"after": 6, "before": 6}}, "headerFooterSettings": {"footer": "第 {page} 页，共 {total} 页", "header": "合同名称", "footerHeight": 1.5, "headerHeight": 1.5, "oddEvenDifferent": false, "firstPageDifferent": false}}', 'f', 't', 0, '2026-04-20 23:09:14+08', '2026-04-20 23:09:14+08');
INSERT INTO "public"."doc_template" VALUES (5, 10, '求职个人模板', '适用于简历、求职信、个人陈述、推荐信', 'system', '{"fontSettings": {"fontSize": 10, "fontFamily": "微软雅黑", "codeFontSize": 10, "headingFonts": {"h1": {"bold": true, "size": 18, "family": "微软雅黑"}, "h2": {"bold": true, "size": 14, "family": "微软雅黑"}, "h3": {"bold": true, "size": 12, "family": "微软雅黑"}, "h4": {"bold": true, "size": 11, "family": "微软雅黑"}, "h5": {"bold": true, "size": 10, "family": "微软雅黑"}, "h6": {"bold": true, "size": 10, "family": "微软雅黑"}}, "codeFontFamily": "Consolas"}, "pageSettings": {"margins": {"top": 2.0, "left": 2.0, "right": 2.0, "bottom": 2.0, "gutter": 0}, "pageSize": "A4", "orientation": "portrait"}, "paragraphSettings": {"alignment": "left", "lineSpacing": 1.15, "firstLineIndent": 0, "paragraphSpacing": {"after": 3, "before": 3}}, "headerFooterSettings": {"footer": "", "header": "", "footerHeight": 1.0, "headerHeight": 1.0, "oddEvenDifferent": false, "firstPageDifferent": false}}', 'f', 't', 0, '2026-04-20 23:09:14+08', '2026-04-20 23:09:14+08');
INSERT INTO "public"."doc_template" VALUES (6, 10, '技术研发模板', '适用于技术文档、API文档、用户手册、开发文档、测试报告', 'system', '{"fontSettings": {"fontSize": 12, "fontFamily": "宋体", "codeFontSize": 10, "headingFonts": {"h1": {"bold": true, "size": 20, "family": "黑体"}, "h2": {"bold": true, "size": 16, "family": "黑体"}, "h3": {"bold": true, "size": 14, "family": "黑体"}, "h4": {"bold": true, "size": 12, "family": "黑体"}, "h5": {"bold": true, "size": 12, "family": "黑体"}, "h6": {"bold": false, "size": 12, "family": "黑体"}}, "codeFontFamily": "Consolas"}, "pageSettings": {"margins": {"top": 2.5, "left": 2.5, "right": 2.5, "bottom": 2.5, "gutter": 0}, "pageSize": "A4", "orientation": "portrait"}, "paragraphSettings": {"alignment": "left", "lineSpacing": 1.5, "firstLineIndent": 2, "paragraphSpacing": {"after": 0, "before": 0}}, "headerFooterSettings": {"footer": "第 {page} 页，共 {total} 页", "header": "技术文档名称 | 版本号：V1.0", "footerHeight": 1.2, "headerHeight": 1.2, "oddEvenDifferent": false, "firstPageDifferent": false}}', 'f', 't', 0, '2026-04-20 23:09:14+08', '2026-04-20 23:09:14+08');
INSERT INTO "public"."doc_template" VALUES (7, 10, '媒体宣传模板', '适用于新闻稿、宣传文案、演讲稿、发言稿、活动策划案', 'system', '{"fontSettings": {"fontSize": 12, "fontFamily": "微软雅黑", "codeFontSize": 10, "headingFonts": {"h1": {"bold": true, "size": 22, "family": "微软雅黑"}, "h2": {"bold": true, "size": 16, "family": "微软雅黑"}, "h3": {"bold": true, "size": 14, "family": "微软雅黑"}, "h4": {"bold": true, "size": 12, "family": "微软雅黑"}, "h5": {"bold": true, "size": 12, "family": "微软雅黑"}, "h6": {"bold": false, "size": 12, "family": "微软雅黑"}}, "codeFontFamily": "Consolas"}, "pageSettings": {"margins": {"top": 2.5, "left": 2.5, "right": 2.5, "bottom": 2.5, "gutter": 0}, "pageSize": "A4", "orientation": "portrait"}, "paragraphSettings": {"alignment": "left", "lineSpacing": 1.5, "firstLineIndent": 2, "paragraphSpacing": {"after": 6, "before": 6}}, "headerFooterSettings": {"footer": "第 {page} 页，共 {total} 页", "header": "宣传主题", "footerHeight": 1.5, "headerHeight": 1.5, "oddEvenDifferent": false, "firstPageDifferent": true}}', 'f', 't', 0, '2026-04-20 23:09:14+08', '2026-04-20 23:09:14+08');
INSERT INTO "public"."doc_template" VALUES (8, 10, '日常通用模板', '适用于个人总结、读书笔记、邮件、便签、通知类小文件', 'system', '{"fontSettings": {"fontSize": 12, "fontFamily": "宋体", "codeFontSize": 10, "headingFonts": {"h1": {"bold": true, "size": 18, "family": "黑体"}, "h2": {"bold": true, "size": 16, "family": "黑体"}, "h3": {"bold": true, "size": 14, "family": "黑体"}, "h4": {"bold": true, "size": 12, "family": "黑体"}, "h5": {"bold": true, "size": 12, "family": "黑体"}, "h6": {"bold": false, "size": 12, "family": "黑体"}}, "codeFontFamily": "Consolas"}, "pageSettings": {"margins": {"top": 2.0, "left": 2.0, "right": 2.0, "bottom": 2.0, "gutter": 0}, "pageSize": "A4", "orientation": "portrait"}, "paragraphSettings": {"alignment": "left", "lineSpacing": 1.25, "firstLineIndent": 2, "paragraphSpacing": {"after": 0, "before": 0}}, "headerFooterSettings": {"footer": "第 {page} 页，共 {total} 页", "header": "", "footerHeight": 1.0, "headerHeight": 1.0, "oddEvenDifferent": false, "firstPageDifferent": false}}', 't', 't', 0, '2026-04-20 23:09:14+08', '2026-04-20 23:09:14+08');
INSERT INTO "public"."doc_template" VALUES (2, 10, '行政公文模板', '适用于通知、公告、函、请示、报告、会议纪要、规章制度', 'system', '{"fontSettings": {"fontSize": 16, "fontFamily": "仿宋_GB2312", "codeFontSize": 10, "headingFonts": {"h1": {"bold": true, "size": 22, "family": "小标宋体"}, "h2": {"bold": true, "size": 18, "family": "仿宋_GB2312"}, "h3": {"bold": true, "size": 16, "family": "仿宋_GB2312"}, "h4": {"bold": true, "size": 16, "family": "仿宋_GB2312"}, "h5": {"bold": false, "size": 16, "family": "仿宋_GB2312"}, "h6": {"bold": false, "size": 16, "family": "仿宋_GB2312"}}, "codeFontFamily": "Consolas"}, "pageSettings": {"margins": {"top": 3.7, "left": 2.8, "right": 2.6, "bottom": 3.5, "gutter": 0}, "pageSize": "A4", "orientation": "portrait"}, "paragraphSettings": {"alignment": "left", "lineSpacing": 28, "firstLineIndent": 2, "paragraphSpacing": {"after": 0, "before": 0}}, "headerFooterSettings": {"footer": "第 {page} 页", "header": "", "footerHeight": 1.5, "headerHeight": 1.5, "oddEvenDifferent": false, "firstPageDifferent": false}}', 'f', 't', 1, '2026-04-20 23:09:14+08', '2026-05-23 19:52:08.097266+08');

-- ----------------------------
-- Table structure for formula_ocr_record
-- ----------------------------
DROP TABLE IF EXISTS "public"."formula_ocr_record";
CREATE TABLE "public"."formula_ocr_record" (
  "id" int8 NOT NULL DEFAULT nextval('formula_ocr_record_id_seq'::regclass),
  "user_id" int8 NOT NULL,
  "status" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'RECOGNIZED'::character varying,
  "error_message" varchar(500) COLLATE "pg_catalog"."default",
  "processing_time_ms" int4,
  "created_at" timestamptz(6) NOT NULL DEFAULT now(),
  "input_content" text COLLATE "pg_catalog"."default",
  "output_content" text COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."formula_ocr_record"."id" IS '主键ID';
COMMENT ON COLUMN "public"."formula_ocr_record"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."formula_ocr_record"."status" IS '状态：RECOGNIZED, FAILED';
COMMENT ON COLUMN "public"."formula_ocr_record"."error_message" IS '错误信息';
COMMENT ON COLUMN "public"."formula_ocr_record"."processing_time_ms" IS '处理耗时（毫秒）';
COMMENT ON COLUMN "public"."formula_ocr_record"."created_at" IS '创建时间';
COMMENT ON TABLE "public"."formula_ocr_record" IS '公式OCR识别记录表';

-- ----------------------------
-- Records of formula_ocr_record
-- ----------------------------
INSERT INTO "public"."formula_ocr_record" VALUES (1, 10, 'RECOGNIZED', NULL, 150, '2026-05-23 19:59:34.309999+08', 'E = mc^2', '\frac{{m{v^2}}}{2} + mgz = C');

-- ----------------------------
-- Table structure for sys_announcement
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_announcement";
CREATE TABLE "public"."sys_announcement" (
  "id" int8 NOT NULL DEFAULT nextval('sys_announcement_id_seq'::regclass),
  "title" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "content" text COLLATE "pg_catalog"."default" NOT NULL,
  "announcement_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'notice'::character varying,
  "is_published" bool NOT NULL DEFAULT false,
  "published_at" timestamptz(6),
  "expire_at" timestamptz(6),
  "created_by" int8 NOT NULL,
  "created_at" timestamptz(6) NOT NULL DEFAULT now(),
  "updated_at" timestamptz(6) NOT NULL DEFAULT now(),
  "image_urls" text[] COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."sys_announcement"."id" IS '主键ID';
COMMENT ON COLUMN "public"."sys_announcement"."title" IS '公告标题';
COMMENT ON COLUMN "public"."sys_announcement"."content" IS '公告内容';
COMMENT ON COLUMN "public"."sys_announcement"."announcement_type" IS '公告类型：notice-通知, thanks-感谢信, update-更新日志';
COMMENT ON COLUMN "public"."sys_announcement"."is_published" IS '是否已发布';
COMMENT ON COLUMN "public"."sys_announcement"."published_at" IS '发布时间';
COMMENT ON COLUMN "public"."sys_announcement"."expire_at" IS '过期时间';
COMMENT ON COLUMN "public"."sys_announcement"."created_by" IS '创建人ID';
COMMENT ON COLUMN "public"."sys_announcement"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."sys_announcement"."updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."sys_announcement"."image_urls" IS '图片URL数组';
COMMENT ON TABLE "public"."sys_announcement" IS '系统公告表';

-- ----------------------------
-- Records of sys_announcement
-- ----------------------------
INSERT INTO "public"."sys_announcement" VALUES (2, '欢迎使用AI文档转换引擎', 'AI文档转换引擎已正式上线，支持Markdown转换为Word文档，支持数学公式、化学公式等多种公式类型。', 'notice', 't', '2026-05-23 19:59:34.307441+08', NULL, 10, '2026-05-23 19:59:34.307441+08', '2026-05-23 19:59:34.307441+08', NULL);

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_login_log";
CREATE TABLE "public"."sys_login_log" (
  "id" int8 NOT NULL DEFAULT nextval('sys_login_log_id_seq'::regclass),
  "user_id" int8,
  "username_or_email" varchar(128) COLLATE "pg_catalog"."default",
  "login_result" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "ip_address" varchar(64) COLLATE "pg_catalog"."default",
  "user_agent" varchar(500) COLLATE "pg_catalog"."default",
  "fail_reason" varchar(255) COLLATE "pg_catalog"."default",
  "created_at" timestamptz(6) NOT NULL DEFAULT now(),
  "browser_type" varchar(20) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."sys_login_log"."id" IS '主键ID';
COMMENT ON COLUMN "public"."sys_login_log"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."sys_login_log"."username_or_email" IS '登录账号';
COMMENT ON COLUMN "public"."sys_login_log"."login_result" IS '登录结果';
COMMENT ON COLUMN "public"."sys_login_log"."ip_address" IS 'IP地址';
COMMENT ON COLUMN "public"."sys_login_log"."user_agent" IS '用户代理';
COMMENT ON COLUMN "public"."sys_login_log"."fail_reason" IS '失败原因';
COMMENT ON COLUMN "public"."sys_login_log"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."sys_login_log"."browser_type" IS '浏览器类型：Chrome, Edge, Firefox, Safari, Other';
COMMENT ON TABLE "public"."sys_login_log" IS '登录日志表';

-- ----------------------------
-- Records of sys_login_log
-- ----------------------------
INSERT INTO "public"."sys_login_log" VALUES (95, 10, 'admin', 'success', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', NULL, '2026-05-23 18:32:18.183097+08', 'Chrome');
INSERT INTO "public"."sys_login_log" VALUES (97, 10, 'admin', 'success', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', NULL, '2026-05-24 11:20:01.14672+08', 'Chrome');
INSERT INTO "public"."sys_login_log" VALUES (99, 10, 'admin', 'success', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', NULL, '2026-05-24 16:10:15.560686+08', 'Chrome');
INSERT INTO "public"."sys_login_log" VALUES (96, 10, 'admin', 'success', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', NULL, '2026-05-23 19:26:28.692433+08', 'Chrome');
INSERT INTO "public"."sys_login_log" VALUES (98, 10, 'admin', 'success', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', NULL, '2026-05-24 12:49:13.088928+08', 'Chrome');
INSERT INTO "public"."sys_login_log" VALUES (92, 10, 'admin', 'success', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', NULL, '2026-05-20 21:10:44.925355+08', 'Chrome');
INSERT INTO "public"."sys_login_log" VALUES (93, 10, 'admin', 'success', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', NULL, '2026-05-23 15:53:26.90958+08', 'Chrome');
INSERT INTO "public"."sys_login_log" VALUES (94, 10, 'admin', 'success', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', NULL, '2026-05-23 16:06:29.158246+08', 'Chrome');

-- ----------------------------
-- Table structure for sys_password_reset_token
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_password_reset_token";
CREATE TABLE "public"."sys_password_reset_token" (
  "id" int8 NOT NULL DEFAULT nextval('sys_password_reset_token_id_seq'::regclass),
  "user_id" int8 NOT NULL,
  "email" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "token" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "expire_at" timestamptz(6) NOT NULL,
  "used" bool NOT NULL DEFAULT false,
  "created_at" timestamptz(6) NOT NULL DEFAULT now(),
  "used_at" timestamptz(6)
)
;
COMMENT ON COLUMN "public"."sys_password_reset_token"."id" IS '主键ID';
COMMENT ON COLUMN "public"."sys_password_reset_token"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."sys_password_reset_token"."email" IS '邮箱';
COMMENT ON COLUMN "public"."sys_password_reset_token"."token" IS '重置令牌';
COMMENT ON COLUMN "public"."sys_password_reset_token"."expire_at" IS '过期时间';
COMMENT ON COLUMN "public"."sys_password_reset_token"."used" IS '是否已使用';
COMMENT ON COLUMN "public"."sys_password_reset_token"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."sys_password_reset_token"."used_at" IS '使用时间';
COMMENT ON TABLE "public"."sys_password_reset_token" IS '密码重置令牌表';

-- ----------------------------
-- Records of sys_password_reset_token
-- ----------------------------
INSERT INTO "public"."sys_password_reset_token" VALUES (1, 10, 'admin@example.com', 'test-token-for-demo-purposes-only', '2026-05-24 12:59:34.314231+08', 'f', '2026-05-23 19:59:34.314231+08', '2026-05-23 21:59:34.314231+08');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_user";
CREATE TABLE "public"."sys_user" (
  "id" int8 NOT NULL DEFAULT nextval('sys_user_id_seq'::regclass),
  "username" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "email" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "password_hash" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "nickname" varchar(64) COLLATE "pg_catalog"."default",
  "avatar_url" varchar(255) COLLATE "pg_catalog"."default",
  "role" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'USER'::user_role,
  "status" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'active'::user_status,
  "last_login_at" timestamptz(6),
  "last_login_ip" varchar(64) COLLATE "pg_catalog"."default",
  "created_at" timestamptz(6) NOT NULL DEFAULT now(),
  "updated_at" timestamptz(6) NOT NULL DEFAULT now(),
  "department" varchar(100) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."sys_user"."id" IS '主键ID';
COMMENT ON COLUMN "public"."sys_user"."username" IS '用户名';
COMMENT ON COLUMN "public"."sys_user"."email" IS '邮箱';
COMMENT ON COLUMN "public"."sys_user"."password_hash" IS '密码哈希（BCrypt）';
COMMENT ON COLUMN "public"."sys_user"."nickname" IS '昵称';
COMMENT ON COLUMN "public"."sys_user"."avatar_url" IS '头像URL';
COMMENT ON COLUMN "public"."sys_user"."role" IS '用户角色';
COMMENT ON COLUMN "public"."sys_user"."status" IS '用户状态';
COMMENT ON COLUMN "public"."sys_user"."last_login_at" IS '最后登录时间';
COMMENT ON COLUMN "public"."sys_user"."last_login_ip" IS '最后登录IP';
COMMENT ON COLUMN "public"."sys_user"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."sys_user"."updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."sys_user"."department" IS '部门';
COMMENT ON TABLE "public"."sys_user" IS '用户表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO "public"."sys_user" VALUES (10, 'admin', 'admin@example.com', '$2a$10$/XncWou71LX0GNvns8f40u9HASfhDMc1bTjPv0Eg/gjN6lUVDwGLC', '管理员', '/uploads/avatar/81ecc46f-bf27-42bf-bae9-ac23d8f51e4e.jpg', 'ADMIN', 'active', '2026-05-24 16:10:15.552189+08', '127.0.0.1', '2026-05-20 20:49:28.213873+08', '2026-05-24 16:10:15.224894+08', '数学与大数据学院');
INSERT INTO "public"."sys_user" VALUES (11, 'lyj3401456945', '3401456945@qq.com', '$2a$10$jvljLvGCSt2WHt4pU5cFvuL0HhBODxUiwl8qsk0VUOVCmR5jlVQjG', '哈哈', '', 'USER', 'active', NULL, NULL, '2026-05-24 12:36:38.511338+08', '2026-05-24 12:48:58.889554+08', '数学与大数据学院');

-- ----------------------------
-- Table structure for user_feedback
-- ----------------------------
DROP TABLE IF EXISTS "public"."user_feedback";
CREATE TABLE "public"."user_feedback" (
  "id" int8 NOT NULL DEFAULT nextval('user_feedback_id_seq'::regclass),
  "user_id" int8 NOT NULL,
  "content" text COLLATE "pg_catalog"."default" NOT NULL,
  "feedback_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'suggestion'::character varying,
  "status" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'pending'::character varying,
  "priority" varchar(20) COLLATE "pg_catalog"."default" DEFAULT 'normal'::character varying,
  "image_urls" text[] COLLATE "pg_catalog"."default",
  "processed_by" int8,
  "created_at" timestamptz(6) NOT NULL DEFAULT now(),
  "updated_at" timestamptz(6) NOT NULL DEFAULT now()
)
;
COMMENT ON COLUMN "public"."user_feedback"."id" IS '主键ID';
COMMENT ON COLUMN "public"."user_feedback"."user_id" IS '提交用户ID';
COMMENT ON COLUMN "public"."user_feedback"."content" IS '反馈内容';
COMMENT ON COLUMN "public"."user_feedback"."feedback_type" IS '反馈类型：suggestion-建议, bug-问题反馈, feature-功能请求, other-其他';
COMMENT ON COLUMN "public"."user_feedback"."status" IS '处理状态：pending-待处理, processing-处理中, resolved-已解决, closed-已关闭';
COMMENT ON COLUMN "public"."user_feedback"."priority" IS '优先级：low-低, normal-普通, high-高, urgent-紧急';
COMMENT ON COLUMN "public"."user_feedback"."image_urls" IS '图片URL数组';
COMMENT ON COLUMN "public"."user_feedback"."processed_by" IS '处理人ID';
COMMENT ON COLUMN "public"."user_feedback"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."user_feedback"."updated_at" IS '更新时间';
COMMENT ON TABLE "public"."user_feedback" IS '用户反馈表';

-- ----------------------------
-- Records of user_feedback
-- ----------------------------
INSERT INTO "public"."user_feedback" VALUES (2, 10, '系统运行稳定，文档转换速度快，非常好用！', 'suggestion', 'resolved', 'normal', NULL, 10, '2026-05-23 19:59:34.302082+08', '2026-05-23 20:05:26.768074+08');
INSERT INTO "public"."user_feedback" VALUES (3, 10, '哈哈，系统运行稳定，文档转换速度快，非常好用！', 'other', 'pending', 'normal', '{/uploads/feedback/3da761bf-aac2-4b58-86ca-0f31b81b0e0a.jpg,/uploads/feedback/66f83d2c-aa31-43ba-b0d7-b2255fa6e7c9.jpg}', NULL, '2026-05-24 12:14:32.033772+08', '2026-05-24 12:14:32.033772+08');

-- ----------------------------
-- Procedure structure for cleanup_expired_tokens
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."cleanup_expired_tokens"();
CREATE OR REPLACE PROCEDURE "public"."cleanup_expired_tokens"()
 AS $BODY$
BEGIN
    DELETE FROM sys_password_reset_token
    WHERE expire_at < NOW() - INTERVAL '7 days';
END;
$BODY$
  LANGUAGE plpgsql;

-- ----------------------------
-- Procedure structure for cleanup_old_login_logs
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."cleanup_old_login_logs"();
CREATE OR REPLACE PROCEDURE "public"."cleanup_old_login_logs"()
 AS $BODY$
BEGIN
    DELETE FROM sys_login_log
    WHERE created_at < NOW() - INTERVAL '90 days';
END;
$BODY$
  LANGUAGE plpgsql;

-- ----------------------------
-- Function structure for count_ip_failures
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."count_ip_failures"("p_ip" varchar, "p_minutes" int4);
CREATE OR REPLACE FUNCTION "public"."count_ip_failures"("p_ip" varchar, "p_minutes" int4=15)
  RETURNS "pg_catalog"."int8" AS $BODY$
BEGIN
    RETURN (
        SELECT COUNT(*)
        FROM sys_login_log
        WHERE ip_address = p_ip
          AND login_result = 'failed'
          AND created_at > NOW() - (p_minutes || ' minutes')::INTERVAL
    );
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- ----------------------------
-- Function structure for count_login_failures
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."count_login_failures"("p_username_or_email" varchar, "p_minutes" int4);
CREATE OR REPLACE FUNCTION "public"."count_login_failures"("p_username_or_email" varchar, "p_minutes" int4=15)
  RETURNS "pg_catalog"."int8" AS $BODY$
BEGIN
    RETURN (
        SELECT COUNT(*)
        FROM sys_login_log
        WHERE username_or_email = p_username_or_email
          AND login_result = 'failed'
          AND created_at > NOW() - (p_minutes || ' minutes')::INTERVAL
    );
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- ----------------------------
-- Function structure for should_lock_account
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."should_lock_account"("p_username_or_email" varchar, "p_ip" varchar);
CREATE OR REPLACE FUNCTION "public"."should_lock_account"("p_username_or_email" varchar, "p_ip" varchar)
  RETURNS "pg_catalog"."bool" AS $BODY$
DECLARE
    v_user_failures BIGINT;
    v_ip_failures BIGINT;
BEGIN
    v_user_failures := count_login_failures(p_username_or_email, 15);
    v_ip_failures := count_ip_failures(p_ip, 15);
    
    RETURN (v_user_failures >= 5 OR v_ip_failures >= 10);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- ----------------------------
-- Function structure for update_updated_at_column
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."update_updated_at_column"();
CREATE OR REPLACE FUNCTION "public"."update_updated_at_column"()
  RETURNS "pg_catalog"."trigger" AS $BODY$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."doc_export_task_id_seq"', 2, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."doc_template_id_seq"
OWNED BY "public"."doc_template"."id";
SELECT setval('"public"."doc_template_id_seq"', 9, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."formula_ocr_record_id_seq"', 1, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_announcement_id_seq"
OWNED BY "public"."sys_announcement"."id";
SELECT setval('"public"."sys_announcement_id_seq"', 2, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_login_log_id_seq"
OWNED BY "public"."sys_login_log"."id";
SELECT setval('"public"."sys_login_log_id_seq"', 99, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_password_reset_token_id_seq"
OWNED BY "public"."sys_password_reset_token"."id";
SELECT setval('"public"."sys_password_reset_token_id_seq"', 12, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_user_id_seq"
OWNED BY "public"."sys_user"."id";
SELECT setval('"public"."sys_user_id_seq"', 11, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."user_feedback_id_seq"
OWNED BY "public"."user_feedback"."id";
SELECT setval('"public"."user_feedback_id_seq"', 3, true);

-- ----------------------------
-- Indexes structure for table doc_export_task
-- ----------------------------
CREATE INDEX "idx_export_status" ON "public"."doc_export_task" USING btree (
  "task_status" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_export_user" ON "public"."doc_export_task" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table doc_export_task
-- ----------------------------
ALTER TABLE "public"."doc_export_task" ADD CONSTRAINT "doc_export_task_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table doc_template
-- ----------------------------
CREATE INDEX "idx_template_config" ON "public"."doc_template" USING gin (
  "config_json" "pg_catalog"."jsonb_ops"
);
CREATE INDEX "idx_template_created_at" ON "public"."doc_template" USING btree (
  "created_at" "pg_catalog"."timestamptz_ops" DESC NULLS FIRST
);

-- ----------------------------
-- Triggers structure for table doc_template
-- ----------------------------
CREATE TRIGGER "trigger_template_updated_at" BEFORE UPDATE ON "public"."doc_template"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Primary Key structure for table doc_template
-- ----------------------------
ALTER TABLE "public"."doc_template" ADD CONSTRAINT "doc_template_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table formula_ocr_record
-- ----------------------------
CREATE INDEX "idx_ocr_created" ON "public"."formula_ocr_record" USING btree (
  "created_at" "pg_catalog"."timestamptz_ops" DESC NULLS FIRST
);
CREATE INDEX "idx_ocr_status" ON "public"."formula_ocr_record" USING btree (
  "status" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_ocr_user" ON "public"."formula_ocr_record" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table formula_ocr_record
-- ----------------------------
ALTER TABLE "public"."formula_ocr_record" ADD CONSTRAINT "formula_ocr_record_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table sys_announcement
-- ----------------------------
CREATE INDEX "idx_announcement_creator" ON "public"."sys_announcement" USING btree (
  "created_by" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_announcement_expire" ON "public"."sys_announcement" USING btree (
  "expire_at" "pg_catalog"."timestamptz_ops" ASC NULLS LAST
) WHERE is_published = true;
CREATE INDEX "idx_announcement_published" ON "public"."sys_announcement" USING btree (
  "is_published" "pg_catalog"."bool_ops" ASC NULLS LAST,
  "published_at" "pg_catalog"."timestamptz_ops" DESC NULLS FIRST
);
CREATE INDEX "idx_announcement_type" ON "public"."sys_announcement" USING btree (
  "announcement_type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table sys_announcement
-- ----------------------------
CREATE TRIGGER "trigger_announcement_updated_at" BEFORE UPDATE ON "public"."sys_announcement"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Primary Key structure for table sys_announcement
-- ----------------------------
ALTER TABLE "public"."sys_announcement" ADD CONSTRAINT "sys_announcement_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table sys_login_log
-- ----------------------------
CREATE INDEX "idx_login_created_at" ON "public"."sys_login_log" USING btree (
  "created_at" "pg_catalog"."timestamptz_ops" DESC NULLS FIRST
);
CREATE INDEX "idx_login_ip" ON "public"."sys_login_log" USING btree (
  "ip_address" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_login_log_fail_stat" ON "public"."sys_login_log" USING btree (
  "username_or_email" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "login_result" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "created_at" "pg_catalog"."timestamptz_ops" ASC NULLS LAST
) WHERE login_result::text = 'failed'::text;
CREATE INDEX "idx_login_result" ON "public"."sys_login_log" USING btree (
  "login_result" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "created_at" "pg_catalog"."timestamptz_ops" DESC NULLS FIRST
);
CREATE INDEX "idx_login_user" ON "public"."sys_login_log" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table sys_login_log
-- ----------------------------
ALTER TABLE "public"."sys_login_log" ADD CONSTRAINT "sys_login_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table sys_password_reset_token
-- ----------------------------
CREATE INDEX "idx_reset_token_email" ON "public"."sys_password_reset_token" USING btree (
  "email" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_reset_token_expire" ON "public"."sys_password_reset_token" USING btree (
  "expire_at" "pg_catalog"."timestamptz_ops" ASC NULLS LAST
) WHERE used = false;
CREATE INDEX "idx_reset_token_user" ON "public"."sys_password_reset_token" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Uniques structure for table sys_password_reset_token
-- ----------------------------
ALTER TABLE "public"."sys_password_reset_token" ADD CONSTRAINT "uk_reset_token" UNIQUE ("token");

-- ----------------------------
-- Primary Key structure for table sys_password_reset_token
-- ----------------------------
ALTER TABLE "public"."sys_password_reset_token" ADD CONSTRAINT "sys_password_reset_token_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table sys_user
-- ----------------------------
CREATE INDEX "idx_user_created_at" ON "public"."sys_user" USING btree (
  "created_at" "pg_catalog"."timestamptz_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table sys_user
-- ----------------------------
CREATE TRIGGER "trigger_user_updated_at" BEFORE UPDATE ON "public"."sys_user"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Uniques structure for table sys_user
-- ----------------------------
ALTER TABLE "public"."sys_user" ADD CONSTRAINT "uk_user_username" UNIQUE ("username");
ALTER TABLE "public"."sys_user" ADD CONSTRAINT "uk_user_email" UNIQUE ("email");

-- ----------------------------
-- Primary Key structure for table sys_user
-- ----------------------------
ALTER TABLE "public"."sys_user" ADD CONSTRAINT "sys_user_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table user_feedback
-- ----------------------------
CREATE INDEX "idx_feedback_created" ON "public"."user_feedback" USING btree (
  "created_at" "pg_catalog"."timestamptz_ops" DESC NULLS FIRST
);
CREATE INDEX "idx_feedback_priority" ON "public"."user_feedback" USING btree (
  "priority" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_feedback_processed" ON "public"."user_feedback" USING btree (
  "processed_by" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_feedback_status" ON "public"."user_feedback" USING btree (
  "status" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_feedback_type" ON "public"."user_feedback" USING btree (
  "feedback_type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_feedback_user" ON "public"."user_feedback" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table user_feedback
-- ----------------------------
CREATE TRIGGER "trigger_feedback_updated_at" BEFORE UPDATE ON "public"."user_feedback"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Primary Key structure for table user_feedback
-- ----------------------------
ALTER TABLE "public"."user_feedback" ADD CONSTRAINT "user_feedback_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table doc_export_task
-- ----------------------------
ALTER TABLE "public"."doc_export_task" ADD CONSTRAINT "fk_export_user" FOREIGN KEY ("user_id") REFERENCES "public"."sys_user" ("id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table doc_template
-- ----------------------------
ALTER TABLE "public"."doc_template" ADD CONSTRAINT "fk_template_user" FOREIGN KEY ("user_id") REFERENCES "public"."sys_user" ("id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table formula_ocr_record
-- ----------------------------
ALTER TABLE "public"."formula_ocr_record" ADD CONSTRAINT "fk_ocr_user" FOREIGN KEY ("user_id") REFERENCES "public"."sys_user" ("id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table sys_announcement
-- ----------------------------
ALTER TABLE "public"."sys_announcement" ADD CONSTRAINT "fk_announcement_creator" FOREIGN KEY ("created_by") REFERENCES "public"."sys_user" ("id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table sys_login_log
-- ----------------------------
ALTER TABLE "public"."sys_login_log" ADD CONSTRAINT "fk_login_log_user" FOREIGN KEY ("user_id") REFERENCES "public"."sys_user" ("id") ON DELETE SET NULL ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table sys_password_reset_token
-- ----------------------------
ALTER TABLE "public"."sys_password_reset_token" ADD CONSTRAINT "fk_reset_user" FOREIGN KEY ("user_id") REFERENCES "public"."sys_user" ("id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table user_feedback
-- ----------------------------
ALTER TABLE "public"."user_feedback" ADD CONSTRAINT "fk_feedback_processor" FOREIGN KEY ("processed_by") REFERENCES "public"."sys_user" ("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."user_feedback" ADD CONSTRAINT "fk_feedback_user" FOREIGN KEY ("user_id") REFERENCES "public"."sys_user" ("id") ON DELETE CASCADE ON UPDATE NO ACTION;
