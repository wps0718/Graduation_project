const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

const picDir = 'G:\\Code\\Graduation_project\\miniapp\\static\\pic';

async function convertToWebP() {
  const files = [
    '推广（广州）.png',
    '推广（南海北）.png'
  ];

  console.log('\n🖼️  Converting promotion images to WebP...\n');

  for (const file of files) {
    const input = path.join(picDir, file);
    const outputName = file.replace('.png', '.webp');
    const output = path.join(picDir, outputName);
    const stats = fs.statSync(input);
    const oldSize = (stats.size / 1024).toFixed(2);

    try {
      await sharp(input)
        .webp({ quality: 80 })
        .toFile(output);

      const newStats = fs.statSync(output);
      const newSize = (newStats.size / 1024).toFixed(2);
      const saved = ((1 - newSize / oldSize) * 100).toFixed(1);

      console.log(`  ✅ ${file}: ${oldSize}KB → ${newSize}KB (${saved}% saved)`);
    } catch (err) {
      console.log(`  ❌ ${file}: ${err.message}`);
    }
  }

  console.log('\n✅ WebP conversion complete!');
}

convertToWebP().catch(console.error);
