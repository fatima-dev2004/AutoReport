import { jsPDF } from 'jspdf';
import 'jspdf-autotable';
import html2canvas from 'html2canvas';
import sewsLogo from '../assets/sews-logo.png';

/**
 * Générateur de PDF professionnel avec design, logo et graphiques
 */

// Couleurs EXACTES du Dashboard AutoReport
const COLORS = {
  primary: [59, 130, 246], // Bleu (#3B82F6) - Production
  secondary: [13, 148, 136], // Teal (#0D9488) - Qualité  
  alert: [239, 68, 68], // Rouge (#EF4444) - Défauts
  dark: [15, 23, 42], // Bleu foncé sidebar
  light: [248, 250, 252], // Fond clair
  border: [226, 232, 240], // Bordure grise
  text: [51, 65, 85], // Texte foncé
  textMuted: [148, 163, 184], // Texte clair
  white: [255, 255, 255], // Blanc pur
  progressBlue: [59, 130, 246], // Barre bleue qualité
  progressGray: [226, 232, 240], // Fond barre grise
};

/**
 * Génère un PDF du rapport Dashboard avec design professionnel
 */
export async function generateDashboardPDF(data, dateRange) {
  const doc = new jsPDF('p', 'mm', 'a4');
  const pageWidth = doc.internal.pageSize.getWidth();
  const pageHeight = doc.internal.pageSize.getHeight();

  console.log('🔵 Génération PDF démarrée...');
  console.log('📐 Dimensions:', { pageWidth, pageHeight });

  // ============ PAGE DE GARDE ============
  // Charger le logo d'abord si disponible
  let logoImg = null;
  try {
    console.log('📸 Chargement logo...', sewsLogo);
    logoImg = await loadImageFromUrl(sewsLogo);
    console.log('✅ Logo chargé:', logoImg);
  } catch (error) {
    console.error('❌ Logo non chargé:', error);
  }
  
  console.log('🎨 Création page de garde...');
  addCoverPage(doc, dateRange, pageWidth, pageHeight, logoImg);
  console.log('✅ Page de garde créée');

  // ============ PAGE 2 : À PROPOS DU RAPPORT ============
  doc.addPage();
  addAboutPage(doc, dateRange, pageWidth, pageHeight);
  console.log('✅ Page À propos créée');

  // ============ NOUVELLE PAGE : CONTENU ============
  doc.addPage();
  let yPosition = 20;

  // En-tête de page
  addPageHeader(doc, 'Rapport de Production', pageWidth);
  yPosition = 35;

  // ============ SECTION 1 : INDICATEURS CLÉS ============
  addSectionTitle(doc, 'Indicateurs généraux', yPosition);
  yPosition += 10;

  // Cartes KPI avec design
  yPosition = addKpiCards(doc, data, yPosition, pageWidth);
  yPosition += 10;

  // ============ SECTION 2 : PRODUCTION PAR LIGNE ============
  if (data.productionByLine && Object.keys(data.productionByLine).length > 0) {
    addSectionTitle(doc, 'Production par ligne', yPosition);
    yPosition += 10;

    yPosition = addProductionByLineTable(doc, data.productionByLine, yPosition, pageWidth);
    yPosition += 10;
  }

  // Footer
  addPageFooter(doc, pageHeight);

  return doc;
}

/**
 * Page de garde simple : Logo + Nom App + Titre Rapport
 */
