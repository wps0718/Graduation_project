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
  const content = readFile('../pages/order/list/list.vue')

  runTest('cancel reasons include role-specific text', () => {
    assert.ok(/roleSpecificReason/.test(content))
    assert.ok(/activeTab\.value\s*===\s*'buyer'\s*\?\s*'不想买了'\s*:\s*'不想卖了'/.test(content))
    assert.ok(/\['双方协商取消',\s*'对方无响应',\s*roleSpecificReason,\s*'其他'\]/.test(content))
  })
}

run().catch((error) => {
  console.error(error)
  process.exitCode = 1
})
