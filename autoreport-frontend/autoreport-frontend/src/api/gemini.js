import axios from 'axios';

const GEMINI_API_KEY = import.meta.env.VITE_GEMINI_API_KEY;
const GEMINI_API_URL = 'https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent';

/**
 * Système de contexte pour l'assistant AutoReport
 */
const SYSTEM_CONTEXT = `Tu es un assistant IA spécialisé pour le système AutoReport, une application de gestion et génération automatique de rapports industriels de production.

**Contexte du système AutoReport :**
- Système développé pour SEWS Maroc (entreprise automobile)
- Génération automatique de rapports de production quotidiens, hebdomadaires, mensuels
- Import de données Excel (colonnes : date, line, quantity, defects)
- Gestion des lignes de production et KPIs (taux de qualité, taux de défaut)
- Dashboard avec graphiques (production par jour, par ligne, tendances)
- Gestion des utilisateurs (rôles : ADMIN, USER, VIEWER)
- Planification de génération automatique de rapports (schedules)
- Audit log pour traçabilité
- Export PDF/Excel des rapports

**Fonctionnalités principales :**
1. Dashboard : Vue d'ensemble des KPIs (production totale, défauts, taux qualité/défaut)
2. Rapports : Génération manuelle ou planifiée, filtres par période/ligne, export
3. Import Excel : Glisser-déposer ou sélection de fichier, validation format
4. Utilisateurs : CRUD complet (admin uniquement), activation/désactivation
5. Planification : Création de schedules (quotidien, hebdomadaire, mensuel)
6. Paramètres : Configuration email SMTP, préférences système
7. Audit Log : Historique des actions utilisateurs

**Ton rôle :**
- Répondre aux questions sur l'utilisation d'AutoReport
- Guider les utilisateurs dans les fonctionnalités
- Expliquer les KPIs et métriques
- Aider au dépannage
- Rester concis et professionnel
- Répondre dans la langue suivante : {{LANGUAGE}}

**Limites :**
- Tu n'as pas accès direct aux données de l'utilisateur
- Tu ne peux pas effectuer d'actions dans le système (seulement conseiller)
- Reste dans le contexte AutoReport et production industrielle`;

/**
 * Chat IA - appelle réellement l'API Gemini si une clé est configurée,
 * sinon retombe automatiquement sur les réponses locales (chatWithLocalAI).
 *
 * ATTENTION SÉCURITÉ : VITE_GEMINI_API_KEY est injectée en clair dans le
 * bundle JS final (visible via "Voir le code source" une fois déployé).
 * Cette version directe-frontend est acceptable pour une démo/soutenance,
 * mais en production la bonne pratique est de faire transiter l'appel par
 * le backend Spring Boot (qui seul détiendrait la clé), pas depuis le
 * navigateur du client.
 */
export const chatWithGemini = async (userMessage, language = 'fr') => {
  const languageLabel = language === 'en' ? 'English' : 'français';
  const contextWithLang = SYSTEM_CONTEXT.replace('{{LANGUAGE}}', languageLabel);

  if (!GEMINI_API_KEY) {
    console.warn('VITE_GEMINI_API_KEY manquante, utilisation du chat local simulé.');
    return chatWithLocalAI(userMessage, language);
  }

  try {
    const response = await axios.post(
      `${GEMINI_API_URL}?key=${GEMINI_API_KEY}`,
      {
        contents: [
          {
            parts: [
              { text: `${contextWithLang}\n\nQuestion de l'utilisateur : ${userMessage}` },
            ],
          },
        ],
      },
      { headers: { 'Content-Type': 'application/json' }, timeout: 15000 }
    );

    const text = response?.data?.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!text) {
      throw new Error('Réponse Gemini vide ou mal formée');
    }
    return text;
  } catch (error) {
    console.error('Erreur appel Gemini, repli sur le chat local :', error?.message || error);
    return chatWithLocalAI(userMessage, language);
  }
};