function addCoverPage(doc, dateRange, pageWidth, pageHeight, logoImg) {
  console.log('🎨 addCoverPage appelée');
  console.log('📊 Params:', { pageWidth, pageHeight, hasLogo: !!logoImg });
  
  // Fond blanc propre
  doc.setFillColor(...COLORS.white);
  doc.rect(0, 0, pageWidth, pageHeight, 'F');
  console.log('✅ Fond blanc ajouté');

  // Logo SEWS en haut (centré) si chargé
  if (logoImg) {
    try {
      const logoWidth = 50;
      const logoHeight = 20;
      const logoX = (pageWidth - logoWidth) / 2;
      const logoY = 40;
      
      console.log('📸 Ajout logo à position:', { logoX, logoY, logoWidth, logoHeight });
      doc.addImage(logoImg, 'PNG', logoX, logoY, logoWidth, logoHeight);
      console.log('✅ Logo ajouté');
    } catch (error) {
      console.error('❌ Erreur ajout logo:', error);
    }
  } else {
    console.warn('⚠️ Pas de logo à ajouter');
  }

  // Nom de l'application (sous le logo)
  doc.setFontSize(16);
  doc.setTextColor(...COLORS.primary); // Bleu
  doc.setFont('helvetica', 'bold');
  doc.text('AUTOREPORT SYSTEM', pageWidth / 2, 75, { align: 'center' });
  console.log('✅ Nom app ajouté');

  // Ligne décorative
  doc.setDrawColor(...COLORS.border);
  doc.setLineWidth(0.5);
  doc.line(pageWidth / 2 - 40, 80, pageWidth / 2 + 40, 80);

  // Titre principal du rapport
  const titleY = pageHeight / 2 - 20;
  
  doc.setFontSize(36);
  doc.setTextColor(...COLORS.text);
  doc.setFont('helvetica', 'bold');
  doc.text('RAPPORT DE', pageWidth / 2, titleY, { align: 'center' });
  
  doc.setFontSize(40);
  doc.setTextColor(...COLORS.primary); // Bleu
  doc.setFont('helvetica', 'bold');
  doc.text('PRODUCTION', pageWidth / 2, titleY + 15, { align: 'center' });

  // Sous-titre
  doc.setFontSize(16);
  doc.setTextColor(...COLORS.textMuted);
  doc.setFont('helvetica', 'normal');
  doc.text('Tableau de bord', pageWidth / 2, titleY + 30, { align: 'center' });
  console.log('✅ Titres ajoutés');

  // Cadre avec les dates (plus bas)
  const infoY = titleY + 50;
  const infoWidth = 150;
  const infoHeight = 50;
  const infoX = (pageWidth - infoWidth) / 2;

  // Ombre légère
  doc.setFillColor(230, 230, 230);
  doc.roundedRect(infoX + 1, infoY + 1, infoWidth, infoHeight, 3, 3, 'F');
  
  // Cadre blanc
  doc.setFillColor(...COLORS.white);
  doc.setDrawColor(...COLORS.border);
  doc.setLineWidth(1);
  doc.roundedRect(infoX, infoY, infoWidth, infoHeight, 3, 3, 'FD');

  // Date de génération
  const today = new Date().toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });

  doc.setFontSize(10);
  doc.setTextColor(...COLORS.text);
  doc.setFont('helvetica', 'bold');
  doc.text('Généré le', infoX + infoWidth / 2, infoY + 15, { align: 'center' });

  doc.setFontSize(14);
  doc.setTextColor(...COLORS.primary);
  doc.setFont('helvetica', 'bold');
  doc.text(today, infoX + infoWidth / 2, infoY + 25, { align: 'center' });

  // Séparateur
  doc.setDrawColor(...COLORS.border);
  doc.setLineWidth(0.3);
  doc.line(infoX + 15, infoY + 30, infoX + infoWidth - 15, infoY + 30);

  // Période analysée
  const startDate = new Date(dateRange.start).toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
  const endDate = new Date(dateRange.end).toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });

  doc.setFontSize(10);
  doc.setTextColor(...COLORS.text);
  doc.setFont('helvetica', 'bold');
  doc.text('Période', infoX + infoWidth / 2, infoY + 38, { align: 'center' });

  doc.setFontSize(11);
  doc.setFont('helvetica', 'normal');
  doc.text(`${startDate}  →  ${endDate}`, infoX + infoWidth / 2, infoY + 46, { align: 'center' });
  console.log('✅ Dates ajoutées');

  // Footer
  doc.setFontSize(9);
  doc.setTextColor(...COLORS.textMuted);
  doc.setFont('helvetica', 'italic');
  doc.text('AutoReport © 2024 - Système de rapports automatisés', pageWidth / 2, pageHeight - 20, {
    align: 'center',
  });
  console.log('✅ Footer ajouté');
  console.log('🎉 Page de garde terminée');
}

/**
 * Page "À propos du rapport" - Description complète
 */
