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
    const result = resolveImageUrl('/static/pic/placeholder.png')
    assert.strictEqual(result, '/static/pic/placeholder.png')
  })

  runTest('resolveImageUrl converts relative upload path to absolute url', () => {
    const result = resolveImageUrl('/uploads/2026/04/a.png')
    assert.strictEqual(result, 'https://7481afbe.r34.cpolar.top/uploads/2026/04/a.png')
  })

  runTest('resolveImageUrl appends version query for non-static url', () => {
    const result = resolveImageUrl('/uploads/a.png', { version: '123' })
    assert.strictEqual(result, 'https://7481afbe.r34.cpolar.top/uploads/a.png?v=123')
  })

  runTest('normalizeProductCardData uses first image when coverImage is missing', () => {
    const result = normalizeProductCardData({
      id: 1,
      title: '测试商品',
      images: ['/uploads/p1.png']
    })
    assert.strictEqual(result.coverImage, 'https://7481afbe.r34.cpolar.top/uploads/p1.png')
  })
}

run().catch((error) => {
  console.error(error)
  process.exitCode = 1
})
