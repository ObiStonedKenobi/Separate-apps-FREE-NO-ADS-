# Separate Instance — P2P Blocker Test Harness

A dedicated Android testing harness built specifically for the Samsung Galaxy S24 Ultra (One UI 9.0 / Android 17, non-root) to independently verify whether a platform's Device-Level Shared-Account Blocker (Blocker A) and Household-Level Trade Blocker (Blocker B) operate autonomously without cross-contamination.

***

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were confirmed during Phase 1 clarification:
> - **Dual Execution Mode**: The harness supports both dedicated in-app sandboxed Web/P2P environments (with isolated cookies, local storage, virtual Android IDs, and per-instance proxy routing) and native app profile launchers.
> - **Toggle 2 (Household Separation)**: Included via per-instance SOCKS5/HTTP proxy egress. Each instance routes its traffic through a dedicated egress node so its public IP and routing footprint are decoupled from the household Wi-Fi/carrier IP without breaking device-wide connectivity.
> - **Test Reporting & Export**: Full test session matrices and checklist verification logs can be exported directly to CSV spreadsheets or plain-text summaries via the native Android Share Sheet.
> - **Legal & Policy Notice**: A prominent, non-dismissible banner confirms this app is exclusively for testing developer-owned platforms with test accounts, not for evading third-party rules.

***

## 1. Overview & Core Concept

### What It Does
"Separate Instance" allows developers to spin up an unlimited number of isolated test instances for any web-based or installed P2P platform client on a locked, non-rooted Samsung Galaxy S24 Ultra running Android 17 / One UI 9.0. Each instance has its own private data sandbox, virtualized identifier profile, independent lifecycle, and telemetry diagnostics.

### The Two-Blocker Problem
1. **Blocker A (Device-Level Shared Account Detection)**: Blocks trading between two accounts if they share physical device signatures, limiting them to messaging only.
2. **Blocker B (Household-Level Trade Block)**: Blocks trading between accounts sharing a household network footprint (same public IP, Wi-Fi BSSID, or local subnet), even if physical devices differ.
3. **The 4-State Test Matrix**:
   - State 1: Both OFF (Control baseline — both blockers trigger).
   - State 2: Device Separation ONLY (Toggle 1 ON, Toggle 2 OFF — default).
   - State 3: Household Separation ONLY (Toggle 1 OFF, Toggle 2 ON).
   - State 4: Both ON (Full isolation — both blockers bypassed if effective).

***

## 2. User Experience & Visual Design

### Key User Flows
1. **Instance Directory & Status (Home)**:
   - View all created test instances with live indicators for Toggle 1 (Device), Toggle 2 (Household), active network type, and external public IP.
   - Quick actions: Launch, Stop, Clone/Duplicate, Edit Toggles, View Diagnostics, and Delete.
2. **Instance Creation Flow**:
   - Target selection: Web/P2P Platform URL or Installed Android Package.
   - Instance Name (e.g. `Test Account Alpha`, `Trader Beta`).
   - Toggle 1: Device-Level Separation (Default: ON).
   - Toggle 2: Household Separation (Default: OFF, with proxy endpoint configuration).
   - Virtual Identity Configuration: Generated randomized Android ID, User-Agent variant, and isolated Keystore alias.
3. **Dedicated Interactive Test Workspace (Instance Sandbox)**:
   - Live sandboxed client interface with dedicated cookie jar, session storage, and WebStorage.
   - Live telemetry bar showing verified egress IP, network latency, connection status (Connected / Reconnecting / Disconnected), and storage consumption.
4. **Structured Test Checklist & Logger**:
   - Real-time QA checklist: "Can message? (YES/NO)", "Can create trade offer? (YES/NO)", "Can complete trade? (YES/NO)", "Platform error code / message", and custom notes.
   - Instant record to persistent Room database.
5. **Test Session Matrix & Export**:
   - Tabular comparison of the 4 test states.
   - Share button to dispatch clean CSV or formatted markdown test logs to email, messaging, or Drive.
6. **Device Info & One UI 9.0 Power Management Guide**:
   - System specs verification (SM-S928U, Knox 3.14, Android 17, Kernel 6.1).
   - Step-by-step deep links to exempt the harness from Samsung Sleeping Apps, Memory Guardian, and Battery Optimization.
7. **Honesty & Limitations Screen**:
   - Explicit declaration of what an unrooted app CAN separate (App-private storage, WebView cookies, HTML5 storage, virtualized Android ID, proxy IP, user-agent) versus what it CANNOT separate without root (real hardware IMEI, baseband serial, StrongBox Knox attestation, cellular IMSI).

### Visual Identity & Theme
- **Theme**: High-precision engineering dark theme (*Obsidian Slate*) with high-contrast status accents.
- **Palette**:
  - Background: Dark slate/neutral `#0E1117` / `#161B22`
  - Primary / Device Isolation: Electric Cyan `#00D2FF`
  - Secondary / Household Separation: Amber Gold `#FFB703`
  - Success / Pass: Neon Mint `#06D6A0`
  - Warning / Blocker Triggered: Crimson Rose `#EF476F`
  - Surface & Card Tonalities: `#21262D` and `#30363D` with subtle borders.
- **Typography**: Clean, monospace-accented technical hierarchy with generous touch targets (48dp+).

***

## 3. Key Product Decisions & Trade-Offs

