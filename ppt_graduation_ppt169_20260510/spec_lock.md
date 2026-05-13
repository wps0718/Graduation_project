# Execution Lock

Machine-readable execution contract. Executor MUST `read_file` this before every SVG page.

## canvas
- viewBox: 0 0 1280 720
- format: PPT 16:9

## colors
- bg: #FFFFFF
- secondary_bg: #F5F7FA
- primary: #1565C0
- accent: #FF6F00
- secondary_accent: #0D47A1
- text: #1A1A2E
- text_secondary: #5A5A7A
- text_tertiary: #9E9E9E
- border: #E0E0E0
- success: #2E7D32
- warning: #C62828

## typography
- font_family: "Microsoft YaHei", Arial, sans-serif
- body_family: "Microsoft YaHei", Arial, sans-serif
- code_family: Consolas, "Courier New", monospace
- body: 20
- title: 32
- cover_title: 60
- hero_number: 40
- subtitle: 24
- annotation: 15
- footnote: 12

## icons
- library: tabler-filled
- inventory: home, alert-triangle, code, database, device-mobile, users, shield-lock, shopping-cart, messages, clock-hour-1, settings, chart-bar, check, star

## page_rhythm
- P01: anchor
- P02: dense
- P03: dense
- P04: dense
- P05: dense
- P06: breathing
- P07: dense
- P08: breathing
- P09: dense
- P10: anchor

## forbidden
- Mixing icon libraries
- rgba()
- `<style>`, `class`, `<foreignObject>`, `textPath`, `@font-face`, `<animate*>`, `<script>`, `<iframe>`, `<symbol>`+`<use>`
- `<g opacity>` (set opacity on each child element individually)
- HTML named entities in text — write as raw Unicode
