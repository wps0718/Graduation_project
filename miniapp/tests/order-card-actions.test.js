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
  const content = readFile('../components/order-card/order-card.vue')

  runTest('completed orders show review action without role gate', () => {
    const blockMatch = content.match(/ORDER_STATUS\.COMPLETED[\s\S]*?ORDER_STATUS\.REVIEWED/)
    assert.ok(blockMatch, 'COMPLETED block not found')
    assert.ok(/list\.push\(\{ key: 'review', label: '去评价' \}\)/.test(blockMatch[0]))
    assert.ok(!/role\s*===\s*'buyer'/.test(blockMatch[0]))
  })
}

run().catch((error) => {
  console.error(error)
  process.exitCode = 1
})
