#!/usr/bin/env node

/**
 * Script de vérification de la cohérence des fichiers de traduction
 * Compare fr.json et en.json pour détecter les clés manquantes
 */

const fs = require('fs');
const path = require('path');

const frPath = path.join(__dirname, '../src/locales/fr.json');
const enPath = path.join(__dirname, '../src/locales/en.json');

function getAllKeys(obj, prefix = '') {
  let keys = [];
  for (const [key, value] of Object.entries(obj)) {
    const fullKey = prefix ? `${prefix}.${key}` : key;
    if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
      keys = keys.concat(getAllKeys(value, fullKey));
    } else {
      keys.push(fullKey);
    }
  }
  return keys;
}

try {
  const frData = JSON.parse(fs.readFileSync(frPath, 'utf8'));
  const enData = JSON.parse(fs.readFileSync(enPath, 'utf8'));

  const frKeys = getAllKeys(frData).sort();
  const enKeys = getAllKeys(enData).sort();

  const frSet = new Set(frKeys);
  const enSet = new Set(enKeys);

  const missingInEn = frKeys.filter(k => !enSet.has(k));
  const missingInFr = enKeys.filter(k => !frSet.has(k));

  console.log('\n🔍 Vérification des fichiers de traduction\n');
  console.log(`📊 Statistiques :`);
  console.log(`   - Clés FR : ${frKeys.length}`);
  console.log(`   - Clés EN : ${enKeys.length}`);

  if (missingInEn.length === 0 && missingInFr.length === 0) {
    console.log('\n✅ Tous les fichiers sont cohérents ! Aucune clé manquante.');
    process.exit(0);
  }

  if (missingInEn.length > 0) {
    console.log(`\n❌ Clés manquantes dans en.json (${missingInEn.length}) :`);
    missingInEn.forEach(k => console.log(`   - ${k}`));
  }

  if (missingInFr.length > 0) {
    console.log(`\n❌ Clés manquantes dans fr.json (${missingInFr.length}) :`);
    missingInFr.forEach(k => console.log(`   - ${k}`));
  }

  console.log('\n');
  process.exit(1);
} catch (error) {
  console.error('❌ Erreur lors de la lecture des fichiers:', error.message);
  process.exit(1);
}
