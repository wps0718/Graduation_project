# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

轻院二手 (Qingyuan Secondhand) — a campus secondhand trading platform built as a graduation project for Guangdong Light Industry Vocational and Technical University. The platform consists of three parts: a Java backend, a WeChat mini-program (uni-app) frontend, and a Vue 3 admin panel.

## Commands

### Backend (Java / Maven)
```bash
# Build the entire project (backend)
mvn clean package -DskipTests

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=ProductServiceImplTest

# Run a single test method
mvn test -Dtest=ProductServiceImplTest#testMethodName

# Run backend locally
mvn spring-boot:run
```

### Admin Panel (Vue 3 + Vite)
```bash
cd admin
npm install          # Install dependencies
npm run dev          # Start dev server (port 3000)
npm run build        # Build for production
```

### Mini Program (uni-app)
The mini program is a uni-app project built for WeChat. Uses HBuilderX for development:
```bash
cd miniapp
npm install          # Install dependencies (sharp for image processing)
npm test             # Run Jest unit tests
```
Open the `miniapp` folder in HBuilderX, then run to WeChat dev tools.

## Architecture

### Backend (Spring Boot 3.3.7 + Java 17)

Standard layered architecture:

```
src/main/java/com/qingyuan/secondhand/
├── SecondhandApplication.java    # Entry point (@EnableScheduling, @EnableAsync)
├── config/                       # Spring config (MyBatis-Plus, Redis, WebSocket, CORS, JWT interceptors)
├── common/
│   ├── constant/                 # Redis key constants
│   ├── context/                  # UserContext (ThreadLocal-based user holder)
│   ├── enums/                    # Enums: OrderStatus, ProductStatus, AuthStatus, etc.
│   ├── exception/                # BusinessException + GlobalExceptionHandler
│   ├── interceptor/              # JwtInterceptor (mini app auth) + AdminJwtInterceptor (admin auth)
│   ├── result/                   # Unified Result<T> wrapper (code 1=success, 0=error)
│   └── util/                     # JwtUtil, FileUtil, PhoneUtil, OrderNoUtil, SessionKeyUtil
├── controller/
│   ├── admin/                    # Admin REST API controllers (auth, user, product, order, report, stats, etc.)
│   ├── mini/                     # Mini program REST API controllers (auth, product, order, chat, etc.)
│   └── common/                   # Shared endpoints (file upload)
├── dto/                          # Request DTOs
├── entity/                       # MyBatis-Plus entity classes (User, Product, TradeOrder, ChatMessage, etc.)
├── mapper/                       # MyBatis-Plus mapper interfaces
├── service/                      # Service interfaces
│   └── impl/                     # Service implementations (including *AsyncService for async tasks)
├── task/                         # Scheduled tasks (order expire, auto-confirm, review auto, product auto-off, etc.)
├── vo/                           # Response VOs (view objects)
└── websocket/                    # WebSocket chat implementation
    ├── WebSocketServer.java      # Core WebSocket server
    ├── WebSocketSessionManager   # Session management
    ├── ChatHandshakeInterceptor  # Auth during handshake
    ├── protocol/                 # Message protocol types and payloads
    └── handler/                  # Message handlers (ChatMessageHandler, PingMessageHandler, etc.)
```

**Key patterns:**
- **Auth**: JWT-based via interceptors. Mini-program uses `JwtInterceptor`, admin uses `AdminJwtInterceptor`. Token in `Authorization` header.
- **CORS**: Configured in `WebMvcConfig` (allows localhost:3000 and localhost:5173).
- **Result wrapper**: All APIs return `Result<T>` with `code` (1=success, 0=error).
- **Scheduled tasks**: 6 tasks managed via `@Scheduled` (order expire, auto-confirm, review auto, review remind, product auto-off, user deactivate).
- **Async operations**: `@EnableAsync` with `NoticeAsyncService` and `ProductAsyncService` for non-blocking operations.
- **Meta-object handler**: Auto-fills `createTime`, `updateTime` on entities.

### Admin Panel (Vue 3 + Vite + Element Plus + Pinia + ECharts)

```
admin/src/
├── api/            # Axios-based API modules (auth, user, product, order, etc.)
├── layout/         # Layout component (sidebar + header + content)
├── router/         # Vue Router with navigation guard (redirects to /login if no token)
├── store/          # Pinia auth store
├── utils/          # Auth token helpers, Axios request interceptor
└── views/          # Page components (dashboard, login, user, product, order, report, etc.)
```