function addAboutPage(doc, dateRange, pageWidth, pageHeight) {
  // Fond blanc propre
  doc.setFillColor(...COLORS.white);
  doc.rect(0, 0, pageWidth, pageHeight, 'F');

  let y = 30;

  // Titre de la page
  doc.setFontSize(20);
  doc.setTextColor(...COLORS.primary);
  doc.setFont('helvetica', 'bold');
  doc.text('À propos de ce rapport', pageWidth / 2, y, { align: 'center' });
  y += 20;

  // Introduction
  doc.setFontSize(11);
  doc.setTextColor(...COLORS.text);
  doc.setFont('helvetica', 'normal');
  
  const startDate = new Date(dateRange.start).toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
  const endDate = new Date(dateRange.end).toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });

  const introText = `Ce rapport de production, généré automatiquement par AutoReport System, présente une synthèse complète de l'activité de production de SEWS CABIND MAROC sur la période du ${startDate} au ${endDate}.`;
  
  const introLines = doc.splitTextToSize(introText, pageWidth - 50);
  doc.text(introLines, 25, y);
  y += introLines.length * 6 + 15;

  // Section "Il regroupe :"
  doc.setFontSize(12);
  doc.setFont('helvetica', 'bold');
  doc.text('Il regroupe :', 25, y);
  y += 10;

  // Liste à puces avec icônes
  doc.setFontSize(10);
  doc.setFont('helvetica', 'normal');
  
  const items = [
    {
      title: 'Les indicateurs clés de performance :',
      text: 'production totale, nombre de défauts constatés, taux de qualité et taux de défaut, permettant d\'évaluer rapidement la performance globale sur la période.',
    },
    {
      title: 'La répartition de la production par ligne',
      text: '(A, B, C, D), avec le détail des unités produites et des défauts associés à chaque ligne.',
    },
    {
      title: 'L\'évolution journalière de la production,',
      text: 'sous forme graphique, pour suivre les tendances et identifier d\'éventuelles variations sur la période sélectionnée.',
    },
    {
      title: 'La comparaison de la production par ligne,',
      text: 'permettant d\'identifier les lignes les plus performantes et celles nécessitant une attention particulière.',
    },
  ];

  items.forEach((item) => {
    // Puce (cercle bleu)
    doc.setFillColor(...COLORS.primary);
    doc.circle(28, y - 1.5, 1.5, 'F');

    // Titre en gras
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(...COLORS.text);
    doc.text(item.title, 33, y);
    
    // Texte normal
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(...COLORS.textMuted);
    const lines = doc.splitTextToSize(item.text, pageWidth - 60);
    doc.text(lines, 33, y + 5);
    
    y += 5 + lines.length * 5 + 8;
  });

  // Séparateur
  y += 5;
  doc.setDrawColor(...COLORS.border);
  doc.setLineWidth(0.5);
  doc.line(25, y, pageWidth - 25, y);
  y += 15;

  // Note finale
  doc.setFontSize(10);
  doc.setFont('helvetica', 'italic');
  doc.setTextColor(...COLORS.textMuted);
  
  const finalText = 'Ce document est destiné à un usage interne pour le suivi qualité et la prise de décision opérationnelle. Les données sont extraites automatiquement du système de production et actualisées à chaque génération.';
  const finalLines = doc.splitTextToSize(finalText, pageWidth - 50);
  doc.text(finalLines, 25, y);

  // Footer de page
  doc.setFontSize(8);
  doc.setTextColor(...COLORS.textMuted);
  doc.setFont('helvetica', 'normal');
  doc.text('AutoReport System - SEWS CABIND MAROC', pageWidth / 2, pageHeight - 15, {
    align: 'center',
  });
}

/**
 * En-tête de page avec ligne décorative
 */
function addPageHeader(doc, title, pageWidth) {
  // Ligne supérieure ambrée
  doc.setDrawColor(...COLORS.primary);
  doc.setLineWidth(2);
  doc.line(20, 15, pageWidth - 20, 15);

  // Titre
  doc.setFontSize(18);
  doc.setTextColor(...COLORS.dark);
  doc.setFont('helvetica', 'bold');
  doc.text(title, 20, 25);

  // Sous-ligne
  doc.setDrawColor(...COLORS.border);
  doc.setLineWidth(0.5);
  doc.line(20, 28, pageWidth - 20, 28);
}

/**
 * Titre de section avec soulignement
 */
function addSectionTitle(doc, title, y) {
  doc.setFontSize(14);
  doc.setTextColor(...COLORS.text);
  doc.setFont('helvetica', 'bold');
  doc.text(title, 20, y);

  // Soulignement ambre
  const textWidth = doc.getTextWidth(title);
  doc.setDrawColor(...COLORS.primary);
  doc.setLineWidth(0.8);
  doc.line(20, y + 1, 20 + textWidth, y + 1);
}

/**
 * Cartes KPI style Dashboard (4 cartes identiques au Dashboard)
 */