### Container & Virtualization Strategy on S24 Ultra (Android 17 / One UI 9.0)
```
┌────────────────────────────────────────────────────────────────────────┐
│                   Samsung Galaxy S24 Ultra (Non-Root)                  │
│               Android 17 / One UI 9.0 / Knox 3.14 Enforcing            │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
      ┌────────────────────────────┴─────────────────────────────┐
      ▼                                                          ▼
┌──────────────────────────────┐           ┌────────────────────────────────┐
│   Dedicated In-Harness       │           │   Installed Native Apps        │
│   Web / P2P Sandboxes        │           │   Profile Manager              │
├──────────────────────────────┤           ├────────────────────────────────┤
│ • Isolated Private App Dir   │           │ • Package query & inspection   │
│ • Distinct WebView Profile   │           │ • Multi-user / Dual Messenger  │
│ • Separate Cookie Jar        │           │   Knox guidance & launcher     │
│ • Unique Virtual Android ID  │           │ • Intent-based isolated states │
│ • Per-Instance Proxy Client  │           │ • External app test checklist  │
└──────────────────────────────┘           └────────────────────────────────┘
```

1. **Why Pure Dynamic Class Loading (DCL) of entire native APKs is avoided**:
   - Android 17 and Samsung Knox MDF v3.3 strictly enforce SELinux, W^X memory restrictions, and Google Play dynamic code loading blocks. Injecting foreign APKs into memory without root triggers ART crashes and Play Integrity failures.
   - Therefore, our Dual Mode leverages dedicated in-harness multi-profile runtime sandboxes (ideal for Web/P2P chat platforms, responsive client portals, WebSockets, and browser flows) paired with native package launch coordination and side-by-side test state logging.
2. **Toggle 1 & Toggle 2 Strict Decoupling**:
   - Toggle 1 operates strictly on local storage paths (`/data/user/0/.../instances/<id>/`), cookie stores, and device identifier state machines.
   - Toggle 2 operates strictly on the networking layer (OkHttp / WebView proxy controller or direct SOCKS5/HTTP socket egress).
   - Neither toggle references or modifies the other's state.

***

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                      Jetpack Compose UI Layer                          │
│  [Instance Manager]  [Create Dialog]  [Test Sandbox]  [Export / Share] │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
┌──────────────────────────────────▼─────────────────────────────────────┐
│                          Harness ViewModels                            │
│           InstanceViewModel  •  TestLogViewModel  •  SystemViewModel   │
└───────────────────┬───────────────────────────────┬────────────────────┘
                    │                               │
┌───────────────────▼──────────────┐   ┌────────────▼────────────────────┐
│      Instance Storage Sandbox    │   │      Network Egress Engine      │
├──────────────────────────────────┤   ├─────────────────────────────────┤
│ • File System: instances/<id>/   │   │ • Direct Public IP Resolver     │
│ • WebView Data Directory Suffix  │   │ • Per-Instance Proxy Config     │
│ • Virtual Device Fingerprint     │   │ • Latency & Connectivity Pings  │
└───────────────────┬──────────────┘   └────────────┬────────────────────┘
                    │                               │
┌───────────────────▼───────────────────────────────▼────────────────────┐
│                  Room Persistence & Test Matrix Store                  │
│       InstanceEntity  •  TestLogEntity  •  ChecklistRecordEntity       │
└────────────────────────────────────────────────────────────────────────┘
```

### Persistence Models (Room)
- `TestInstance`: ID, Name, TargetType (WEB / NATIVE_PACKAGE), TargetUrlOrPackage, Toggle1DeviceSeparation, Toggle2HouseholdSeparation, ProxyHost, ProxyPort, ProxyAuth, VirtualAndroidId, CreatedAt, LastLaunchedAt.
- `TestLogEntry`: ID, InstanceId, InstanceName, StateMatrix (1..4), CanMessage, CanTradeOffer, CanCompleteTrade, PlatformMessage, Notes, Timestamp, ExternalIp, NetworkType.

***

## Step-by-Step Implementation Sequence
1. **Configure Dependencies & Manifest**:
   - Room Database (Entities, DAO, Database).
   - OkHttp & Moshi for network telemetry and IP lookup.
   - Required permissions: `INTERNET`, `ACCESS_NETWORK_STATE`, `FOREGROUND_SERVICE`, `POST_NOTIFICATIONS` with Android 17 / One UI compliance.
2. **Sandbox Engine & Virtual Identity**:
   - Dedicated directory partitioning for each instance.
   - Unique virtual `android_id` generator and device info provider.
   - Network diagnostics service for resolving external IP and latency in real time.
3. **Core Jetpack Compose UI**:
   - `HomeScreen`: Instance list, toggle badges, status chips, quick actions.
   - `CreateInstanceSheet`: Package picker & Web platform URL selector, independent Toggle 1 and Toggle 2 switches, proxy configuration.
   - `SandboxScreen`: Live instance viewport with embedded telemetry bar, quick refresh, and checklist bottom drawer.
   - `TestMatrixScreen`: Visual 4-state matrix summary and Share Sheet export (CSV / formatted text).
   - `DeviceInfoScreen`: One UI 9.0 battery optimization checklist with direct system intent launchers.
   - `LimitationsScreen`: Clear disclosure of hardware boundaries on non-rooted Android 17.
4. **App Branding & Polish**:
   - Custom adaptive app icon with high-tech separation motif.
   - Strings and metadata synchronization.
5. **Compilation & Verification**:
   - Run `compile_applet` to ensure zero compilation or syntax errors.
