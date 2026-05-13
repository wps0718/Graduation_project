const fs = require('fs');
const path = require('path');

// 配置压缩参数
const MAX_SIZE_KB = 200;
const QUALITY = 80;

// 需要优化的图片列表（超过200KB的图片）
const largeImages = [
  '台灯.png',
  '羽绒服.png',
  '耳机.png',
  '自行车.png',
  '高数教材.png'
];

console.log('开始优化小程序图片资源...');

largeImages.forEach(imageName => {
  const imagePath = path.join(__dirname, 'static', 'pic', imageName);

  if (fs.existsSync(imagePath)) {
    const stats = fs.statSync(imagePath);
    const fileSizeInKB = stats.size / 1024;

    console.log(`${imageName}: ${fileSizeInKB.toFixed(1)}KB`);

    if (fileSizeInKB > MAX_SIZE_KB) {
      console.log(`  ⚠️  超过${MAX_SIZE_KB}KB限制，需要压缩`);
      // 这里应该调用图片压缩库，如 sharp 或 imagemin
      // 由于环境限制，建议使用微信开发者工具的图片压缩功能
    }
  } else {
    console.log(`${imageName}: 文件不存在`);
  }
});

console.log('\n📝 优化建议:');
console.log('1. 使用微信开发者工具的图片压缩功能');
console.log('2. 推荐格式: PNG用于图标，JPG用于照片');
console.log('3. 尺寸建议: 根据实际显示需求调整');
console.log('4. 可以使用在线工具如 tinypng.com 进行批量压缩');