function addKpiCards(doc, data, y, pageWidth) {
  const cardWidth = (pageWidth - 70) / 4;
  const cardHeight = 32;
  const startX = 20;
  const gap = 5;

  const kpis = [
    {
      label: 'PRODUCTION TOTALE',
      value: formatNumber(data.totalProduction || 0),
      unit: 'unités',
      color: COLORS.primary, // Bleu
      hasBar: false,
    },
    {
      label: 'DÉFAUTS CONSTATÉS',
      value: formatNumber(data.totalDefects || 0),
      unit: 'unités',
      color: COLORS.alert, // Rouge
      hasBar: false,
    },
    {
      label: 'TAUX DE QUALITÉ',
      value: `${data.qualityRate ?? 0}`,
      unit: '%',
      color: COLORS.secondary, // Teal
      hasBar: true,
      barValue: data.qualityRate || 0,
      barColor: COLORS.progressBlue, // Barre bleue
    },
    {
      label: 'TAUX DE DÉFAUT',
      value: `${data.defectRate ?? 0}`,
      unit: '%',
      color: COLORS.alert, // Rouge
      hasBar: true,
      barValue: data.defectRate || 0,
      barColor: COLORS.progressGray, // Barre grise
    },
  ];

  kpis.forEach((kpi, index) => {
    const x = startX + index * (cardWidth + gap);

    // Rectangle de fond blanc
    doc.setFillColor(...COLORS.white);
    doc.setDrawColor(...COLORS.border);
    doc.setLineWidth(0.5);
    doc.roundedRect(x, y, cardWidth, cardHeight, 2, 2, 'FD');

    // Label en haut (petit, gris, uppercase)
    doc.setFontSize(7);
    doc.setTextColor(...COLORS.textMuted);
    doc.setFont('helvetica', 'normal');
    doc.text(kpi.label, x + cardWidth / 2, y + 6, { align: 'center' });

    // Valeur (grande, colorée, bold)
    doc.setFontSize(18);
    doc.setTextColor(...kpi.color);
    doc.setFont('helvetica', 'bold');
    doc.text(kpi.value, x + cardWidth / 2, y + 16, { align: 'center' });

    // Unité (petite, grise)
    doc.setFontSize(8);
    doc.setTextColor(...COLORS.textMuted);
    doc.setFont('helvetica', 'normal');
    doc.text(kpi.unit, x + cardWidth / 2, y + 21, { align: 'center' });

    // Barre de progression (si applicable)
    if (kpi.hasBar) {
      const barWidth = cardWidth - 10;
      const barHeight = 3;
      const barX = x + 5;
      const barY = y + cardHeight - 7;
      const percentage = Math.min(Math.max(parseFloat(kpi.barValue) || 0, 0), 100);

      // Fond de la barre (gris clair)
      doc.setFillColor(...COLORS.progressGray);
      doc.roundedRect(barX, barY, barWidth, barHeight, 1, 1, 'F');

      // Barre remplie (bleue pour qualité, grise pour défaut)
      const fillWidth = (barWidth * percentage) / 100;
      if (fillWidth > 0) {
        doc.setFillColor(...kpi.barColor);
        doc.roundedRect(barX, barY, fillWidth, barHeight, 1, 1, 'F');
      }
    }
  });

  return y + cardHeight + 8;
}

/**
 * Tableau de production par ligne (style Dashboard propre)
 */
function addProductionByLineTable(doc, productionByLine, y, pageWidth) {
  // Vérifier que productionByLine existe et n'est pas vide
  if (!productionByLine || typeof productionByLine !== 'object' || Object.keys(productionByLine).length === 0) {
    return y; // Retourner y sans rien faire si pas de données
  }

  const tableData = Object.entries(productionByLine).map(([line, value]) => {
    const production = typeof value === 'number' ? value : (value.qty || value.quantity || 0);
    const defects = typeof value === 'object' ? (value.defects || 0) : 0;
    
    return [
      line,
      formatNumber(production),
      formatNumber(defects),
    ];
  });

  doc.autoTable({
    startY: y,
    head: [['Ligne', 'Production', 'Défauts']],
    body: tableData,
    theme: 'grid',
    headStyles: {
      fillColor: COLORS.dark,
      textColor: [255, 255, 255],
      fontStyle: 'bold',
      fontSize: 10,
      halign: 'center',
      cellPadding: 3,
    },
    bodyStyles: {
      fontSize: 10,
      textColor: COLORS.text,
      halign: 'center',
      cellPadding: 3,
    },
    columnStyles: {
      0: { 
        fontStyle: 'bold',
        textColor: COLORS.text,
      },
      1: {
        fontStyle: 'bold',
        textColor: COLORS.primary, // Bleu pour production
      },
      2: {
        textColor: COLORS.alert, // Rouge pour défauts
      },
    },
    alternateRowStyles: {
      fillColor: COLORS.light,
    },
    margin: { left: 20, right: 20 },
    tableWidth: pageWidth - 40,
  });

  return doc.lastAutoTable.finalY + 5;
}

