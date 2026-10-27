# AI INSTRUCTIONS - Gym Mobile App (Portable)

**Har AI modal is file ko pehle padhe, tabhi kaam start kare. Yeh React Native + Redux ka mandatory guide hai.**
> Note: Saare paths project root `src/` se relative hain. Absolute path (`D:\gym-app`) hardcode mat karo.

---

## 1) Common Components - MANDATORY

- **Hamesha `src/components/common/` ke components use karo:**
  - `AppButton`, `DeleteButton`, `EditButton`, `AppInput`, `AppCard`, `AppHeader`, `Loader`, `ConfirmModal`, `EmptyState`
  - Screen me direct `<TouchableOpacity>` / `<ActivityIndicator>` mat likho.

```tsx
// Sahi:
import { AppButton } from '../components/common/AppButton';
import { DeleteButton } from '../components/common/DeleteButton';
<AppButton title="Save" onPress={onSave} loading={loading} />
<DeleteButton onDelete={() => dispatch(deleteGym(id))} />

// Galat:
<TouchableOpacity onPress={onDelete}><Text>Delete</Text></TouchableOpacity>
```

---

## 2) Type Conversions - DataTypeUtility JS Port

- Backend `DataTypeUtility.stringValue()` jaisa yaha `src/utils/typeUtils.ts` use karo:

```ts
import { stringValue, longValue, booleanValue } from '../utils/typeUtils';
const name = stringValue(param.gym_name);
const id = longValue(param.id);
```

- Direct `String(param)` / `parseInt` mat karo.

---

## 3) Redux + Loader

- **Redux Toolkit `src/store/` me hi rakho:** `authSlice.ts` (gymCode, token), `gymSlice.ts` (list, loading), `store.ts`
- Har async `createAsyncThunk` se, UI me `const {loading} = useAppSelector(s => s.gym)` + `<Loader visible={loading} />` common.
- Screen me direct `fetch` mat likho, `dispatch(fetchGyms())` use karo.

```tsx
const dispatch = useAppDispatch();
const { list, loading } = useAppSelector(s => s.gym);
useEffect(() => { dispatch(fetchGyms()); }, []);
<Loader visible={loading} text="Loading..." />
```

---

## 4) Multi-DB Flow

- `gymcommon` catalog → `dbgym` tenant flow: `src/api/client.ts` har request me `X-Gym-Code` + `Authorization` header auto bhejta hai.
- `src/storage/index.ts` me `AsyncStorage` se `gym_code` / `token` save, logout pe `clearStorage()`.

## 4.1) Login / Navigation

- `src/navigation/AppNavigator.tsx` Redux `auth.token` dekh ke screens dikhata hai: token hai to Home/GymForm, nahi to GymCode/Login. Login/logout ke baad `navigation.replace` mat karo — token badalte hi screen khud badalti hai.
- App start pe `restoreSession()` (auto-login). Logout ke liye `dispatch(logoutUser())`.
- Backend 401 de to `src/api/client.ts` ka response interceptor apne aap logout kar deta hai.
- List screens me `useFocusEffect` se data load karo taaki Add/Edit ke baad list fresh ho.
- Edit form me saari fields bhejo (backend sirf bheji hui fields update karta hai).

---

## 5) Project Flow & Structure (Clean/Collo - proper)

```
gym-app/
 src/api/client.ts (axios + X-Gym-Code)
 src/components/common/* (sab common)
 src/components/gym/GymCard.tsx
 src/screens/GymCodeScreen.tsx → LoginScreen.tsx → HomeScreen.tsx → GymFormScreen.tsx
 src/navigation/AppNavigator.tsx
 src/store/* (Redux)
 src/utils/typeUtils.ts
 src/storage/index.ts
```

- Classes alag, flow proper, simple code — normal person samjhe, hyfy animation nahi.

---

## 6) IRONBASE Brutalist design (Phase 1 se naye screens)

- **Naye screens** `src/theme/tokens.ts` (COLORS, BORDER, SHADOW, SPACING, TYPE, STATUS_COLORS) + `src/components/brutal/*` use karein. Rang / font size hardcode mat karo.
  - `BrutalScreen` (safe area + keyboard + footer), `BrutalHeader`, `BrutalBox` (hard shadow), `BrutalButton`, `BrutalInput`, `DateField`, `ChipSelect`, `SearchBar`, `StatusChip`, `StatTile`, `SectionLabel`, `Skeleton`, `BrutalEmpty`, `BottomBar`.
- Saare screens (GymCode, Login, Gyms, GymForm bhi) ab brutal design me hain. `components/common/*` ko restyle mat karo; `Loader`, `ConfirmModal` reuse kar sakte ho. Brand naam `components/brutal/BrandBlock.tsx` (`BRAND_NAME`) me.
- Naya package (navigation, font, icon, date picker...) add mat karo. Date: `DateField` + `src/utils/dateUtils.ts`. Tabs: `BottomBar`.
- Login ke baad stack: Dashboard → Members / MemberForm / MemberDetail / AssignMembership → Plans / PlanForm → Home (Gyms). Naya main screen ho to `BottomBar` TABS me add karo.
- Redux: `memberSlice` (list + pagination + search/status + detail + assign/renew/cancel), `planSlice`, `dashboardSlice`. Har naya slice `logoutUser.fulfilled` pe reset ho. Ek action pe `addCase` ek hi baar — common loading flag ke liye `addMatcher(isPending(...))`.
- Role: `src/hooks/useRole.ts` (`isManager`) sirf button chhupane ke liye; asli check backend karta hai.
- Renew me `after_membership_id: member.latest_membership_id` zaroor bhejo (double tap safe).

---

**Is file ko har AI task se pehle padhna ZAROORI hai.**
