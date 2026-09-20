# 已加好权限注解的 Controller

直接覆盖到 `com/txy/controller/` 即可。**共 38 个接口，注解覆盖率 100%（0 遗漏）。**

## 文件清单

| 文件 | 接口数 | 用到的权限码 |
|---|---|---|
| `NpcController` | 5 | npc:view / npc:edit / npc:delete |
| `ShowController` | 5 | show:view / show:edit / show:delete |
| `OrderController` | 4 | order:view / order:edit / order:delete |
| `NoticeController` | 5 | notice:view / notice:edit / notice:delete |
| `PlayerController` | 2 | player:view |
| `IntimacyController` | 5 | intimacy:view / intimacy:edit |
| `GachaController` | 8 | gacha:view / gacha:edit / gacha:delete |
| `DashboardController` | 1 | dashboard:view |
| `SettingController` | 2 | setting:view / setting:edit |
| `WebUploadController` | 1 | upload:edit |

## 两处必须同步的改动

### ⚠️ ① R 的包是 `com.txy.dto.R`

我之前给的 AuthController / AdminRoleController / AdminAuditController / AdminAuthInterceptor
用的是 `com.txy.common.R`，**已全部改成 `com.txy.dto.R`**，跟你的项目一致。

### ⚠️ ② 上传接口路径改了：`/web/upload` → `/api/upload`

原因：`/web/**` 不在拦截器范围内，任何人不用登录就能上传文件。

```java
@RestController
@RequestMapping("/api/upload")     // ← 原来是 "/web/upload"
public class WebUploadController extends BaseController {
```

**如果前端已经在调 `/web/upload/image`**，二选一：
- ① 前端同步改成 `/api/upload/image`（推荐）
- ② 保留旧路径，在 `WebMvcConfig` 里加 `.addPathPatterns("/web/**")`

前端 `api.js` 已加好 `API.upload.image(file)`，返回可直接用的 URL。

## 不需要改的类

| 类 | 原因 |
|---|---|
| `FossaController` | `@Controller` 返回 index 页面，路径是 `/`，不在 `/api/**` |
| `BaseController` | 基类，无接口 |

## 发现的两个情况

**1. `PlayerController` 只有 `list` 和 `get`，没有增删改。**
所以只加了 `player:view`。文件里留了写接口模板，将来补上时照着加注解即可。

**2. `ShowController.list` 只接 `npcId` 一个参数。**
前端场次页已传 keyword/status/city/日期/价格区间，后端收不到，目前是前端拿全量后自己过滤（功能正常，但数据量大时性能差）。
要后端真查询的话，用之前给的 `show-query-backend/ShowMapper.xml` 替换即可，前端一行不用改。

## 你已有的 `@ApiLog` 和这套审计的关系

- `@ApiLog` — 你自己的 AOP 日志（大概率是打日志文件或别的落库）
- 本套 `admin_audit_log` — 写库表，只记 POST/PUT/DELETE，带操作人/模块/对象ID/前后摘要/IP/耗时

**两者可共存，不冲突。** 如果想统一，可以让 `@ApiLog` 的切面同时往 `admin_audit_log` 插一条，避免两套日志口径不一致。

## 新增的权限点

`upload:edit`（图片上传）已加进 `01_权限与审计建表.sql`，并授权给 super + editor（**只读访客不能上传**）。

如果你之前已经跑过那份 SQL，补跑这一段：

```sql
INSERT INTO admin_perm (perm_code,perm_name,module,sort) VALUES ('upload:edit','图片上传','upload',1);
INSERT INTO admin_role_perm (role_key,perm_code) VALUES ('super','upload:edit'),('editor','upload:edit');
```
