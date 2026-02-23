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
  const appStore = readFile('../store/app.js')

  runTest('setCampus persists campus selection', () => {
    assert.ok(/setCampus\([\s\S]*?uni\.setStorageSync\('currentCampusId'/.test(appStore))
    assert.ok(/setCampus\([\s\S]*?uni\.setStorageSync\('currentCampusName'/.test(appStore))
  })

  runTest('loadCampusList restores campus selection from storage', () => {
    assert.ok(/loadCampusList\([\s\S]*?uni\.getStorageSync\('currentCampusId'/.test(appStore))
    assert.ok(/loadCampusList\([\s\S]*?uni\.getStorageSync\('currentCampusName'/.test(appStore))
  })
}

run().catch((error) => {
  console.error(error)
  process.exitCode = 1
})