/**
 * Chat IA simulé - Réponses pré-programmées sans API
 */
export const chatWithLocalAI = (userMessage, language = 'fr') => {
  const message = userMessage.toLowerCase().trim();
  
  const responses = {
    fr: {
      greeting: "Bonjour ! Je suis votre assistant AutoReport. Je peux vous aider avec la génération de rapports, l'import Excel, les indicateurs KPI et bien plus. Comment puis-je vous aider ?",
      generateReport: "Pour générer un rapport de production :\n\n1. Allez dans 'Rapports'\n2. Cliquez sur 'Nouveau rapport'\n3. Sélectionnez la période et la ligne de production\n4. Cliquez sur 'Générer'\n5. Le rapport sera créé et disponible en téléchargement (PDF ou Excel)\n\nVous pouvez aussi planifier des rapports automatiques dans 'Planification'.",
      importExcel: "Pour importer des données Excel :\n\n1. Allez dans 'Import Excel'\n2. Glissez-déposez votre fichier ou cliquez pour sélectionner\n3. Format attendu : colonnes 'date', 'line', 'quantity', 'defects'\n4. Cliquez sur 'Importer'\n5. Les données seront validées et importées\n\nAssurez-vous que votre fichier respecte le format requis !",
      qualityRate: "Le **Taux de Qualité** représente le pourcentage de pièces conformes par rapport à la production totale.\n\n📊 Formule : (Production - Défauts) / Production × 100\n\n✅ Exemple : Si vous produisez 1000 unités avec 50 défauts, le taux de qualité est de 95%.\n\nUn taux supérieur à 95% est généralement considéré comme excellent dans l'industrie automobile.",
      defectRate: "Le **Taux de Défaut** représente le pourcentage de pièces défectueuses par rapport à la production totale.\n\n📊 Formule : Défauts / Production × 100\n\n❌ Exemple : Si vous produisez 1000 unités avec 50 défauts, le taux de défaut est de 5%.\n\nObjectif SEWS : Maintenir le taux de défaut sous 3% pour rester compétitif.",
      scheduling: "Pour créer un planning automatique :\n\n1. Allez dans 'Planification'\n2. Cliquez sur 'Nouveau planning'\n3. Choisissez la fréquence (quotidien, hebdomadaire, mensuel)\n4. Sélectionnez l'heure d'envoi\n5. Ajoutez les destinataires email\n6. Activez le planning\n\nLes rapports seront générés et envoyés automatiquement selon votre configuration !",
      users: "Pour gérer les utilisateurs (Admin uniquement) :\n\n1. Allez dans 'Utilisateurs'\n2. Cliquez sur 'Ajouter un utilisateur'\n3. Renseignez : nom, email, rôle (Admin/Manager/User)\n4. Cliquez sur 'Enregistrer'\n\n**Rôles** :\n- 👑 Admin : Accès complet\n- 📊 Manager : Rapports + Import\n- 👤 User : Consultation uniquement",
      kpi: "Les **KPI disponibles** dans AutoReport :\n\n📈 **Production totale** : Nombre total d'unités produites\n❌ **Défauts constatés** : Nombre total de pièces défectueuses\n✅ **Taux de qualité** : % de conformité (Production - Défauts) / Production\n📉 **Taux de défaut** : % de non-conformité Défauts / Production\n\nCes indicateurs sont calculés automatiquement et affichés en temps réel sur le Dashboard.",
      email: "Pour configurer l'envoi d'emails automatiques :\n\n1. Allez dans 'Paramètres'\n2. Section 'Configuration Email'\n3. Renseignez : serveur SMTP, port, identifiants\n4. Pour Gmail : utilisez smtp.gmail.com (port 587) et un mot de passe d'application\n5. Testez l'envoi\n6. Activez l'envoi automatique\n\n⚠️ Important : Gmail nécessite un mot de passe d'application, pas votre mot de passe normal !",
      download: "Pour télécharger un rapport :\n\n**Format PDF** :\n- Inclut : logo, KPI, graphiques, tableaux\n- Idéal pour : présentations, archivage\n\n**Format Excel** :\n- Inclut : 3 feuilles (indicateurs, production par ligne, journalière)\n- Idéal pour : analyse, traitement de données\n\nCliquez sur le bouton 'Excel' ou 'PDF' en haut du Dashboard pour télécharger !",
      help: "Je peux vous aider avec :\n\n📊 Génération de rapports\n📁 Import de données Excel\n📈 Explication des KPI (qualité, défauts)\n⚙️ Configuration des plannings automatiques\n👥 Gestion des utilisateurs\n📧 Configuration email SMTP\n💾 Export PDF/Excel\n\nPosez-moi une question spécifique et je vous guiderai !",
      thanks: "De rien ! N'hésitez pas si vous avez d'autres questions. Je suis là pour vous aider ! 😊",
      default: "Je n'ai pas bien compris votre question. Voici ce que je peux faire :\n\n• Expliquer comment générer un rapport\n• Guider pour l'import Excel\n• Expliquer les KPI (taux de qualité, défauts)\n• Aider avec la planification automatique\n• Gérer les utilisateurs\n• Configurer les emails\n\nPosez-moi une question plus précise ou tapez 'aide' pour voir toutes les options !"
    },
    en: {
      greeting: "Hello! I'm your AutoReport assistant. I can help you with report generation, Excel imports, KPI indicators and much more. How can I help you?",
      generateReport: "To generate a production report:\n\n1. Go to 'Reports'\n2. Click on 'New Report'\n3. Select the period and production line\n4. Click on 'Generate'\n5. The report will be created and available for download (PDF or Excel)\n\nYou can also schedule automatic reports in 'Scheduling'.",
      importExcel: "To import Excel data:\n\n1. Go to 'Import Excel'\n2. Drag and drop your file or click to select\n3. Expected format: columns 'date', 'line', 'quantity', 'defects'\n4. Click on 'Import'\n5. Data will be validated and imported\n\nMake sure your file follows the required format!",
      qualityRate: "The **Quality Rate** represents the percentage of conforming parts relative to total production.\n\n📊 Formula: (Production - Defects) / Production × 100\n\n✅ Example: If you produce 1000 units with 50 defects, the quality rate is 95%.\n\nA rate above 95% is generally considered excellent in the automotive industry.",
      defectRate: "The **Defect Rate** represents the percentage of defective parts relative to total production.\n\n📊 Formula: Defects / Production × 100\n\n❌ Example: If you produce 1000 units with 50 defects, the defect rate is 5%.\n\nSEWS Target: Keep the defect rate under 3% to remain competitive.",
      scheduling: "To create an automatic schedule:\n\n1. Go to 'Scheduling'\n2. Click on 'New Schedule'\n3. Choose frequency (daily, weekly, monthly)\n4. Select send time\n5. Add recipient emails\n6. Activate the schedule\n\nReports will be generated and sent automatically according to your configuration!",
      users: "To manage users (Admin only):\n\n1. Go to 'Users'\n2. Click on 'Add User'\n3. Enter: name, email, role (Admin/Manager/User)\n4. Click on 'Save'\n\n**Roles**:\n- 👑 Admin: Full access\n- 📊 Manager: Reports + Import\n- 👤 User: View only",
      kpi: "**Available KPIs** in AutoReport:\n\n📈 **Total production**: Total number of units produced\n❌ **Defects found**: Total number of defective parts\n✅ **Quality rate**: % conformity (Production - Defects) / Production\n📉 **Defect rate**: % non-conformity Defects / Production\n\nThese indicators are calculated automatically and displayed in real-time on the Dashboard.",
      email: "To configure automatic email sending:\n\n1. Go to 'Settings'\n2. 'Email Configuration' section\n3. Enter: SMTP server, port, credentials\n4. For Gmail: use smtp.gmail.com (port 587) and an application password\n5. Test the sending\n6. Enable automatic sending\n\n⚠️ Important: Gmail requires an application password, not your regular password!",
      download: "To download a report:\n\n**PDF Format**:\n- Includes: logo, KPIs, charts, tables\n- Ideal for: presentations, archiving\n\n**Excel Format**:\n- Includes: 3 sheets (indicators, production by line, daily)\n- Ideal for: analysis, data processing\n\nClick the 'Excel' or 'PDF' button at the top of the Dashboard to download!",
      help: "I can help you with:\n\n📊 Report generation\n📁 Excel data import\n📈 KPI explanation (quality, defects)\n⚙️ Automatic scheduling configuration\n👥 User management\n📧 SMTP email configuration\n💾 PDF/Excel export\n\nAsk me a specific question and I'll guide you!",
      thanks: "You're welcome! Don't hesitate if you have any other questions. I'm here to help! 😊",
      default: "I didn't quite understand your question. Here's what I can do:\n\n• Explain how to generate a report\n• Guide you through Excel import\n• Explain KPIs (quality rate, defects)\n• Help with automatic scheduling\n• Manage users\n• Configure emails\n\nAsk me a more specific question or type 'help' to see all options!"
    }
  };
  
  const lang = responses[language] || responses.fr;
  
  // Réponses en fonction des mots-clés (multilingue)
  if (message.includes('bonjour') || message.includes('salut') || message.includes('hello') || message.includes('hi')) {
    return lang.greeting;
  }
  
  if ((message.includes('rapport') || message.includes('report')) && (message.includes('générer') || message.includes('generate'))) {
    return lang.generateReport;
  }
  
  if (message.includes('excel') && (message.includes('import') || message.includes('importer'))) {
    return lang.importExcel;
  }
  
  if (message.includes('taux de qualité') || message.includes('quality rate') || (message.includes('qualité') && !message.includes('défaut'))) {
    return lang.qualityRate;
  }
  
  if (message.includes('taux de défaut') || message.includes('defect rate') || message.includes('défaut')) {
    return lang.defectRate;
  }
  
  if (message.includes('planning') || message.includes('planification') || message.includes('automatique') || message.includes('schedule') || message.includes('automatic')) {
    return lang.scheduling;
  }
  
  if (message.includes('utilisateur') || message.includes('user') || (message.includes('gérer') && !message.includes('rapport'))) {
    return lang.users;
  }
  
  if (message.includes('kpi') || message.includes('indicateur') || message.includes('métrique') || message.includes('metric')) {
    return lang.kpi;
  }
  
  if (message.includes('email') || message.includes('smtp')) {
    return lang.email;
  }
  
  if (message.includes('pdf') || message.includes('télécharger') || message.includes('download')) {
    return lang.download;
  }
  
  if (message.includes('aide') || message.includes('help') || message.includes('comment') || message.includes('how')) {
    return lang.help;
  }
  
  if (message.includes('merci') || message.includes('thank')) {
    return lang.thanks;
  }
  
  // Réponse par défaut
  return lang.default;
};

/**
 * Vérifier si le chat local est disponible
 */
export const isLocalAIAvailable = () => {
  return true; // Toujours disponible !
};

/**
 * Obtenir des suggestions de questions selon la langue
 */
export const getSuggestions = (language = 'fr') => {
  const suggestions = {
    fr: [
      'Comment générer un rapport de production ?',
      'Comment importer des données Excel ?',
      'Qu\'est-ce que le taux de qualité ?',
      'Comment créer un planning automatique ?',
      'Comment gérer les utilisateurs ?',
      'Quelles sont les métriques KPI disponibles ?',
    ],
    en: [
      'How to generate a production report?',
      'How to import Excel data?',
      'What is the quality rate?',
      'How to create an automatic schedule?',
      'How to manage users?',
      'What KPI metrics are available?',
    ],
  };
  
  return suggestions[language] || suggestions.fr;
};
