const assert = require('assert')
const fs = require('fs')
const path = require('path')

function runTest(name, fn) {
  try {
    fn()
    console.log(`✔ ${name}`)
  } catch (error) {
    console.error(`✘ ${name}`)
    console.error(error)
    process.exitCode = 1
  }
}

function readFile(relativePath) {
  const filePath = path.resolve(__dirname, relativePath)
  return fs.readFileSync(filePath, 'utf8')
}

async function run() {
  const content = readFile('../pages/index/index.vue')

  runTest('index page defines LOCAL_BANNERS with required webp paths', () => {
    assert.ok(/const\s+LOCAL_BANNERS\s*=\s*\[/.test(content))
    assert.ok(/\/static\/pic\/推广（南海北）\.webp/.test(content))
    assert.ok(/\/static\/pic\/推广（广州）\.webp/.test(content))
  })

  runTest('loadBanners sets local banners first then uses api as fallback', () => {
    const loadMatch = content.match(/async function loadBanners\(\)\s*\{[\s\S]*?\n\}/)
    assert.ok(loadMatch, 'loadBanners not found')
    assert.ok(/bannerList\.value\s*=\s*LOCAL_BANNERS/.test(loadMatch[0]))
    assert.ok(/get\('\/mini\/banner\/list'/.test(loadMatch[0]))
    assert.ok(/remoteBannerList\.value\s*=/.test(loadMatch[0]))
    assert.ok(!/bannerList\.value\s*=\s*res/.test(loadMatch[0]))
    assert.ok(!/bannerList\.value\s*=\s*remoteBannerList\.value/.test(loadMatch[0]))
  })

  runTest('index banner image error handler exists for local-only fallback', () => {
    assert.ok(/@error="handleBannerImageError\(item\)"/.test(content))
    assert.ok(/async function handleBannerImageError\(/.test(content))
    assert.ok(/id\.startsWith\('local-'\)/.test(content))
    assert.ok(/bannerList\.value\s*=\s*remoteBannerList\.value/.test(content))
  })
}

run().catch((error) => {
  console.error(error)
  process.exitCode = 1
})
