# AI Doc Engine — Code Wiki

> **AI 文档转换引擎**：将 Markdown 文档智能转换为可编辑的 Word 文档，支持 LaTeX 公式（OMML）、Mermaid 流程图、表格、代码块等丰富内容。

---

## 目录

1. [项目概述](#1-项目概述)
2. [整体架构](#2-整体架构)
3. [技术栈与依赖](#3-技术栈与依赖)
4. [项目目录结构](#4-项目目录结构)
5. [后端模块详解](#5-后端模块详解)
   - 5.1 [启动与配置](#51-启动与配置)
   - 5.2 [公共模块 (common)](#52-公共模块-common)
   - 5.3 [安全模块 (security)](#53-安全模块-security)
   - 5.4 [数据模型 (model)](#54-数据模型-model)
   - 5.5 [数据访问层 (repository)](#55-数据访问层-repository)
   - 5.6 [业务逻辑层 (service)](#56-业务逻辑层-service)
   - 5.7 [解析器 (parser)](#57-解析器-parser)
   - 5.8 [渲染器 (renderer)](#58-渲染器-renderer)
   - 5.9 [导出器 (exporter)](#59-导出器-exporter)
   - 5.10 [控制器 (controller)](#510-控制器-controller)
   - 5.11 [工具类 (util)](#511-工具类-util)
6. [前端模块详解](#6-前端模块详解)
   - 6.1 [技术架构](#61-技术架构)
   - 6.2 [路由与页面](#62-路由与页面)
   - 6.3 [API 层](#63-api-层)
   - 6.4 [状态管理](#64-状态管理)
   - 6.5 [组件体系](#65-组件体系)
   - 6.6 [工具函数](#66-工具函数)
7. [核心数据流](#7-核心数据流)
8. [UDM 统一文档模型](#8-udm-统一文档模型)
9. [公式转换链路](#9-公式转换链路)
10. [依赖关系图](#10-依赖关系图)
11. [项目运行方式](#11-项目运行方式)
12. [API 接口一览](#12-api-接口一览)
13. [错误码体系](#13-错误码体系)

---

## 1. 项目概述

| 属性 | 值 |
|------|-----|
| 项目名称 | AI Doc Engine（AI 文档转换引擎） |
| 核心能力 | Markdown → 可编辑 Word 文档 |
| 后端框架 | Spring Boot 3.2.0 / Java 17 |
| 前端框架 | Vue 3.4 + TypeScript + Vite 5 |
| 数据库 | MySQL 8.0 |
| 认证方式 | Spring Security + JWT |
| 文档模型 | UDM（统一文档模型） |
| 公式支持 | LaTeX → MathML → OMML（Word 原生公式） |
| 流程图支持 | Mermaid → PNG 图片（未来升级 DrawingML） |
| 导出引擎 | docx4j 11.4.9 |

**核心价值**：将 AI 生成的 Markdown 文档转换为格式规范、完全可编辑的 Word 文档，公式以 Word 原生 OMML 格式呈现，表格和代码块均使用 Word 原生格式。

---

## 2. 整体架构

```
┌──────────────────────────────────────────────────────────────────┐
│                     Ai-doc-engine-web (前端)                      │
│  Vue 3 + Pinia + Element Plus + KaTeX + Mermaid.js              │
│  端口: 3514  |  Vite Dev Server + Proxy → :8687                  │
└────────────────────────┬─────────────────────────────────────────┘
                         │ HTTP (REST API)
                         ▼
┌──────────────────────────────────────────────────────────────────┐
│                   Ai-doc-engine-server (后端)                     │
│  Spring Boot 3.2.0  |  端口: 8687                                │
│                                                                  │
│  ┌─────────┐   ┌──────────┐   ┌──────────┐   ┌──────────────┐  │
│  │Controller│──▶│ Service  │──▶│  Parser  │──▶│ UDM Document │  │
│  └─────────┘   └──────────┘   └──────────┘   └──────┬───────┘  │
│                                                      │          │
│  ┌─────────┐   ┌──────────┐   ┌──────────┐   ┌──────▼───────┐  │
│  │Exporter │◀──│ Renderer │◀──│TemplateAp│   │  FormulaConv │  │
│  │(docx4j) │   │(7种Block)│   │  plier   │   │ (MathJax)    │  │
│  └─────────┘   └──────────┘   └──────────┘   └──────────────┘  │
│                                                                  │
│  ┌──────────────────┐  ┌────────────┐  ┌─────────────────────┐  │
│  │ Spring Security   │  │ MySQL/JPA  │  │ Mermaid CLI (mmdc)  │  │
│  │ + JWT + OAuth2   │  │            │  │ (Node.js 子进程)    │  │
│  └──────────────────┘  └────────────┘  └─────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
```

**架构分层**：

- **前端**：SPA 单页应用，负责 Markdown 编辑、实时预览、模板选择、文件导出
- **后端 Controller 层**：REST API 入口，参数校验，响应封装
- **后端 Service 层**：业务逻辑编排，事务管理
- **后端 Parser 层**：Markdown → UDM 解析（基于 flexmark-java）
- **后端 Renderer 层**：UDM → Word 渲染（基于 docx4j），策略模式按 Block 类型分发
- **后端 Exporter 层**：Word 文档组装与输出

---

## 3. 技术栈与依赖

### 后端核心依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.0 | 应用框架 |
| Spring Data JPA | (Boot 管理) | ORM / 数据访问 |
| Spring Security | (Boot 管理) | 认证与授权 |
| Spring Boot Starter Mail | (Boot 管理) | 邮件发送 |
| Spring Boot Starter Thymeleaf | (Boot 管理) | 邮件模板 |
| MySQL Connector/J | (Boot 管理) | MySQL 驱动 |
| jjwt | 0.12.3 | JWT Token 生成与验证 |
| flexmark-java | 0.64.8 | Markdown AST 解析 |
| docx4j | 11.4.9 | Word 文档生成（OOXML） |
| Lombok | 1.18.30 | 代码简化 |
| Hutool | 5.8.25 | 工具库（OAuth HTTP 请求） |
| Jackson | (Boot 管理) | JSON 序列化 |
| Apache Commons Lang | 3.14.0 | 字符串/对象工具 |
| Apache Commons IO | 2.15.1 | IO 工具 |
| SnakeYAML | 2.2 | YAML 解析 |

### 后端外部依赖

| 依赖 | 用途 |
|------|------|
| Node.js 16+ | MathJax 公式转换脚本运行时 |
| mathjax-full (npm) | LaTeX → MathML 转换 |
| Mermaid CLI (mmdc) | Mermaid 代码 → PNG 渲染 |

### 前端核心依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| Vue | ^3.4.21 | UI 框架 |
| Vue Router | ^4.3.0 | 路由管理 |
| Pinia | ^2.1.7 | 状态管理 |
| Axios | ^1.6.8 | HTTP 请求 |
| Element Plus | ^2.6.3 | UI 组件库 |
| KaTeX | ^0.16.10 | 公式实时预览渲染 |
| Mermaid | ^10.9.0 | 流程图实时预览渲染 |
| Vite | ^5.2.0 | 构建工具 |
| TypeScript | ^5.4.3 | 类型系统 |
| Vitest | ^1.4.0 | 单元测试 |

---

## 4. 项目目录结构

```
Ai-doc-engine/
├── Ai-doc-engine-server/              # 后端 Spring Boot 项目
│   ├── pom.xml                        # Maven 配置
│   ├── mathjax-to-mathml.js           # MathJax Node.js 转换脚本
│   ├── package.json                   # Node.js 依赖（mathjax-full）
│   ├── src/main/java/com/aidoc/engine/
│   │   ├── AiDocEngineApplication.java    # 启动类
│   │   ├── common/                        # 公共模块
│   │   │   ├── exception/                     # 异常体系
│   │   │   │   ├── BusinessException.java         # 业务异常
│   │   │   │   ├── EnhancedGlobalExceptionHandler.java  # 全局异常处理器
│   │   │   │   └── ErrorCode.java                 # 错误码枚举
│   │   │   ├── response/                      # 统一响应
│   │   │   │   └── ApiResponse.java               # 统一 API 响应格式
│   │   │   └── OauthProperties.java           # OAuth 配置属性
│   │   ├── config/                        # 配置类
│   │   │   ├── AppOcrProperties.java           # OCR 配置属性
│   │   │   ├── CorsConfig.java                 # 跨域配置
│   │   │   ├── FormulaConverterConfig.java      # 公式转换配置
│   │   │   ├── MermaidProperties.java           # Mermaid 配置属性
│   │   │   ├── OcrClientConfig.java             # OCR 客户端配置
│   │   │   └── SecurityConfig.java              # Spring Security 配置
│   │   ├── controller/                    # REST 控制器
│   │   │   ├── AuthController.java             # 认证接口
│   │   │   ├── DocumentController.java         # 文档接口
│   │   │   ├── FormulaController.java          # 公式接口
│   │   │   ├── OauthController.java            # OAuth2 统一登录
│   │   │   └── TemplateController.java         # 模板接口
│   │   ├── enums/                         # 枚举
│   │   │   ├── BlockType.java                  # UDM Block 类型枚举
│   │   │   └── TemplateType.java               # 模板类型枚举
│   │   ├── exporter/                      # 导出器
│   │   │   ├── WordExporter.java               # 导出器接口
│   │   │   └── impl/
│   │   │       └── Docx4jWordExporter.java         # docx4j 实现
│   │   ├── model/                         # 数据模型
│   │   │   ├── entity/                         # JPA 实体
│   │   │   │   ├── UserEntity.java
│   │   │   │   ├── TemplateEntity.java
│   │   │   │   ├── LoginLogEntity.java
│   │   │   │   └── PasswordResetTokenEntity.java
│   │   │   └── udm/                            # 统一文档模型
│   │   │       ├── UdmDocument.java                # 文档根对象
│   │   │       ├── UdmBlock.java                   # Block 抽象基类
│   │   │       ├── block/                          # 具体 Block 类型
│   │   │       │   ├── HeadingBlock.java
│   │   │       │   ├── ParagraphBlock.java
│   │   │       │   ├── ListBlock.java
│   │   │       │   ├── TableBlock.java
│   │   │       │   ├── FormulaBlock.java
│   │   │       │   ├── FlowchartBlock.java
│   │   │       │   ├── CodeBlock.java
│   │   │       │   ├── TaskListBlock.java
│   │   │       │   └── FootnoteBlock.java
│   │   │       └── content/                        # Block 内容模型
│   │   │           ├── HeadingContent.java
│   │   │           ├── ParagraphContent.java
│   │   │           ├── RichText.java
│   │   │           ├── ListContent.java
│   │   │           ├── TableContent.java
│   │   │           ├── FormulaContent.java
│   │   │           ├── FlowchartContent.java
│   │   │           ├── CodeBlockContent.java
│   │   │           ├── TaskListContent.java
│   │   │           └── FootnoteContent.java
│   │   ├── parser/                        # 解析器
│   │   │   ├── DocumentParser.java             # 解析器接口
│   │   │   └── impl/
│   │   │       └── ExtendedMarkdownDocumentParser.java  # 扩展 Markdown 解析器
│   │   ├── renderer/                      # 渲染器
│   │   │   ├── BlockRenderer.java              # Block 渲染器接口
│   │   │   ├── DocumentRenderer.java           # 文档渲染器（分发器）
│   │   │   ├── TemplateApplier.java            # 模板应用器
│   │   │   └── impl/
│   │   │       ├── HeadingRenderer.java
│   │   │       ├── ParagraphRenderer.java
│   │   │       ├── ListRenderer.java
│   │   │       ├── TableRenderer.java
│   │   │       ├── FormulaRenderer.java
│   │   │       ├── FlowchartRenderer.java
│   │   │       └── CodeBlockRenderer.java
│   │   ├── repository/                    # 数据访问层
│   │   │   ├── UserRepository.java
│   │   │   ├── TemplateRepository.java
│   │   │   ├── LoginLogRepository.java
│   │   │   └── PasswordResetTokenRepository.java
│   │   ├── security/                      # 安全模块
│   │   │   ├── JwtTokenProvider.java           # JWT Token 工具
│   │   │   ├── JwtAuthenticationFilter.java    # JWT 过滤器
│   │   │   ├── JwtAuthenticationEntryPoint.java # 认证入口点
│   │   │   └── CustomUserDetailsService.java   # 用户详情服务
│   │   ├── service/                       # 业务逻辑层
│   │   │   ├── AuthService.java
│   │   │   ├── DocumentService.java
│   │   │   ├── FormulaService.java
│   │   │   ├── FormulaConvertService.java
│   │   │   ├── TemplateService.java
│   │   │   ├── EmailService.java
│   │   │   ├── LoginLogService.java
│   │   │   ├── PasswordResetService.java
│   │   │   ├── MermaidParserService.java
│   │   │   ├── MermaidRenderService.java
│   │   │   ├── OAuthLoginTicketService.java
│   │   │   └── impl/                           # 实现类
│   │   │       ├── AuthServiceImpl.java
│   │   │       ├── DocumentServiceImpl.java
│   │   │       ├── FormulaServiceImpl.java
│   │   │       ├── MathJaxFormulaConvertService.java
│   │   │       ├── TemplateServiceImpl.java
│   │   │       ├── EmailServiceImpl.java
│   │   │       ├── LoginLogServiceImpl.java
│   │   │       ├── PasswordResetServiceImpl.java
│   │   │       ├── MermaidParserServiceImpl.java
│   │   │       └── MermaidRenderServiceImpl.java
│   │   └── util/                          # 工具类
│   │       ├── OmmlCleaner.java                # OMML 清理工具
│   │       └── TextNormalizer.java             # 文本规范化工具
│   └── src/main/resources/
│       ├── application.yml                    # 主配置
│       ├── application-dev.yml                # 开发环境配置
│       ├── application-prod.yml               # 生产环境配置
│       └── MML2OMML.XSL                       # MathML→OMML XSLT 转换表
│
├── Ai-doc-engine-web/                  # 前端 Vue 3 项目
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── index.html
│   └── src/
│       ├── main.ts                          # 应用入口
│       ├── App.vue                          # 根组件
│       ├── router/index.ts                  # 路由配置
│       ├── api/                             # API 请求层
│       │   ├── http.ts                          # Axios 封装（拦截器）
│       │   ├── auth.ts                          # 认证 API
│       │   ├── document.ts                      # 文档 API
│       │   ├── formula.ts                       # 公式 API
│       │   └── template.ts                      # 模板 API
│       ├── store/                           # Pinia 状态管理
│       │   ├── auth.ts                          # 认证状态
│       │   ├── document.ts                      # 文档状态
│       │   └── template.ts                      # 模板状态
│       ├── types/                           # TypeScript 类型定义
│       │   ├── auth.ts
│       │   ├── document.ts
│       │   ├── formula.ts
│       │   └── template.ts
│       ├── components/                      # 组件
│       │   ├── ContentInput.vue                 # Markdown 编辑器
│       │   ├── PreviewPanel.vue                 # 实时预览面板
│       │   ├── ExportToolbar.vue                # 导出工具栏
│       │   ├── TemplatePanel.vue                # 模板选择面板
│       │   ├── TableEditor.vue                  # 表格编辑器
│       │   ├── FlowchartEditor.vue              # 流程图编辑器
│       │   ├── ListRenderer.vue                 # 列表渲染器
│       │   ├── auth/                            # 认证相关组件
│       │   │   ├── LoginForm.vue
│       │   │   ├── RegisterForm.vue
│       │   │   ├── ForgotPasswordForm.vue
│       │   │   ├── ResetPasswordForm.vue
│       │   │   └── UserMenu.vue
│       │   └── formula/                         # 公式相关组件
│       │       ├── FormulaPreview.vue
│       │       ├── FormulaLatexEditor.vue
│       │       ├── FormulaImageUpload.vue
│       │       ├── FormulaOcrResultPanel.vue
│       │       └── FormulaCandidateList.vue
│       ├── views/                           # 页面视图
│       │   ├── DocumentWorkbench.vue            # 文档工作台（主页面）
│       │   └── auth/
│       │       ├── OAuthCallbackView.vue        # OAuth 回调页
│       │       └── ResetPasswordView.vue        # 密码重置页
│       └── utils/                           # 工具函数
│           ├── auth.ts                          # 认证工具（Token 管理）
│           ├── formulaCache.ts                  # 公式缓存
│           ├── markdownFormatter.ts             # Markdown 格式化
│           ├── paragraphAlignment.ts            # 段落对齐
│           ├── storage.ts                       # 本地存储
│           ├── scrollSync.ts                    # 滚动同步
│           └── smoothScrollSync.ts              # 平滑滚动同步
│
└── out/                                # 测试输出目录（AI 生成文档样本）
```

---

## 5. 后端模块详解

### 5.1 启动与配置

**启动类**：[AiDocEngineApplication.java](file:///e:/Desktop/Ai-doc-engine/Ai-doc-engine-server/src/main/java/com/aidoc/engine/AiDocEngineApplication.java)

- `@SpringBootApplication` + `@EnableJpaAuditing` + `@EnableConfigurationProperties(AppOcrProperties.class)`
- 启动后输出 ASCII Art Banner 及系统信息

**配置文件体系**：

| 文件 | 用途 |
|------|------|
| `application.yml` | 主配置：数据源、JPA、JWT、OCR、Mermaid、日志、端口(8687) |
| `application-dev.yml` | 开发环境：本地数据库、邮件、OAuth2 配置 |
| `application-prod.yml` | 生产环境：通过环境变量覆盖敏感配置 |

**关键配置项**：

```yaml
server.port: 8687
jwt.secret / jwt.expiration: 7天
app.ocr.api-url: http://127.0.0.1:5000/parse
app.formula.mathjax.script-path: mathjax-to-mathml.js
app.mermaid.command: mmdc.cmd
app.frontend-url: http://localhost:3514
```

### 5.2 公共模块 (common)

#### ApiResponse\<T\>

统一 API 响应封装，包含 `code`、`message`、`data` 三个字段。

```java
ApiResponse.success(data)     // 200 成功
ApiResponse.error(code, msg)  // 错误响应
```

#### ErrorCode

分域错误码枚举，覆盖 5 大领域：

| 域 | 范围 | 示例 |
|----|------|------|
| 通用 | 400-499 | BAD_REQUEST(400), UNAUTHORIZED(401) |
| 用户认证 | 1000-1099 | USERNAME_EXISTS(1001), INVALID_CREDENTIALS(1003) |
| 文档处理 | 2000-2099 | PARSE_FAILED(2008), EXPORT_FAILED(2007) |
| 公式处理 | 3000-3099 | FORMULA_OCR_ERROR(3001), OMML_CONVERT_ERROR(3004) |
| 流程图 | 4000-4099 | FLOWCHART_JSON_ERROR(4001) |
| 文件处理 | 5000-5099 | FILE_SIZE_EXCEEDED(5002) |
| 邮件 | 6000-6099 | EMAIL_SEND_ERROR(6001) |

#### BusinessException

业务异常类，携带 `ErrorCode`，由 `EnhancedGlobalExceptionHandler` 统一捕获并转换为 `ApiResponse`。

#### OauthProperties

OAuth2 统一登录配置属性（client-id、client-secret、auth-host、redirect-uri）。

### 5.3 安全模块 (security)

#### SecurityConfig

Spring Security 核心配置：

- **无状态会话**：`SessionCreationPolicy.STATELESS`
- **CSRF 禁用**：前后端分离架构
- **JWT 过滤器**：`JwtAuthenticationFilter` 插入 `UsernamePasswordAuthenticationFilter` 之前
- **接口权限**：
  - 公开：`/api/auth/**`、`/oauth/**`、`/api/document/parse`、`/api/formula/validate`、`/api/template/list`
  - 需认证：`/api/document/export/**`、`/api/formula/ocr-image`
- **密码编码**：BCrypt

#### JwtTokenProvider

JWT Token 生成与验证：

| 方法 | 功能 |
|------|------|
| `generateToken(userId, username)` | 生成 JWT（HS256 签名，7天有效期） |
| `getUserIdFromToken(token)` | 从 Token 提取用户 ID |
| `getUsernameFromToken(token)` | 从 Token 提取用户名 |
| `validateToken(token)` | 验证 Token 有效性 |

#### JwtAuthenticationFilter

继承 `OncePerRequestFilter`，从 `Authorization: Bearer <token>` 提取并验证 JWT，设置 `SecurityContext`。

#### CustomUserDetailsService

实现 `UserDetailsService`，支持**用户名或邮箱**双重查询登录。

### 5.4 数据模型 (model)

#### JPA 实体

| 实体 | 表名 | 核心字段 |
|------|------|----------|
| UserEntity | sys_user | id, username, email, passwordHash, nickname, status, deleted |
| TemplateEntity | doc_template | id, userId, name, description, templateType, configJson(JSON), isDefault, isPublic, useCount |
| LoginLogEntity | — | 登录日志（IP、User-Agent、成功/失败） |
| PasswordResetTokenEntity | — | 密码重置令牌（24小时有效） |

**设计特点**：
- 软删除：所有实体使用 `deleted` 字段标记
- 自动时间戳：`@CreationTimestamp` / `@UpdateTimestamp`
- Lombok `@Builder` 模式

#### UDM 模型（核心）

详见 [第 8 节：UDM 统一文档模型](#8-udm-统一文档模型)。

### 5.5 数据访问层 (repository)

基于 Spring Data JPA 的 Repository 接口：

| Repository | 关键查询方法 |
|------------|-------------|
| UserRepository | `findByUsernameAndDeleted`, `findByEmailAndDeleted`, `existsByUsername`, `existsByEmail` |
| TemplateRepository | 标准 CRUD |
| LoginLogRepository | 按用户/IP 统计失败次数 |
| PasswordResetTokenRepository | 按令牌值查询 |

### 5.6 业务逻辑层 (service)

#### DocumentService（文档服务）

| 方法 | 功能 |
|------|------|
| `parseMarkdown(markdown)` | Markdown → UDM |
| `exportToWord(udm, templateId)` | UDM → Word（带模板） |
| `parseAndExportToWord(markdown, templateId)` | 一步到位：Markdown → Word |

**依赖关系**：`DocumentServiceImpl` → `DocumentParser` + `WordExporter` + `TemplateService`

#### AuthService（认证服务）

| 方法 | 功能 |
|------|------|
| `register(request)` | 用户注册（BCrypt 加密密码） |
| `login(request, ip, userAgent)` | 登录（含失败保护、日志记录） |
| `loginBySchoolOAuth(schoolUser, ip, userAgent)` | 学校 OAuth2 登录 |
| `forgotPassword(email)` | 忘记密码（静默处理） |
| `resetPassword(token, newPassword)` | 重置密码 |
| `getCurrentUser()` | 获取当前用户 |

**安全特性**：
- 15分钟内失败5次锁定账号
- 同一IP失败10次锁定IP
- 邮箱不存在时静默处理（防探测）

#### FormulaConvertService（公式转换服务）

| 方法 | 功能 |
|------|------|
| `convertLatexToMathml(latex, inline)` | LaTeX → MathML（调用 MathJax Node.js） |
| `convertMathmlToOmml(mathml)` | MathML → OMML（XSLT 转换） |
| `convertLatexToOmml(latex, inline)` | LaTeX → OMML（完整链路） |

**实现类**：`MathJaxFormulaConvertService`
- 通过 `ProcessBuilder` 调用 Node.js 执行 `mathjax-to-mathml.js`
- 使用 `MML2OMML.XSL` XSLT 转换表进行 MathML → OMML
- 内置 LRU 缓存（MathML 1024条，OMML 2048条）
- 支持 mhchem（化学）、physics（物理）扩展

#### FormulaService（公式业务服务）

| 方法 | 功能 |
|------|------|
| `ocrImage(image)` | 公式图片 OCR 识别（当前暂未启用） |
| `validateLatex(latex)` | LaTeX 公式格式校验 |

#### MermaidParserService / MermaidRenderService

| 方法 | 功能 |
|------|------|
| `parseMermaidCode(code)` | 解析 Mermaid 代码为结构化 JSON |
| `renderToPng(mermaidCode)` | Mermaid → PNG（调用 mmdc CLI） |
| `renderToBase64(mermaidCode)` | Mermaid → Base64 图片 |

**实现**：`MermaidRenderServiceImpl` 通过 `ProcessBuilder` 调用 `mmdc` 命令行工具，生成临时文件并读取 PNG。

#### TemplateService（模板服务）

模板 CRUD 管理，配置项包括：页面设置、字体设置、段落设置、页眉页脚。

#### EmailService（邮件服务）

- 密码重置邮件（同步）
- 欢迎邮件（异步 `@Async`）
- 基于 Thymeleaf 模板

#### OAuthLoginTicketService

OAuth2 一次性票据服务，用于将后端 OAuth 回调结果安全传递给前端 SPA：
- `issue(authResponse)` → 生成 2 分钟有效的一次性 ticket
- `consume(ticket)` → 消费 ticket 获取 AuthResponse

### 5.7 解析器 (parser)

#### DocumentParser 接口

```java
public interface DocumentParser {
    UdmDocument parse(String source);
}
```

#### ExtendedMarkdownDocumentParser（核心解析器）

标注 `@Primary`，是系统实际使用的解析器。

**解析流程**：

1. **预处理**：HTML 实体解码、表格预处理、`\[...\]` 块级公式占位符替换
2. **特殊块预处理**：提取 Mermaid 代码块、任务列表、脚注定义
3. **flexmark 解析**：使用 flexmark-java 将 Markdown 解析为 AST
4. **AST 遍历**：逐节点转换为 UDM Block
5. **后处理**：恢复块级公式占位符、合并脚注引用

**支持的 Markdown 元素**：
- 标题（H1-H6）
- 段落（含行内公式、加粗、斜体、删除线、链接、代码）
- 有序/无序列表（含嵌套）
- 表格（含合并单元格）
- 代码块（语法高亮标记）
- 块级公式（`$$...$$`、`\[...\]`）
- 行内公式（`$...$`）
- Mermaid 流程图
- 任务列表（`- [ ] / - [x]`）
- 脚注（`[^1]`）

### 5.8 渲染器 (renderer)

#### BlockRenderer\<T\> 接口

```java
public interface BlockRenderer<T extends UdmBlock> {
    P render(T block, WordprocessingMLPackage wordPackage);
    default P render(T block, WordprocessingMLPackage wordPackage, TemplateConfig templateConfig);
}
```

**策略模式**：每种 Block 类型对应一个渲染器实现。

#### DocumentRenderer（分发器）

遍历 UDM 文档的所有 Block，根据 `instanceof` 判断类型，分发给对应的渲染器：

| Block 类型 | 渲染器 | 输出 |
|-----------|--------|------|
| HeadingBlock | HeadingRenderer | Word 标题段落（H1-H6） |
| ParagraphBlock | ParagraphRenderer | Word 普通段落（含行内公式 OMML） |
| ListBlock | ListRenderer | Word 编号/项目符号列表 |
| TableBlock | TableRenderer | Word 原生表格 |
| FormulaBlock | FormulaRenderer | Word OMML 公式（块级） |
| FlowchartBlock | FlowchartRenderer | Word 内嵌 PNG 图片 |
| CodeBlock | CodeBlockRenderer | Word 等宽字体段落 |

未知 Block 类型生成占位符 `[未知内容类型: xxx]`。

#### TemplateApplier

将模板配置应用到 Word 文档：
- 页面设置（纸张大小、页边距、方向）
- 页眉页脚
- 字体设置
- 段落设置

### 5.9 导出器 (exporter)

#### WordExporter 接口

```java
public interface WordExporter {
    byte[] export(UdmDocument document);
    byte[] export(UdmDocument document, TemplateConfig templateConfig);
}
```

#### Docx4jWordExporter

基于 docx4j 的实现：

1. 创建 `WordprocessingMLPackage`
2. 调用 `DocumentRenderer.render()` 渲染内容
3. 导出为 `byte[]`

### 5.10 控制器 (controller)

| 控制器 | 路径前缀 | 核心接口 |
|--------|---------|---------|
| AuthController | `/api/auth` | register, login, logout, forgot-password, reset-password, me, oauth/exchange |
| DocumentController | `/api/document` | parse, export, export/word, export/word/from-markdown |
| FormulaController | `/api/formula` | ocr-image, validate |
| TemplateController | `/api/template` | list, {id}, save, update/{id}, {id}(DELETE) |
| OauthController | `/oauth` | login(跳转), callback, logout |

### 5.11 工具类 (util)

| 类 | 功能 |
|----|------|
| OmmlCleaner | 清理 OMML 中 docx4j 不兼容的元素（如 `m:scrLvl`） |
| TextNormalizer | 文本规范化：全角→半角、Unicode 规范化、空白字符处理 |

---

## 6. 前端模块详解

### 6.1 技术架构

- **框架**：Vue 3 Composition API + TypeScript
- **UI 库**：Element Plus
- **状态管理**：Pinia
- **构建工具**：Vite 5
- **公式渲染**：KaTeX（实时预览）
- **流程图渲染**：Mermaid.js 10.9（实时预览 + 语法校验）
- **HTTP 客户端**：Axios（封装拦截器）

### 6.2 路由与页面

| 路径 | 组件 | 说明 |
|------|------|------|
| `/` | — | 重定向到 `/workbench` |
| `/workbench` | DocumentWorkbench.vue | 主工作台（Markdown 编辑 + 预览 + 导出） |
| `/reset-password` | ResetPasswordView.vue | 密码重置页 |
| `/oauth/callback` | OAuthCallbackView.vue | OAuth2 回调页 |

**路由守卫**：工作台页面无需登录即可访问（有登录对话框），其他路由需认证。

### 6.3 API 层

#### http.ts（Axios 封装）

- **baseURL**：`/api`（通过 Vite proxy 转发到后端 `:8687`）
- **请求拦截器**：自动添加 `Authorization: Bearer <token>`
- **响应拦截器**：
  - Blob 类型直接返回
  - 401 错误区分登录接口和受保护 API
  - 统一错误提示（Element Plus Message）

#### API 模块

| 模块 | 方法 | 说明 |
|------|------|------|
| auth.ts | login, register, logout, forgotPassword, resetPassword, getCurrentUser, exchangeOAuthTicket | 认证相关 |
| document.ts | parseMarkdown, exportWord, exportWordFromMarkdown | 文档相关 |
| formula.ts | ocrImage, validateLatex | 公式相关 |
| template.ts | getList, getById, save, update, delete | 模板相关 |

### 6.4 状态管理

| Store | 状态 | 核心方法 |
|-------|------|---------|
| auth (Pinia) | token, user, isAuthenticated | login, register, logout, checkAuth |
| document (Pinia) | markdown, udm, loading | parseMarkdown, exportWord, exportWordDirect, insertBlock, clearDocument |
| template (Pinia) | templates, currentTemplate, loading | fetchTemplates, selectTemplate |

**exportWordDirect**：直接从 Markdown 导出 Word（绕过 UDM 序列化），并对矩阵公式中的 `\\` 换行符做预处理。

### 6.5 组件体系

#### 核心业务组件

| 组件 | 功能 |
|------|------|
| ContentInput.vue | Markdown 文本编辑器（输入区） |
| PreviewPanel.vue | 实时预览面板（渲染 Markdown → HTML，含 KaTeX 公式、Mermaid 流程图） |
| ExportToolbar.vue | 导出工具栏（选择模板、导出 Word） |
| TemplatePanel.vue | 模板选择与配置面板 |
| TableEditor.vue | 表格编辑器 |
| FlowchartEditor.vue | 流程图编辑器（Mermaid 语法） |
| ListRenderer.vue | 列表渲染组件 |

#### 认证组件

| 组件 | 功能 |
|------|------|
| LoginForm.vue | 登录表单（用户名/邮箱 + 密码） |
| RegisterForm.vue | 注册表单 |
| ForgotPasswordForm.vue | 忘记密码表单 |
| ResetPasswordForm.vue | 重置密码表单 |
| UserMenu.vue | 用户菜单（头像、退出） |

#### 公式组件

| 组件 | 功能 |
|------|------|
| FormulaPreview.vue | 公式实时预览（KaTeX 渲染） |
| FormulaLatexEditor.vue | LaTeX 公式编辑器 |
| FormulaImageUpload.vue | 公式图片上传（OCR） |
| FormulaOcrResultPanel.vue | OCR 识别结果面板 |
| FormulaCandidateList.vue | 候选公式列表 |

### 6.6 工具函数

| 模块 | 功能 |
|------|------|
| auth.ts | Token 管理（getToken, setToken, clearAuth, isAuthenticated） |
| formulaCache.ts | 公式转换结果缓存（LRU 策略） |
| markdownFormatter.ts | Markdown 格式化（缩进、空行等） |
| paragraphAlignment.ts | 段落对齐工具 |
| storage.ts | localStorage 封装 |
| scrollSync.ts | 编辑器与预览滚动同步 |

---

## 7. 核心数据流

### 7.1 Markdown → Word 完整链路

```
用户输入 Markdown
       │
       ▼
┌─────────────────────────────────┐
│  前端 ContentInput.vue          │
│  (Markdown 文本编辑)             │
└──────────────┬──────────────────┘
               │ POST /api/document/export/word/from-markdown
               ▼
┌─────────────────────────────────┐
│  DocumentController             │
│  exportWordFromMarkdown()       │
└──────────────┬──────────────────┘
               │
               ▼
┌─────────────────────────────────┐
│  DocumentServiceImpl            │
│  parseAndExportToWord()         │
│  ┌───────────────────────────┐  │
│  │ 1. parseMarkdown()        │  │
│  │    → ExtendedMarkdown-    │  │
│  │      DocumentParser       │  │
│  │    → UdmDocument          │  │
│  └───────────────────────────┘  │
│  ┌───────────────────────────┐  │
│  │ 2. exportToWord()         │  │
│  │    → Docx4jWordExporter   │  │
│  │    → DocumentRenderer     │  │
│  │    → BlockRenderer(7种)   │  │
│  │    → FormulaConvertService│  │
│  │    → MermaidRenderService │  │
│  │    → byte[] (.docx)       │  │
│  └───────────────────────────┘  │
└──────────────┬──────────────────┘
               │ ResponseEntity<byte[]>
               ▼
┌─────────────────────────────────┐
│  前端 ExportToolbar.vue         │
│  (Blob 下载为 .docx 文件)       │
└─────────────────────────────────┘
```

### 7.2 公式转换子链路

```
LaTeX: "\sum_{i=1}^{n} i"
         │
         ▼
MathJaxFormulaConvertService.convertLatexToOmml()
         │
         ├──▶ convertLatexToMathml()     [Node.js 子进程]
         │    ProcessBuilder: node mathjax-to-mathml.js "..." "false"
         │    → MathML 字符串
         │
         ├──▶ convertMathmlToOmml()      [XSLT 转换]
         │    TransformerFactory + MML2OMML.XSL
         │    → OMML 字符串
         │
         └──▶ sanitizeOmml()            [清理不兼容元素]
              → 最终 OMML（可被 docx4j 解析）
```

### 7.3 流程图转换子链路

```
Mermaid 代码: "graph TD\n  A-->B"
         │
         ▼
MermaidRenderServiceImpl.renderToPng()
         │
         ├──▶ 创建临时 .mmd 文件
         ├──▶ ProcessBuilder: mmdc -i input.mmd -o output.png
         ├──▶ 读取 PNG 字节
         └──▶ 清理临时文件
         │
         ▼
FlowchartRenderer.render()
         │
         └──▶ 将 PNG 嵌入 Word 文档（Inline Image）
```

---

## 8. UDM 统一文档模型

UDM（Unified Document Model）是本项目的核心数据模型，作为 Markdown 与 Word 之间的中间表示层。

### 8.1 模型层次

```
UdmDocument
├── blocks: List<UdmBlock>      # 文档块列表
└── metadata: Map<String, Object>  # 文档元数据

UdmBlock (抽象基类)
├── type: String                # Block 类型标识
├── metadata: Map<String, Object>
└── @JsonTypeInfo / @JsonSubTypes  # Jackson 多态序列化
    ├── HeadingBlock            # type="heading"
    ├── ParagraphBlock          # type="paragraph"
    ├── ListBlock               # type="list"
    ├── TableBlock              # type="table"
    ├── FormulaBlock            # type="formula"
    ├── FlowchartBlock          # type="flowchart"
    ├── CodeBlock               # type="codeblock"
    ├── TaskListBlock           # type="tasklist"
    └── FootnoteBlock           # type="footnote"
```

### 8.2 Block 类型与内容模型

| Block 类型 | Content 类 | 核心字段 |
|-----------|-----------|---------|
| heading | HeadingContent | level(1-6), text |
| paragraph | ParagraphContent | text, segments(RichText[]) |
| list | ListContent | ordered, items, itemContents |
| table | TableContent | headers, rows |
| formula | FormulaContent | latex, mathml, inline |
| flowchart | FlowchartContent | rawSource, sourceType, imageBase64 |
| codeblock | CodeBlockContent | code, language |
| tasklist | TaskListContent | items(TaskItem[]) |
| footnote | FootnoteContent | id, text |

### 8.3 RichText 富文本模型

段落中的行内内容使用 `RichText` 模型，支持：

| 字段 | 类型 | 说明 |
|------|------|------|
| text | String | 纯文本内容 |
| bold | Boolean | 加粗 |
| italic | Boolean | 斜体 |
| strikethrough | Boolean | 删除线 |
| code | Boolean | 行内代码 |
| linkUrl | String | 链接地址 |
| inlineFormula | String | 行内 LaTeX 公式 |

### 8.4 BlockType 枚举

```java
HEADING, PARAGRAPH, LIST, TABLE, FORMULA, FLOWCHART, IMAGE, CODE, CODEBLOCK, TASKLIST, FOOTNOTE
```

---

## 9. 公式转换链路

### 9.1 转换流程

```
LaTeX ──(MathJax Node.js)──▶ MathML ──(XSLT: MML2OMML.XSL)──▶ OMML
```

### 9.2 关键组件

| 组件 | 文件 | 职责 |
|------|------|------|
| mathjax-to-mathml.js | 服务端 Node.js 脚本 | 接收 LaTeX 参数，输出 MathML |
| MML2OMML.XSL | 资源文件 | Microsoft 提供的 MathML→OMML XSLT 转换表 |
| MathJaxFormulaConvertService | Java 服务 | 编排完整转换链路，管理缓存 |
| FormulaRenderer | 渲染器 | 将 OMML 字符串解析为 docx4j CTOMath 对象嵌入 Word |
| ParagraphRenderer | 渲染器 | 处理行内公式（段落中的 `$...$`） |

### 9.3 公式类型

| 类型 | Markdown 语法 | UDM 表示 | Word 输出 |
|------|--------------|---------|----------|
| 行内公式 | `$x^2$` | ParagraphBlock.segments[].inlineFormula | 行内 OMML |
| 块级公式 | `$$\int_0^1$$` | FormulaBlock(inline=false) | 独立 OMML 段落 |
| 化学公式 | `$\ce{H2O}$` | 同行内公式（mhchem 扩展） | OMML |
| 物理公式 | `$\hbar$` | 同行内公式（physics 扩展） | OMML |

### 9.4 缓存策略

- MathML 缓存：LRU 1024 条
- OMML 缓存：LRU 2048 条
- 缓存 Key：`inline标志|规范化后的LaTeX`

---

## 10. 依赖关系图

### 10.1 后端模块依赖

```
Controller
    ├──▶ Service
    │       ├──▶ Repository (JPA)
    │       ├──▶ Parser
    │       │       └──▶ MermaidParserService
    │       ├──▶ FormulaConvertService
    │       │       └──▶ Node.js (MathJax)
    │       ├──▶ MermaidRenderService
    │       │       └──▶ mmdc CLI
    │       └──▶ EmailService
    │               └──▶ JavaMail + Thymeleaf
    └──▶ Security
            ├──▶ JwtTokenProvider
            ├──▶ JwtAuthenticationFilter
            └──▶ CustomUserDetailsService
                    └──▶ UserRepository

Exporter (Docx4jWordExporter)
    └──▶ DocumentRenderer
            ├──▶ TemplateApplier
            └──▶ BlockRenderer (7种)
                    └──▶ FormulaConvertService
                    └──▶ MermaidRenderService
```

### 10.2 前端模块依赖

```
App.vue
    ├──▶ Router
    │       ├──▶ DocumentWorkbench.vue (主页面)
    │       ├──▶ ResetPasswordView.vue
    │       └──▶ OAuthCallbackView.vue
    ├──▶ Pinia Stores
    │       ├──▶ auth store → auth API
    │       ├──▶ document store → document API
    │       └──▶ template store → template API
    └──▶ API Layer
            └──▶ http.ts (Axios)
                    └──▶ auth utils (Token)
```

### 10.3 前后端交互

```
前端 (:3514)  ──proxy──▶  后端 (:8687)

/api/auth/**          →  AuthController
/api/document/**      →  DocumentController
/api/formula/**       →  FormulaController
/api/template/**      →  TemplateController
/oauth/**             →  OauthController
```

---

## 11. 项目运行方式

### 11.1 环境要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| JDK | 17+ | 后端运行时 |
| Maven | 3.6+ | 后端构建 |
| MySQL | 8.0+ | 数据库 |
| Node.js | 16+ | MathJax 脚本 + Mermaid CLI |
| npm | 8+ | 前端构建 + 后端 Node 依赖 |

### 11.2 后端启动

```bash
# 1. 创建数据库
mysql -u root -p < AI_DOC-DATA.sql

# 2. 安装 Node.js 依赖（MathJax）
cd Ai-doc-engine-server
npm install

# 3. 安装 Mermaid CLI
npm install -g @mermaid-js/mermaid-cli
mmdc --version  # 验证

# 4. 配置 application-dev.yml（数据库、邮件等）

# 5. 构建并运行
mvn clean install
mvn spring-boot:run

# 服务启动于 http://localhost:8687
```

### 11.3 前端启动

```bash
cd Ai-doc-engine-web

# 安装依赖
npm install

# 开发模式
npm run dev
# 启动于 http://localhost:3514
# Vite 自动代理 /api → http://localhost:8687

# 生产构建
npm run build

# 预览构建结果
npm run preview
```

### 11.4 测试

```bash
# 后端测试
cd Ai-doc-engine-server
mvn test

# 前端测试
cd Ai-doc-engine-web
npm run test          # 监听模式
npm run test:run      # 单次运行
npm run test:coverage # 覆盖率报告
```

### 11.5 生产部署

```bash
# 后端打包
cd Ai-doc-engine-server
mvn clean package -Pprod
java -jar target/ai-doc-engine-server-1.0.0.jar --spring.profiles.active=prod

# 前端打包
cd Ai-doc-engine-web
npm run build
# 将 dist/ 目录部署到 Nginx 等 Web 服务器
```

---

## 12. API 接口一览

### 认证模块 `/api/auth`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/auth/register` | 否 | 用户注册 |
| POST | `/api/auth/login` | 否 | 用户登录（用户名或邮箱） |
| POST | `/api/auth/logout` | 是 | 退出登录 |
| GET | `/api/auth/me` | 是 | 获取当前用户信息 |
| POST | `/api/auth/forgot-password` | 否 | 忘记密码（静默处理） |
| POST | `/api/auth/reset-password` | 否 | 重置密码 |
| GET | `/api/auth/oauth/exchange?ticket=` | 否 | OAuth 票据换 JWT |

### 文档模块 `/api/document`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/document/parse` | 否 | 解析 Markdown 为 UDM |
| POST | `/api/document/export` | 是 | 导出 Word（接受 UDM） |
| POST | `/api/document/export/word` | 是 | 导出 Word（直接下载） |
| POST | `/api/document/export/word/from-markdown` | 是 | Markdown 一步转 Word |

### 公式模块 `/api/formula`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/formula/ocr-image` | 是 | 公式 OCR 识别（暂未启用） |
| POST | `/api/formula/validate` | 否 | 校验 LaTeX 公式格式 |

### 模板模块 `/api/template`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/template/list` | 否 | 获取模板列表 |
| GET | `/api/template/{id}` | 否 | 获取模板详情 |
| POST | `/api/template/save` | — | 保存模板 |
| PUT | `/api/template/update/{id}` | — | 更新模板 |
| DELETE | `/api/template/{id}` | — | 删除模板 |

### OAuth2 模块 `/oauth`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/oauth/login` | 否 | 跳转学校统一登录 |
| GET | `/oauth/callback` | 否 | OAuth2 回调 |
| GET | `/oauth/logout` | 否 | 退出（重定向前端） |

---

## 13. 错误码体系

| 域 | 范围 | 错误码示例 |
|----|------|-----------|
| 通用 | 400-499 | BAD_REQUEST(400), UNAUTHORIZED(401), FORBIDDEN(403), NOT_FOUND(404), INTERNAL_ERROR(500) |
| 用户认证 | 1000-1099 | USERNAME_EXISTS(1001), EMAIL_EXISTS(1002), INVALID_CREDENTIALS(1003), INVALID_TOKEN(1005) |
| 文档处理 | 2000-2099 | MARKDOWN_PARSE_ERROR(2001), EXPORT_FAILED(2007), PARSE_FAILED(2008) |
| 公式处理 | 3000-3099 | FORMULA_OCR_ERROR(3001), LATEX_FORMAT_ERROR(3002), OMML_CONVERT_ERROR(3004) |
| 流程图 | 4000-4099 | FLOWCHART_JSON_ERROR(4001), DRAWINGML_CONVERT_ERROR(4002) |
| 文件处理 | 5000-5099 | FILE_UPLOAD_ERROR(5001), FILE_SIZE_EXCEEDED(5002) |
| 邮件 | 6000-6099 | EMAIL_SEND_ERROR(6001), EMAIL_TEMPLATE_ERROR(6002) |

---

> **文档生成时间**：2026-05-16  
> **项目版本**：1.0.0
