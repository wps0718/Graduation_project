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
  const { buildMenuOptions, buildSharePayload } = await import('../utils/product-detail-helpers.js')

  runTest('owner menu shows off-shelf option when on sale', () => {
    const menu = buildMenuOptions({ isOwner: true, isOffShelf: false })
    assert.deepStrictEqual(menu.items, ['编辑', '下架', '删除'])
    assert.deepStrictEqual(menu.actions, ['edit', 'offShelf', 'delete'])
  })

  runTest('owner menu shows on-shelf option when off shelf', () => {
    const menu = buildMenuOptions({ isOwner: true, isOffShelf: true })
    assert.deepStrictEqual(menu.items, ['编辑', '上架', '删除'])
    assert.deepStrictEqual(menu.actions, ['edit', 'onShelf', 'delete'])
  })

  runTest('guest menu shows share and report', () => {
    const menu = buildMenuOptions({ isOwner: false, isOffShelf: false })
    assert.deepStrictEqual(menu.items, ['分享', '举报'])
    assert.deepStrictEqual(menu.actions, ['share', 'report'])
  })

  runTest('buildSharePayload uses detail fields', () => {
    const detail = {
      id: 10086,
      title: 'iPad Air 4 64G 天蓝色',
      images: ['/static/pic/耳机.png']
    }
    const payload = buildSharePayload(detail)
    assert.strictEqual(payload.title, detail.title)
    assert.strictEqual(payload.path, '/pages/product/detail/detail?id=10086')
    assert.strictEqual(payload.imageUrl, '/static/pic/耳机.png')
  })
}

run().catch((error) => {
  console.error(error)
  process.exitCode = 1
})
