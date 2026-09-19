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

**Is file ko har AI task se pehle padhna ZAROORI hai. Agar iska ullanghan hua to code review fail samjho.**
