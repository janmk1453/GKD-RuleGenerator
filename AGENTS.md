# AGENTS.md

## Project Overview

GKD Rule Generator — Android app for visually creating GKD (auto-click) rules. Users import GKD snapshots, select nodes on screenshots, and generate selector rules via templates or AI.

## Build & Run

Working directory: `GKD-RuleGenerator/`

```bash
# Debug build (Windows)
build.bat
# or
./gradlew assembleDebug

# Run tests
./gradlew test

# Core module unit test only
./gradlew :core:snapshot:test
```

JDK 17 required. compileSdk=37, minSdk=33.

## Critical: Docs vs Code Mismatch

**`方案.md` and `需求文档.md` describe a planned architecture that differs from the actual code.** Trust the actual code over these docs:

| Docs say | Actual code |
|----------|-------------|
| Hilt/Dagger DI | No DI framework — manual instantiation |
| MIUIX as local module (`:miuix:miuix-ui`) | Maven Central: `top.yukonga.miuix.kmp:miuix-ui:0.9.3` |
| `kapt("hilt-compiler")` | Not present in build files |
| `minSdk = 26` | `minSdk = 33` (miuix-blur requires it) |

## Architecture

```
GKD-RuleGenerator/
├── app/                          # Android app module
│   └── src/main/java/com/gkd/rulegenerator/
│       ├── MainActivity.kt       # Entry point
│       ├── data/                 # AI config, API client
│       ├── ui/
│       │   ├── MainScreen.kt     # Scaffold + bottom nav
│       │   ├── snapshot/         # Snapshot import & node selection
│       │   ├── rule/             # Template-based rule generation
│       │   ├── ai/               # AI-assisted rule generation
│       │   ├── settings/         # Config screens
│       │   └── theme/            # MiuixTheme wrapper
│       └── util/
├── core/
│   ├── snapshot/                 # Snapshot JSON parsing (kotlinx.serialization)
│   ├── selector/                 # GKD selector engine
│   └── rule/                     # Rule models & templates
└── gradle/
```

Data flow: `Screen → ViewModel (StateFlow) → Repository/UseCase → DataSource`

## UI Rules (MIUIX-only)

These are hard requirements from `UI 规范.md`:

- **All UI components must come from miuix** — `Card`, `TopAppBar`, `NavigationBar`, `SmallTitle`, `TextButton`, `TextField`, `SwitchPreference`, `ArrowPreference`, `OverlayDropdownPreference`, etc. Never use raw Material3 components for user-facing elements.
- **Squircle shapes**: Never use `RoundedCornerShape`. For custom shapes use `top.yukonga.miuix.kmp.squircle.*`:
  - Non-clickable background → `Modifier.squircleBackground(color, radius)`
  - Clip content (images) → `Modifier.squircleClip(radius)`
  - Clickable → `Modifier.squircleSurface(color, radius)` + `.clickable{}`
  - Exception: 3dp badges can use `clip(RoundedCornerShape(3.dp))`
- **Page skeleton**: `Scaffold` + `TopAppBar(scrollBehavior)` + `LazyColumn`
  - LazyColumn needs: `.scrollEndHaptic().overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection)`
  - `contentPadding = PaddingValues(top = innerPadding.calculateTopPadding())` — top only, no bottom
  - End spacer: `item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }`
- **Blur bars**: Wrap TopAppBar/NavigationBar with `BlurredBar` + `rememberBlurBackdrop()`
- **Card spacing**: `padding(horizontal = 12.dp).padding(bottom = 12.dp)` — no `Arrangement.spacedBy`
- **TextField forms**: No Card wrapper, just `padding(horizontal = 12.dp).padding(bottom = 12.dp)`
- **Dialogs**: Long content → `Column(Modifier.heightIn(max = 500.dp))` with scrollable area + fixed bottom buttons
- **Theme colors**: Always `MiuixTheme.colorScheme.*` — never hardcode `Color(0xFF...)`
- **Text styles**: Always `MiuixTheme.textStyles.*`

## i18n

- All user-facing strings: `stringResource(R.string.xxx)` in Composables, `context.getString(R.string.xxx)` elsewhere
- Add new strings to both `app/src/main/res/values/strings.xml` AND `res/values-zh-rCN/strings.xml`
- Key format: `{page}_{description}`, common buttons use `common_` prefix
- Code comments in Chinese, log messages in English

