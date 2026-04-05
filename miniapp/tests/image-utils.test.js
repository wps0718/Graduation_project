const assert = require('assert')

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

async function run() {
  const { resolveImageUrl, normalizeProductCardData } = await import('../utils/image.js')

  runTest('resolveImageUrl keeps static assets path', () => {
    const result = resolveImageUrl('/static/pic/高数教材.png')
    assert.strictEqual(result, '/static/pic/高数教材.png')
  })

  runTest('resolveImageUrl converts relative upload path to absolute url', () => {
    const result = resolveImageUrl('/uploads/2026/04/a.png')
    assert.strictEqual(result, 'http://localhost:8080/uploads/2026/04/a.png')
  })

  runTest('resolveImageUrl appends version query for non-static url', () => {
    const result = resolveImageUrl('/uploads/a.png', { version: '123' })
    assert.strictEqual(result, 'http://localhost:8080/uploads/a.png?v=123')
  })

  runTest('normalizeProductCardData uses first image when coverImage is missing', () => {
    const result = normalizeProductCardData({
      id: 1,
      title: '测试商品',
      images: ['/uploads/p1.png']
    })
    assert.strictEqual(result.coverImage, 'http://localhost:8080/uploads/p1.png')
  })
}

run().catch((error) => {
  console.error(error)
  process.exitCode = 1
})
