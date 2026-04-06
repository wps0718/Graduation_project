const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

const picDir = 'G:\\Code\\Graduation_project\\miniapp\\static\\pic';
const tabbarDir = 'G:\\Code\\Graduation_project\\miniapp\\static\\tabbar';

async function compressImages(dir, quality = 70) {
  const files = fs.readdirSync(dir).filter(f => f.endsWith('.png'));
  console.log(`\n📁 Processing: ${dir}`);
  console.log(`   Found ${files.length} PNG files\n`);

  for (const file of files) {
    const input = path.join(dir, file);
    const output = path.join(dir, file + '.tmp');
    const final = path.join(dir, file);
    const stats = fs.statSync(input);
    const oldSize = (stats.size / 1024).toFixed(2);

    try {
      await sharp(input)
        .png({ quality, compressionLevel: 9 })
        .toFile(output);

      fs.unlinkSync(input);
      fs.renameSync(output, final);

      const newStats = fs.statSync(final);
      const newSize = (newStats.size / 1024).toFixed(2);
      const saved = ((1 - newSize / oldSize) * 100).toFixed(1);

      console.log(`  ✅ ${file}: ${oldSize}KB → ${newSize}KB (${saved}% saved)`);
    } catch (err) {
      if (fs.existsSync(output)) fs.unlinkSync(output);
      console.log(`  ❌ ${file}: ${err.message}`);
    }
  }
}

async function main() {
  console.log('🗜️  Starting PNG compression...\n');
  await compressImages(picDir, 70);
  await compressImages(tabbarDir, 80);
  console.log('\n✅ Compression complete!');
}

main().catch(console.error);