## Conventions

- **State**: Use `collectAsStateWithLifecycle()` (not `collectAsState`)
- **Immutable state**: `@Immutable data class` for UiState; use `ImmutableList`/`ImmutableMap` from `kotlinx.collections.immutable` for collections
- **Composable API**: Expose `modifier: Modifier = Modifier` as first optional parameter
- **Wide screen**: Adapt layout when width ≥ 600dp (NavigationRail instead of NavigationBar)
- **No local miuix**: MIUIX is consumed from Maven Central, the `miuix-0.9.3/` folder at repo root is a reference copy only — do not add it as a Gradle include

## GKD Domain — Selector Syntax

GKD uses a CSS-like selector syntax to match UI nodes. Selectors are the core of every rule.

### Basic Attribute Selectors

| Syntax | Meaning | Example |
|--------|---------|---------|
| `[attr="value"]` | Exact match | `[text="跳过"]` |
| `[attr*="value"]` | Contains | `[text*="跳过"]` |
| `[attr^="value"]` | Starts with | `[id^="com.example"]` |
| `[attr$="value"]` | Ends with | `[id$="btn_skip"]` |
| `[attr~="regex"]` | Regex match | `[text~="(?i)skip"]` |

### Common Node Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| `text` | string | Node text content |
| `desc` | string | Content description (accessibility) |
| `id` | string | Resource ID (e.g. `com.example:id/btn_skip`) |
| `vid` | string | View ID — short form (e.g. `btn_skip`) |
| `name` | string | Class name (e.g. `TextView`, `Button`, `LinearLayout`) |
| `clickable` | boolean | Whether clickable |
| `visibleToUser` | boolean | Whether visible on screen |
| `editable` | boolean | Whether editable (EditText) |
| `focusable` | boolean | Whether focusable |
| `checked` | boolean | Whether checked (CheckBox) |
| `enabled` | boolean | Whether enabled |
| `text.length` | int | Length of text attribute |

### Boolean Logic

- `[a="1"][b="2"]` — AND (implicit)
- `[a="1" && b="2"]` — AND (explicit)
- `[a="1" || b="2"]` — OR

### Relationship Combinators

| Syntax | Meaning |
|--------|---------|
| `A > B` | B is direct child of A |
| `A B` | B is descendant of A |
| `A + B` | B is adjacent sibling of A |
| `A ~ B` | B is general sibling of A |
| `A < B` | A is direct child of B (reverse) |
| `A << B` | A is descendant of B (reverse) |

### Quick Query

- `@` marks the target node: `@TextView[text="跳过"]`
- When `fastQuery: true` is set, prefer `vid` or `id` for performance

### Index Selectors

- `(n)` — nth child
- `(n,m)` — nth to mth child

### Complete Selector Examples

```
# Click skip button
[vid="btn_skip"][clickable=true]

# Close popup by text
[text="关闭" || text="取消"][clickable=true][visibleToUser=true]

# Skip splash ad
[text*="跳过"][text.length<10][visibleToUser=true]

# Click specific node with parent constraint
@TextView[text="确定"] < LinearLayout[clickable=true]

# Match by resource ID
[id="com.example:id/btn_agree"][clickable=true]
```

## GKD Domain — Rule Format

Rules use JSON format (standard JSON with double quotes, not JSON5):

```json
{
  "id": "com.example.app",
  "name": "应用名称",
  "groups": [
    {
      "key": 0,
      "name": "规则组名称",
      "rules": [
        {
          "key": 0,
          "name": "规则描述",
          "fastQuery": true,
          "activityIds": "com.example.app.MainActivity",
          "matches": "[vid=\"btn_skip\"][clickable=true]",
          "action": "click",
          "matchOnce": true,
          "matchTime": 10000,
          "actionMaximum": 1,
          "resetMatch": "app",
          "snapshotUrls": []
        }
      ]
    }
  ]
}
```

