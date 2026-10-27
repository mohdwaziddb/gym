# AI INSTRUCTIONS - Backend Project (Portable)

**Har AI modal is file ko pehle padhe, tabhi kaam start kare. Yeh project ka mandatory coding guide hai.**
> **Note:** Is file me diye gaye saare paths **project root se relative** hain. Chahe project kisi bhi drive/folder me ho, hamesha relative path `src/...` use karo. Absolute path hardcode mat karo.

---

## 1) DataTypeUtility ka use - MANDATORY

- **Jab bhi `long`, `String`, `Integer`, `Boolean`, `Float`, `Double` ka conversion karna ho, direct `String.valueOf()` / `Long.parseLong()` / `obj.toString()` MAT karo.**
- **Hamesha `DataTypeUtility` ka use karo:**

```java
import static com.backend.utility.DataTypeUtility.*;
// ya
import com.backend.utility.DataTypeUtility;

// Sahi:
String name = DataTypeUtility.stringValue(param.get("gym_name"));
Long id = DataTypeUtility.longValue(param.get("id"));
Long fk = DataTypeUtility.getForeignKeyValue(param.get("gym_code"));
Integer limit = DataTypeUtility.integerValue(param.get("limit"));
boolean active = DataTypeUtility.booleanValue(param.get("is_active"));
long zero = DataTypeUtility.longZeroValue(obj);
String safe = DataTypeUtility.stringNullValue(obj);

// Galat (mana hai):
String name = (String) param.get("gym_name");
Long id = Long.parseLong(param.get("id").toString());
```

- Service me `import static com.backend.utility.DataTypeUtility.*;` karke `stringValue()`, `longValue()` direct bhi call kar sakte ho, par import hamesha `DataTypeUtility` se hona chahiye.

---

## 2) Local DB kaha se uthega

- **Local DB `dbgym` ka config `application.properties` se nahi uthega.**
- **Sahi jagah (relative path):** `src/main/java/com/backend/plateform/tomcat/MysqlDataSourceService.java:23` `loadDatabaseCredentialsFromLocalHost()`
  ```java
  // File: src/main/java/com/backend/plateform/tomcat/MysqlDataSourceService.java
  Registry.dbmap.put("databasename", "dbgym");
  ```
  > Project root se relative: `src/main/java/...` . Kisi bhi OS me absolute path hardcode mat karo.
- `src/main/resources/application.properties` me `microservice.local.database.*` sirf documentation ke liye hai. Runtime me `src/main/java/com/backend/core/multitenancy/MultiTenancyJpaConfiguration.java:92` `dataSourcesMtApp()` isi `MysqlDataSourceService` ko call karta hai.
- Naya client DB banana ho (e.g. `dbgym_noida`) to yaha change nahi, `gymcommon.gym_registry` table me `db_name` alag set karo, `src/main/java/com/backend/core/multitenancy/RequestInterceptor.java:104` `ensureTenantDataSource()` lazy pool bana dega.

---

## 3) Multi-DB Gym Flow (gymcommon + dbgym)

- `gymcommon.gym_registry` = catalog DB (gym_code → db_name) — `CREATE DATABASE gymcommon`
- `dbgym` = tenant DB (gym_details, gym_user) — `CREATE DATABASE dbgym`
- Mobile: `Gym Code` → `POST /rest/auth/validateGym?gym_code=LAJPAT` → `src/main/java/com/backend/core/gymcommon/GymRegistryService.java:20` `gymcommon` lookup → `X-Gym-Code` header → `src/main/java/com/backend/core/multitenancy/RequestInterceptor.java:37` → `TenantContextHolder` → `dbgym` pool (`src/main/java/com/backend/core/multitenancy/MultiTenancyJpaConfiguration.java:92`) → `login` usi DB me → JWT → next APIs `X-Gym-Code` se isolated.

---

## 4) Code Style

- Service: `Map<String,Object> param, HttpServletRequest request` signature → try/catch, `MobileResponseDTOFactory.successMessage/failedMessage`, `DataTypeUtility` conversions, `@Transactional`.
- Controller: `SanitizeData.sanitizeMapObj(param)` hamesha call karo.
- Entity: Lombok `@Getter @Setter`, `@Table(name="gym_details")`.

---

## 5) Auth / Security - MANDATORY

- Login pe signed JWT `src/main/java/com/backend/core/auth/JwtTokenService.java` se banta hai (secret `jwt.secret` = env `JWT_SECRET`, min 32 chars). Token me secret kabhi mat daalo.
- `src/main/java/com/backend/core/auth/AuthInterceptor.java` protected APIs pe `Authorization: Bearer <token>` + token ka `gym_code` == `X-Gym-Code` check karta hai, warna 401.
- **Naya module (members, fees...) banao to uska BASE path `WebConfig.addInterceptors()` me authInterceptor ke saath add karo.**
- Service me login wala gym code `request.getAttribute(AuthInterceptor.AUTH_GYM_CODE)` se lo; get/edit/delete by id me record ka gym_code match karo (dekho `GymDetailsServiceImpl.isAccessible()`).
- Password hamesha BCrypt (`BCryptPasswordEncoder`). Purane plain password login pe apne aap hash ho jaate hain.
- Error me `e.getMessage()` user ko mat bhejo — `e.printStackTrace()` + generic message.
- Server pe env vars: `JWT_SECRET`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS`.
- Naye gym ke liye `gymcommon.gym_registry` me row (gym_code, gym_name, db_name) zaroori hai, warna validateGym/login fail.

---

## 6) Schema changes + Roles (Phase 1 se)

- `hbm2ddl=none` hai aur Flyway use nahi karte. **Har schema change ek nayi SQL file `db/phase<N>_<naam>.sql` me** (`CREATE TABLE IF NOT EXISTS`), har gym DB pe manually chalao. Purani SQL file edit mat karo.
- Har naye tenant table me `gym_code` column + `created_by/created_on/updated_by/updated_on`, paisa `DECIMAL(10,2)`, soft delete `isdelete`.
- Service me gym code / user / role **sirf** `src/main/java/com/backend/utility/AuthContext.java` se: `AuthContext.gymCode(request)`, `username()`, `role()`, `isManager()`. Client ka bheja gym_code kabhi trust mat karo.
- Roles: `OWNER` / `ADMIN` = manager (plans, archive, cancel). `STAFF` = members add/edit + plan assign/renew. Permission check service me: `if (!AuthContext.isManager(request)) return failedMessage("Permission denied")`.
- Membership date / status logic sirf `service/member/MembershipDateCalculator.java` (Java) + `MemberStatusSql.java` (SQL) me — dono same rule rakho, list aur dashboard ke total isi se match karte hain. Unit test: `MembershipDateCalculatorTest`.
- Assign/renew me member row lock (`MemberRepo.lockByIdAndGymCode`) + overlap check; renew me `after_membership_id` se double-tap reject. Membership banne se paid nahi hoti (`payment_status=UNPAID`).
- Response me `LocalDate`/`LocalDateTime` seedha mat bhejo (ObjectMapper me date module nahi) — `stringValue(date)` karo.

---

**Is file ko har AI task se pehle padhna ZAROORI hai. Agar iska ullanghan hua to code review fail samjho.**
