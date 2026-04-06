# 中小学诗词大模型应用 - 第一周迭代记录

## 需求分析

- 目标用户：中小学学生
- 核心场景：诗词讲解、启发式提问、背诵训练、随堂练习
- 第一周目标：
  - 提供4种教学模式切换
  - 提供学段选择
  - 回答过程展示知识依据

## 技术方案

- 后端
  - 扩展对话参数，新增 `mode`、`gradeLevel`、`enableCitation`
  - 在聊天接口注入教学策略系统提示词
  - 在启用知识库和依据展示时执行向量检索，先通过SSE发送 `citation` 事件，再发送 `message` 事件
- 前端
  - 右侧选项区新增教学模式、学段、依据展示开关
  - SSE新增 `citation` 事件监听
  - 聊天消息区新增“知识依据”卡片

## 完成情况

- [x] 功能1：教学模式参数打通（后端）
  - 文件：`AiMessageParams.java`、`AiMessageController.java`
  - 结果：支持 `explain/question/recite/practice`
- [x] 功能2：学段参数打通（后端）
  - 文件：`AiMessageParams.java`、`AiMessageController.java`
  - 结果：支持按学段注入回答策略
- [x] 功能3：依据引用事件输出（后端）
  - 文件：`AiMessageController.java`
  - 结果：SSE新增 `citation` 事件，输出检索片段
- [x] 功能4：模式/学段/依据展示开关（前端）
  - 文件：`chat-view.vue`
  - 结果：选项面板可配置并随请求提交
- [x] 功能5：依据卡片展示（前端）
  - 文件：`chat-view.vue`
  - 结果：消息区顶部显示“知识依据”卡片
- [x] 功能6：依据来源跳转能力（前后端）
  - 文件：`DocumentController.java`、`AiMessageController.java`、`chat-view.vue`
  - 结果：文档入库时写入 `sourceUrl/chunkIndex`，前端依据卡片可点击查看来源并显示片段编号
- [x] 功能7：练习/背诵模式策略增强（后端）
  - 文件：`AiMessageController.java`
  - 结果：练习模式默认带“题目+答案+解析+举一反三”，背诵模式默认按“首字->关键词->整句”分层提示
- [x] 功能8：学段词汇难度表（后端）
  - 文件：`PoetryTeachingProfiles.java`、`AiMessageController.java`
  - 结果：按小学低年级/中年级/高年级/初中划分语言规则、讲解重点、篇幅要求和解释方式
- [x] 功能9：学段与模式提示卡（前端）
  - 文件：`chat-view.vue`
  - 结果：右侧面板实时展示当前教学模式说明和学段讲解策略
- [x] 功能10：学段策略细化为模式化模板（后端）
  - 文件：`PoetryTeachingProfiles.java`、`AiMessageController.java`
  - 结果：将学段策略与讲解/提问/背诵/练习模式组合，统一生成更细的提示词变量
- [x] 功能11：练习模式结构化题卡（前后端）
  - 文件：`AiMessageController.java`、`chat-view.vue`
  - 结果：后端新增 `exercise` SSE 事件输出题目 JSON，前端渲染随堂练习答题卡、判题结果和解析
- [x] 功能12：修复首页 500 的启动链路问题（后端）
  - 文件：`RedisVectorConfig.java`、`DocumentController.java`
  - 结果：修复文档控制器构造注入异常，并兼容本地非 RedisStack 环境，避免 `/api/session/user` 因后端未启动而前端代理报 500
- [x] 功能13：修复登录态透传与未登录跳转（前端）
  - 文件：`request.ts`、`router/index.ts`、`login-view.vue`、`chat-view.vue`、`register-view.vue`
  - 结果：统一请求自动携带 token，未登录访问聊天页自动跳转登录页，登录失败不再抛出未处理异常
- [x] 功能14：修复消息气泡组件脚本解析错误（前端）
  - 文件：`message-row.vue`
  - 结果：将组件脚本语言从 `tsx` 修正为 `ts`，消除 Vite 对 `defineProps<T>()` 的 JSX 解析报错
- [x] 功能15：练习记录与错题归档（前后端）
  - 文件：`ExerciseRecord.java`、`ExerciseRecordRepository.java`、`ExerciseRecordController.java`、`ExerciseRecordTableInitializer.java`、`chat-view.vue`
  - 结果：提交练习后自动保存作答记录，右侧面板展示最近练习与错题本

## 验证记录

- 后端编译：`mvn -DskipTests clean compile` 通过
- 后端编译：`mvn -DskipTests compile` 通过
- 运行预览：
  - 前端预览地址：`http://localhost:5179/`
  - 后端启动兼容性修复：当本地 Redis 缺少 RedisStack 搜索模块时，向量库自动降级为 `NoOpVectorStore`，避免服务因 `FT._LIST` 失败
- 前端校验：
  - `npm run lint` 已执行
  - `npm run type-check` 已执行
  - 当前终端输出有乱码干扰，但 VS Code 诊断未发现本轮新增文件的即时语法错误
- 本轮修复：
  - 已解决 `message-row.vue?lang.tsx` 导致的 `Unexpected token` 解析异常
- 本轮排障：
  - 已定位 `/api/session/user` 的 400 主要由前端未统一携带 token、页面无登录态守卫、登录失败未捕获异常引起
- 本轮实现：
  - 已新增练习记录表自动初始化逻辑，避免本地数据库缺表导致错题本无法保存
  - 已在聊天页接入练习记录查询与错题本展示

## 今日总结

- 今天完成了三类关键工作：
  - 交互能力增强：补齐练习记录、错题本、登录态守卫、练习题卡闭环
  - 稳定性修复：解决首页 `500/400`、消息组件 `tsx` 解析异常、后端非 RedisStack 启动失败问题
  - 工程清理：移除多处未使用导入、历史注释代码和冗余实现，清理 `pom.xml` 重复依赖，当前 IDE 诊断已清零
- 当前项目状态：
  - 诗词教学模式、学段策略、依据卡片、练习题卡、错题本已形成一条可演示主链路
  - 前后端主要页面已具备继续做“教师追问树”和“错题再练”的基础
- 下一阶段建议：
  - 先做提问模式“教师追问树”
  - 再做错题本“再次练习”和按会话筛选

## 下一步计划

- [x] 依据卡片支持“点击跳转原文段落/来源文档”
- [x] 练习模式升级：自动出选择题 + 自动判题
- [x] 背诵模式升级：分层提示（首字、关键词、整句）
- [x] 建立学段词汇难度表，提升低龄解释可读性
- [x] 将学段策略进一步拆分为“讲解/提问/背诵/练习”四套更细模板
- [x] 为练习模式输出结构化题目JSON，便于前端渲染成答题卡
- [x] 将练习模式题卡升级为可记录作答历史和错题归档
- [ ] 为提问模式增加“教师追问树”，支持连续追问教学
- [ ] 为错题本增加“再次练习”与按会话筛选能力