### Rule Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | App package name |
| `name` | string | Display name |
| `groups` | array | Rule groups |
| `groups[].key` | int | Unique group key |
| `groups[].name` | string | Group display name |
| `groups[].rules` | array | Individual rules |
| `rules[].matches` | string | **Selector expression** (the core) |
| `rules[].action` | string | `click`, `longClick`, `inputText`, `scrollUp`, `scrollDown` |
| `rules[].fastQuery` | boolean | Enable fast query (use with `vid`/`id`) |
| `rules[].activityIds` | string | Limit to specific Activity |
| `rules[].matchOnce` | boolean | Match only once per session |
| `rules[].matchTime` | int | Match timeout in ms |
| `rules[].actionMaximum` | int | Max execution count |
| `rules[].resetMatch` | string | Reset timing (`app` = on app switch) |
| `rules[].snapshotUrls` | array | Related snapshot links |

### Anti-Misfire Best Practices

1. **Prefer `vid` or `id`** — unique and stable, unlike `text` (may change with locale)
2. **Add constraints**: `[clickable=true]`, `[visibleToUser=true]`, `[text.length<10]`
3. **Limit with `activityIds`** — prevent triggering on wrong screens
4. **Use `matchOnce: true`** for one-shot actions (skip ad, close popup)
5. **Set `matchTime`** — avoid indefinite waiting
6. **Keep selectors minimal** — fewer attributes = faster matching

### Common Rule Templates

**Splash ad skip:**
```json
{ "matches": "[text*=\"跳过\"][text.length<10][visibleToUser=true]", "action": "click", "matchOnce": true }
```

**Popup close:**
```json
{ "matches": "[text=\"关闭\" || text=\"取消\"][clickable=true]", "action": "click" }
```

**Update prompt dismiss:**
```json
{ "matches": "[text*=\"稍后\" || text*=\"取消\"][visibleToUser=true]", "action": "click" }
```

## GKD Domain — Snapshot Structure

A snapshot is a zip file from the GKD app containing:

```
snapshot.zip
├── {id}.json    # Node tree, app info, device info
└── {id}.png     # Screenshot
```

The JSON structure:
```json
{
  "id": 1785237014493,
  "appId": "com.coolapk.market",
  "activityId": "com.coolapk.market.view.main.MainActivity",
  "screenWidth": 1440,
  "screenHeight": 3200,
  "appInfo": { "id": "...", "name": "酷安", ... },
  "device": { "model": "...", "manufacturer": "Xiaomi", ... },
  "nodes": [
    {
      "id": 0,
      "pid": -1,
      "attr": {
        "id": null,
        "vid": null,
        "name": "android.widget.FrameLayout",
        "text": null,
        "desc": null,
        "clickable": false,
        "visibleToUser": true,
        "left": 0, "top": 0, "right": 1440, "bottom": 3200,
        "width": 1440, "height": 3200,
        "depth": 0, "index": 0, "childCount": 3
      }
    }
  ]
}
```

Key node attributes for rule writing: `id`, `vid`, `name`, `text`, `desc`, `clickable`, `visibleToUser`, `editable`, `bounds`.

## GKD Domain — References

| Resource | URL | Description |
|----------|-----|-------------|
| GKD Official | https://gkd.li | Main site |
| Selector Syntax | https://gkd.li/guide/selector | Complete selector reference |
| Node Attributes | https://gkd.li/guide/node | All node attribute methods |
| Selector Examples | https://gkd.li/guide/example | Practical selector examples |
| Query Optimization | https://gkd.li/guide/optimize | Fast query and performance |
| Snapshot Guide | https://gkd.li/guide/snapshot | How to capture and use snapshots |
| Subscription Format | https://gkd.li/guide/subscription | Rule JSON format spec |
| GKD GitHub | https://github.com/gkd-kit/gkd | Source code |
| Example Subscriptions | https://github.com/gkd-kit/gkd/blob/main/packages/gkd/public/store | Official subscription store |
| Selector Syntax (alt) | https://github.com/Lin-arm/GKD_subscription/blob/main/docs/Selectors.md | Community selector docs |

## Key Files

| File | Purpose |
|------|---------|
| `酷安_MainActivity-1785237014493/*.json` | Sample snapshot for testing |
| `miuix-0.9.3/CLAUDE.md` | MIUIX library conventions (component API patterns) |