- Dev server on port 3000, proxies `/api` → `http://localhost:3576` (strips `/api` prefix)
- Route guard in `router/index.js` checks auth token before navigation

### Mini Program (uni-app + Vue 3 + Pinia)

```
miniapp/
├── components/         # Reusable components (navbar, order-card, price, empty-state)
├── pages/
│   ├── index/          # Home page (product listing, banners, categories, campuses)
│   ├── product/        # Product detail, publish, edit, my-list
│   ├── chat/           # Chat list, chat detail, chat settings
│   ├── order/          # Order list, order detail
│   ├── user/           # User profile, settings, edit-profile
│   ├── auth-sub/       # Campus auth, auth history
│   ├── login-sub/      # Login, SMS login, search, report, help, agreements
│   ├── user-sub/       # Favorite, footprint, review, seller profile, settings
│   └── notification-sub/ # Notifications, received replies, received favorites, followers
├── store/              # Pinia stores (app state, user state)
└── tests/              # Jest unit tests
```

- 4-tab layout: Home, Publish, Messages, Profile
- Tab bar defined in `pages.json`

### Database (MySQL)

Key tables include: `user`, `product`, `trade_order`, `chat_session`, `chat_message`, `campus`, `category`, `college`, `banner`, `notice`, `review`, `report`, `favorite`, `user_follow`, `campus_auth`, `search_keyword`, `notification`, etc.

SQL scripts in `sql/init.sql`.

### Key Infrastructure
- **MySQL** (primary database)
- **Redis** (caching, distributed locks for orders, SMS codes, login fail counting, IM online tracking)
- **WebSocket** (real-time chat, custom protocol with message types: chat, read receipt, ping/pong)
- **File uploads**: Local file storage configured via `upload.path`, served via static resource handler
- **WeChat integration**: `WxConfig` for mini-program login/code exchange

## Development Configuration

The app uses Spring profiles. Default profile is `dev`. Key environment variables (all have dev defaults):

| Variable | Default (dev) | Purpose |
|----------|---------------|---------|
| `SERVER_PORT` | 3576 | Backend server port |
| `SPRING_PROFILES_ACTIVE` | dev | Spring profile |
| `DB_URL` | jdbc:mysql://localhost:3306/secondhand | Database connection |
| `DB_USERNAME` / `DB_PASSWORD` | root / (see application-dev.yml) | Database credentials |
| `REDIS_HOST` / `REDIS_PORT` | localhost / 6379 | Redis connection |
| `JWT_SECRET` | changeme-dev-jwt-secret | JWT signing key |
| `UPLOAD_PATH` | G:/Code/Graduation_project/uploads/ | File upload directory |
| `WX_APP_ID` / `WX_APP_SECRET` | (see application-dev.yml) | WeChat mini-program credentials |

Scheduled tasks can be individually toggled via `task.enabled.*` properties in `application.yml`.

## Business Flows

### Order State Machine
```
1(待接单) → 3(已完成)  [seller confirms, within 7 days]
1(待接单) → 5(已取消)  [buyer cancels or 72h expire]
3(已完成) → 4(已评价)  [buyer submits review or auto-review after 7 days]
```
- Order creation uses Redis distributed lock to prevent duplicate orders on the same product.
- `TradeOrder.expire_time` = 72h after creation → scheduled task auto-cancels.
- `TradeOrder.confirm_deadline` = 7d after completion → scheduled task auto-creates review.

### Product Status Flow
```
0(待审核) → 1(在售)  [admin approves]
0(待审核) → 4(已拒绝) [admin rejects]
1(在售)   → 2(已下架) [seller takes off-shelf]
1(在售)   → 3(已售出) [order completes]
2(已下架) → 1(在售)   [seller re-lists]
(any)     → 2(已下架) [scheduled auto-off after auto_off_time]
```
- `Product.is_deleted` uses `@TableLogic` — deletes are soft deletes.

### Admin Authentication Flow
- Admin logs in via `/admin/employee/login` → receives JWT token.
- `AdminJwtInterceptor` validates token on all `/admin/**` paths (except login).
- Token passed in `Authorization` header.

### Mini Program Auth Flow
- WeChat login: client sends `code` → backend exchanges for `openid` via WeChat API → creates/returns user + JWT.
- Phone+password login: direct credential check → JWT.
- `JwtInterceptor` validates token on all `/mini/**` paths (except login/register).

## Testing

Backend tests use JUnit 5 + Mockito + MockMvc, located in `src/test/java/com/qingyuan/secondhand/`. Test commands are listed in the Commands section above.