/**
 * Footer de page
 */
function addPageFooter(doc, pageHeight) {
  const y = pageHeight - 15;

  // Ligne supérieure
  doc.setDrawColor(...COLORS.border);
  doc.setLineWidth(0.3);
  doc.line(20, y - 5, doc.internal.pageSize.getWidth() - 20, y - 5);

  // Texte du footer
  doc.setFontSize(8);
  doc.setTextColor(...COLORS.textMuted);
  doc.setFont('helvetica', 'italic');

  const today = new Date().toLocaleDateString('fr-FR');
  doc.text(`Généré le ${today}`, 20, y);
  doc.text('AutoReport © 2024', doc.internal.pageSize.getWidth() - 20, y, { align: 'right' });
}

/**
 * Ajoute un graphique capturé en image
 */
export async function addChartToPDF(doc, chartElement, title, yPosition) {
  if (!chartElement) {
    console.warn('Element chart non fourni');
    return yPosition;
  }

  try {
    const canvas = await html2canvas(chartElement, {
      backgroundColor: '#ffffff',
      scale: 1.5, // Qualité réduite pour meilleure performance
      logging: false,
    });

    const imgData = canvas.toDataURL('image/png');
    const pageWidth = doc.internal.pageSize.getWidth();
    const imgWidth = pageWidth - 40; // Marges de 20mm de chaque côté
    let imgHeight = (canvas.height * imgWidth) / canvas.width;
    
    // Limiter la hauteur maximale pour éviter les débordements
    const maxHeight = 90; // mm
    if (imgHeight > maxHeight) {
      imgHeight = maxHeight;
    }

    // Titre du graphique
    addSectionTitle(doc, title, yPosition);
    yPosition += 10;

    // Ajouter l'image
    doc.addImage(imgData, 'PNG', 20, yPosition, imgWidth, imgHeight);
    yPosition += imgHeight + 10;

    return yPosition;
  } catch (error) {
    console.error('Erreur capture graphique:', error);
    return yPosition;
  }
}

/**
 * Formate un nombre avec séparateurs de milliers (compatible PDF)
 */
function formatNumber(n) {
  if (n == null || isNaN(n)) return '0';
  // Formatage manuel pour éviter les problèmes d'encodage avec jsPDF
  const num = Math.abs(n);
  const str = num.toString();
  const parts = [];
  
  for (let i = str.length - 1, j = 0; i >= 0; i--, j++) {
    if (j > 0 && j % 3 === 0) {
      parts.unshift(' '); // Espace normal, pas insécable
    }
    parts.unshift(str[i]);
  }
  
  return (n < 0 ? '-' : '') + parts.join('');
}

/**
 * Charge une image depuis une URL ou import
 */
function loadImageFromUrl(src) {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.crossOrigin = 'Anonymous';
    img.onload = () => resolve(img);
    img.onerror = () => reject(new Error('Impossible de charger le logo'));
    img.src = src;
  });
}

/**
 * Sauvegarde le PDF
 */
export function savePDF(doc, filename) {
  doc.save(filename);
}


/**
 * Génère le texte de description pour l'email d'accompagnement (version courte)
 */
export function getEmailReportDescription(dateRange) {
  const startDate = new Date(dateRange.start).toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
  const endDate = new Date(dateRange.end).toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });

  return `Bonjour,

Veuillez trouver ci-joint le rapport de production automatisé pour la période du ${startDate} au ${endDate}.

Ce rapport présente :
• Les indicateurs clés de performance (production, qualité, défauts)
• La répartition par ligne de production
• L'évolution journalière et les graphiques de suivi

Les données sont extraites automatiquement du système de production SEWS CABIND MAROC.

Pour toute question, n'hésitez pas à nous contacter.

Cordialement,
AutoReport System`;
}
