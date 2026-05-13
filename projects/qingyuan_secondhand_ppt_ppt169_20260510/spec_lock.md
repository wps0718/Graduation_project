# Execution Lock

Machine-readable execution contract. Executor MUST `read_file` this before every SVG page. Values NOT listed here must NOT appear in SVGs.

## canvas
- viewBox: 0 0 1280 720
- format: PPT 16:9

## colors
- bg: #FFFFFF
- bg_secondary: #F9FAFB
- primary: #3B82F6
- accent: #1E40AF
- secondary_accent: #F59E0B
- success: #10B981
- purple: #8B5CF6
- warning: #EF4444
- text: #1F2937
- text_secondary: #6B7280
- text_tertiary: #9CA3AF
- border: #E5E7EB
- dark_bg: #1E293B
- dark_card: #334155
- dark_text: #F1F5F9
- dark_text_secondary: #94A3B8

## typography
- font_family: "Microsoft YaHei", "PingFang SC", Arial, sans-serif
- body_family: "Microsoft YaHei", "PingFang SC", Arial, sans-serif
- code_family: Consolas, "Courier New", monospace
- body: 18
- title: 32
- subtitle: 24
- annotation: 14
- cover_title: 60
- hero_number: 36
- page_number: 11

## icons
- library: tabler-filled
- brand_library: simple-icons
- inventory: school, code, chart-pie, message-circle, shield-x, clock, trash, device-mobile, server, database, shield-lock, message, bolt, lock, key, login, certificate, upload, search, messages, clipboard-check, star, heart, bell, clipboard-list, settings, users, calendar, gauge, test-pipe, sparkles, rocket, vuedotjs, springboot, mysql, redis, check, arrow-right, circle-check, chart-bar, chart-line, chart-donut, shield-check, layers

## images
- hero_cover: images/hero_cover.png
- campus_students: images/campus_students.png
- admin_dashboard: images/admin_dashboard.png
- chat_interface: images/chat_interface.png
- miniapp_screenshots: images/miniapp_screenshots.png
- future_vision: images/future_vision.png

## page_rhythm
- P01: anchor
- P02: dense
- P03: anchor
- P04: dense
- P05: dense
- P06: breathing
- P07: dense
- P08: dense
- P09: anchor
- P10: dense
- P11: dense
- P12: dense
- P13: dense
- P14: anchor

## forbidden
- Mixing icon libraries
- rgba()
- `<style>`, `class`, `<foreignObject>`, `textPath`, `@font-face`, `<animate*>`, `<script>`, `<iframe>`, `<symbol>`+`<use>`
- `<g opacity>` (set opacity on each child element individually)
- HTML named entities in text (`&nbsp;`, `&mdash;`, `&copy;`, `&ndash;`, `&reg;`, `&hellip;`, `&bull;` …)
