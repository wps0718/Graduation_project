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

function extractQuickReplies(content) {
  const match = content.match(/export const QUICK_REPLIES\s*=\s*\[([\s\S]*?)\]/)
  if (!match) return []
  const list = match[1].match(/'([^']+)'/g) || []
  return list.map((item) => item.replace(/^'|'$/g, ''))
}

async function run() {
  const detailContent = readFile('../pages/chat/detail/detail.vue')
  const constantContent = readFile('../utils/constant.js')

  runTest('chat detail uses QUICK_REPLIES constant', () => {
    assert.ok(/const\s+quickReplies\s*=\s*QUICK_REPLIES/.test(detailContent))
  })

  runTest('QUICK_REPLIES matches required fixed texts', () => {
    const replies = extractQuickReplies(constantContent)
    assert.deepStrictEqual(replies, ['还在吗？', '可以小刀吗？', '什么时候方便？'])
  })
}

run().catch((error) => {
  console.error(error)
  process.exitCode = 1
})
